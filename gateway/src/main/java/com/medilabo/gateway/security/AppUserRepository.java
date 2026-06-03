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

    Mono<AppUser> findByUsername(String username);
}
