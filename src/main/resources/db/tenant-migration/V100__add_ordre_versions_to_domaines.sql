-- Un domaine admet un ordre d'affichage distinct selon la version
-- (étatique / privé), à l'image des modules et des examens.
ALTER TABLE domaines ADD COLUMN IF NOT EXISTS ordre_etatique INTEGER NOT NULL DEFAULT 1;
ALTER TABLE domaines ADD COLUMN IF NOT EXISTS ordre_prive    INTEGER NOT NULL DEFAULT 1;

-- Backfill : reprendre l'ordre unique existant pour les deux versions
UPDATE domaines SET ordre_etatique = ordre, ordre_prive = ordre;
