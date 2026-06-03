package com.medilabo.gateway.security;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

/**
 * Charge un utilisateur depuis la base d'auth pour l'authentification Basic.
 *
 * <p>Remplace l'ancien {@code MapReactiveUserDetailsService} en memoire : la
 * source des utilisateurs est desormais la table app_user (lecture seule).
 *
 * <p>Conformement au besoin client, aucune gestion fine de droits : chaque
 * utilisateur recoit l'unique autorite {@code ROLE_USER}, et les regles de
 * securite ne font que {@code authenticated()} (jamais {@code hasRole(...)}).
 */
@Service
public class DatabaseUserDetailsService implements ReactiveUserDetailsService {

    private final AppUserRepository userRepository;

    public DatabaseUserDetailsService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(appUser -> User.withUsername(appUser.getUsername())
                        .password(appUser.getPassword())
                        .disabled(!appUser.isEnabled())
                        .authorities("ROLE_USER")
                        .build());
    }
}
