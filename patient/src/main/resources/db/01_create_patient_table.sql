CREATE TABLE `patient` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `prenom` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
    `nom` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
    `date_naissance` date NOT NULL,
    `genre` ENUM('M', 'F') NOT NULL,
    `telephone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `adresse` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
