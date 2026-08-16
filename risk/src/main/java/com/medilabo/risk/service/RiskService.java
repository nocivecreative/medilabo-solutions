package com.medilabo.risk.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.medilabo.risk.client.NoteClient;
import com.medilabo.risk.client.PatientClient;
import com.medilabo.risk.dto.NoteView;
import com.medilabo.risk.dto.PatientView;
import com.medilabo.risk.dto.RiskReportDTO;
import com.medilabo.risk.model.RiskLevel;

import lombok.RequiredArgsConstructor;

/**
 * Évalue le niveau de risque de diabète d'un patient.
 *
 * <p>Seul service de la stack à ne détenir aucune donnée : il ne possède ni base
 * ni schéma, et reconstruit son rapport à chaque appel en interrogeant le
 * patient-service et le notes-service. Rien n'est mis en cache ni recopié
 * localement — un rapport reflète donc toujours l'état courant des deux sources,
 * et aucune donnée de santé ne se retrouve dupliquée dans un troisième magasin.
 *
 * <p>Le calcul se fait en trois temps, répartis entre trois collaborateurs :
 * {@link TriggerDetector} compte les termes déclencheurs distincts du texte des
 * notes, {@code ageOf} déduit l'âge de la date de naissance, et {@code evaluate}
 * croise âge, genre et nombre de déclencheurs pour produire le niveau.
 */
@Service
@RequiredArgsConstructor
public class RiskService {

    private final PatientClient patientClient;
    private final NoteClient noteClient;
    private final TriggerDetector triggerDetector;

    /**
     * Produit le rapport de risque d'un patient.
     *
     * <p>Les notes sont concaténées en un seul texte avant analyse, et non
     * examinées une à une : le comptage porte sur les déclencheurs distincts de
     * l'historique complet, de sorte qu'un terme répété dans plusieurs notes ne
     * compte qu'une fois. Les notes au contenu absent sont écartées de cette
     * concaténation.
     *
     * <p>Les deux appels amont sont séquentiels et bloquants. Un patient sans
     * aucune note donne un rapport valide à zéro déclencheur, pas une erreur.
     *
     * @param patId identifiant du patient à évaluer
     * @return le rapport : niveau de risque, âge, nombre de déclencheurs et
     *         liste des termes trouvés, dans l'ordre de la configuration
     * @throws RestClientException si un service amont est injoignable ou répond
     *                             en erreur — notamment lorsque l'identifiant est
     *                             inconnu du patient-service, dont le {@code 404}
     *                             est propagé
     */
    public RiskReportDTO assessRisk(Long patId) {
        PatientView patient = patientClient.getPatient(patId);
        List<NoteView> notes = noteClient.getNotes(patId);

        String combinedNotes = notes.stream()
                .map(NoteView::note)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));

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
