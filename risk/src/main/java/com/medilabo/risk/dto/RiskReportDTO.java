package com.medilabo.risk.dto;

import java.util.List;

import com.medilabo.risk.model.RiskLevel;

/**
 * Rapport de risque renvoyé au client.
 *
 * <p>Au-delà du niveau (seul strictement demandé par l'US), on expose l'âge, le
 * nombre de déclencheurs et la liste trouvée : le praticien — et le jury — voient
 * ainsi POURQUOI un niveau est calculé (transparence + débogage).
 */
public record RiskReportDTO(
        Long patId,
        RiskLevel riskLevel,
        int age,
        int triggerCount,
        List<String> triggersFound) {
}
