package com.medilabo.risk.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.medilabo.risk.config.RiskProperties;

/**
 * Test unitaire (base de la pyramide) : logique pure, aucun contexte Spring.
 * Verrouille les deux subtilites imposees par les cas de test — comptage
 * distinct et matching souple (accents, casse, radicaux).
 */
@DisplayName("TriggerDetector (unit)")
class TriggerDetectorTest {

    private static final List<String> TRIGGERS = List.of(
            "hemoglobine a1c", "microalbumine", "taille", "poids", "fumeu", "anormal",
            "cholesterol", "vertige", "rechute", "reaction", "anticorps");

    private TriggerDetector detector;

    @BeforeEach
    void setUp() {
        RiskProperties properties = new RiskProperties();
        properties.setTriggers(TRIGGERS);
        detector = new TriggerDetector(properties);
    }

    @Test
    @DisplayName("Should count a repeated trigger only once (distinct terms)")
    void shouldCountRepeatedTriggerOnce() {
        // Arrange — "Poids" et "poids" dans la meme note (cas TestNone).
        String notes = "se sent tres bien Poids egal ou inferieur au poids recommande";

        // Act
        var found = detector.findDistinctTriggers(notes);

        // Assert
        assertThat(found).containsExactly("poids");
    }

    @Test
    @DisplayName("Should match regardless of case and accents")
    void shouldMatchIgnoringCaseAndAccents() {
        // Arrange
        String notes = "Taux de CHOLESTÉROL élevé, réaction aux médicaments";

        // Act
        var found = detector.findDistinctTriggers(notes);

        // Assert
        assertThat(found).containsExactlyInAnyOrder("cholesterol", "reaction");
    }

    @Test
    @DisplayName("Should match a plural via its stem (vertige captures Vertiges)")
    void shouldMatchPluralViaStem() {
        // Arrange — le brief liste "Vertiges", la note dit "Vertige".
        String notes = "Taille, Poids, Cholestérol, Vertige et Réaction";

        // Act
        var found = detector.findDistinctTriggers(notes);

        // Assert
        assertThat(found)
                .contains("vertige")
                .containsExactlyInAnyOrder("taille", "poids", "cholesterol", "vertige", "reaction");
    }

    @Test
    @DisplayName("Should return no trigger for text without terminology")
    void shouldReturnEmptyWhenNoTrigger() {
        // Arrange
        String notes = "Le patient se sent tres bien et fait du sport";

        // Act & Assert
        assertThat(detector.findDistinctTriggers(notes)).isEmpty();
    }

    @Test
    @DisplayName("Should treat null text as no trigger")
    void shouldHandleNullText() {
        // Arrange, Act & Assert
        assertThat(detector.findDistinctTriggers(null)).isEmpty();
    }
}
