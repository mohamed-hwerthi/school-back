-- Recommandations enseignant par élève par domaine par trimestre
CREATE TABLE IF NOT EXISTS recommandations (
    id              UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id      UUID          NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    domaine_id      UUID          NOT NULL REFERENCES domaines(id) ON DELETE CASCADE,
    trimestre       INTEGER         NOT NULL CHECK (trimestre BETWEEN 1 AND 3),
    texte           TEXT,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    UNIQUE (student_id, domaine_id, trimestre)
);

-- Observations comportement + certificat par élève par trimestre
CREATE TABLE IF NOT EXISTS observations_trimestre (
    id              UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id      UUID          NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    trimestre       INTEGER         NOT NULL CHECK (trimestre BETWEEN 1 AND 3),
    comportement    TEXT,
    certificat_type VARCHAR(100),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    UNIQUE (student_id, trimestre)
);
