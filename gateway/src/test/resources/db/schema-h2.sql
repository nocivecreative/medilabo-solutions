-- Schema d'auth pour les tests (H2). Equivalent de 01_create_app_user_table.sql.
-- Nom de table quote en minuscules pour matcher le @Table("app_user") du dialecte
-- H2 ; colonnes laissees non quotees (H2 les passe en MAJUSCULES, comme le SQL genere).
CREATE TABLE IF NOT EXISTS "app_user" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);
