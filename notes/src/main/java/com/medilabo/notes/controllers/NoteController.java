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
import com.medilabo.notes.services.NoteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * API REST des notes d'observation du praticien, exposée sous {@code /notes}.
 *
 * <p>Le préfixe est distinct de {@code /patients} bien que les deux routes
 * portent un identifiant de patient : à la gateway, deux services différents
 * répondent derrière ces préfixes, qui doivent donc rester disjoints pour que
 * le routage soit décidable.
 *
 * <p>Service interne, non publié vers l'hôte : l'authentification est portée par
 * la gateway pour toute la stack, rien n'est vérifié ici.
 *
 * <p>Les erreurs remontent à {@code GlobalExceptionHandler}, qui les rend au
 * format {@code ProblemDetail} (RFC 9457) — {@code 400} en cas de payload
 * invalide.
 *
 * <p>Les journaux ne contiennent jamais le texte des notes : c'est une donnée de
 * santé, et les journaux d'un conteneur n'ont pas le contrôle d'accès de la
 * base. Seuls l'identifiant du patient et le nombre de notes y figurent.
 */
@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private static final Logger logger = LoggerFactory.getLogger(NoteController.class);

    private final NoteService noteService;

    /**
     * Historique des notes d'un patient — {@code GET /notes/patient/{patId}}.
     *
     * <p>Sert l'US « Vue historique du patient ». Les notes sont renvoyées de la
     * plus récente à la plus ancienne, ordre sur lequel s'appuie l'affichage.
     *
     * @param patId identifiant du patient dont on veut l'historique
     * @return {@code 200 OK} et les notes triées par date décroissante ; un
     *         patient sans note, comme un identifiant inconnu, donne une liste
     *         vide et non un {@code 404} — l'existence du patient est du ressort
     *         du patient-service
     */
    @GetMapping("/patient/{patId}")
    public ResponseEntity<List<NoteDTO>> getNotesByPatient(@PathVariable Long patId) {
        logger.info("[CALL] GET /notes/patient/{}", patId);
        List<NoteDTO> notes = noteService.getNotesByPatId(patId);
        logger.info("[RESPONSE] GET /notes/patient/{} -> {} note(s)", patId, notes.size());
        return ResponseEntity.ok(notes);
    }

    /**
     * Ajoute une note d'observation — {@code POST /notes}.
     *
     * <p>Sert l'US « Ajouter une note à l'historique ». Répond {@code 201 Created}
     * avec un en-tête {@code Location} construit sur l'identifiant attribué par
     * MongoDB. L'identifiant et la date présents dans le corps sont ignorés : le
     * serveur les maîtrise.
     *
     * @param dto note à enregistrer ; validée avant l'entrée dans cette méthode,
     *            une violation produisant un {@code 400 Bad Request} détaillant
     *            les champs en échec
     * @return {@code 201 Created}, la note enregistrée en corps et son URL en
     *         {@code Location}
     */
    @PostMapping
    public ResponseEntity<NoteDTO> addNote(@Valid @RequestBody NoteDTO dto) {
        logger.info("[CALL] POST /notes - patId={}", dto.patId());
        NoteDTO created = noteService.addNote(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        logger.info("[RESPONSE] POST /notes -> id={}", created.id());
        return ResponseEntity.created(location).body(created);
    }
}
