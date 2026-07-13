-- Aligne les tables factures / facture_lignes / echeanciers / echeances
-- sur les entites JPA (Facture, FactureLigne, Echeancier, Echeance).
-- V18 avait cree un schema divergent (eleve_id, nb_mensualites, description,
-- montant_remise/net, pdf_url, paiement_id, contraintes CHECK obsoletes).
-- Toutes ces tables etant vides au moment de la migration, les ALTER sont surs.

-- ─── factures ───────────────────────────────────────────────────────
ALTER TABLE factures DROP CONSTRAINT IF EXISTS factures_statut_check;

ALTER TABLE factures RENAME COLUMN eleve_id TO student_id;

ALTER TABLE factures DROP COLUMN IF EXISTS montant_remise;
ALTER TABLE factures DROP COLUMN IF EXISTS montant_net;
ALTER TABLE factures DROP COLUMN IF EXISTS pdf_url;

ALTER TABLE factures ADD COLUMN IF NOT EXISTS montant_paye DECIMAL(10,2) NOT NULL DEFAULT 0;
ALTER TABLE factures ADD COLUMN IF NOT EXISTS notes TEXT;

ALTER TABLE factures ALTER COLUMN numero TYPE VARCHAR(50);
ALTER TABLE factures ALTER COLUMN statut SET DEFAULT 'BROUILLON';

-- ─── facture_lignes ─────────────────────────────────────────────────
ALTER TABLE facture_lignes RENAME COLUMN description TO designation;
ALTER TABLE facture_lignes DROP COLUMN IF EXISTS type_frais_id;
ALTER TABLE facture_lignes ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT NOW();

-- ─── echeanciers ────────────────────────────────────────────────────
ALTER TABLE echeanciers RENAME COLUMN eleve_id TO student_id;
ALTER TABLE echeanciers RENAME COLUMN nb_mensualites TO nombre_echeances;

ALTER TABLE echeanciers DROP COLUMN IF EXISTS date_debut;
ALTER TABLE echeanciers DROP COLUMN IF EXISTS type_frais_id;

ALTER TABLE echeanciers ADD COLUMN IF NOT EXISTS annee_scolaire VARCHAR(9) NOT NULL DEFAULT '2025-2026';
ALTER TABLE echeanciers ADD COLUMN IF NOT EXISTS statut VARCHAR(20) NOT NULL DEFAULT 'ACTIF';
ALTER TABLE echeanciers ADD COLUMN IF NOT EXISTS notes TEXT;
ALTER TABLE echeanciers ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT NOW();

-- ─── echeances ──────────────────────────────────────────────────────
ALTER TABLE echeances DROP CONSTRAINT IF EXISTS echeances_statut_check;

ALTER TABLE echeances DROP COLUMN IF EXISTS paiement_id;

ALTER TABLE echeances ADD COLUMN IF NOT EXISTS montant_paye DECIMAL(10,2) NOT NULL DEFAULT 0;
ALTER TABLE echeances ADD COLUMN IF NOT EXISTS date_paiement DATE;
ALTER TABLE echeances ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT NOW();
ALTER TABLE echeances ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT NOW();

ALTER TABLE echeances ALTER COLUMN statut SET DEFAULT 'EN_ATTENTE';

-- Index sur la nouvelle FK (l'ancien indexait eleve_id → renomme suit automatiquement,
-- mais on s'assure que l'index existe avec le bon nom).
DROP INDEX IF EXISTS idx_factures_eleve;
CREATE INDEX IF NOT EXISTS idx_factures_student ON factures(student_id);
CREATE INDEX IF NOT EXISTS idx_echeanciers_student ON echeanciers(student_id);
