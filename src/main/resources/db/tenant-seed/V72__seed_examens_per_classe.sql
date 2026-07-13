-- Seed: pour chaque (classe × module du même niveau × trimestre 1/2/3),
-- on crée 2 examens : "Contrôle continu" (coeff 1) et "Examen synthèse" (coeff 2).
-- L'enseignant est affecté par correspondance specialization == module.name (NULL sinon).
-- Idempotent : ne s'exécute que si la table examens est vide.

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM examens LIMIT 1) THEN
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
    END IF;
END $$;
