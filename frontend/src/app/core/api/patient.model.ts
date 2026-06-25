export type Genre = 'M' | 'F';

/** Fiche patient telle qu'exposée par patient-service (Sprint 1). */
export interface Patient {
  id: number;
  prenom: string;
  nom: string;
  dateNaissance: string; // ISO 'yyyy-MM-dd'
  genre: Genre;
  telephone?: string | null;
  adresse?: string | null;
}

/**
 * Réponse paginée Spring Data (PagedModel) telle que sérialisée par le backend :
 * un tableau `content` + un bloc `page` de métadonnées. Le `totalElements` alimente
 * le mat-paginator (longueur totale, indépendante de la page courante).
 */
export interface PagedModel<T> {
  content: T[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
}
