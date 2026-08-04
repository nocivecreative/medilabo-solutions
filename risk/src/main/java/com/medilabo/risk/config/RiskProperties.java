package com.medilabo.risk.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration externalisée du calcul de risque.
 *
 * <p>Les termes déclencheurs ne sont PAS en dur : ils vivent dans
 * {@code application.properties} (clé {@code medilabo.risk.triggers}) pour rester
 * extensibles sans recompilation — le brief indique une liste ouverte (« … »).
 * Ils sont fournis sous forme de radicaux normalisés (minuscules, sans accents),
 * ce qui gère singulier/pluriel (« vertige » capte « Vertige » et « Vertiges »).
 */
@ConfigurationProperties(prefix = "medilabo.risk")
@Getter
@Setter
public class RiskProperties {

    private List<String> triggers = List.of();

    /** URI interne du patient-service (surchargée selon le profil local/docker). */
    private String patientServiceUri;

    /** URI interne du notes-service (surchargée selon le profil local/docker). */
    private String notesServiceUri;
}
