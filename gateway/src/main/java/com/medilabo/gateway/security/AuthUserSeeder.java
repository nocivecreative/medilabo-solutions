package com.medilabo.gateway.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

/**
 * Amorce l'unique utilisateur praticien au demarrage si la table est vide.
 *
 * <p>Ce n'est PAS de l'inscription : aucun endpoint public ne cree de compte.
 * On materialise simplement le seul utilisateur configure (identifiants via
 * variables d'environnement) dans la base d'auth, de facon idempotente. La
 * table reste en lecture seule pour le reste de l'application.
 */
@Component
public class AuthUserSeeder implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AuthUserSeeder.class);

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${medilabo.security.username}")
    private String username;

    @Value("${medilabo.security.password}")
    private String password;

    public AuthUserSeeder(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Bloquer ici est acceptable : on est au demarrage, hors event loop.
        userRepository.findByUsername(username)
                .doOnNext(existing -> logger.info("Utilisateur '{}' deja present, pas de seed.", username))
                .switchIfEmpty(Mono.defer(() -> {
                    AppUser user = new AppUser(null, username, passwordEncoder.encode(password), true);
                    logger.info("Seed de l'utilisateur '{}' dans la base d'auth.", username);
                    return userRepository.save(user);
                }))
                .block();
    }
}
