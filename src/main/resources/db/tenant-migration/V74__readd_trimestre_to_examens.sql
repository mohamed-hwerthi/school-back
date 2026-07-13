-- Réintroduction de la colonne trimestre sur examens : un examen porte
-- désormais son trimestre (1, 2 ou 3). On vide d'abord et on reseed.

-- 1. Vider les examens (cascade sur notes)
DELETE FROM examens;

-- 2. Ajouter la colonne trimestre
ALTER TABLE examens
    ADD COLUMN trimestre INTEGER NOT NULL DEFAULT 1
        CHECK (trimestre BETWEEN 1 AND 3);

CREATE INDEX IF NOT EXISTS idx_examens_trimestre ON examens(trimestre);

-- 3. Re-seed : pour chaque (classe × module du même niveau × trimestre 1/2/3),
-- 2 examens "Contrôle continu" (coeff 1) et "Examen synthèse" (coeff 2).
INSERT INTO examens (
    name, name_prive,
    coeff_etatique, coeff_prive,
    ordre_etatique, ordre_prive,
    trimestre,
    classe_id, teacher_id, module_id,
    version_etatique, version_privee
)
SELECT
    CASE ex.kind WHEN 1 THEN 'Contrôle continu' ELSE 'Examen synthèse' END,
    NULL,
    CASE ex.kind WHEN 1 THEN 1.0 ELSE 2.0 END,
    CASE ex.kind WHEN 1 THEN 1.0 ELSE 2.0 END,
    ex.kind,
    ex.kind,
    t.trimestre,
    c.id,
    (SELECT id FROM teachers WHERE specialization = m.name ORDER BY id LIMIT 1),
    m.id,
    true,
    true
FROM classes c
JOIN modules m ON m.niveau_id = c.niveau_id
CROSS JOIN (VALUES (1), (2), (3)) AS t(trimestre)
CROSS JOIN (VALUES (1), (2)) AS ex(kind);
