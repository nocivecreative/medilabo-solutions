package com.medilabo.patient.model;

/**
 * Genre administratif du patient, tel que le porte le dossier médical.
 *
 * <p>Volontairement binaire et non extensible : le barème de risque du
 * risk-service distingue les seuils « homme » et « femme » pour les patients de
 * moins de 30 ans, et n'a pas de branche pour une troisième valeur. Ajouter une
 * constante ici sans reprendre ce barème ferait silencieusement tomber les
 * patients concernés dans la branche « femme ».
 *
 * <p>Persisté en {@code VARCHAR(1)} via {@code @Enumerated(EnumType.STRING)} :
 * c'est le NOM de la constante qui est stocké, pas son ordinal — réordonner
 * l'énumération ne corrompt donc pas les données existantes.
 */
public enum Genre {

    /** Homme. */
    M,

    /** Femme. */
    F
}
