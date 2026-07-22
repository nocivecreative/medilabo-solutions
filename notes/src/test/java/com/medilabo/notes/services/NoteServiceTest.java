package com.medilabo.notes.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.medilabo.notes.dto.NoteDTO;
import com.medilabo.notes.model.Note;
import com.medilabo.notes.repositories.NoteRepository;

/**
 * Tests unitaires (base de la pyramide) : repository mocke, aucun contexte Spring,
 * aucune base Mongo. Structure Arrange-Act-Assert, un comportement par test.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NoteService (unit)")
class NoteServiceTest {

    private static final Instant OLDER = Instant.parse("2024-01-15T10:30:00Z");
    private static final Instant RECENT = Instant.parse("2024-04-20T11:00:00Z");

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private NoteService noteService;

    private Note note(String id, Long patId, String text, Instant date) {
        return Note.builder().id(id).patId(patId).note(text).date(date).build();
    }

    @Nested
    @DisplayName("getNotesByPatId")
    class GetNotesByPatId {

        @Test
        @DisplayName("Should return an empty history when the patient has no note")
        void shouldReturnEmptyHistory() {
            // Arrange
            when(noteRepository.findByPatIdOrderByDateDesc(9L)).thenReturn(List.of());

            // Act
            List<NoteDTO> result = noteService.getNotesByPatId(9L);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should map every entity of the history to its DTO")
        void shouldMapEntitiesToDtos() {
            // Arrange
            when(noteRepository.findByPatIdOrderByDateDesc(2L))
                    .thenReturn(List.of(note("a1", 2L, "stress au travail", OLDER)));

            // Act
            List<NoteDTO> result = noteService.getNotesByPatId(2L);

            // Assert
            assertThat(result)
                    .singleElement()
                    .extracting(NoteDTO::getId, NoteDTO::getPatId, NoteDTO::getNote, NoteDTO::getDate)
                    .containsExactly("a1", 2L, "stress au travail", OLDER);
        }

        @Test
        @DisplayName("Should preserve the ordering returned by the repository")
        void shouldPreserveRepositoryOrdering() {
            // Arrange — le tri est delegue a Mongo (query method OrderByDateDesc),
            // le service ne doit pas retrier en memoire.
            when(noteRepository.findByPatIdOrderByDateDesc(2L)).thenReturn(List.of(
                    note("recent", 2L, "la plus recente", RECENT),
                    note("ancienne", 2L, "la plus ancienne", OLDER)));

            // Act
            List<NoteDTO> result = noteService.getNotesByPatId(2L);

            // Assert
            assertThat(result)
                    .extracting(NoteDTO::getId)
                    .containsExactly("recent", "ancienne");
        }

        @Test
        @DisplayName("Should query the repository with the requested patient id")
        void shouldQueryRepositoryWithGivenPatId() {
            // Arrange
            when(noteRepository.findByPatIdOrderByDateDesc(4L)).thenReturn(List.of());

            // Act
            noteService.getNotesByPatId(4L);

            // Assert
            verify(noteRepository).findByPatIdOrderByDateDesc(4L);
        }
    }

    @Nested
    @DisplayName("addNote")
    class AddNote {

        @Test
        @DisplayName("Should stamp a server-side date and ignore any client-supplied id/date")
        void shouldStampDateAndIgnoreClientValues() {
            // Arrange
            NoteDTO input = NoteDTO.builder()
                    .id("should-be-ignored").patId(3L).note("fumeur")
                    .date(Instant.parse("1999-01-01T00:00:00Z")).build();
            // Renvoie une NOUVELLE instance (comme Mongo) : ne pas muter l'objet capture,
            // sinon l'ArgumentCaptor verrait l'id post-save au lieu de celui pose par le service.
            when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> {
                Note toSave = invocation.getArgument(0);
                return note("generated-id", toSave.getPatId(), toSave.getNote(), toSave.getDate());
            });

            // Act
            noteService.addNote(input);

            // Assert
            ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
            verify(noteRepository).save(captor.capture());
            Note persisted = captor.getValue();
            assertThat(persisted.getId())
                    .as("L'id est laisse a Mongo, jamais impose par le client")
                    .isNull();
            assertThat(persisted.getDate())
                    .as("La date est posee par le serveur, celle du client est ignoree")
                    .isNotNull()
                    .isNotEqualTo(Instant.parse("1999-01-01T00:00:00Z"));
            assertThat(persisted)
                    .extracting(Note::getPatId, Note::getNote)
                    .containsExactly(3L, "fumeur");
        }

        @Test
        @DisplayName("Should return the persisted note with the id generated by Mongo")
        void shouldReturnPersistedNoteWithGeneratedId() {
            // Arrange
            NoteDTO input = NoteDTO.builder().patId(3L).note("nouvelle observation").build();
            when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> {
                Note toSave = invocation.getArgument(0);
                return note("generated-id", toSave.getPatId(), toSave.getNote(), toSave.getDate());
            });

            // Act
            NoteDTO result = noteService.addNote(input);

            // Assert
            assertThat(result)
                    .extracting(NoteDTO::getId, NoteDTO::getPatId, NoteDTO::getNote)
                    .containsExactly("generated-id", 3L, "nouvelle observation");
            assertThat(result.getDate()).isNotNull();
        }
    }
}
