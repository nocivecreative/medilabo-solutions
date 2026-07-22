package com.medilabo.patient.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;
import com.medilabo.patient.dto.PatientDTO;
import com.medilabo.patient.exceptions.PatientNotFoundException;
import com.medilabo.patient.model.Genre;
import com.medilabo.patient.services.IPatientService;

/**
 * Test de tranche web (milieu de pyramide) : seule la couche MVC est chargee,
 * le service est mocke. Verifie le contrat HTTP (statuts, JSON, en-tetes).
 */
@WebMvcTest(PatientController.class)
@DisplayName("PatientController (web slice)")
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IPatientService patientService;

    /** Fabrique statique : utilisable aussi par les @MethodSource des classes @Nested. */
    private static PatientDTO validDto() {
        return PatientDTO.builder()
                .id(1L)
                .prenom("Jean")
                .nom("Dupont")
                .dateNaissance(LocalDate.of(1980, 5, 12))
                .genre(Genre.M)
                .telephone("0102030405")
                .adresse("12 rue des Lilas")
                .build();
    }

    /** Un DTO valide auquel on applique une mutation invalidante. */
    private static PatientDTO invalidatedBy(Consumer<PatientDTO> mutation) {
        PatientDTO dto = validDto();
        mutation.accept(dto);
        return dto;
    }

    /**
     * Champs obligatoires selon les user stories : prenom, nom, date de naissance, genre.
     * Meme acte et meme assertion pour chacun -> un seul test parametre.
     */
    static Stream<Arguments> invalidPayloads() {
        return Stream.of(
                Arguments.of("prenom absent", invalidatedBy(dto -> dto.setPrenom(null))),
                Arguments.of("nom vide", invalidatedBy(dto -> dto.setNom(""))),
                Arguments.of("date de naissance absente", invalidatedBy(dto -> dto.setDateNaissance(null))),
                Arguments.of("genre absent", invalidatedBy(dto -> dto.setGenre(null))));
    }

    @Nested
    @DisplayName("GET /patients")
    class ListPatients {

        @Test
        @DisplayName("Should return 200 with a paged patient list")
        void shouldListPatients() throws Exception {
            // Arrange
            when(patientService.getPatients(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(validDto())));

            // Act & Assert
            mockMvc.perform(get("/patients"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].nom").value("Dupont"))
                    .andExpect(jsonPath("$.page.totalElements").value(1));
        }

        @Test
        @DisplayName("Should apply the default paging (size 10, sorted by nom ASC) when no param is given")
        void shouldApplyDefaultPageable() throws Exception {
            // Arrange
            when(patientService.getPatients(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(validDto())));

            // Act
            mockMvc.perform(get("/patients"))
                    .andExpect(status().isOk());

            // Assert — le Pageable reellement transmis au service doit refleter @PageableDefault.
            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(patientService).getPatients(captor.capture());
            Pageable used = captor.getValue();
            assertThat(used.getPageSize()).isEqualTo(10);
            assertThat(used.getSort().getOrderFor("nom"))
                    .isNotNull()
                    .extracting(Sort.Order::getDirection)
                    .isEqualTo(Sort.Direction.ASC);
        }
    }

    @Nested
    @DisplayName("GET /patients/{id}")
    class GetPatientById {

        @Test
        @DisplayName("Should return 200 with the patient")
        void shouldReturnPatientById() throws Exception {
            // Arrange
            when(patientService.getPatientById(1L)).thenReturn(validDto());

            // Act & Assert
            mockMvc.perform(get("/patients/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.prenom").value("Jean"))
                    .andExpect(jsonPath("$.dateNaissance").value("1980-05-12"));
        }

        @Test
        @DisplayName("Should return 404 when the patient is unknown")
        void shouldReturn404WhenPatientMissing() throws Exception {
            // Arrange
            when(patientService.getPatientById(99L)).thenThrow(new PatientNotFoundException(99L));

            // Act & Assert
            mockMvc.perform(get("/patients/{id}", 99L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message")
                            .value("Patient introuvable pour l'identifiant : 99"));
        }
    }

    @Nested
    @DisplayName("POST /patients")
    class CreatePatient {

        @Test
        @DisplayName("Should return 201 with a Location header")
        void shouldCreatePatient() throws Exception {
            // Arrange
            PatientDTO toCreate = validDto();
            toCreate.setId(null);
            when(patientService.createPatient(any(PatientDTO.class))).thenReturn(validDto());

            // Act & Assert
            mockMvc.perform(post("/patients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(toCreate)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", endsWith("/patients/1")))
                    .andExpect(jsonPath("$.id").value(1));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.medilabo.patient.controllers.PatientControllerTest#invalidPayloads")
        @DisplayName("Should return 400 when a mandatory field is missing")
        void shouldRejectInvalidPayload(String caseName, PatientDTO invalid) throws Exception {
            // Arrange — le payload invalide du cas courant.

            // Act & Assert
            mockMvc.perform(post("/patients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors").isArray());
        }
    }

    @Nested
    @DisplayName("PUT /patients/{id}")
    class UpdatePatient {

        @Test
        @DisplayName("Should return 200 with the updated patient")
        void shouldUpdatePatient() throws Exception {
            // Arrange
            PatientDTO payload = validDto();
            when(patientService.updatePatient(eq(1L), any(PatientDTO.class))).thenReturn(validDto());

            // Act & Assert
            mockMvc.perform(put("/patients/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("Should return 404 when the patient is unknown")
        void shouldReturn404WhenUpdatingMissingPatient() throws Exception {
            // Arrange
            PatientDTO payload = validDto();
            when(patientService.updatePatient(eq(99L), any(PatientDTO.class)))
                    .thenThrow(new PatientNotFoundException(99L));

            // Act & Assert
            mockMvc.perform(put("/patients/{id}", 99L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isNotFound());
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.medilabo.patient.controllers.PatientControllerTest#invalidPayloads")
        @DisplayName("Should return 400 when a mandatory field is invalid")
        void shouldRejectInvalidUpdate(String caseName, PatientDTO invalid) throws Exception {
            // Arrange — la validation s'applique aussi a la mise a jour.

            // Act & Assert
            mockMvc.perform(put("/patients/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").isArray());
        }
    }
}
