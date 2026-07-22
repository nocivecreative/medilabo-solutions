package com.medilabo.notes.controllers;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;
import com.medilabo.notes.dto.NoteDTO;
import com.medilabo.notes.services.INoteService;

/**
 * Test de tranche web (milieu de pyramide) : seule la couche MVC est chargee,
 * le service est mocke. Verifie le contrat HTTP (statuts, JSON, en-tetes).
 */
@WebMvcTest(NoteController.class)
@DisplayName("NoteController (web slice)")
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private INoteService noteService;

    @Nested
    @DisplayName("GET /notes/patient/{patId}")
    class GetHistory {

        @Test
        @DisplayName("Should return 200 with the patient history")
        void shouldReturnHistory() throws Exception {
            // Arrange
            NoteDTO dto = NoteDTO.builder()
                    .id("a1").patId(2L).note("stress au travail")
                    .date(Instant.parse("2026-01-01T10:00:00Z")).build();
            when(noteService.getNotesByPatId(2L)).thenReturn(List.of(dto));

            // Act & Assert
            mockMvc.perform(get("/notes/patient/{patId}", 2L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].patId").value(2))
                    .andExpect(jsonPath("$[0].note").value("stress au travail"));
        }

        @Test
        @DisplayName("Should return 200 with an empty array when the patient has no note")
        void shouldReturnEmptyHistory() throws Exception {
            // Arrange
            when(noteService.getNotesByPatId(9L)).thenReturn(List.of());

            // Act & Assert
            mockMvc.perform(get("/notes/patient/{patId}", 9L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("POST /notes")
    class AddNote {

        @Test
        @DisplayName("Should return 201 with a Location header")
        void shouldAddNote() throws Exception {
            // Arrange
            NoteDTO input = NoteDTO.builder().patId(2L).note("nouvelle observation").build();
            NoteDTO saved = NoteDTO.builder()
                    .id("gen1").patId(2L).note("nouvelle observation").date(Instant.now()).build();
            when(noteService.addNote(any(NoteDTO.class))).thenReturn(saved);

            // Act & Assert
            mockMvc.perform(post("/notes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", endsWith("/notes/gen1")))
                    .andExpect(jsonPath("$.id").value("gen1"));
        }

        @Test
        @DisplayName("Should return 400 when patId is missing")
        void shouldRejectMissingPatId() throws Exception {
            // Arrange
            NoteDTO invalid = NoteDTO.builder().note("orpheline").build();

            // Act & Assert
            mockMvc.perform(post("/notes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors").isArray());
        }

        @Test
        @DisplayName("Should return 400 when the note text is blank")
        void shouldRejectBlankNote() throws Exception {
            // Arrange
            NoteDTO invalid = NoteDTO.builder().patId(2L).note("  ").build();

            // Act & Assert
            mockMvc.perform(post("/notes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").isArray());
        }
    }
}
