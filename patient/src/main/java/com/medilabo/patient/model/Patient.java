package com.medilabo.patient.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Patient tel qu'il est stocké en base relationnelle.
 *
 * <p>Cette entité ne sort jamais de la couche service : les contrôleurs
 * échangent des {@code PatientDTO}. Le découplage évite d'exposer la structure
 * de la table dans le contrat d'API et de sérialiser un objet géré par
 * Hibernate hors de sa transaction.
 *
 * <p>Les contraintes portées par les colonnes ne sont pas décoratives : elles
 * doublent en base les règles de validation du DTO, de sorte qu'une écriture
 * qui contournerait la couche web ne puisse pas produire un patient inexploitable
 * par le risk-service. L'application tournant en {@code ddl-auto=validate},
 * elles doivent rester alignées sur le DDL de {@code db/init} — un écart fait
 * échouer le démarrage.
 *
 * <p>Aucune donnée médicale ici : les observations du praticien vivent dans le
 * notes-service, rattachées par identifiant. Ce service ne détient que le
 * démographique.
 */
@Entity
@Table(name = "patient")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    /** Clé technique générée par la base ({@code AUTO_INCREMENT}), immuable une fois posée. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Prénom du patient. Obligatoire : sert à l'identification à l'écran. */
    @Column(name = "prenom", length = 100, nullable = false)
    private String prenom;

    /** Nom du patient. Obligatoire : sert à l'identification et au tri par défaut de la liste. */
    @Column(name = "nom", length = 100, nullable = false)
    private String nom;

    /**
     * Date de naissance. Obligatoire car le risk-service en dérive l'âge, dont
     * dépend le choix du barème de risque.
     */
    @Column(name = "date_naissance", nullable = false)
    private LocalDate dateNaissance;

    /**
     * Genre administratif. Obligatoire pour la même raison que la date de
     * naissance : il départage les seuils de risque avant 30 ans.
     *
     * <p>Stocké en clair sur un caractère ({@code EnumType.STRING} et non
     * {@code ORDINAL}) : la base reste lisible et réordonner {@link Genre} ne
     * réinterprète pas les lignes existantes.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "genre", length = 1, nullable = false)
    private Genre genre;

    /** Numéro de téléphone. Facultatif : donnée de contact, non exploitée par le calcul de risque. */
    @Column(name = "telephone", length = 20)
    private String telephone;

    /** Adresse postale. Facultative, au même titre que le téléphone. */
    @Column(name = "adresse", length = 255)
    private String adresse;
}
