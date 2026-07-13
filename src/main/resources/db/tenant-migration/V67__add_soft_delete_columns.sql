-- ============================================================
-- V67: Add soft-delete columns (deleted, deleted_at) to all
--      tenant-schema tables.
-- ============================================================

-- Core: students, teachers, courses, enrollments
ALTER TABLE students              ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE students              ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE teachers              ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE teachers              ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE courses               ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE courses               ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE enrollments           ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE enrollments           ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Structure: niveaux, classes, modules, domaines
ALTER TABLE niveaux               ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE niveaux               ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE classes               ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE classes               ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE modules               ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE modules               ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE domaines              ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE domaines              ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE sous_domaines         ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE sous_domaines         ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Évaluations: examens, notes, barèmes, compétences, bulletins
ALTER TABLE examens               ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE examens               ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE notes                 ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE notes                 ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE baremes               ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE baremes               ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE competences           ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE competences           ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE evaluations_competences ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE evaluations_competences ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE bulletin_templates    ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE bulletin_templates    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE observations_trimestre ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE observations_trimestre ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE recommandations       ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE recommandations       ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Finance: paiements, dépenses, caisse, remises, relances, bourses, budgets
ALTER TABLE paiements             ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE paiements             ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE depenses              ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE depenses              ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE categories_depense    ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE categories_depense    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE types_frais           ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE types_frais           ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE factures              ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE factures              ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE echeanciers           ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE echeanciers           ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE echeances             ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE echeances             ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE facture_lignes        ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE facture_lignes        ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE budgets               ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE budgets               ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE bourses               ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE bourses               ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE caisses               ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE caisses               ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE mouvements_caisse     ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE mouvements_caisse     ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE remises               ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE remises               ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE penalites             ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE penalites             ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE relances              ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE relances              ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE audit_financier       ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE audit_financier       ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Absences & discipline
ALTER TABLE absences              ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE absences              ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE justificatifs         ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE justificatifs         ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE incidents             ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE incidents             ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE incident_eleves       ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE incident_eleves       ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE sanctions             ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE sanctions             ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Devoirs & ressources
ALTER TABLE devoirs               ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE devoirs               ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE soumissions           ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE soumissions           ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE ressources_pedagogiques ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE ressources_pedagogiques ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Inscriptions
ALTER TABLE inscriptions          ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE inscriptions          ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE liste_attente         ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE liste_attente         ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Communication
ALTER TABLE circulaires           ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE circulaires           ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE rapports              ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE rapports              ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE meetings              ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE meetings              ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE messages              ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE messages              ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE message_recipients    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE notifications         ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE notifications         ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE annonces              ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE annonces              ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Examens en ligne
ALTER TABLE quiz                  ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE quiz                  ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE questions             ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE questions             ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE tentatives            ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE tentatives            ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE reponses_eleve        ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE reponses_eleve        ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE choix_reponse         ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE choix_reponse         ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Bibliothèque
ALTER TABLE livres                ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE livres                ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE emprunts              ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE emprunts              ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Transport
ALTER TABLE circuits              ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE circuits              ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE vehicules             ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE vehicules             ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE affectations_transport ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE affectations_transport ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE arrets                ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE arrets                ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Cantine
ALTER TABLE menus                 ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE menus                 ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE abonnements_cantine   ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE abonnements_cantine   ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE pointages_repas       ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE pointages_repas       ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- RH
ALTER TABLE contrats_enseignant   ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE contrats_enseignant   ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE conges                ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE conges                ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE formations            ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE formations            ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE formation_participants ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE formation_participants ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE fiches_paie           ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE fiches_paie           ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE pointage_personnel    ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE pointage_personnel    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE teacher_evaluations   ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE teacher_evaluations   ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Emploi du temps & salles
ALTER TABLE emploi_du_temps       ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE emploi_du_temps       ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE creneaux              ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE creneaux              ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE remplacements         ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE remplacements         ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE classrooms            ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE classrooms            ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Documents & passages
ALTER TABLE documents_generes     ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE documents_generes     ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE student_documents     ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE student_documents     ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE passages              ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE passages              ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Année scolaire
ALTER TABLE annees_scolaires      ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE annees_scolaires      ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE trimestres            ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE trimestres            ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE jours_feries          ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE jours_feries          ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE vacances              ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE vacances              ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Analytics & config
ALTER TABLE kpi_config            ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE kpi_config            ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Parent
ALTER TABLE parent_students       ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE parent_students       ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Vitrine
ALTER TABLE vitrine_config        ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE vitrine_config        ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE vitrine_pages         ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE vitrine_pages         ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE vitrine_sections      ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE vitrine_sections      ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE vitrine_gallery       ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE vitrine_gallery       ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE vitrine_announcements ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE vitrine_announcements ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE vitrine_contacts      ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE vitrine_contacts      ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE vitrine_page_views    ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE vitrine_page_views    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- ============================================================
-- INDEX: speed up queries that filter on deleted = false
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_students_deleted      ON students (deleted);
CREATE INDEX IF NOT EXISTS idx_teachers_deleted      ON teachers (deleted);
CREATE INDEX IF NOT EXISTS idx_paiements_deleted     ON paiements (deleted);
CREATE INDEX IF NOT EXISTS idx_depenses_deleted      ON depenses (deleted);
CREATE INDEX IF NOT EXISTS idx_factures_deleted      ON factures (deleted);
CREATE INDEX IF NOT EXISTS idx_absences_deleted      ON absences (deleted);
CREATE INDEX IF NOT EXISTS idx_notes_deleted         ON notes (deleted);
CREATE INDEX IF NOT EXISTS idx_examens_deleted       ON examens (deleted);
CREATE INDEX IF NOT EXISTS idx_inscriptions_deleted  ON inscriptions (deleted);
CREATE INDEX IF NOT EXISTS idx_devoirs_deleted       ON devoirs (deleted);
