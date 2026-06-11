USE `medilabo_auth`;

-- Praticien initial. Depuis le retrait de l'amorcage applicatif, cette base est
-- l'unique source des utilisateurs : ce script remplace l'ancien seeding au demarrage.
--
-- Mot de passe 'P9Medilabo2026' encode en BCrypt (force 10, defaut Spring), prefixe
-- '{bcrypt}' pour le DelegatingPasswordEncoder de la gateway. Le hash porte son propre
-- sel : il n'est pas reconstituable a la main et n'expose pas le mot de passe en clair.
--
-- INSERT IGNORE : idempotent grace a la cle unique uk_app_user_username -> rejouer le
-- script ne cree pas de doublon et ne touche pas un mot de passe deja en place.
INSERT IGNORE INTO `app_user` (`username`, `password`, `enabled`) VALUES
('praticien', '{bcrypt}$2a$10$Kb2TQlTxW9O7F2Wa6NRfsO60EwkGFPST3lIzXn2f5tFiRVoJt6chm', TRUE);
