package com.medilabo.gateway.security;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Mono;

/**
 * Repository reactif sur la table app_user.
 *
 * <p>{@code ReactiveCrudRepository} renvoie des {@link Mono}/{@code Flux} :
 * l'acces base reste non-bloquant, compatible avec l'event loop de la gateway.
 */
public interface AppUserRepository extends ReactiveCrudRepository<AppUser, Long> {

    /**
     * Recherche un utilisateur par son identifiant de connexion.
     *
     * <p>La colonne porte une contrainte d'unicite, ce qui garantit un resultat
     * au plus. Appelee a chaque requete authentifiee, Basic Auth ne conservant
     * aucune session cote serveur.
     *
     * @param username identifiant saisi par le praticien
     * @return l'utilisateur correspondant, ou un {@link Mono} vide si aucun ne
     *         porte cet identifiant — l'authentification echoue alors sans que
     *         la reponse distingue un compte inexistant d'un mot de passe faux
     */
    Mono<AppUser> findByUsername(String username);
}
