import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Note } from './note.model';

@Injectable({ providedIn: 'root' })
export class NoteService {
  private readonly http = inject(HttpClient);

  // Préfixe /api : strippé par le proxy de dev (et nginx en prod) avant la gateway.
  // L'Authorization Basic est ajouté par l'authInterceptor.
  private readonly baseUrl = '/api/notes';

  /** Historique d'un patient, déjà trié du plus récent au plus ancien côté backend. */
  getByPatient(patId: number): Observable<Note[]> {
    return this.http.get<Note[]>(`${this.baseUrl}/patient/${patId}`);
  }

  /** Ajoute une note ; l'horodatage est posé côté serveur. */
  add(patId: number, note: string): Observable<Note> {
    return this.http.post<Note>(this.baseUrl, { patId, note });
  }
}
