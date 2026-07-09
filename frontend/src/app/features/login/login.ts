import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

import { AuthService } from '../../core/auth/auth.service';
import { PatientService } from '../../core/api/patient.service';

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './login.html',
  styleUrl: './login.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly patientService = inject(PatientService);
  private readonly router = inject(Router);

  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);

    const { username, password } = this.form.getRawValue();
    // Stocke les credentials : l'authInterceptor les ajoutera à la requête de vérification.
    this.auth.login(username, password);

    // Validation immédiate via un appel authentifié léger contre la gateway.
    // 200 -> identifiants OK ; 401 -> on efface les credentials et on signale l'erreur.
    this.patientService.getPatients(0, 1).subscribe({
      next: () => this.router.navigate(['/patients']),
      error: () => {
        this.auth.logout();
        this.error.set('Identifiants invalides.');
        this.submitting.set(false);
      },
    });
  }
}
