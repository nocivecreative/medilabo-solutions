package com.medilabo.notes.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Note d'observation du praticien, stockée telle quelle (texte libre) dans MongoDB.
 * Le rattachement au patient se fait par {@code patId} (l'identifiant du patient côté
 * patient-service, base relationnelle) : aucune donnée démographique n'est dupliquée ici.
 */
@Document(collection = "notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Note {

    @Id
    private String id;

    /** Identifiant du patient (clé de rattachement vers patient-service). Indexé : filtre le plus fréquent. */
    @Indexed
    private Long patId;

    /** Texte libre de l'observation. */
    private String note;

    /** Horodatage de création, posé côté serveur — sert à ordonner l'historique. */
    private Instant date;
}
