import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  inject,
  input,
  signal,
} from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { RiskService } from '../../../core/api/risk.service';
import { RiskLevel, RiskReport } from '../../../core/api/risk.model';

/**
 * Rendu d'un niveau de risque : un libellé lisible par le praticien et un « ton »
 * visuel. Le ton est volontairement abstrait (pas une couleur) : le CSS décide
 * du rendu, le composant décide de la gravité.
 */
export interface RiskDisplay {
  label: string;
  tone: 'ok' | 'warn' | 'alert' | 'critical';
}

const RISK_DISPLAY: Record<RiskLevel, RiskDisplay> = {
  NONE: { label: 'Aucun risque', tone: 'ok' },
  BORDERLINE: { label: 'Limite', tone: 'warn' },
  IN_DANGER: { label: 'En danger', tone: 'alert' },
  EARLY_ONSET: { label: 'Signes précoces', tone: 'critical' },
};

/**
 * Rapport de risque de diabète d'un patient (US Sprint 3).
 * Embarqué dans la fiche patient : reçoit le patId en entrée.
 */
@Component({
  selector: 'app-patient-risk',
  imports: [MatCardModule, MatProgressBarModule],
  templateUrl: './patient-risk.html',
  styleUrl: './patient-risk.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatientRisk implements OnInit {
  private readonly riskService = inject(RiskService);

  /** Identifiant du patient évalué (fourni par le parent). */
  readonly patId = input.required<number>();

  protected readonly report = signal<RiskReport | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  /**
   * Dérivé du rapport : recalculé uniquement quand `report` change, jamais à
   * chaque cycle de détection (contrairement à un appel de méthode en template).
   */
  protected readonly display = computed<RiskDisplay | null>(() => {
    const current = this.report();
    return current === null ? null : this.toDisplay(current.riskLevel);
  });

  ngOnInit(): void {
    this.load();
  }

  /**
   * (Re)charge le rapport. Public : le parent l'appelle après l'ajout d'une note,
   * seul événement de la session capable de changer le nombre de déclencheurs.
   */
  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.riskService.getByPatient(this.patId()).subscribe({
      next: (report) => {
        this.report.set(report);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.error.set("Le rapport de risque n'a pas pu être calculé.");
      },
    });
  }

  /** Traduit un niveau technique en information affichable pour le praticien. */
  private toDisplay(level: RiskLevel): RiskDisplay {
    return RISK_DISPLAY[level];
  }
}
