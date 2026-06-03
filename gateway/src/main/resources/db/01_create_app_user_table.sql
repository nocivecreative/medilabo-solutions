USE `medilabo_auth`;

-- Stockage minimaliste des identifiants (pas de roles : besoin client = auth seule).
-- L'utilisateur praticien est amorce au demarrage de la gateway (AuthUserSeeder).
CREATE TABLE IF NOT EXISTS `app_user` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `username` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
    `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
    `enabled` boolean NOT NULL DEFAULT TRUE,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_app_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
