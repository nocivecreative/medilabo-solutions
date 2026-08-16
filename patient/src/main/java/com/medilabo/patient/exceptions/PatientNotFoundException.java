package com.medilabo.patient.exceptions;

/**
 * Signale qu'aucun patient ne porte l'identifiant demandé.
 *
 * <p>Non vérifiée (hérite de {@link RuntimeException}) : la couche service la
 * laisse remonter sans la déclarer, et {@code GlobalExceptionHandler} la traduit
 * en réponse {@code 404 Not Found} au format {@code ProblemDetail}. Aucun
 * appelant n'a donc à l'attraper pour produire le bon statut HTTP.
 */
public class PatientNotFoundException extends RuntimeException {

    /**
     * Construit l'exception avec un message mentionnant l'identifiant cherché.
     *
     * <p>Le message part tel quel dans le {@code detail} de la réponse HTTP :
     * il ne contient donc que l'identifiant, jamais de donnée nominative ou
     * médicale.
     *
     * @param id identifiant réclamé par le client, absent de la base
     */
    public PatientNotFoundException(Long id) {
        super("Patient introuvable pour l'identifiant : " + id);
    }
}
