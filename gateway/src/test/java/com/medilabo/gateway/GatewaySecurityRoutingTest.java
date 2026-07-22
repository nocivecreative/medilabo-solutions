package com.medilabo.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.medilabo.gateway.security.AppUser;
import com.medilabo.gateway.security.AppUserRepository;

/**
 * Test d'integration (haut de la pyramide) : verifie les deux responsabilites de
 * la gateway — authentification centralisee (Basic Auth) et routage vers patient.
 *
 * <p>Astuce : le service patient n'est PAS demarre pendant ce test. Une requete
 * authentifiee sur une route declaree echoue donc en 5xx (backend injoignable),
 * ce qui distingue "route connue, backend down" (5xx) d'un "aucune route" (404).
 * On prouve ainsi l'existence de la route sans avoir besoin d'un vrai backend.
 *
 * <p>{@code WebTestClient} est l'equivalent reactif de {@code MockMvc}, et
 * {@code RANDOM_PORT} demarre un vrai serveur Netty sur un port libre.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Gateway security & routing (integration)")
class GatewaySecurityRoutingTest {

    // Utilisateur de test, insere dans la base d'auth par la fixture ci-dessous.
    private static final String USER = "praticien";
    private static final String PASSWORD = "medilabo";

    // Port reel attribue au serveur Netty demarre par RANDOM_PORT.
    @Value("${local.server.port}")
    private int port;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        // La gateway ne seede plus aucun utilisateur (DB = source de verite) :
        // on insere le praticien de test directement, encode comme en prod.
        userRepository.findByUsername(USER)
                .switchIfEmpty(userRepository.save(
                        new AppUser(null, USER, passwordEncoder.encode(PASSWORD), true)))
                .block();
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    /**
     * Les trois facons d'echouer l'authentification sur une route protegee :
     * elles ne different que par les en-tetes envoyes, d'ou le parametrage.
     */
    static Stream<Arguments> unauthorizedCases() {
        return Stream.of(
                Arguments.of("aucun identifiant", (Consumer<HttpHeaders>) headers -> { }),
                Arguments.of("mot de passe errone",
                        (Consumer<HttpHeaders>) headers -> headers.setBasicAuth(USER, "wrong-password")),
                Arguments.of("utilisateur inconnu",
                        (Consumer<HttpHeaders>) headers -> headers.setBasicAuth("inconnu", PASSWORD)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unauthorizedCases")
    @DisplayName("Should return 401 on a protected route without valid credentials")
    void shouldRejectUnauthorizedAccess(String caseName, Consumer<HttpHeaders> credentials) {
        // Arrange — les en-tetes du cas courant.

        // Act & Assert
        webTestClient.get().uri("/patients")
                .headers(credentials)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Should expose /actuator/health without authentication")
    void shouldExposeHealthEndpointPublicly() {
        // Arrange — sonde publique declaree en permitAll dans SecurityConfig.

        // Act & Assert
        webTestClient.get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("Should pass authentication and match the patient route with valid credentials")
    void shouldPassAuthAndRouteWithValidCredentials() {
        // Arrange — identifiants valides, inseres en base par setUp().

        // Act
        int status = webTestClient.get().uri("/patients")
                .headers(h -> h.setBasicAuth(USER, PASSWORD))
                .exchange()
                .returnResult(String.class)
                .getStatus()
                .value();

        // Assert — le backend patient n'etant pas demarre, on ne peut pas exiger 200 ;
        // la garantie utile est : ni 401 (auth en echec) ni 404 (aucune route).
        assertThat(status)
                .as("Auth franchie et route patient trouvee")
                .isNotIn(401, 404);
    }
}
