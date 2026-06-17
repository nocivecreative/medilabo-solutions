-- 00 — Creation du second schema d'authentification.
--
-- Contexte d'execution : ce script tourne en ROOT, au tout premier demarrage du
-- conteneur MySQL (volume vide), AVANT les scripts 10/11/20/21.
--
-- A ce stade :
--   - `medilabo_patient` existe deja (cree par la variable MYSQL_DATABASE du compose),
--     et l'utilisateur `medilabo` a deja TOUS les droits dessus (grant automatique).
--   - `medilabo_auth` n'existe PAS encore, et l'utilisateur `medilabo` n'a donc AUCUN
--     droit dessus. C'est le piege : MYSQL_DATABASE ne grante que le schema primaire.
--
-- Sans ce script, le 20_auth_table.sql (USE medilabo_auth) echouerait, et la gateway
-- ne pourrait pas lire les utilisateurs.

-- Base d'authentification dediee, distincte de la base patient (aucune table partagee).
-- Cohabite sur la meme instance MySQL (3307) dans un schema separe.
CREATE DATABASE IF NOT EXISTS `medilabo_auth`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- L'utilisateur applicatif (cree par MYSQL_USER) doit pouvoir lire/ecrire ce schema.
GRANT ALL PRIVILEGES ON `medilabo_auth`.* TO 'medilabo'@'%';
FLUSH PRIVILEGES;
