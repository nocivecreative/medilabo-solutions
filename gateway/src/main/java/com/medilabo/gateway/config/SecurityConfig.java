package com.medilabo.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

/**
 * Securite de la gateway : authentification centralisee pour toute la stack.
 *
 * <p>Choix (ADR-03 v4) : HTTP Basic Auth, utilisateurs stockes dans une base
 * d'auth dediee (medilabo_auth), sans inscription ni gestion fine de roles. La
 * gateway etant reactive (WebFlux/Netty), on utilise {@link EnableWebFluxSecurity}
 * et non {@code @EnableWebSecurity}. Le chargement des utilisateurs est assure
 * par {@code DatabaseUserDetailsService} (bean {@code ReactiveUserDetailsService})
 * que Spring Security cable automatiquement avec le {@link #passwordEncoder()}.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * Chaine de filtres reactive : tout est protege sauf la sonde de sante.
     * Basic Auth est stateless par nature (credentials renvoyes a chaque requete),
     * donc aucun contexte de securite n'est conserve cote serveur.
     */
    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // API stateless sans cookie de session : CSRF non applicable.
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/health").permitAll()
                        .anyExchange().authenticated())
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    /**
     * Encode/verifie les mots de passe. BCrypt par defaut, avec prefixe d'algo
     * ({bcrypt}...) pour permettre une migration d'algorithme sans casser
     * les hash existants. Le hash stocke en base (cf. 02_insert_app_user.sql)
     * porte ce prefixe ; cet encoder le verifie a chaque requete.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
