-- Suppression de tous les examens existants (et notes via ON DELETE CASCADE)
DELETE FROM examens;

-- Ajout de la colonne trimestre (1, 2 ou 3)
ALTER TABLE examens
    ADD COLUMN trimestre INTEGER NOT NULL DEFAULT 1
        CHECK (trimestre BETWEEN 1 AND 3);

CREATE INDEX IF NOT EXISTS idx_examens_trimestre ON examens(trimestre);
