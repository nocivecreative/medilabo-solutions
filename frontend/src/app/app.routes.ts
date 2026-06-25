import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'patients' },
  {
    // Lazy loading : la vue patients n'est chargée que lorsqu'on la visite (Green Code).
    path: 'patients',
    loadComponent: () =>
      import('./features/patient/patient-list/patient-list').then((m) => m.PatientList),
  },
];
