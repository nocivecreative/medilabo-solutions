package com.medilabo.patient.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medilabo.patient.dto.PatientDTO;
import com.medilabo.patient.exceptions.PatientNotFoundException;
import com.medilabo.patient.model.Patient;
import com.medilabo.patient.repositories.PatientRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PatientService implements IPatientService {

    private final PatientRepository patientRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDTO> getPatients(Pageable pageable) {
        // findAll(Pageable) traduit page/size/sort en LIMIT/OFFSET/ORDER BY cote SQL :
        // seule la tranche demandee est chargee (et non toute la table) -> levier Green Code.
        return patientRepository.findAll(pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));
        return toDTO(patient);
    }

    @Override
    @Transactional
    public PatientDTO createPatient(PatientDTO dto) {
        Patient patient = new Patient();
        applyTo(patient, dto);
        return toDTO(patientRepository.save(patient));
    }

    @Override
    @Transactional
    public PatientDTO updatePatient(Long id, PatientDTO dto) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));
        applyTo(patient, dto);
        return toDTO(patientRepository.save(patient));
    }

    /**
     * Recopie les champs modifiables du DTO vers l'entité — création et mise à jour
     * partagent la même liste, donc ajouter un champ ne se fait qu'à un seul endroit.
     *
     * <p>L'identifiant n'en fait volontairement PAS partie : il est généré par la base
     * à la création et immuable ensuite. Tout id transmis par le client est donc ignoré
     * par construction, sans avoir à le neutraliser après coup.
     */
    private void applyTo(Patient patient, PatientDTO dto) {
        patient.setPrenom(dto.prenom());
        patient.setNom(dto.nom());
        patient.setDateNaissance(dto.dateNaissance());
        patient.setGenre(dto.genre());
        patient.setTelephone(dto.telephone());
        patient.setAdresse(dto.adresse());
    }

    private PatientDTO toDTO(Patient p) {
        return new PatientDTO(p.getId(), p.getPrenom(), p.getNom(), p.getDateNaissance(),
                p.getGenre(), p.getTelephone(), p.getAdresse());
    }
}
