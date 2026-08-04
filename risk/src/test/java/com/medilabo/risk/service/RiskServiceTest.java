package com.medilabo.risk.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.medilabo.risk.client.NoteClient;
import com.medilabo.risk.client.PatientClient;
import com.medilabo.risk.config.RiskProperties;
import com.medilabo.risk.dto.NoteView;
import com.medilabo.risk.dto.PatientView;
import com.medilabo.risk.dto.RiskReportDTO;
import com.medilabo.risk.model.RiskLevel;

/**
 * Tests unitaires (base de la pyramide) : clients amont mockes, {@link TriggerDetector}
 * réel. On teste l'algorithme complet (détection + barème) sans HTTP ni contexte Spring.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RiskService (unit)")
class RiskServiceTest {

    // Mots dont le radical normalisé correspond à un déclencheur distinct — pour
    // fabriquer une note contenant exactement N déclencheurs dans les tests de bornes.
    private static final List<String> DISTINCT_TRIGGER_WORDS = List.of(
            "taille", "poids", "anticorps", "rechute", "microalbumine", "anormal",
            "cholesterol", "vertige", "reaction", "fumeur");

    @Mock
    private PatientClient patientClient;

    @Mock
    private NoteClient noteClient;

    private RiskService riskService;

    @BeforeEach
    void setUp() {
        RiskProperties properties = new RiskProperties();
        properties.setTriggers(List.of(
                "hemoglobine a1c", "microalbumine", "taille", "poids", "fumeu", "anormal",
                "cholesterol", "vertige", "rechute", "reaction", "anticorps"));
        riskService = new RiskService(patientClient, noteClient, new TriggerDetector(properties));
    }

    private void givenPatient(long patId, LocalDate dob, String genre, String... notes) {
        when(patientClient.getPatient(patId)).thenReturn(new PatientView(patId, dob, genre));
        when(noteClient.getNotes(patId)).thenReturn(Arrays.stream(notes).map(NoteView::new).toList());
    }

    /** Une note contenant exactement {@code n} déclencheurs distincts. */
    private String noteWith(int n) {
        return String.join(" ", DISTINCT_TRIGGER_WORDS.subList(0, n));
    }

    @Nested
    @DisplayName("Cas de test officiels (patients auto-étiquetés)")
    class OfficialTestCases {

        static Stream<Arguments> testPatients() {
            return Stream.of(
                    Arguments.of("TestNone", LocalDate.of(1966, 12, 3), "F", RiskLevel.NONE,
                            new String[] {
                                    "Le patient déclare qu'il 'se sent très bien' Poids égal ou inférieur au poids recommandé"
                            }),
                    Arguments.of("TestBorderline", LocalDate.of(1945, 6, 24), "M", RiskLevel.BORDERLINE,
                            new String[] {
                                    "Le patient déclare qu'il ressent beaucoup de stress au travail Il se plaint également que son audition est anormale dernièrement",
                                    "Le patient déclare avoir fait une réaction aux médicaments au cours des 3 derniers mois Il remarque également que son audition continue d'être anormale"
                            }),
                    Arguments.of("TestInDanger", LocalDate.of(2004, 6, 18), "M", RiskLevel.IN_DANGER,
                            new String[] {
                                    "Le patient déclare qu'il fume depuis peu",
                                    "Le patient déclare qu'il est fumeur et qu'il a cessé de fumer l'année dernière Il se plaint également de crises d'apnée respiratoire anormales Tests de laboratoire indiquant un taux de cholestérol LDL élevé"
                            }),
                    Arguments.of("TestEarlyOnset", LocalDate.of(2002, 6, 28), "F", RiskLevel.EARLY_ONSET,
                            new String[] {
                                    "Le patient déclare qu'il lui est devenu difficile de monter les escaliers Il se plaint également d'être essoufflé Tests de laboratoire indiquant que les anticorps sont élevés Réaction aux médicaments",
                                    "Le patient déclare qu'il a mal au dos lorsqu'il reste assis pendant longtemps",
                                    "Le patient déclare avoir commencé à fumer depuis peu Hémoglobine A1C supérieure au niveau recommandé",
                                    "Taille, Poids, Cholestérol, Vertige et Réaction"
                            }));
        }

        @ParameterizedTest(name = "{0} -> {3}")
        @MethodSource("testPatients")
        @DisplayName("Should classify each official test patient as its name states")
        void shouldClassifyOfficialPatients(String name, LocalDate dob, String genre,
                RiskLevel expected, String[] notes) {
            // Arrange
            givenPatient(1L, dob, genre, notes);

            // Act
            RiskReportDTO report = riskService.assessRisk(1L);

            // Assert
            assertThat(report.riskLevel())
                    .as("%s doit être classé %s", name, expected)
                    .isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Bornes du barème")
    class RiskThresholds {

        static Stream<Arguments> thresholds() {
            return Stream.of(
                    // âge > 30 (les deux sexes) : None 0-1 | Borderline 2-5 | In Danger 6-7 | Early Onset >=8
                    Arguments.of("> 30, 1 déclencheur", 50, "M", 1, RiskLevel.NONE),
                    Arguments.of("> 30, 2 déclencheurs", 50, "F", 2, RiskLevel.BORDERLINE),
                    Arguments.of("> 30, 5 déclencheurs", 50, "M", 5, RiskLevel.BORDERLINE),
                    Arguments.of("> 30, 6 déclencheurs", 50, "F", 6, RiskLevel.IN_DANGER),
                    Arguments.of("> 30, 8 déclencheurs", 50, "M", 8, RiskLevel.EARLY_ONSET),
                    // homme < 30 : None 0-2 | In Danger 3-4 | Early Onset >=5
                    Arguments.of("homme < 30, 2 déclencheurs", 25, "M", 2, RiskLevel.NONE),
                    Arguments.of("homme < 30, 3 déclencheurs", 25, "M", 3, RiskLevel.IN_DANGER),
                    Arguments.of("homme < 30, 5 déclencheurs", 25, "M", 5, RiskLevel.EARLY_ONSET),
                    // femme < 30 : None 0-3 | In Danger 4-6 | Early Onset >=7
                    Arguments.of("femme < 30, 3 déclencheurs", 25, "F", 3, RiskLevel.NONE),
                    Arguments.of("femme < 30, 4 déclencheurs", 25, "F", 4, RiskLevel.IN_DANGER),
                    Arguments.of("femme < 30, 7 déclencheurs", 25, "F", 7, RiskLevel.EARLY_ONSET));
        }

        @ParameterizedTest(name = "{0} -> {4}")
        @MethodSource("thresholds")
        @DisplayName("Should map age, sex and trigger count to the right level")
        void shouldApplyThresholds(String label, int age, String genre, int triggerCount,
                RiskLevel expected) {
            // Arrange — date de naissance relative au jour pour un âge déterministe.
            givenPatient(1L, LocalDate.now().minusYears(age), genre, noteWith(triggerCount));

            // Act
            RiskReportDTO report = riskService.assessRisk(1L);

            // Assert
            assertThat(report)
                    .extracting(RiskReportDTO::triggerCount, RiskReportDTO::riskLevel)
                    .containsExactly(triggerCount, expected);
        }
    }

    @Nested
    @DisplayName("Rapport")
    class Report {

        @DisplayName("Should return NONE with an empty trigger list when the patient has no note")
        @org.junit.jupiter.api.Test
        void shouldReturnNoneWithoutNotes() {
            // Arrange
            when(patientClient.getPatient(5L))
                    .thenReturn(new PatientView(5L, LocalDate.of(1980, 1, 1), "M"));
            when(noteClient.getNotes(5L)).thenReturn(List.of());

            // Act
            RiskReportDTO report = riskService.assessRisk(5L);

            // Assert
            assertThat(report)
                    .extracting(RiskReportDTO::patId, RiskReportDTO::riskLevel,
                            RiskReportDTO::triggerCount)
                    .containsExactly(5L, RiskLevel.NONE, 0);
            assertThat(report.triggersFound()).isEmpty();
            assertThat(report.age()).isGreaterThan(30);
        }

        @DisplayName("Should expose the distinct triggers found in the report")
        @org.junit.jupiter.api.Test
        void shouldExposeTriggersFound() {
            // Arrange
            givenPatient(6L, LocalDate.now().minusYears(50), "F", "Taille et Poids anormal");

            // Act
            RiskReportDTO report = riskService.assessRisk(6L);

            // Assert
            assertThat(report.triggersFound())
                    .containsExactlyInAnyOrder("taille", "poids", "anormal");
        }
    }
}
