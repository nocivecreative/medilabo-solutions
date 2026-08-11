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

/**
 * Test de bout en bout (sommet de la pyramide) : contexte complet, vraie
 * persistance (H2). Verifie qu'un patient cree traverse toutes les couches
 * et se relit correctement — peu nombreux par nature, car les plus couteux.
 */
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
        return new PatientDTO(null, prenom, nom, LocalDate.of(1988, 7, 21), Genre.M,
                "0102030405", "8 boulevard Voltaire");
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
        // Arrange & Act — creation via l'API, id genere par la base
        PatientDTO created = create(buildPatient("Camille", "Leroy"));

        // Act — relecture par l'id genere
        String fetched = mockMvc.perform(get("/patients/{id}", created.id()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        PatientDTO body = objectMapper.readValue(fetched, PatientDTO.class);

        // Assert
        assertThat(created.id()).isNotNull();
        assertThat(body)
                .extracting(PatientDTO::prenom, PatientDTO::nom)
                .containsExactly("Camille", "Leroy");
    }
}
