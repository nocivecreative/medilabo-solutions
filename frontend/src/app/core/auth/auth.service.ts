import { Injectable, computed, signal } from '@angular/core';

/** Clé de stockage des credentials Basic dans le sessionStorage. */
const STORAGE_KEY = 'basic_credentials';

/**
 * Source de vérité de l'authentification côté front (cf. ADR-07).
 *
 * On stocke les credentials Basic encodés (`btoa("user:pass")`), PAS le mot de passe
 * en clair. Persistés en `sessionStorage` : effacés à la fermeture de l'onglet, ce qui
 * borne la durée de vie de la session sans mécanisme serveur (Basic Auth est stateless).
 *
 * ⚠️ Base64 = encodage, pas chiffrement → HTTPS non négociable en prod.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  // Initialisé depuis sessionStorage pour survivre à un rechargement de l'onglet.
  private readonly _credentials = signal<string | null>(sessionStorage.getItem(STORAGE_KEY));

  /** Credentials Basic encodés (ou null si déconnecté). Lus par l'interceptor. */
  readonly credentials = this._credentials.asReadonly();

  /** Vrai dès qu'un couple identifiant/mot de passe a été enregistré. */
  readonly isAuthenticated = computed(() => this._credentials() !== null);

  /** Enregistre la session : encode `user:pass` en Base64 et le persiste. */
  login(username: string, password: string): void {
    const encoded = btoa(`${username}:${password}`);
    sessionStorage.setItem(STORAGE_KEY, encoded);
    this._credentials.set(encoded);
  }

  /** Déconnexion : effacement applicatif des credentials (rien à invalider côté serveur). */
  logout(): void {
    sessionStorage.removeItem(STORAGE_KEY);
    this._credentials.set(null);
  }
}
