package com.medilabo.notes.services;

import java.util.List;

import com.medilabo.notes.dto.NoteDTO;

/**
 * Contrat de gestion des notes d'observation.
 */
public interface INoteService {

    /** Historique d'un patient, du plus récent au plus ancien. */
    List<NoteDTO> getNotesByPatId(Long patId);

    /** Ajoute une note ; l'horodatage est posé côté serveur. */
    NoteDTO addNote(NoteDTO dto);
}
