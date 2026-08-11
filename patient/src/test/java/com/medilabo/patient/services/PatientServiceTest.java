package com.medilabo.patient.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.medilabo.patient.dto.PatientDTO;
import com.medilabo.patient.exceptions.PatientNotFoundException;
import com.medilabo.patient.model.Genre;
import com.medilabo.patient.model.Patient;
import com.medilabo.patient.repositories.PatientRepository;

/**
 * Tests unitaires (base de la pyramide) : repository mocke, aucun contexte Spring.
 * Structure Arrange-Act-Assert, un comportement verifie par test.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PatientService (unit)")
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient existingPatient;

    @BeforeEach
    void setUp() {
        existingPatient = Patient.builder()
                .id(1L)
                .prenom("Jean")
                .nom("Dupont")
                .dateNaissance(LocalDate.of(1980, 5, 12))
                .genre(Genre.M)
                .telephone("0102030405")
                .adresse("12 rue des Lilas")
                .build();
    }

    private PatientDTO sampleDto() {
        return new PatientDTO(null, "Marie", "Martin", LocalDate.of(1992, 3, 8), Genre.F,
                "0605040302", "5 avenue du Parc");
    }

    @Nested
    @DisplayName("getPatients")
    class GetPatients {

        @Test
        @DisplayName("Should return an empty page when no patient exists")
        void shouldReturnEmptyPageWhenNoPatient() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(patientRepository.findAll(pageable)).thenReturn(Page.empty(pageable));

            // Act
            Page<PatientDTO> result = patientService.getPatients(pageable);

            // Assert
            assertThat(result).isEmpty();
            verify(patientRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should map every entity of the page to its DTO")
        void shouldMapEntitiesToDtos() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(patientRepository.findAll(pageable))
                    .thenReturn(new PageImpl<>(List.of(existingPatient), pageable, 1));

            // Act
            Page<PatientDTO> result = patientService.getPatients(pageable);

            // Assert
            assertThat(result.getContent())
                    .singleElement()
                    .extracting(PatientDTO::id, PatientDTO::prenom, PatientDTO::nom,
                            PatientDTO::dateNaissance, PatientDTO::genre,
                            PatientDTO::telephone, PatientDTO::adresse)
                    .containsExactly(1L, "Jean", "Dupont", LocalDate.of(1980, 5, 12),
                            Genre.M, "0102030405", "12 rue des Lilas");
        }
    }

    @Nested
    @DisplayName("getPatientById")
    class GetPatientById {

        @Test
        @DisplayName("Should return the patient when it exists")
        void shouldReturnPatientWhenFound() {
            // Arrange
            when(patientRepository.findById(1L)).thenReturn(Optional.of(existingPatient));

            // Act
            PatientDTO result = patientService.getPatientById(1L);

            // Assert
            assertThat(result)
                    .extracting(PatientDTO::id, PatientDTO::nom)
                    .containsExactly(1L, "Dupont");
        }

        @Test
        @DisplayName("Should throw PatientNotFoundException when patient is missing")
        void shouldThrowWhenNotFound() {
            // Arrange
            when(patientRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> patientService.getPatientById(99L))
                    .isInstanceOf(PatientNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("createPatient")
    class CreatePatient {

        @Test
        @DisplayName("Should force id to null and persist the new patient")
        void shouldForceIdToNullAndSave() {
            // Arrange
            // Payload portant un id : le service doit l'ignorer, la base seule le genere.
            PatientDTO input = new PatientDTO(123L, "Marie", "Martin", LocalDate.of(1992, 3, 8),
                    Genre.F, "0605040302", "5 avenue du Parc");
            // On retourne une COPIE avec l'id genere, sans muter l'argument capture
            // (le captor garde la reference : le muter ici fausserait l'assertion sur l'id null).
            when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
                Patient toSave = invocation.getArgument(0);
                return Patient.builder()
                        .id(7L)
                        .prenom(toSave.getPrenom())
                        .nom(toSave.getNom())
                        .dateNaissance(toSave.getDateNaissance())
                        .genre(toSave.getGenre())
                        .telephone(toSave.getTelephone())
                        .adresse(toSave.getAdresse())
                        .build();
            });

            // Act
            PatientDTO result = patientService.createPatient(input);

            // Assert
            ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);
            verify(patientRepository).save(captor.capture());
            assertThat(captor.getValue().getId())
                    .as("Id transmis dans le payload doit etre ignore")
                    .isNull();
            assertThat(result)
                    .extracting(PatientDTO::id, PatientDTO::nom)
                    .containsExactly(7L, "Martin");
        }
    }

    @Nested
    @DisplayName("updatePatient")
    class UpdatePatient {

        @Test
        @DisplayName("Should update every field of an existing patient")
        void shouldUpdateExistingPatient() {
            // Arrange
            when(patientRepository.findById(1L)).thenReturn(Optional.of(existingPatient));
            when(patientRepository.save(any(Patient.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            PatientDTO update = sampleDto();

            // Act
            PatientDTO result = patientService.updatePatient(1L, update);

            // Assert
            assertThat(result)
                    .extracting(PatientDTO::id, PatientDTO::prenom, PatientDTO::nom,
                            PatientDTO::dateNaissance, PatientDTO::genre,
                            PatientDTO::telephone, PatientDTO::adresse)
                    .containsExactly(1L, "Marie", "Martin", LocalDate.of(1992, 3, 8),
                            Genre.F, "0605040302", "5 avenue du Parc");
            verify(patientRepository).save(existingPatient);
        }

        @Test
        @DisplayName("Should throw PatientNotFoundException and not save when patient is missing")
        void shouldThrowWhenUpdatingMissingPatient() {
            // Arrange
            when(patientRepository.findById(42L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> patientService.updatePatient(42L, sampleDto()))
                    .isInstanceOf(PatientNotFoundException.class)
                    .hasMessageContaining("42");
            verify(patientRepository, never()).save(any());
        }
    }
}
