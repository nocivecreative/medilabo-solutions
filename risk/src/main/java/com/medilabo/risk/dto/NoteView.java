package com.medilabo.risk.dto;

/**
 * Vue partielle d'une note telle que consommée depuis notes-service :
 * seul le texte libre importe pour la recherche des déclencheurs.
 *
 * @param note texte de l'observation ; peut être {@code null} si le service amont
 *             en renvoie une sans contenu, cas écarté avant l'analyse
 */
public record NoteView(String note) {
}
