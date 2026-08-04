import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { NoteService } from '../../../core/api/note.service';
import { Note } from '../../../core/api/note.model';

/**
 * Historique des notes d'un patient + ajout d'une observation (Sprint 2).
 * Embarqué dans la page d'édition patient : reçoit le patId en entrée.
 */
@Component({
  selector: 'app-patient-notes',
  imports: [
    DatePipe,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressBarModule,
  ],
  templateUrl: './patient-notes.html',
  styleUrl: './patient-notes.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatientNotes implements OnInit {
  private readonly noteService = inject(NoteService);

  /** Identifiant du patient dont on affiche l'historique (fourni par le parent). */
  readonly patId = input.required<number>();

  /**
   * Émis après l'enregistrement d'une note. La note peut introduire de nouveaux
   * déclencheurs : le parent s'en sert pour redemander le rapport de risque.
   * Ce composant ignore volontairement qui écoute (couplage faible).
   */
  readonly noteAdded = output<void>();

  protected readonly notes = signal<Note[]>([]);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);

  // Champ de saisie en reactive form (comme patient-form) : integration native avec matInput.
  protected readonly noteControl = new FormControl('', { nonNullable: true });

  ngOnInit(): void {
    this.load();
  }

  protected add(): void {
    const text = this.noteControl.value.trim();
    if (!text || this.saving()) {
      return;
    }
    this.saving.set(true);
    this.error.set(null);
    this.noteService.add(this.patId(), text).subscribe({
      next: () => {
        this.noteControl.reset('');
        this.saving.set(false);
        this.load(); // recharge l'historique pour voir la note en tête
        this.noteAdded.emit();
      },
      error: () => {
        this.saving.set(false);
        this.error.set("L'ajout de la note a échoué. Réessayez.");
      },
    });
  }

  private load(): void {
    this.loading.set(true);
    this.noteService.getByPatient(this.patId()).subscribe({
      next: (notes) => {
        this.notes.set(notes);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
