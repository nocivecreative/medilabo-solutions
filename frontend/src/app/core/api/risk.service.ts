import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { RiskReport } from './risk.model';

@Injectable({ providedIn: 'root' })
export class RiskService {
  private readonly http = inject(HttpClient);

  // Préfixe /api : strippé par le proxy de dev (et nginx en prod) avant la gateway.
  // L'Authorization Basic est ajouté par l'authInterceptor.
  private readonly baseUrl = '/api/risk';

  /**
   * Rapport de risque d'un patient. Le calcul est entièrement côté serveur :
   * le front n'embarque aucune règle métier ni liste de déclencheurs.
   */
  getByPatient(patId: number): Observable<RiskReport> {
    return this.http.get<RiskReport>(`${this.baseUrl}/patient/${patId}`);
  }
}
