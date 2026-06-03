package com.medilabo.patient.services;

import java.util.List;

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
    public List<PatientDTO> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
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
        Patient patient = toEntity(dto);
        // L'identifiant est généré par la base — on ignore tout id transmis dans le payload.
        patient.setId(null);
        return toDTO(patientRepository.save(patient));
    }

    @Override
    @Transactional
    public PatientDTO updatePatient(Long id, PatientDTO dto) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));
        patient.setPrenom(dto.getPrenom());
        patient.setNom(dto.getNom());
        patient.setDateNaissance(dto.getDateNaissance());
        patient.setGenre(dto.getGenre());
        patient.setTelephone(dto.getTelephone());
        patient.setAdresse(dto.getAdresse());
        return toDTO(patientRepository.save(patient));
    }

    private PatientDTO toDTO(Patient p) {
        return PatientDTO.builder()
                .id(p.getId())
                .prenom(p.getPrenom())
                .nom(p.getNom())
                .dateNaissance(p.getDateNaissance())
                .genre(p.getGenre())
                .telephone(p.getTelephone())
                .adresse(p.getAdresse())
                .build();
    }

    private Patient toEntity(PatientDTO dto) {
        return Patient.builder()
                .id(dto.getId())
                .prenom(dto.getPrenom())
                .nom(dto.getNom())
                .dateNaissance(dto.getDateNaissance())
                .genre(dto.getGenre())
                .telephone(dto.getTelephone())
                .adresse(dto.getAdresse())
                .build();
    }
}
