import { Routes } from '@angular/router';

import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'patients' },
  {
    path: 'login',
    loadComponent: () => import('./features/login/login').then((m) => m.Login),
  },
  {
    // Lazy loading : chaque vue n'est chargée que lorsqu'on la visite (Green Code).
    path: 'patients',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/patient/patient-list/patient-list').then((m) => m.PatientList),
  },
  {
    // 'new' AVANT ':id' : sinon 'new' serait capturé comme un id.
    path: 'patients/new',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/patient/patient-form/patient-form').then((m) => m.PatientForm),
  },
  {
    path: 'patients/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/patient/patient-form/patient-form').then((m) => m.PatientForm),
  },
];
