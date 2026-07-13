-- Table d'affectation des enseignants à une (classe, matière) pour une année scolaire.
-- Source de vérité pour "quel prof enseigne quoi à qui".
--
-- module_id is nullable to support "professeur principal" / homeroom teacher of a class
-- without a specific subject attached.
--
-- date_debut/date_fin allow handling mid-year changes (a teacher replacing another at trim 2)
-- without losing the historical assignment.
CREATE TABLE IF NOT EXISTS affectations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_id      UUID      NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
    classe_id       UUID      NOT NULL REFERENCES classes(id)  ON DELETE CASCADE,
    module_id       UUID               REFERENCES modules(id)  ON DELETE SET NULL,
    annee_scolaire  VARCHAR(20) NOT NULL,
    date_debut      DATE,
    date_fin        DATE,
    notes           TEXT,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN     NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_affectations_teacher ON affectations(teacher_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_affectations_classe  ON affectations(classe_id)  WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_affectations_module  ON affectations(module_id)  WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_affectations_annee   ON affectations(annee_scolaire);

-- Empêche le doublon exact (teacher, classe, module, année) sur les lignes vivantes.
-- Un même prof peut avoir plusieurs lignes pour la même classe avec des module_id différents.
CREATE UNIQUE INDEX IF NOT EXISTS uq_affectations_unique
    ON affectations(teacher_id, classe_id, COALESCE(module_id, '00000000-0000-0000-0000-000000000000'::uuid), annee_scolaire)
    WHERE deleted = FALSE;
