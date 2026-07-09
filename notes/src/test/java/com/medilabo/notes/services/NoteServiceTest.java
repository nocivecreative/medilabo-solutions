package com.medilabo.notes.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.medilabo.notes.dto.NoteDTO;
import com.medilabo.notes.model.Note;
import com.medilabo.notes.repositories.NoteRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("NoteService (unit)")
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private NoteService noteService;

    @Test
    @DisplayName("getNotesByPatId maps the repository result to DTOs")
    void shouldReturnHistoryAsDto() {
        Note n = Note.builder()
                .id("a1").patId(2L).note("stress")
                .date(Instant.parse("2026-01-01T10:00:00Z")).build();
        when(noteRepository.findByPatIdOrderByDateDesc(2L)).thenReturn(List.of(n));

        List<NoteDTO> result = noteService.getNotesByPatId(2L);

        assertThat(result).singleElement().satisfies(dto -> {
            assertThat(dto.getId()).isEqualTo("a1");
            assertThat(dto.getPatId()).isEqualTo(2L);
            assertThat(dto.getNote()).isEqualTo("stress");
            assertThat(dto.getDate()).isEqualTo(Instant.parse("2026-01-01T10:00:00Z"));
        });
    }

    @Test
    @DisplayName("getNotesByPatId returns an empty list when the patient has no note")
    void shouldReturnEmptyHistory() {
        when(noteRepository.findByPatIdOrderByDateDesc(9L)).thenReturn(List.of());
        assertThat(noteService.getNotesByPatId(9L)).isEmpty();
    }

    @Test
    @DisplayName("addNote stamps a server-side date and ignores any client-supplied id/date")
    void shouldStampDateOnCreate() {
        NoteDTO input = NoteDTO.builder()
                .id("should-be-ignored").patId(3L).note("fumeur")
                .date(Instant.parse("1999-01-01T00:00:00Z")).build();
        // Renvoie une NOUVELLE instance (comme Mongo) : ne pas muter l'objet capture,
        // sinon l'ArgumentCaptor verrait l'id post-save au lieu de celui pose par le service.
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> {
            Note in = inv.getArgument(0);
            return Note.builder()
                    .id("generated-id").patId(in.getPatId())
                    .note(in.getNote()).date(in.getDate()).build();
        });

        NoteDTO result = noteService.addNote(input);

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(captor.capture());
        Note persisted = captor.getValue();
        assertThat(persisted.getId()).isNull();                 // id laisse a Mongo
        assertThat(persisted.getPatId()).isEqualTo(3L);
        assertThat(persisted.getNote()).isEqualTo("fumeur");
        assertThat(persisted.getDate())
                .isNotNull()
                .isNotEqualTo(Instant.parse("1999-01-01T00:00:00Z")); // date client ignoree
        assertThat(result.getId()).isEqualTo("generated-id");
    }
}
