package com.medilabo.risk.model;

/**
 * Niveau de risque de diabète de type 2 d'un patient (US Sprint 3).
 * Ordre croissant de gravité — l'évaluation teste du plus grave au moins grave.
 */
public enum RiskLevel {
    NONE,
    BORDERLINE,
    IN_DANGER,
    EARLY_ONSET
}
