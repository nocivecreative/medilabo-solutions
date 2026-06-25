import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from './auth.service';

/**
 * Protège les routes nécessitant une session : redirige vers /login si aucun
 * credential n'est présent. La validation réelle des identifiants reste faite
 * côté gateway (le guard ne fait que vérifier qu'on a *tenté* de se connecter).
 */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.isAuthenticated() ? true : router.createUrlTree(['/login']);
};
