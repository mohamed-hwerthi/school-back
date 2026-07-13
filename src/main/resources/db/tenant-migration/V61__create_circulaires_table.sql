CREATE TABLE IF NOT EXISTS circulaires (
    id              UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    titre           VARCHAR(255)    NOT NULL,
    type            VARCHAR(50)     NOT NULL CHECK (type IN ('Information', 'Règlement', 'Événement', 'Urgent', 'Pédagogique')),
    contenu         TEXT            NOT NULL,
    date_creation   TIMESTAMP       NOT NULL DEFAULT NOW(),
    date_publication TIMESTAMP,
    statut          VARCHAR(30)     NOT NULL DEFAULT 'Brouillon' CHECK (statut IN ('Brouillon', 'Publiée', 'Archivée')),
    auteur          VARCHAR(255)    NOT NULL,
    cible           VARCHAR(50)     NOT NULL DEFAULT 'Tous' CHECK (cible IN ('Tous', 'Enseignants', 'Parents', 'Élèves', 'Administration')),
    piece_jointe    VARCHAR(500),
    important       BOOLEAN         DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);
