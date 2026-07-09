// Seed des notes praticien pour les 4 cas de test fournis par le client (PDF Sprint 2).
// Execute UNE SEULE FOIS par le conteneur mongo au premier demarrage (volume /data/db vide),
// via docker-entrypoint-initdb.d. Le rattachement au patient se fait par patId (id cote
// patient-service) : 1=TestNone, 2=TestBorderline, 3=TestInDanger, 4=TestEarlyOnset.
//
// patId en NumberLong pour coller au type Long de l'entite (Spring Data ecrit un int64).
// _class renseigne pour rester coherent avec le discriminateur ecrit par Spring Data Mongo.
const NOTE_CLASS = "com.medilabo.notes.model.Note";

db = db.getSiblingDB("medilabo_notes");

db.notes.insertMany([
  // --- patId 1 : TestNone (aucun terme declencheur) ---
  {
    patId: NumberLong(1),
    note: "Le patient déclare qu'il 'se sent très bien' Poids égal ou inférieur au poids recommandé",
    date: ISODate("2024-02-10T09:00:00Z"),
    _class: NOTE_CLASS
  },

  // --- patId 2 : TestBorderline ---
  {
    patId: NumberLong(2),
    note: "Le patient déclare qu'il ressent beaucoup de stress au travail Il se plaint également que son audition est anormale dernièrement",
    date: ISODate("2024-01-15T10:30:00Z"),
    _class: NOTE_CLASS
  },
  {
    patId: NumberLong(2),
    note: "Le patient déclare avoir fait une réaction aux médicaments au cours des 3 derniers mois Il remarque également que son audition continue d'être anormale",
    date: ISODate("2024-04-20T11:00:00Z"),
    _class: NOTE_CLASS
  },

  // --- patId 3 : TestInDanger ---
  {
    patId: NumberLong(3),
    note: "Le patient déclare qu'il fume depuis peu",
    date: ISODate("2024-03-05T08:45:00Z"),
    _class: NOTE_CLASS
  },
  {
    patId: NumberLong(3),
    note: "Le patient déclare qu'il est fumeur et qu'il a cessé de fumer l'année dernière Il se plaint également de crises d'apnée respiratoire anormales Tests de laboratoire indiquant un taux de cholestérol LDL élevé",
    date: ISODate("2024-06-12T14:15:00Z"),
    _class: NOTE_CLASS
  },

  // --- patId 4 : TestEarlyOnset ---
  {
    patId: NumberLong(4),
    note: "Le patient déclare qu'il lui est devenu difficile de monter les escaliers Il se plaint également d'être essoufflé Tests de laboratoire indiquant que les anticorps sont élevés Réaction aux médicaments",
    date: ISODate("2024-01-08T09:20:00Z"),
    _class: NOTE_CLASS
  },
  {
    patId: NumberLong(4),
    note: "Le patient déclare qu'il a mal au dos lorsqu'il reste assis pendant longtemps",
    date: ISODate("2024-02-14T10:10:00Z"),
    _class: NOTE_CLASS
  },
  {
    patId: NumberLong(4),
    note: "Le patient déclare avoir commencé à fumer depuis peu Hémoglobine A1C supérieure au niveau recommandé",
    date: ISODate("2024-05-19T15:40:00Z"),
    _class: NOTE_CLASS
  },
  {
    patId: NumberLong(4),
    note: "Taille, Poids, Cholestérol, Vertige et Réaction",
    date: ISODate("2024-07-22T16:00:00Z"),
    _class: NOTE_CLASS
  }
]);

// Pas de createIndex ici : l'index sur patId appartient a l'application (@Indexed +
// auto-index-creation), qui le cree au demarrage sous le nom `patId`. En creer un ici
// le nommerait `patId_1` et provoquerait un IndexOptionsConflict cote app.

print("[seed] medilabo_notes.notes : " + db.notes.countDocuments() + " note(s) inserees.");
