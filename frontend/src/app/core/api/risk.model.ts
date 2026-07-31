/**
 * Niveau de risque de diabète tel qu'exposé par risk-service (Sprint 3).
 * Miroir de l'enum Java `RiskLevel` : Jackson sérialise les constantes en chaînes.
 */
export type RiskLevel = 'NONE' | 'BORDERLINE' | 'IN_DANGER' | 'EARLY_ONSET';

/** Rapport renvoyé par GET /risk/patient/{patId}. */
export interface RiskReport {
  patId: number;
  riskLevel: RiskLevel;
  /** Âge en années révolues, calculé côté serveur (source de vérité unique). */
  age: number;
  triggerCount: number;
  /** Déclencheurs distincts trouvés dans les notes : justifie le niveau affiché. */
  triggersFound: string[];
}
