-- ============================================================
-- V9 : Relances (payment reminders)
-- ============================================================

CREATE TABLE IF NOT EXISTS relances (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id       UUID       NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    paiement_id      UUID       REFERENCES paiements(id) ON DELETE SET NULL,
    type             VARCHAR(10)  NOT NULL CHECK (type IN ('EMAIL','SMS','COURRIER')),
    statut           VARCHAR(15)  NOT NULL CHECK (statut IN ('EN_ATTENTE','ENVOYEE','ECHOUEE')),
    message          TEXT         NOT NULL,
    destinataire     VARCHAR(255) NOT NULL,
    montant_du       NUMERIC(10,2),
    date_envoi       DATE,
    date_prevue      DATE         NOT NULL,
    annee_scolaire   VARCHAR(9)   NOT NULL,
    numero_relance   INTEGER      NOT NULL DEFAULT 1,
    created_at       TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_relances_student ON relances(student_id);
CREATE INDEX idx_relances_annee   ON relances(annee_scolaire);
CREATE INDEX idx_relances_statut  ON relances(statut);
