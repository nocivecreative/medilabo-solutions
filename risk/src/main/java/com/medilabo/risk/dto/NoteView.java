package com.medilabo.risk.dto;

/**
 * Vue partielle d'une note telle que consommée depuis notes-service :
 * seul le texte libre importe pour la recherche des déclencheurs.
 */
public record NoteView(String note) {
}
