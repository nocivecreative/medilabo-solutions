package com.medilabo.risk.dto;

import java.time.LocalDate;

/**
 * Vue partielle du patient telle que consommée depuis patient-service.
 * On ne lit que ce dont le calcul a besoin (âge + sexe) ; les autres champs
 * de la réponse JSON sont ignorés à la désérialisation.
 */
public record PatientView(Long id, LocalDate dateNaissance, String genre) {
}
