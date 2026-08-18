package com.medilabo.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
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
     *
     * @param http constructeur de la chaine, fourni par Spring Security
     * @return la chaine appliquee a toutes les requetes traversant la gateway,
     *         y compris celles routees vers les services metier
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // API stateless sans cookie de session : CSRF non applicable.
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/health").permitAll()
                        .anyExchange().authenticated())
                // Basic Auth SANS challenge WWW-Authenticate : sur un 401, on renvoie un
                // statut nu. Sinon le navigateur, voyant "WWW-Authenticate: Basic", affiche
                // sa propre fenetre de login native par-dessus la SPA. C'est le formulaire
                // Angular qui doit gerer l'auth (message d'erreur + redirection /login),
                // l'interceptor continue d'envoyer l'en-tete Authorization: Basic.
                .httpBasic(basic -> basic.authenticationEntryPoint((exchange, ex) -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }))
                .build();
    }

    /**
     * Encode/verifie les mots de passe. BCrypt par defaut, avec prefixe d'algo
     * ({bcrypt}...) pour permettre une migration d'algorithme sans casser
     * les hash existants. Le hash stocke en base (cf. db/init/21_auth_seed.sql,
     * a la racine du monorepo) porte ce prefixe ; cet encoder le verifie a
     * chaque requete.
     *
     * @return un encoder delegant, capable de verifier plusieurs algorithmes en
     *         se fiant au prefixe du hash stocke, et qui encode en BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
