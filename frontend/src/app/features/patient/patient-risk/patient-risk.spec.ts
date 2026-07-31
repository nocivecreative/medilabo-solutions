import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Observable, of, throwError } from 'rxjs';

import { PatientRisk } from './patient-risk';
import { RiskService } from '../../../core/api/risk.service';
import { RiskLevel, RiskReport } from '../../../core/api/risk.model';

/** Rapport minimal : seul le niveau varie d'un cas de test à l'autre. */
function reportWith(riskLevel: RiskLevel): RiskReport {
  return { patId: 1, riskLevel, age: 40, triggerCount: 2, triggersFound: [] };
}

/**
 * Monte le composant avec un RiskService bouchonné. Le mapping niveau -> libellé
 * est vérifié via le DOM plutôt qu'en exportant la constante : on teste ce que
 * voit le praticien, pas un détail d'implémentation.
 */
async function setup(getByPatient: () => Observable<RiskReport>): Promise<ComponentFixture<PatientRisk>> {
  TestBed.configureTestingModule({
    imports: [PatientRisk],
    providers: [{ provide: RiskService, useValue: { getByPatient } }],
  });
  const fixture = TestBed.createComponent(PatientRisk);
  fixture.componentRef.setInput('patId', 1);
  await fixture.whenStable();
  return fixture;
}

describe('PatientRisk', () => {
  describe('rendu du niveau de risque', () => {
    it.each([
      ['NONE', 'Aucun risque', 'ok'],
      ['BORDERLINE', 'Limite', 'warn'],
      ['IN_DANGER', 'En danger', 'alert'],
      ['EARLY_ONSET', 'Signes précoces', 'critical'],
    ] as const)('affiche %s comme « %s » avec le ton %s', async (level, label, tone) => {
      // Arrange + Act
      const fixture = await setup(() => of(reportWith(level)));

      // Assert
      const element: HTMLElement = fixture.nativeElement.querySelector('.level');
      expect(element.textContent?.trim()).toBe(label);
      expect(element.getAttribute('data-tone')).toBe(tone);
    });
  });

  it('liste les déclencheurs qui justifient le niveau', async () => {
    // Arrange
    const report: RiskReport = {
      patId: 1,
      riskLevel: 'IN_DANGER',
      age: 22,
      triggerCount: 3,
      triggersFound: ['Fumeur', 'Anormal', 'Vertiges'],
    };

    // Act
    const fixture = await setup(() => of(report));

    // Assert
    const triggers = Array.from(
      fixture.nativeElement.querySelectorAll('.triggers li') as NodeListOf<HTMLElement>,
    ).map((li) => li.textContent?.trim());
    expect(triggers).toEqual(['Fumeur', 'Anormal', 'Vertiges']);
  });

  it('signale un échec du calcul sans casser la vue', async () => {
    // Arrange
    const fixture = await setup(() => throwError(() => new Error('risk-service indisponible')));

    // Act + Assert
    // TODO(human)
  });
});
