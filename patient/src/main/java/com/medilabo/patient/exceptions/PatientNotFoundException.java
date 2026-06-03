package com.medilabo.patient.exceptions;

public class PatientNotFoundException extends RuntimeException {

    public PatientNotFoundException(Long id) {
        super("Patient introuvable pour l'identifiant : " + id);
    }
}
