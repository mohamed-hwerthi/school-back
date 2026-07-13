-- V64: Create meetings table for parent-teacher meeting scheduler
CREATE TABLE IF NOT EXISTS meetings (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(255) NOT NULL,
    date            DATE NOT NULL,
    heure_debut     TIME NOT NULL,
    heure_fin       TIME NOT NULL,
    enseignant_id   UUID,
    parent_id       UUID,
    student_id      UUID,
    statut          VARCHAR(20) NOT NULL DEFAULT 'PLANIFIE'
                    CHECK (statut IN ('PLANIFIE', 'CONFIRME', 'ANNULE')),
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_meetings_enseignant ON meetings(enseignant_id);
CREATE INDEX IF NOT EXISTS idx_meetings_parent ON meetings(parent_id);
CREATE INDEX IF NOT EXISTS idx_meetings_student ON meetings(student_id);
CREATE INDEX IF NOT EXISTS idx_meetings_date ON meetings(date);
CREATE INDEX IF NOT EXISTS idx_meetings_statut ON meetings(statut);
