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

    /**
     * Charge un utilisateur et le convertit au format attendu par Spring Security.
     *
     * <p>Le mot de passe est transmis tel qu'il est stocke, sous forme de hash
     * prefixe par son algorithme : c'est le {@code PasswordEncoder} de la chaine
     * de securite qui le confronte a celui de la requete, jamais cette methode.
     *
     * <p>L'autorite {@code ROLE_USER} est posee en dur pour tous : le besoin
     * client ne prevoit aucune gestion fine de droits.
     *
     * @param username identifiant de connexion recherche
     * @return l'utilisateur au format Spring Security, ou un {@link Mono} vide si
     *         l'identifiant est inconnu ; un compte desactive est renvoye mais
     *         marque comme tel, et l'authentification echoue en aval
     */
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
