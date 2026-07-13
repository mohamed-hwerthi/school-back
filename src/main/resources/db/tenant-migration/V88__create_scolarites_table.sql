-- V88: historise la scolarité de chaque élève année par année (ANN-004).
-- Une ligne = (élève, année scolaire) avec son niveau/classe et son statut.
-- Sert de base au suivi des réinscriptions (ANN-050/051).

CREATE TABLE IF NOT EXISTS scolarites (
    id              UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id      UUID       NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    annee_scolaire  VARCHAR(20)  NOT NULL,
    niveau          VARCHAR(100),
    classe          VARCHAR(50),
    statut          VARCHAR(20)  NOT NULL DEFAULT 'INSCRIT',
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN      NOT NULL DEFAULT false,
    deleted_at      TIMESTAMP,
    UNIQUE (student_id, annee_scolaire)
);

CREATE INDEX IF NOT EXISTS idx_scolarites_annee   ON scolarites(annee_scolaire);
CREATE INDEX IF NOT EXISTS idx_scolarites_student ON scolarites(student_id);

-- Backfill : reconstitue l'historique depuis les passages déjà enregistrés
-- (niveau/classe de l'élève au début de chaque année traitée).
INSERT INTO scolarites (student_id, annee_scolaire, niveau, classe, statut, created_at, deleted)
SELECT DISTINCT ON (p.student_id, p.annee_scolaire)
       p.student_id, p.annee_scolaire, p.ancien_niveau, p.ancienne_classe, 'INSCRIT', NOW(), false
FROM passages p
WHERE p.deleted = false
ORDER BY p.student_id, p.annee_scolaire, p.id
ON CONFLICT (student_id, annee_scolaire) DO NOTHING;
