-- Types de frais (scolarité, inscription, transport, etc.)
CREATE TABLE IF NOT EXISTS types_frais (
    id              UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    nom             VARCHAR(255)    NOT NULL,
    montant         DECIMAL(10,2)   NOT NULL,
    frequence       VARCHAR(20)     NOT NULL CHECK (frequence IN ('MENSUEL', 'TRIMESTRIEL', 'ANNUEL', 'UNIQUE')),
    description     TEXT,
    actif           BOOLEAN         NOT NULL DEFAULT true,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Paiements (entrées financières liées aux élèves)
CREATE TABLE IF NOT EXISTS paiements (
    id              UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id      UUID          NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    type_frais_id   UUID          NOT NULL REFERENCES types_frais(id) ON DELETE RESTRICT,
    mois            VARCHAR(10)     NOT NULL,
    annee_scolaire  VARCHAR(9)      NOT NULL,
    montant_du      DECIMAL(10,2)   NOT NULL,
    montant_paye    DECIMAL(10,2)   NOT NULL DEFAULT 0,
    date_paiement   DATE,
    mode_paiement   VARCHAR(20)     CHECK (mode_paiement IN ('ESPECES', 'VIREMENT', 'CHEQUE', 'CARTE_BANCAIRE', 'PRELEVEMENT')),
    statut          VARCHAR(20)     NOT NULL DEFAULT 'EN_ATTENTE' CHECK (statut IN ('PAYE', 'PARTIEL', 'EN_ATTENTE', 'EN_RETARD')),
    reference       VARCHAR(255)    UNIQUE,
    notes           TEXT,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Index pour les requêtes fréquentes
CREATE INDEX idx_paiements_student_id ON paiements(student_id);
CREATE INDEX idx_paiements_annee_scolaire ON paiements(annee_scolaire);
CREATE INDEX idx_paiements_statut ON paiements(statut);
CREATE INDEX idx_paiements_mois_annee ON paiements(mois, annee_scolaire);
