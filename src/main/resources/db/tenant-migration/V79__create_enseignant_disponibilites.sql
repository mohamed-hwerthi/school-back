-- V79: store per-teacher availability and preferences across the weekly grid.
-- Used by the timetable solver as a hard constraint (INDISPONIBLE) and
-- soft constraints (PREFERE / EVITER).

CREATE TABLE IF NOT EXISTS enseignant_disponibilites (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    enseignant_id   UUID       NOT NULL REFERENCES teachers(id)  ON DELETE CASCADE,
    jour_semaine    INTEGER      NOT NULL CHECK (jour_semaine BETWEEN 1 AND 6),
    creneau_id      UUID       NOT NULL REFERENCES creneaux(id)  ON DELETE CASCADE,
    type            VARCHAR(20)  NOT NULL CHECK (type IN ('INDISPONIBLE', 'PREFERE', 'EVITER')),
    motif           VARCHAR(200),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN      NOT NULL DEFAULT false,
    deleted_at      TIMESTAMP,
    CONSTRAINT enseignant_disponibilites_unique UNIQUE (enseignant_id, jour_semaine, creneau_id)
);

CREATE INDEX IF NOT EXISTS idx_dispo_enseignant ON enseignant_disponibilites(enseignant_id);
CREATE INDEX IF NOT EXISTS idx_dispo_creneau    ON enseignant_disponibilites(creneau_id);
