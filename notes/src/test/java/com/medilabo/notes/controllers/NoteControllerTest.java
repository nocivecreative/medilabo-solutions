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
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
            NoteDTO dto = new NoteDTO("a1", 2L, "stress au travail",
                    Instant.parse("2026-01-01T10:00:00Z"));
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
            NoteDTO input = new NoteDTO(null, 2L, "nouvelle observation", null);
            NoteDTO saved = new NoteDTO("gen1", 2L, "nouvelle observation", Instant.now());
            when(noteService.addNote(any(NoteDTO.class))).thenReturn(saved);

            // Act & Assert
            mockMvc.perform(post("/notes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", endsWith("/notes/gen1")))
                    .andExpect(jsonPath("$.id").value("gen1"));
        }

        /**
         * Les payloads rejetes par la validation Bean : memes acte et assertion,
         * seule la donnee change -> un seul test parametre plutot que N copies.
         */
        static Stream<Arguments> invalidPayloads() {
            return Stream.of(
                    Arguments.of("patId manquant", new NoteDTO(null, null, "orpheline", null)),
                    Arguments.of("note vide", new NoteDTO(null, 2L, "  ", null)),
                    Arguments.of("note absente", new NoteDTO(null, 2L, null, null)));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidPayloads")
        @DisplayName("Should return 400 when the payload is invalid")
        void shouldRejectInvalidPayload(String caseName, NoteDTO invalid) throws Exception {
            // Arrange — le payload invalide du cas courant.

            // Act & Assert
            mockMvc.perform(post("/notes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors").isArray());
        }
    }
}
