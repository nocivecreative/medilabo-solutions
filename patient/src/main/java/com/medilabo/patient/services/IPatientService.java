package com.medilabo.patient.services;

import java.util.List;

import com.medilabo.patient.dto.PatientDTO;

/**
 * Service contract for Patient management.
 */
public interface IPatientService {

    List<PatientDTO> getAllPatients();

    PatientDTO getPatientById(Long id);

    PatientDTO createPatient(PatientDTO dto);

    PatientDTO updatePatient(Long id, PatientDTO dto);
}
