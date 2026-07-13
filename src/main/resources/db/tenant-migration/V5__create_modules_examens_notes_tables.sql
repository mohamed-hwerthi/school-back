-- Modules (matières pédagogiques) par niveau
CREATE TABLE IF NOT EXISTS modules (
    id                  UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255)    NOT NULL,
    name_vp             VARCHAR(255),
    coeff_etatique      DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    coeff_prive         DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    ordre_etatique      INTEGER         NOT NULL DEFAULT 1,
    ordre_prive         INTEGER         NOT NULL DEFAULT 1,
    niveau_id           UUID          NOT NULL REFERENCES niveaux(id) ON DELETE CASCADE,
    version_etatique    BOOLEAN         NOT NULL DEFAULT true,
    version_privee      BOOLEAN         NOT NULL DEFAULT true,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Examens (épreuves) liés à un module, une classe et un enseignant
CREATE TABLE IF NOT EXISTS examens (
    id                  UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255)    NOT NULL,
    name_prive          VARCHAR(255),
    coeff_etatique      DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    coeff_prive         DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    ordre_etatique      INTEGER         NOT NULL DEFAULT 1,
    ordre_prive         INTEGER         NOT NULL DEFAULT 1,
    classe_id           UUID          NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    teacher_id          UUID          REFERENCES teachers(id) ON DELETE SET NULL,
    module_id           UUID          NOT NULL REFERENCES modules(id) ON DELETE CASCADE,
    version_etatique    BOOLEAN         NOT NULL DEFAULT true,
    version_privee      BOOLEAN         NOT NULL DEFAULT true,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Notes (une note par élève par examen par trimestre)
CREATE TABLE IF NOT EXISTS notes (
    id                  UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id          UUID          NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    examen_id           UUID          NOT NULL REFERENCES examens(id) ON DELETE CASCADE,
    trimestre           INTEGER         NOT NULL CHECK (trimestre BETWEEN 1 AND 3),
    valeur              DOUBLE PRECISION,
    observation         TEXT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    UNIQUE (student_id, examen_id, trimestre)
);
