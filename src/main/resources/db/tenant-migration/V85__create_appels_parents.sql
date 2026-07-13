-- V85: log of phone calls made to parents (free-form notes captured by staff).
-- Used by AppelDialog on the front so calls leave an audit trail per student.

CREATE TABLE IF NOT EXISTS appels_parents (
    id              UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    eleve_id        UUID       NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    appele_par      VARCHAR(150),                          -- name of the staff member
    telephone       VARCHAR(30),                           -- snapshot of the number at call time
    motif           VARCHAR(100),                          -- short categorization (paiement, absence, autre)
    notes           TEXT         NOT NULL,
    date_appel      TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN      NOT NULL DEFAULT false,
    deleted_at      TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_appels_eleve     ON appels_parents(eleve_id);
CREATE INDEX IF NOT EXISTS idx_appels_date      ON appels_parents(date_appel DESC);
