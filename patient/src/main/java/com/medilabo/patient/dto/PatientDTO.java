package com.medilabo.patient.dto;

import java.time.LocalDate;

import com.medilabo.patient.model.Genre;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

/**
 * Payload d'API du patient, découplé de l'entité JPA.
 *
 * <p>Record plutôt que classe : un DTO n'est qu'un porteur de valeurs immuable.
 * Le compilateur génère accesseurs, {@code equals}, {@code hashCode} et
 * {@code toString} ; les contraintes de validation portent directement sur les
 * composants.
 *
 * @param id            généré par la base : ignoré en entrée, renvoyé en sortie
 * @param prenom        prénom du patient (obligatoire)
 * @param nom           nom du patient (obligatoire)
 * @param dateNaissance date de naissance (obligatoire, dans le passé)
 * @param genre         M ou F (obligatoire)
 * @param telephone     numéro de téléphone (facultatif)
 * @param adresse       adresse postale (facultative)
 */
public record PatientDTO(

        Long id,

        @NotBlank(message = "Le prénom doit être fourni")
        @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
        String prenom,

        @NotBlank(message = "Le nom doit être fourni")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String nom,

        @NotNull(message = "La date de naissance doit être fournie")
        @Past(message = "La date de naissance doit être dans le passé")
        LocalDate dateNaissance,

        @NotNull(message = "Le genre doit être fourni")
        Genre genre,

        @Size(max = 20, message = "Le numéro de téléphone ne doit pas dépasser 20 caractères")
        String telephone,

        @Size(max = 255, message = "L'adresse ne doit pas dépasser 255 caractères")
        String adresse) {
}
