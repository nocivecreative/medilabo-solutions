package com.medilabo.patient.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.test.context.ActiveProfiles;

import com.medilabo.patient.model.Genre;
import com.medilabo.patient.model.Patient;

/**
 * Test de tranche (milieu de pyramide) : seule la couche JPA est chargee,
 * sur H2 en memoire. Verifie le mapping entite/table, pas la logique metier.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DisplayName("PatientRepository (JPA slice)")
class PatientRepositoryTest {

    @Autowired
    private PatientRepository patientRepository;

    private Patient newPatient() {
        return Patient.builder()
                .prenom("Alice")
                .nom("Bernard")
                .dateNaissance(LocalDate.of(1975, 11, 2))
                .genre(Genre.F)
                .telephone("0708091011")
                .adresse("3 impasse des Roses")
                .build();
    }

    @Test
    @DisplayName("Should generate an id and persist all fields including the genre enum")
    void shouldPersistAndReadBack() {
        // Arrange
        Patient toPersist = newPatient();

        // Act
        Patient saved = patientRepository.save(toPersist);
        Patient reloaded = patientRepository.findById(saved.getId()).orElseThrow();

        // Assert
        assertThat(saved.getId())
                .as("L'identifiant doit etre genere par la base")
                .isNotNull();
        assertThat(reloaded)
                .extracting(Patient::getPrenom, Patient::getNom, Patient::getDateNaissance,
                        Patient::getGenre, Patient::getTelephone, Patient::getAdresse)
                .containsExactly("Alice", "Bernard", LocalDate.of(1975, 11, 2),
                        Genre.F, "0708091011", "3 impasse des Roses");
    }

    @Test
    @DisplayName("Should return all persisted patients")
    void shouldReturnAllPatients() {
        // Arrange
        patientRepository.save(newPatient());
        patientRepository.save(newPatient());

        // Act
        var allPatients = patientRepository.findAll();

        // Assert
        assertThat(allPatients).hasSize(2);
    }
}
