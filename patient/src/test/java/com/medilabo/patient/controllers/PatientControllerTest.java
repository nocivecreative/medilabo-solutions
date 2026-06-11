package com.medilabo.patient.controllers;

import static org.assertj.core.api.Assertions.assertThat;
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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

@WebMvcTest(PatientController.class)
@DisplayName("PatientController (web slice)")
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IPatientService patientService;

    private PatientDTO validDto() {
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

    @Test
    @DisplayName("GET /patients should return 200 with a paged patient list")
    void shouldListPatients() throws Exception {
        when(patientService.getPatients(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(validDto())));

        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].nom").value("Dupont"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /patients without params applies the default paging (size 10, sorted by nom ASC)")
    void shouldApplyDefaultPageable() throws Exception {
        when(patientService.getPatients(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(validDto())));

        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk());

        // Le Pageable réellement transmis au service doit refléter @PageableDefault.
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(patientService).getPatients(captor.capture());
        Pageable used = captor.getValue();
        assertThat(used.getPageSize()).isEqualTo(10);
        assertThat(used.getSort().getOrderFor("nom"))
                .isNotNull()
                .extracting(Sort.Order::getDirection)
                .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("GET /patients/{id} should return 200 with the patient")
    void shouldReturnPatientById() throws Exception {
        when(patientService.getPatientById(1L)).thenReturn(validDto());

        mockMvc.perform(get("/patients/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.prenom").value("Jean"))
                .andExpect(jsonPath("$.dateNaissance").value("1980-05-12"));
    }

    @Test
    @DisplayName("GET /patients/{id} should return 404 when patient is unknown")
    void shouldReturn404WhenPatientMissing() throws Exception {
        when(patientService.getPatientById(99L)).thenThrow(new PatientNotFoundException(99L));

        mockMvc.perform(get("/patients/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Patient introuvable pour l'identifiant : 99"));
    }

    @Test
    @DisplayName("POST /patients should return 201 with a Location header")
    void shouldCreatePatient() throws Exception {
        PatientDTO toCreate = validDto();
        toCreate.setId(null);
        when(patientService.createPatient(any(PatientDTO.class))).thenReturn(validDto());

        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(toCreate)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/patients/1")))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /patients should return 400 when a mandatory field is missing")
    void shouldRejectInvalidCreation() throws Exception {
        PatientDTO invalid = validDto();
        invalid.setPrenom(null);

        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("PUT /patients/{id} should return 200 with the updated patient")
    void shouldUpdatePatient() throws Exception {
        PatientDTO payload = validDto();
        when(patientService.updatePatient(eq(1L), any(PatientDTO.class))).thenReturn(validDto());

        mockMvc.perform(put("/patients/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PUT /patients/{id} should return 404 when patient is unknown")
    void shouldReturn404WhenUpdatingMissingPatient() throws Exception {
        PatientDTO payload = validDto();
        when(patientService.updatePatient(eq(99L), any(PatientDTO.class)))
                .thenThrow(new PatientNotFoundException(99L));

        mockMvc.perform(put("/patients/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /patients/{id} should return 400 when a mandatory field is invalid")
    void shouldRejectInvalidUpdate() throws Exception {
        PatientDTO invalid = validDto();
        invalid.setNom("");

        mockMvc.perform(put("/patients/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }
}
