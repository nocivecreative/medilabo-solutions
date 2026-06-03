package com.medilabo.gateway.security;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Utilisateur applicatif stocke dans la base d'auth dediee (medilabo_auth).
 *
 * <p>Entite Spring Data R2DBC (pas JPA) : la gateway est reactive, l'acces a la
 * base doit rester non-bloquant. Volontairement minimaliste (pas de gestion de
 * roles ni d'inscription, cf. besoin client) : un mot de passe BCrypt et un flag.
 */
@Table("app_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {

    @Id
    private Long id;

    private String username;

    /** Hash BCrypt prefixe ({bcrypt}...), jamais le mot de passe en clair. */
    private String password;

    private boolean enabled;
}
