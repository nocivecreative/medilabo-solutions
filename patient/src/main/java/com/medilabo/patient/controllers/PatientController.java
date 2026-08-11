package com.medilabo.patient.controllers;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.medilabo.patient.dto.PatientDTO;
import com.medilabo.patient.services.PatientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

    private final PatientService patientService;

    /**
     * Liste paginée des patients.
     * Paramètres de requête : ?page=0&size=20&sort=nom,asc (résolus par Spring Data).
     * La réponse est un {@link PagedModel} : format JSON stable ({@code content} + bloc
     * {@code page}), recommandé plutôt que la sérialisation directe d'une {@code Page}.
     */
    @GetMapping
    public ResponseEntity<PagedModel<PatientDTO>> getPatients(
            @PageableDefault(size = 10, sort = "nom", direction = Sort.Direction.ASC)
            Pageable pageable) {
        logger.info("[CALL] GET /patients - page={} size={} sort=[{}]",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<PatientDTO> page = patientService.getPatients(pageable);
        logger.info("[RESPONSE] GET /patients -> {} patient(s) sur {} au total",
                page.getNumberOfElements(), page.getTotalElements());
        return ResponseEntity.ok(new PagedModel<>(page));
    }

    /**
     * Récupère un patient par son identifiant.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable Long id) {
        logger.info("[CALL] GET /patients/{}", id);
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    /**
     * Crée un nouveau patient.
     */
    @PostMapping
    public ResponseEntity<PatientDTO> createPatient(@Valid @RequestBody PatientDTO dto) {
        logger.info("[CALL] POST /patients - {} {}", dto.prenom(), dto.nom());
        PatientDTO created = patientService.createPatient(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        logger.info("[RESPONSE] POST /patients -> id={}", created.id());
        return ResponseEntity.created(location).body(created);
    }

    /**
     * Met à jour les informations d'un patient existant.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PatientDTO> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientDTO dto) {
        logger.info("[CALL] PUT /patients/{}", id);
        PatientDTO updated = patientService.updatePatient(id, dto);
        logger.info("[RESPONSE] PUT /patients/{} -> OK", id);
        return ResponseEntity.ok(updated);
    }
}
