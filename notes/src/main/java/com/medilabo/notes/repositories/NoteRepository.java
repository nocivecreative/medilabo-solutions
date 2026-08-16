package com.medilabo.notes.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.medilabo.notes.model.Note;

/**
 * Accès aux notes d'observation en base documentaire.
 *
 * <p>Les opérations d'écriture utilisées par {@code NoteService} viennent de
 * {@link MongoRepository} ; seule la lecture de l'historique justifie une
 * méthode dédiée, pour porter son tri.
 */
public interface NoteRepository extends MongoRepository<Note, String> {

    /**
     * Historique d'un patient, du plus récent au plus ancien.
     *
     * <p>Query method dérivée : Spring Data traduit le tri en {@code sort} MongoDB
     * (pas de chargement complet puis tri en mémoire) — levier Green Code. Le tri
     * s'appuie sur l'index posé sur {@code patId}.
     *
     * @param patId identifiant du patient, tel que détenu par le patient-service
     * @return les notes du patient triées par date décroissante ; liste vide si
     *         aucune ne correspond
     */
    List<Note> findByPatIdOrderByDateDesc(Long patId);
}
