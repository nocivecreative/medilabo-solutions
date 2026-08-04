import { ChangeDetectionStrategy, Component, inject, signal, viewChild } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';

import { PatientService } from '../../../core/api/patient.service';
import { Patient } from '../../../core/api/patient.model';
import { PatientNotes } from '../patient-notes/patient-notes';
import { PatientRisk } from '../patient-risk/patient-risk';

@Component({
  selector: 'app-patient-form',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    PatientNotes,
    PatientRisk,
  ],
  templateUrl: './patient-form.html',
  styleUrl: './patient-form.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatientForm {
  private readonly fb = inject(FormBuilder);
  private readonly patientService = inject(PatientService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  // null = mode création ; un id = mode édition.
  protected readonly patientId = signal<number | null>(null);
  protected readonly saving = signal(false);

  // Absent en mode création (le panneau n'est rendu qu'avec un id) : d'où le `?.`
  // à l'appel. Recalculer le risque coûte 2 appels HTTP internes côté serveur, on
  // ne le fait donc que sur l'événement qui peut réellement changer le résultat.
  protected readonly riskPanel = viewChild(PatientRisk);

  // Champs obligatoires : prénom, nom, date de naissance, genre (cf. user stories).
  // Téléphone et adresse optionnels.
  protected readonly form = this.fb.nonNullable.group({
    prenom: ['', Validators.required],
    nom: ['', Validators.required],
    dateNaissance: ['', Validators.required],
    genre: ['', Validators.required],
    telephone: [''],
    adresse: [''],
  });

  constructor() {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      this.patientId.set(id);
      this.patientService.getById(id).subscribe((p) => {
        this.form.patchValue({
          prenom: p.prenom,
          nom: p.nom,
          dateNaissance: p.dateNaissance,
          genre: p.genre,
          telephone: p.telephone ?? '',
          adresse: p.adresse ?? '',
        });
      });
    }
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    const payload = this.form.getRawValue() as Omit<Patient, 'id'>;
    const id = this.patientId();

    // L'Observable est sélectionné selon le mode, puis abonné UNE seule fois :
    // un HttpClient Observable est froid -> sans subscribe(), aucune requête ne part.
    const request$ =
      id === null
        ? this.patientService.create(payload)
        : this.patientService.update(id, { id, ...payload });

    request$.subscribe({
      next: () => this.router.navigate(['/patients']),
      error: () => this.saving.set(false),
    });
  }
}
