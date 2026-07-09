package com.medilabo.notes.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteDTO {

    /** Identifiant Mongo — renvoyé en réponse, ignoré en création. */
    private String id;

    @NotNull(message = "L'identifiant du patient doit être fourni")
    private Long patId;

    @NotBlank(message = "La note ne doit pas être vide")
    private String note;

    /** Posé côté serveur à la création ; jamais accepté depuis le client. */
    private Instant date;
}
