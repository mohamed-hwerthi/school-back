-- Niveaux (levels) and classes (sections) tables

CREATE TABLE IF NOT EXISTS niveaux (
    id          UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100)    NOT NULL UNIQUE,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS classes (
    id          UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    niveau_id   UUID          NOT NULL REFERENCES niveaux(id) ON DELETE CASCADE,
    letter      VARCHAR(5)      NOT NULL,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    UNIQUE (niveau_id, letter)
);
