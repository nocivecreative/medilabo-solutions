package com.medilabo.notes.controllers;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;
import com.medilabo.notes.dto.NoteDTO;
import com.medilabo.notes.services.INoteService;

@WebMvcTest(NoteController.class)
@DisplayName("NoteController (web slice)")
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private INoteService noteService;

    @Test
    @DisplayName("GET /notes/patient/{patId} should return 200 with the history")
    void shouldReturnHistory() throws Exception {
        NoteDTO dto = NoteDTO.builder()
                .id("a1").patId(2L).note("stress au travail")
                .date(Instant.parse("2026-01-01T10:00:00Z")).build();
        when(noteService.getNotesByPatId(2L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/notes/patient/{patId}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].patId").value(2))
                .andExpect(jsonPath("$[0].note").value("stress au travail"));
    }

    @Test
    @DisplayName("GET /notes/patient/{patId} should return 200 with an empty array when no note")
    void shouldReturnEmptyHistory() throws Exception {
        when(noteService.getNotesByPatId(9L)).thenReturn(List.of());

        mockMvc.perform(get("/notes/patient/{patId}", 9L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("POST /notes should return 201 with a Location header")
    void shouldAddNote() throws Exception {
        NoteDTO input = NoteDTO.builder().patId(2L).note("nouvelle observation").build();
        NoteDTO saved = NoteDTO.builder()
                .id("gen1").patId(2L).note("nouvelle observation").date(Instant.now()).build();
        when(noteService.addNote(any(NoteDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/notes/gen1")))
                .andExpect(jsonPath("$.id").value("gen1"));
    }

    @Test
    @DisplayName("POST /notes should return 400 when patId is missing")
    void shouldRejectMissingPatId() throws Exception {
        NoteDTO invalid = NoteDTO.builder().note("orpheline").build();

        mockMvc.perform(post("/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("POST /notes should return 400 when note is blank")
    void shouldRejectBlankNote() throws Exception {
        NoteDTO invalid = NoteDTO.builder().patId(2L).note("  ").build();

        mockMvc.perform(post("/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }
}
