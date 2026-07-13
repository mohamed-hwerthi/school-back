CREATE TABLE pointage_personnel (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employe_id UUID NOT NULL,
    employe_type VARCHAR(20) NOT NULL CHECK (employe_type IN ('ENSEIGNANT', 'ADMIN', 'PERSONNEL')),
    date_pointage DATE NOT NULL DEFAULT CURRENT_DATE,
    heure_arrivee TIME,
    heure_depart TIME,
    heures_travaillees NUMERIC(4,2),
    statut VARCHAR(20) DEFAULT 'PRESENT' CHECK (statut IN ('PRESENT', 'ABSENT', 'RETARD', 'CONGE')),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_pointage_employe ON pointage_personnel(employe_id, date_pointage);
