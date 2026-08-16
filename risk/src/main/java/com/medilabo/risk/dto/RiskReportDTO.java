package com.medilabo.risk.dto;

import java.util.List;

import com.medilabo.risk.model.RiskLevel;

/**
 * Rapport de risque renvoyé au client.
 *
 * <p>Au-delà du niveau (seul strictement demandé par l'US), on expose l'âge, le
 * nombre de déclencheurs et la liste trouvée : le praticien — et le jury — voient
 * ainsi POURQUOI un niveau est calculé (transparence + débogage).
 *
 * @param patId         identifiant du patient évalué
 * @param riskLevel     niveau retenu par le barème
 * @param age           âge en années révolues au jour du calcul
 * @param triggerCount  nombre de déclencheurs DISTINCTS ; c'est cette valeur, et
 *                      non le nombre d'occurrences, qui alimente le barème
 * @param triggersFound termes trouvés sous leur forme normalisée (minuscules,
 *                      sans accents), dans l'ordre de la configuration
 */
public record RiskReportDTO(
        Long patId,
        RiskLevel riskLevel,
        int age,
        int triggerCount,
        List<String> triggersFound) {
}
