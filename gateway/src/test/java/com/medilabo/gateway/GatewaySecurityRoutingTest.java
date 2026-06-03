package com.medilabo.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Verifie les deux responsabilites de la gateway pour le sprint 1 :
 * l'authentification centralisee (Basic Auth) et le routage vers patient.
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
class GatewaySecurityRoutingTest {

    // Identifiants par defaut du profil de dev (cf. application.yml).
    private static final String USER = "praticien";
    private static final String PASSWORD = "medilabo";

    // Port reel attribue au serveur Netty demarre par RANDOM_PORT.
    @Value("${local.server.port}")
    private int port;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    @DisplayName("Une route protegee sans authentification renvoie 401")
    void protectedRoute_withoutCredentials_isUnauthorized() {
        webTestClient.get().uri("/patients")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("La sonde /actuator/health est accessible sans authentification")
    void healthEndpoint_isPublic() {
        webTestClient.get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("Des identifiants invalides sont rejetes en 401")
    void protectedRoute_withBadCredentials_isUnauthorized() {
        webTestClient.get().uri("/patients")
                .headers(h -> h.setBasicAuth(USER, "wrong-password"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Identifiants valides : auth franchie + route trouvee (backend down -> 5xx, ni 401 ni 404)")
    void patientRoute_withValidCredentials_passesAuthAndRoutes() {
        webTestClient.get().uri("/patients")
                .headers(h -> h.setBasicAuth(USER, PASSWORD))
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
