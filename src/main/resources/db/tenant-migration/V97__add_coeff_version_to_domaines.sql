-- Un domaine admet un coefficient distinct selon le type d'école (étatique / privé)
-- et peut s'afficher uniquement dans la version étatique, privée, ou les deux.
ALTER TABLE domaines ADD COLUMN IF NOT EXISTS coeff_etatique   DOUBLE PRECISION NOT NULL DEFAULT 1.0;
ALTER TABLE domaines ADD COLUMN IF NOT EXISTS coeff_prive      DOUBLE PRECISION NOT NULL DEFAULT 1.0;
ALTER TABLE domaines ADD COLUMN IF NOT EXISTS version_etatique BOOLEAN          NOT NULL DEFAULT true;
ALTER TABLE domaines ADD COLUMN IF NOT EXISTS version_privee   BOOLEAN          NOT NULL DEFAULT true;
