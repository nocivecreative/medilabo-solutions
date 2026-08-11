package com.medilabo.notes.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Payload d'API d'une note d'observation, découplé du document MongoDB.
 *
 * <p>Record plutôt que classe : un DTO n'est qu'un porteur de valeurs immuable.
 * Le compilateur génère accesseurs, {@code equals}, {@code hashCode} et
 * {@code toString} ; les contraintes de validation portent sur les composants.
 *
 * @param id    identifiant Mongo — renvoyé en réponse, ignoré en création
 * @param patId identifiant du patient rattaché (obligatoire)
 * @param note  texte libre de l'observation (obligatoire)
 * @param date  horodatage posé côté serveur ; jamais accepté depuis le client
 */
public record NoteDTO(

        String id,

        @NotNull(message = "L'identifiant du patient doit être fourni")
        Long patId,

        @NotBlank(message = "La note ne doit pas être vide")
        String note,

        Instant date) {
}
