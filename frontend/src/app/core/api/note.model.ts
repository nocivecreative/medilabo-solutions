/** Note d'observation telle qu'exposée par notes-service (Sprint 2). */
export interface Note {
  id: string;
  patId: number;
  note: string;
  date: string; // ISO 8601 (Instant), posé côté serveur
}
