package com.medilabo.notes.controllers;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.medilabo.notes.dto.NoteDTO;
import com.medilabo.notes.services.INoteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private static final Logger logger = LoggerFactory.getLogger(NoteController.class);

    private final INoteService noteService;

    /**
     * Historique des notes d'un patient (US « Vue historique du patient »).
     * Préfixe /notes distinct de /patients pour éviter la collision de routage à la gateway.
     */
    @GetMapping("/patient/{patId}")
    public ResponseEntity<List<NoteDTO>> getNotesByPatient(@PathVariable Long patId) {
        logger.info("[CALL] GET /notes/patient/{}", patId);
        List<NoteDTO> notes = noteService.getNotesByPatId(patId);
        logger.info("[RESPONSE] GET /notes/patient/{} -> {} note(s)", patId, notes.size());
        return ResponseEntity.ok(notes);
    }

    /**
     * Ajoute une note d'observation (US « Ajouter une note à l'historique »).
     */
    @PostMapping
    public ResponseEntity<NoteDTO> addNote(@Valid @RequestBody NoteDTO dto) {
        logger.info("[CALL] POST /notes - patId={}", dto.getPatId());
        NoteDTO created = noteService.addNote(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        logger.info("[RESPONSE] POST /notes -> id={}", created.getId());
        return ResponseEntity.created(location).body(created);
    }
}
