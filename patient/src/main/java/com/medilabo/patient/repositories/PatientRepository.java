package com.medilabo.patient.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medilabo.patient.model.Patient;

public interface PatientRepository extends JpaRepository<Patient, Long> {
}
