import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { AuthService } from './auth.service';

/**
 * Ajoute l'en-tête `Authorization: Basic <credentials>` à chaque requête sortante
 * quand une session est active (cf. ADR-07). La gateway valide ce header contre la
 * base medilabo_auth ; sans lui, elle répond 401.
 *
 * Interceptor fonctionnel (HttpInterceptorFn) : enregistré via
 * provideHttpClient(withInterceptors([authInterceptor])) dans app.config.ts.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const credentials = inject(AuthService).credentials();

  if (credentials !== null) {
    req = req.clone({
      setHeaders: { Authorization: `Basic ${credentials}` },
    });
  }

  return next(req);
};
