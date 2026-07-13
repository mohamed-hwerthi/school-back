ALTER TABLE devoirs               ADD COLUMN IF NOT EXISTS annee_scolaire VARCHAR(20);
ALTER TABLE soumissions           ADD COLUMN IF NOT EXISTS annee_scolaire VARCHAR(20);
ALTER TABLE ressources_pedagogiques ADD COLUMN IF NOT EXISTS annee_scolaire VARCHAR(20);

UPDATE devoirs                SET annee_scolaire = COALESCE((SELECT label FROM annees_scolaires WHERE active = true LIMIT 1), '2025-2026') WHERE annee_scolaire IS NULL;
UPDATE soumissions            SET annee_scolaire = COALESCE((SELECT label FROM annees_scolaires WHERE active = true LIMIT 1), '2025-2026') WHERE annee_scolaire IS NULL;
UPDATE ressources_pedagogiques SET annee_scolaire = COALESCE((SELECT label FROM annees_scolaires WHERE active = true LIMIT 1), '2025-2026') WHERE annee_scolaire IS NULL;

ALTER TABLE devoirs               ALTER COLUMN annee_scolaire SET NOT NULL;
ALTER TABLE soumissions           ALTER COLUMN annee_scolaire SET NOT NULL;
ALTER TABLE ressources_pedagogiques ALTER COLUMN annee_scolaire SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_devoirs_annee_scolaire               ON devoirs(annee_scolaire);
CREATE INDEX IF NOT EXISTS idx_soumissions_annee_scolaire           ON soumissions(annee_scolaire);
CREATE INDEX IF NOT EXISTS idx_ressources_annee_scolaire            ON ressources_pedagogiques(annee_scolaire);
