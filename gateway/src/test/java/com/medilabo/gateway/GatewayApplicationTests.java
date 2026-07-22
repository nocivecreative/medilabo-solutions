package com.medilabo.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test de fumee : garantit que le contexte Spring de la gateway demarre
 * (routes, securite reactive et acces R2DBC correctement cables).
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("GatewayApplication (smoke)")
class GatewayApplicationTests {

    @Test
    @DisplayName("Should load the application context")
    void shouldLoadApplicationContext() {
        // Aucune assertion : l'echec du demarrage du contexte fait echouer le test.
    }
}
