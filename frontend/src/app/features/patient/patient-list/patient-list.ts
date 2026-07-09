import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';

import { PatientService } from '../../../core/api/patient.service';
import { Patient } from '../../../core/api/patient.model';

@Component({
  selector: 'app-patient-list',
  imports: [MatTableModule, MatPaginatorModule, MatProgressBarModule, MatButtonModule, RouterLink],
  templateUrl: './patient-list.html',
  styleUrl: './patient-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatientList {
  private readonly patientService = inject(PatientService);

  // État de la vue (signals).
  protected readonly patients = signal<Patient[]>([]);
  protected readonly total = signal(0);
  protected readonly loading = signal(false);
  protected readonly pageSize = signal(10);

  protected readonly displayedColumns = ['nom', 'prenom', 'dateNaissance', 'genre', 'telephone'];

  constructor() {
    this.load(0, this.pageSize());
  }

  /** Réagit à un changement de page/taille du mat-paginator. */
  protected onPage(event: PageEvent): void {
    this.pageSize.set(event.pageSize);
    this.load(event.pageIndex, event.pageSize);
  }

  private load(page: number, size: number): void {
    this.loading.set(true);
    this.patientService.getPatients(page, size).subscribe({
      next: (res) => {
        this.patients.set(res.content);
        this.total.set(res.page.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
