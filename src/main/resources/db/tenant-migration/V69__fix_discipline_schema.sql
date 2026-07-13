-- Align incidents, sanctions, incident_eleves columns with JPA entities.

-- ── incidents ────────────────────────────────────────────────
ALTER TABLE incidents DROP CONSTRAINT IF EXISTS incidents_type_incident_check;
ALTER TABLE incidents DROP CONSTRAINT IF EXISTS incidents_gravite_check;

ALTER TABLE incidents RENAME COLUMN date_incident TO date;
ALTER TABLE incidents RENAME COLUMN type_incident TO type;
ALTER TABLE incidents RENAME COLUMN enseignant_id TO signale_par_id;

ALTER TABLE incidents ADD COLUMN IF NOT EXISTS titre VARCHAR(255);
UPDATE incidents SET titre = COALESCE(type, 'Incident') WHERE titre IS NULL;
ALTER TABLE incidents ALTER COLUMN titre SET NOT NULL;

ALTER TABLE incidents ADD COLUMN IF NOT EXISTS lieu VARCHAR(255);
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT NOW();
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- ── incident_eleves ──────────────────────────────────────────
ALTER TABLE incident_eleves ADD COLUMN IF NOT EXISTS role_eleve VARCHAR(20);
ALTER TABLE incident_eleves ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE incident_eleves ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- ── sanctions ────────────────────────────────────────────────
ALTER TABLE sanctions DROP CONSTRAINT IF EXISTS sanctions_type_sanction_check;

ALTER TABLE sanctions RENAME COLUMN type_sanction TO type;
ALTER TABLE sanctions RENAME COLUMN notifie_parent TO notifie_parents;

ALTER TABLE sanctions ADD COLUMN IF NOT EXISTS decide_par_id UUID;
ALTER TABLE sanctions ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT NOW();
ALTER TABLE sanctions ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE sanctions ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
