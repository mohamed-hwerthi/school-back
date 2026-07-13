-- Expand teachers table with all fields required by the admin frontend.

ALTER TABLE teachers ADD COLUMN IF NOT EXISTS sexe            VARCHAR(1);
ALTER TABLE teachers ADD COLUMN IF NOT EXISTS telephone       VARCHAR(50);
ALTER TABLE teachers ADD COLUMN IF NOT EXISTS date_naissance  DATE;
ALTER TABLE teachers ADD COLUMN IF NOT EXISTS date_embauche   DATE DEFAULT CURRENT_DATE;
ALTER TABLE teachers ADD COLUMN IF NOT EXISTS statut          VARCHAR(20) NOT NULL DEFAULT 'Actif';

-- email was required + unique; frontend treats it as optional
ALTER TABLE teachers ALTER COLUMN email DROP NOT NULL;
ALTER TABLE teachers DROP CONSTRAINT IF EXISTS teachers_email_key;
DROP INDEX IF EXISTS teachers_email_key;
