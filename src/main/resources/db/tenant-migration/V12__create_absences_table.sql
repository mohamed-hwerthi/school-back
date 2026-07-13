CREATE TABLE IF NOT EXISTS absences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    eleve_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    type VARCHAR(10) NOT NULL CHECK (type IN ('ABSENCE', 'RETARD')),
    seance VARCHAR(30),
    heure_arrivee TIME,
    justifie BOOLEAN NOT NULL DEFAULT FALSE,
    motif TEXT,
    enseignant_id UUID REFERENCES teachers(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_absences_eleve ON absences(eleve_id);
CREATE INDEX idx_absences_date ON absences(date);
CREATE INDEX idx_absences_eleve_date ON absences(eleve_id, date);

CREATE TABLE IF NOT EXISTS justificatifs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    absence_id UUID NOT NULL REFERENCES absences(id) ON DELETE CASCADE,
    fichier_url VARCHAR(500),
    date_soumission TIMESTAMP NOT NULL DEFAULT NOW(),
    valide BOOLEAN DEFAULT NULL
);
