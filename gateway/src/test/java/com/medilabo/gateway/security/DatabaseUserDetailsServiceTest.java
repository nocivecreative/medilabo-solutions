package com.medilabo.gateway.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Test unitaire (base de la pyramide) : repository mocke, aucun contexte Spring,
 * aucune base. StepVerifier est l'equivalent reactif des assertions bloquantes.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DatabaseUserDetailsService (unit)")
class DatabaseUserDetailsServiceTest {

    private static final String USERNAME = "praticien";
    private static final String PASSWORD_HASH = "{bcrypt}$2a$10$7EqJtq98hPqEX7fNZaFWoO";

    @Mock
    private AppUserRepository userRepository;

    @InjectMocks
    private DatabaseUserDetailsService userDetailsService;

    private AppUser storedUser(boolean enabled) {
        return new AppUser(1L, USERNAME, PASSWORD_HASH, enabled);
    }

    @Test
    @DisplayName("Should map the stored account to UserDetails, hash included")
    void shouldMapStoredAccountToUserDetails() {
        // Arrange
        when(userRepository.findByUsername(USERNAME)).thenReturn(Mono.just(storedUser(true)));

        // Act
        Mono<UserDetails> result = userDetailsService.findByUsername(USERNAME);

        // Assert
        StepVerifier.create(result)
                .assertNext(details -> assertThat(details)
                        .extracting(UserDetails::getUsername, UserDetails::getPassword,
                                UserDetails::isEnabled)
                        .containsExactly(USERNAME, PASSWORD_HASH, true))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should grant exactly ROLE_USER and no finer-grained authority")
    void shouldGrantOnlyRoleUser() {
        // Arrange
        when(userRepository.findByUsername(USERNAME)).thenReturn(Mono.just(storedUser(true)));

        // Act
        Mono<UserDetails> result = userDetailsService.findByUsername(USERNAME);

        // Assert — le besoin client exclut toute gestion fine de roles.
        StepVerifier.create(result)
                .assertNext(details -> assertThat(details.getAuthorities())
                        .extracting(GrantedAuthority::getAuthority)
                        .containsExactly("ROLE_USER"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should expose a deactivated account as disabled")
    void shouldExposeDeactivatedAccountAsDisabled() {
        // Arrange
        when(userRepository.findByUsername(USERNAME)).thenReturn(Mono.just(storedUser(false)));

        // Act
        Mono<UserDetails> result = userDetailsService.findByUsername(USERNAME);

        // Assert
        StepVerifier.create(result)
                .assertNext(details -> assertThat(details.isEnabled())
                        .as("Un compte enabled=false doit etre refuse par Spring Security")
                        .isFalse())
                .verifyComplete();
    }

    @Test
    @DisplayName("Should complete empty when the username is unknown")
    void shouldCompleteEmptyWhenUsernameUnknown() {
        // Arrange
        when(userRepository.findByUsername("inconnu")).thenReturn(Mono.empty());

        // Act
        Mono<UserDetails> result = userDetailsService.findByUsername("inconnu");

        // Assert — un Mono vide, pas une exception : Spring Security traduit en 401.
        StepVerifier.create(result).verifyComplete();
    }
}
