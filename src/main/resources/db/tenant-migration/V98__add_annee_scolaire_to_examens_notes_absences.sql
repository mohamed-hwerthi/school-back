-- Add annee_scolaire column to examens, notes, and absences tables
-- This enables filtering by school year across all modules.

ALTER TABLE examens ADD COLUMN IF NOT EXISTS annee_scolaire VARCHAR(20);
ALTER TABLE notes   ADD COLUMN IF NOT EXISTS annee_scolaire VARCHAR(20);
ALTER TABLE absences ADD COLUMN IF NOT EXISTS annee_scolaire VARCHAR(20);

-- Backfill existing rows with the active school year label (or a sensible default)
UPDATE examens  SET annee_scolaire = COALESCE((SELECT label FROM annees_scolaires WHERE active = true LIMIT 1), '2025-2026') WHERE annee_scolaire IS NULL;
UPDATE notes    SET annee_scolaire = COALESCE((SELECT label FROM annees_scolaires WHERE active = true LIMIT 1), '2025-2026') WHERE annee_scolaire IS NULL;
UPDATE absences SET annee_scolaire = COALESCE((SELECT label FROM annees_scolaires WHERE active = true LIMIT 1), '2025-2026') WHERE annee_scolaire IS NULL;

-- Make the column NOT NULL after backfill
ALTER TABLE examens  ALTER COLUMN annee_scolaire SET NOT NULL;
ALTER TABLE notes    ALTER COLUMN annee_scolaire SET NOT NULL;
ALTER TABLE absences ALTER COLUMN annee_scolaire SET NOT NULL;

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_examens_annee_scolaire  ON examens(annee_scolaire);
CREATE INDEX IF NOT EXISTS idx_notes_annee_scolaire    ON notes(annee_scolaire);
CREATE INDEX IF NOT EXISTS idx_absences_annee_scolaire ON absences(annee_scolaire);
