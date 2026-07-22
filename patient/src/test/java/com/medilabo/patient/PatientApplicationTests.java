package com.medilabo.patient;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test de fumee : garantit que le contexte Spring du patient-service demarre
 * (JPA, validation et couche web correctement cablees).
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("PatientApplication (smoke)")
class PatientApplicationTests {

    @Test
    @DisplayName("Should load the application context")
    void shouldLoadApplicationContext() {
        // Aucune assertion : l'echec du demarrage du contexte fait echouer le test.
    }
}
