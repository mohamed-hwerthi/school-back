CREATE TABLE IF NOT EXISTS rapports (
    id              UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    titre           VARCHAR(255)    NOT NULL,
    type            VARCHAR(100)    NOT NULL,
    periode         VARCHAR(100),
    date_generation TIMESTAMP       NOT NULL DEFAULT NOW(),
    statut          VARCHAR(30)     NOT NULL DEFAULT 'Brouillon',
    auteur          VARCHAR(255)    NOT NULL,
    destinataire    VARCHAR(255),
    description     TEXT,
    fichier         VARCHAR(500),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);
