import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { PagedModel, Patient } from './patient.model';

@Injectable({ providedIn: 'root' })
export class PatientService {
  private readonly http = inject(HttpClient);

  // Préfixe /api : intercepté par le proxy de dev (et nginx en prod), strippé avant
  // d'atteindre la gateway. L'Authorization Basic est ajouté par l'authInterceptor.
  private readonly baseUrl = '/api/patients';

  /** Liste paginée. `page` est 0-based (convention Spring Data / mat-paginator). */
  getPatients(page: number, size: number): Observable<PagedModel<Patient>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PagedModel<Patient>>(this.baseUrl, { params });
  }

  getById(id: number): Observable<Patient> {
    return this.http.get<Patient>(`${this.baseUrl}/${id}`);
  }

  create(patient: Omit<Patient, 'id'>): Observable<Patient> {
    return this.http.post<Patient>(this.baseUrl, patient);
  }

  update(id: number, patient: Patient): Observable<Patient> {
    return this.http.put<Patient>(`${this.baseUrl}/${id}`, patient);
  }
}
