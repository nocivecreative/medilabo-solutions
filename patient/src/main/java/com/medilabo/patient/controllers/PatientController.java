package com.medilabo.patient.controllers;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.medilabo.patient.services.IPatientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

    private final IPatientService patientService;

    /**
     * Liste tous les patients.
     */
    @GetMapping
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        logger.info("[CALL] GET /patients");
        List<PatientDTO> patients = patientService.getAllPatients();
        logger.info("[RESPONSE] GET /patients -> {} patient(s)", patients.size());
        return ResponseEntity.ok(patients);
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
        logger.info("[CALL] POST /patients - {} {}", dto.getPrenom(), dto.getNom());
        PatientDTO created = patientService.createPatient(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        logger.info("[RESPONSE] POST /patients -> id={}", created.getId());
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
