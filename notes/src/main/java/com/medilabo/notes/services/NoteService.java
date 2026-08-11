package com.medilabo.notes.services;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.medilabo.notes.dto.NoteDTO;
import com.medilabo.notes.model.Note;
import com.medilabo.notes.repositories.NoteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoteService implements INoteService {

    private final NoteRepository noteRepository;

    @Override
    public List<NoteDTO> getNotesByPatId(Long patId) {
        return noteRepository.findByPatIdOrderByDateDesc(patId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public NoteDTO addNote(NoteDTO dto) {
        // L'id et la date sont maîtrisés côté serveur : on ignore tout ce que le client aurait transmis.
        Note note = Note.builder()
                .patId(dto.patId())
                .note(dto.note())
                .date(Instant.now())
                .build();
        return toDTO(noteRepository.save(note));
    }

    private NoteDTO toDTO(Note n) {
        return new NoteDTO(n.getId(), n.getPatId(), n.getNote(), n.getDate());
    }
}
