package com.medilabo.patient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;
import com.medilabo.patient.dto.PatientDTO;
import com.medilabo.patient.model.Genre;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Patient API (end-to-end)")
class PatientApplicationE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private PatientDTO buildPatient(String prenom, String nom) {
        return PatientDTO.builder()
                .prenom(prenom)
                .nom(nom)
                .dateNaissance(LocalDate.of(1988, 7, 21))
                .genre(Genre.M)
                .telephone("0102030405")
                .adresse("8 boulevard Voltaire")
                .build();
    }

    private PatientDTO create(PatientDTO dto) throws Exception {
        String response = mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(response, PatientDTO.class);
    }

    @Test
    @DisplayName("A created patient can be fetched back by its generated id")
    void shouldCreateThenFetch() throws Exception {
        PatientDTO created = create(buildPatient("Camille", "Leroy"));
        assertThat(created.getId()).isNotNull();

        mockMvc.perform(get("/patients/{id}", created.getId()))
                .andExpect(status().isOk());

        String fetched = mockMvc.perform(get("/patients/{id}", created.getId()))
                .andReturn().getResponse().getContentAsString();
        PatientDTO body = objectMapper.readValue(fetched, PatientDTO.class);
        assertThat(body)
                .extracting(PatientDTO::getPrenom, PatientDTO::getNom)
                .containsExactly("Camille", "Leroy");
    }
}
