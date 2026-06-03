-- Base d'authentification dediee, distincte de la base patient (aucune table partagee).
-- Cohabite sur la meme instance MySQL (3307) dans un schema separe.
CREATE DATABASE IF NOT EXISTS `medilabo_auth`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- L'utilisateur applicatif (cree par MYSQL_USER) doit pouvoir lire/ecrire ce schema.
GRANT ALL PRIVILEGES ON `medilabo_auth`.* TO 'medilabo'@'%';
FLUSH PRIVILEGES;
