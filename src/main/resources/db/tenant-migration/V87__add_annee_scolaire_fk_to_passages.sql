-- V87: link passages to annees_scolaires via a real foreign key (ANN-003).
-- The legacy annee_scolaire VARCHAR is kept as a denormalised label for
-- backward compatibility; annee_scolaire_id is the referential source of truth.

ALTER TABLE passages ADD COLUMN IF NOT EXISTS annee_scolaire_id UUID;

-- Backfill from the matching academic-year label.
UPDATE passages p
SET annee_scolaire_id = a.id
FROM annees_scolaires a
WHERE a.label = p.annee_scolaire
  AND p.annee_scolaire_id IS NULL;

ALTER TABLE passages
    ADD CONSTRAINT fk_passages_annee_scolaire
    FOREIGN KEY (annee_scolaire_id) REFERENCES annees_scolaires(id);

CREATE INDEX IF NOT EXISTS idx_passages_annee_scolaire_id
    ON passages(annee_scolaire_id);
