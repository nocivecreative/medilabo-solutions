package com.medilabo.risk.service;

import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.medilabo.risk.config.RiskProperties;

/**
 * Détecte les termes déclencheurs dans le texte agrégé des notes d'un patient.
 *
 * <p>Deux règles imposées par les cas de test :
 * <ul>
 *   <li><b>Comptage DISTINCT</b> : un terme présent plusieurs fois compte une fois.
 *       (TestNone : « Poids … poids » = 1 déclencheur, pas 2, sinon il basculerait
 *       à tort en Borderline.)</li>
 *   <li><b>Matching souple</b> : texte et termes normalisés (minuscules, sans
 *       accents), recherche par sous-chaîne sur des radicaux — « vertige » capte
 *       « Vertige » et « Vertiges » (bascule TestEarlyOnset de 6 à 7 déclencheurs).</li>
 * </ul>
 */
@Component
public class TriggerDetector {

    private final List<String> normalizedTriggers;

    public TriggerDetector(RiskProperties properties) {
        this.normalizedTriggers = properties.getTriggers().stream()
                .map(TriggerDetector::normalize)
                .filter(t -> !t.isBlank())
                .toList();
    }

    /**
     * Ensemble des termes déclencheurs distincts présents dans le texte fourni.
     * L'ordre d'insertion (= ordre de la config) est préservé pour un rapport lisible.
     *
     * @param combinedNotes texte agrégé des notes du patient ; peut être vide, et
     *                      {@code null} est traité comme vide
     * @return les termes trouvés, sous leur forme normalisée (minuscules, sans
     *         accents) et non sous leur graphie d'origine dans les notes ;
     *         ensemble vide si aucun terme ne correspond
     */
    public Set<String> findDistinctTriggers(String combinedNotes) {
        String haystack = normalize(combinedNotes);
        Set<String> found = new LinkedHashSet<>();
        for (String trigger : normalizedTriggers) {
            if (haystack.contains(trigger)) {
                found.add(trigger);
            }
        }
        return found;
    }

    /** Minuscule + suppression des diacritiques (é → e, à → a, …). */
    static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String lowered = text.toLowerCase();
        return Normalizer.normalize(lowered, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}
