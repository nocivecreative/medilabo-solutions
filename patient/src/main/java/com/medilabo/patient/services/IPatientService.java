package com.medilabo.patient.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.medilabo.patient.dto.PatientDTO;

/**
 * Service contract for Patient management.
 */
public interface IPatientService {

    Page<PatientDTO> getPatients(Pageable pageable);

    PatientDTO getPatientById(Long id);

    PatientDTO createPatient(PatientDTO dto);

    PatientDTO updatePatient(Long id, PatientDTO dto);
}
