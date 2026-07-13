-- ============================================================
-- V8 : Remises & Pénalités
-- ============================================================

-- Remises (discounts)
CREATE TABLE IF NOT EXISTS remises (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id      UUID       NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    type_frais_id   UUID       REFERENCES types_frais(id) ON DELETE SET NULL,
    type            VARCHAR(20)  NOT NULL CHECK (type IN ('FRATRIE','BOURSE','PERSONNEL','ANTICIPATION','COMMERCIAL')),
    valeur          NUMERIC(10,2) NOT NULL CHECK (valeur > 0),
    est_pourcentage BOOLEAN      NOT NULL DEFAULT FALSE,
    motif           VARCHAR(255),
    annee_scolaire  VARCHAR(9)   NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_remises_student   ON remises(student_id);
CREATE INDEX idx_remises_annee     ON remises(annee_scolaire);

-- Pénalités (late fees)
CREATE TABLE IF NOT EXISTS penalites (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id       UUID       NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    paiement_id      UUID       REFERENCES paiements(id) ON DELETE SET NULL,
    montant          NUMERIC(10,2) NOT NULL CHECK (montant > 0),
    motif            VARCHAR(255) NOT NULL,
    date_application DATE         NOT NULL DEFAULT CURRENT_DATE,
    annee_scolaire   VARCHAR(9)   NOT NULL,
    payee            BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_penalites_student ON penalites(student_id);
CREATE INDEX idx_penalites_annee   ON penalites(annee_scolaire);
