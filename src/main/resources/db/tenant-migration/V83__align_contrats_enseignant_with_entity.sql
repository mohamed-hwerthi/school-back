-- Bring contrats_enseignant in sync with ContratEnseignant entity:
-- rename salaire_base -> salaire, add statut + observations columns.

ALTER TABLE contrats_enseignant
    RENAME COLUMN salaire_base TO salaire;

UPDATE contrats_enseignant SET salaire = 0 WHERE salaire IS NULL;

ALTER TABLE contrats_enseignant
    ALTER COLUMN salaire SET NOT NULL;

ALTER TABLE contrats_enseignant
    ADD COLUMN IF NOT EXISTS statut VARCHAR(20) NOT NULL DEFAULT 'ACTIF';

ALTER TABLE contrats_enseignant
    ADD COLUMN IF NOT EXISTS observations TEXT;
