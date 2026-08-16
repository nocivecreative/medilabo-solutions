package com.medilabo.patient.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medilabo.patient.model.Patient;

/**
 * Accès aux patients en base relationnelle.
 *
 * <p>Aucune méthode déclarée : les opérations utilisées par
 * {@code PatientService} (findAll paginé, findById, save) proviennent toutes de
 * {@link JpaRepository}. En particulier {@code findAll(Pageable)} traduit la
 * pagination en {@code LIMIT}/{@code OFFSET} côté SQL, de sorte que seule la
 * tranche demandée traverse le réseau et la mémoire.
 *
 * <p>Le schéma n'est pas généré depuis cette interface : l'application tourne en
 * {@code ddl-auto=validate} contre le DDL versionné dans {@code db/init}. Un écart
 * entre l'entité et la table fait échouer le démarrage plutôt que muter la base.
 */
public interface PatientRepository extends JpaRepository<Patient, Long> {
}
