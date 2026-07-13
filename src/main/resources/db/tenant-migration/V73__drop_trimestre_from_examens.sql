-- Le trimestre ne s'applique pas à l'examen lui-même : une note (student_id,
-- examen_id, trimestre) porte déjà cette dimension. On retire donc la colonne
-- pour garder un modèle cohérent.

-- 1. Vider les examens (cascade sur notes via FK)
DELETE FROM examens;

-- 2. Drop colonne + index
DROP INDEX IF EXISTS idx_examens_trimestre;
ALTER TABLE examens DROP COLUMN IF EXISTS trimestre;

-- 3. Re-seed propre : 2 examens par (classe × module du même niveau)
INSERT INTO examens (
    name, name_prive,
    coeff_etatique, coeff_prive,
    ordre_etatique, ordre_prive,
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
    c.id,
    (SELECT id FROM teachers WHERE specialization = m.name ORDER BY id LIMIT 1),
    m.id,
    true,
    true
FROM classes c
JOIN modules m ON m.niveau_id = c.niveau_id
CROSS JOIN (VALUES (1), (2)) AS ex(kind);
