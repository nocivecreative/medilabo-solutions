USE `medilabo_patient`;

-- Les 4 cas de test fournis par le client (PDF Sprint 1 - donnees patient).
INSERT INTO `patient` (`id`, `prenom`, `nom`, `date_naissance`, `genre`, `telephone`, `adresse`) VALUES
(1, 'Test', 'TestNone', '1966-12-03', 'F', '100-222-3333', '1 Brookside St'),
(2, 'Test', 'TestBorderline', '1945-06-24', 'M', '200-333-4444', '2 High St'),
(3, 'Test', 'TestInDanger', '2004-06-18', 'M', '300-444-5555', '3 Club Road'),
(4, 'Test', 'TestEarlyOnset', '2002-06-28', 'F', '400-555-6666', '4 Valley Dr');
