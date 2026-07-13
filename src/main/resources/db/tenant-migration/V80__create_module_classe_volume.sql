-- V80: weekly hour volume per (module x classe x annee_scolaire).
-- The solver reads nb_heures_hebdo to know how many lessons to schedule
-- for a given (classe, module) pair, replacing the legacy per-request input.

CREATE TABLE IF NOT EXISTS module_classe_volume (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    module_id           UUID  NOT NULL REFERENCES modules(id)           ON DELETE CASCADE,
    classe_id           UUID  NOT NULL REFERENCES classes(id)           ON DELETE CASCADE,
    enseignant_id       UUID           REFERENCES teachers(id)          ON DELETE SET NULL,
    annee_scolaire_id   UUID           REFERENCES annees_scolaires(id)  ON DELETE CASCADE,
    nb_heures_hebdo     INTEGER NOT NULL CHECK (nb_heures_hebdo >= 1 AND nb_heures_hebdo <= 20),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN   NOT NULL DEFAULT false,
    deleted_at          TIMESTAMP,
    CONSTRAINT module_classe_volume_unique UNIQUE (module_id, classe_id, annee_scolaire_id)
);

CREATE INDEX IF NOT EXISTS idx_volume_classe          ON module_classe_volume(classe_id);
CREATE INDEX IF NOT EXISTS idx_volume_module          ON module_classe_volume(module_id);
CREATE INDEX IF NOT EXISTS idx_volume_annee_scolaire  ON module_classe_volume(annee_scolaire_id);
