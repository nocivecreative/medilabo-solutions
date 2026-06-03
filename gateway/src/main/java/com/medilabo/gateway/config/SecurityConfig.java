package com.medilabo.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

/**
 * Securite de la gateway : authentification centralisee pour toute la stack.
 *
 * <p>Choix arretes (ADR-03) : HTTP Basic Auth + utilisateur en memoire, sans
 * inscription ni gestion fine de roles. La gateway etant reactive (WebFlux/Netty),
 * on utilise {@link EnableWebFluxSecurity} et non {@code @EnableWebSecurity}.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${medilabo.security.username}")
    private String username;

    @Value("${medilabo.security.password}")
    private String password;

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
     * Unique utilisateur (praticien) charge en memoire au demarrage.
     * Identifiants externalises (variables d'environnement) ; mot de passe
     * hashe avec BCrypt via le {@link #passwordEncoder() DelegatingPasswordEncoder}.
     */
    @Bean
    MapReactiveUserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails praticien = User.withUsername(username)
                .password(encoder.encode(password))
                .roles("USER")
                .build();
        return new MapReactiveUserDetailsService(praticien);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
