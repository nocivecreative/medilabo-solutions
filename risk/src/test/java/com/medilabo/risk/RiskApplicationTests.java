package com.medilabo.risk;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test de fumée : garantit que le contexte Spring du risk-service démarre
 * (clients RestClient et liaison des propriétés correctement câblés). Aucun
 * appel réseau n'est émis au démarrage.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("RiskApplication (smoke)")
class RiskApplicationTests {

    @Test
    @DisplayName("Should load the application context")
    void shouldLoadApplicationContext() {
        // Aucune assertion : l'échec du démarrage du contexte fait échouer le test.
    }
}
