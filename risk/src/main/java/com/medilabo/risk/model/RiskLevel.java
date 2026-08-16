package com.medilabo.risk.model;

/**
 * Niveau de risque de diabète de type 2 d'un patient (US Sprint 3).
 * Ordre croissant de gravité — l'évaluation teste du plus grave au moins grave.
 *
 * <p>Les seuils qui mènent à chaque niveau dépendent de l'âge et du genre : ils
 * ne sont donc pas rappelés ici, mais dans le barème que porte
 * {@code RiskService.evaluate}. Les repères ci-dessous valent pour un patient de
 * plus de 30 ans.
 *
 * <p>Sérialisé par son nom dans le rapport d'API : renommer une constante casse
 * le contrat côté client.
 */
public enum RiskLevel {

    /** Aucun risque décelé. Toujours atteignable, quels que soient l'âge et le genre. */
    NONE,

    /**
     * Risque limité. Niveau réservé aux patients de plus de 30 ans : le brief le
     * définit par « plus de 30 ans », il n'existe donc pas avant cet âge, où l'on
     * passe directement de {@link #NONE} à {@link #IN_DANGER}.
     */
    BORDERLINE,

    /** Risque avéré, appelant une surveillance. */
    IN_DANGER,

    /** Risque le plus élevé : apparition précoce. */
    EARLY_ONSET
}
