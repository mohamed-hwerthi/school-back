-- ============================================================
-- V86: Une matière (module) appartient obligatoirement à un domaine.
--      Rend modules.domaine_id NOT NULL après avoir rattaché les
--      matières orphelines.
-- ============================================================

-- (Les id de "domaines" sont des UUID générés par défaut — aucune séquence
--  SERIAL à resynchroniser, contrairement à l'ancien schéma BIGSERIAL.)

-- 1. Créer un domaine "Domaine général" pour les niveaux qui possèdent
--    des matières sans domaine et aucun domaine actif réutilisable.
INSERT INTO domaines (name, ordre, niveau_id)
SELECT 'Domaine général', 1, m.niveau_id
FROM modules m
WHERE m.domaine_id IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM domaines d
      WHERE d.niveau_id = m.niveau_id AND d.deleted = false
  )
GROUP BY m.niveau_id;

-- 2. Rattacher chaque matière orpheline au premier domaine actif de son niveau.
UPDATE modules m
SET domaine_id = (
    SELECT d.id FROM domaines d
    WHERE d.niveau_id = m.niveau_id AND d.deleted = false
    ORDER BY d.ordre, d.id
    LIMIT 1
)
WHERE m.domaine_id IS NULL;

-- 3. La colonne devient obligatoire.
ALTER TABLE modules ALTER COLUMN domaine_id SET NOT NULL;

-- 4. ON DELETE SET NULL est incompatible avec NOT NULL : passer en CASCADE
--    (supprimer un domaine supprime ses matières).
ALTER TABLE modules DROP CONSTRAINT IF EXISTS modules_domaine_id_fkey;
ALTER TABLE modules
    ADD CONSTRAINT modules_domaine_id_fkey
    FOREIGN KEY (domaine_id) REFERENCES domaines(id) ON DELETE CASCADE;
