package com.medilabo.notes.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.medilabo.notes.model.Note;

public interface NoteRepository extends MongoRepository<Note, String> {

    /**
     * Historique d'un patient, du plus récent au plus ancien.
     * Query method dérivée : Spring Data traduit le tri en {@code sort} MongoDB
     * (pas de chargement complet puis tri en mémoire) — levier Green Code.
     */
    List<Note> findByPatIdOrderByDateDesc(Long patId);
}
