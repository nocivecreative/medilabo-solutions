USE `medilabo_auth`;

-- Stockage minimaliste des identifiants (pas de roles : besoin client = auth seule).
-- Les utilisateurs sont inseres en base (cf. 02_insert_app_user.sql), seule source de verite.
CREATE TABLE IF NOT EXISTS `app_user` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `username` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
    `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
    `enabled` boolean NOT NULL DEFAULT TRUE,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_app_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
