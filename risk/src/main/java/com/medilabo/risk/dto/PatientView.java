package com.medilabo.risk.dto;

import java.time.LocalDate;

/**
 * Vue partielle du patient telle que consommée depuis patient-service.
 * On ne lit que ce dont le calcul a besoin (âge + sexe) ; les autres champs
 * de la réponse JSON sont ignorés à la désérialisation.
 *
 * @param id            identifiant du patient, tel que renvoyé par patient-service
 * @param dateNaissance date de naissance, dont le calcul dérive l'âge
 * @param genre         genre administratif, reçu en texte brut ({@code "M"} ou
 *                      {@code "F"}) et non typé : ce service ne partage pas
 *                      l'énumération du patient-service, chacun restant
 *                      déployable indépendamment
 */
public record PatientView(Long id, LocalDate dateNaissance, String genre) {
}
