CREATE TABLE IF NOT EXISTS evenements_calendrier (
    id              UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    titre           VARCHAR(255)    NOT NULL,
    description     TEXT,
    date_debut      DATE            NOT NULL,
    date_fin        DATE,
    type            VARCHAR(30)     NOT NULL DEFAULT 'GENERAL'
                                    CHECK (type IN ('GENERAL', 'REUNION', 'SORTIE', 'EXAMEN', 'FERIE', 'AUTRE')),
    couleur         VARCHAR(20),
    lieu            VARCHAR(255),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_evenements_calendrier_date_debut
    ON evenements_calendrier (date_debut);
