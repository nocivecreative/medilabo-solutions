package com.medilabo.risk.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.medilabo.risk.client.NoteClient;
import com.medilabo.risk.client.PatientClient;
import com.medilabo.risk.dto.NoteView;
import com.medilabo.risk.dto.PatientView;
import com.medilabo.risk.dto.RiskReportDTO;
import com.medilabo.risk.model.RiskLevel;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RiskService implements IRiskService {

    private final PatientClient patientClient;
    private final NoteClient noteClient;
    private final TriggerDetector triggerDetector;

    @Override
    public RiskReportDTO assessRisk(Long patId) {
        PatientView patient = patientClient.getPatient(patId);
        List<NoteView> notes = noteClient.getNotes(patId);

        String combinedNotes = notes.stream()
                .map(NoteView::note)
                .filter(java.util.Objects::nonNull)
                .reduce("", (a, b) -> a + " " + b);

        Set<String> triggers = triggerDetector.findDistinctTriggers(combinedNotes);
        int age = ageOf(patient.dateNaissance());
        RiskLevel level = evaluate(age, patient.genre(), triggers.size());

        return new RiskReportDTO(patId, level, age, triggers.size(), List.copyOf(triggers));
    }

    /** Âge en années révolues à la date du jour. */
    private int ageOf(LocalDate dateNaissance) {
        return Period.between(dateNaissance, LocalDate.now()).getYears();
    }

    /**
     * Applique le barème du brief, rendu cohérent sur les seuils voisins :
     *
     * <pre>
     *  &gt; 30 ans     : None 0-1 | Borderline 2-5 | In Danger 6-7 | Early Onset ≥8
     *  Homme &lt; 30   : None 0-2 |     (n/a)      | In Danger 3-4 | Early Onset ≥5
     *  Femme &lt; 30   : None 0-3 |     (n/a)      | In Danger 4-6 | Early Onset ≥7
     * </pre>
     *
     * Borderline exige « plus de 30 ans » → n'existe pas pour les &lt; 30.
     * Choix documenté pour l'âge pivot : {@code age > 30} déclenche les règles
     * « plus de 30 ans » ; 30 ans pile relève donc des règles « moins de 30 ans »
     * (le brief ne couvre ni ce cas ni les &lt;30 sans distinction — aucun patient
     * de test n'est concerné).
     */
    private RiskLevel evaluate(int age, String genre, int triggerCount) {
        boolean over30 = age > 30;

        if (over30) {
            if (triggerCount >= 8) {
                return RiskLevel.EARLY_ONSET;
            }
            if (triggerCount >= 6) {
                return RiskLevel.IN_DANGER;
            }
            if (triggerCount >= 2) {
                return RiskLevel.BORDERLINE;
            }
            return RiskLevel.NONE;
        }

        boolean male = "M".equalsIgnoreCase(genre);
        if (male) {
            if (triggerCount >= 5) {
                return RiskLevel.EARLY_ONSET;
            }
            if (triggerCount >= 3) {
                return RiskLevel.IN_DANGER;
            }
            return RiskLevel.NONE;
        }

        // Femme de moins de 30 ans.
        if (triggerCount >= 7) {
            return RiskLevel.EARLY_ONSET;
        }
        if (triggerCount >= 4) {
            return RiskLevel.IN_DANGER;
        }
        return RiskLevel.NONE;
    }
}
