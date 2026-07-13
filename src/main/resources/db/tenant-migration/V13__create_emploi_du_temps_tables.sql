CREATE TABLE IF NOT EXISTS creneaux (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    label VARCHAR(50) NOT NULL,
    heure_debut TIME NOT NULL,
    heure_fin TIME NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'COURS' CHECK (type IN ('COURS', 'PAUSE', 'RECREATION'))
);

CREATE TABLE IF NOT EXISTS emploi_du_temps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    classe_id UUID NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    creneau_id UUID NOT NULL REFERENCES creneaux(id),
    jour_semaine INTEGER NOT NULL CHECK (jour_semaine BETWEEN 1 AND 6),
    module_id UUID REFERENCES modules(id),
    enseignant_id UUID REFERENCES teachers(id),
    salle VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(enseignant_id, creneau_id, jour_semaine),
    UNIQUE(classe_id, creneau_id, jour_semaine)
);
CREATE INDEX idx_edt_classe ON emploi_du_temps(classe_id);
CREATE INDEX idx_edt_enseignant ON emploi_du_temps(enseignant_id);

-- Seed default creneaux
INSERT INTO creneaux (label, heure_debut, heure_fin, type) VALUES
('Séance 1', '08:00', '09:00', 'COURS'),
('Séance 2', '09:00', '10:00', 'COURS'),
('Récréation', '10:00', '10:15', 'RECREATION'),
('Séance 3', '10:15', '11:15', 'COURS'),
('Séance 4', '11:15', '12:15', 'COURS'),
('Pause déjeuner', '12:15', '14:00', 'PAUSE'),
('Séance 5', '14:00', '15:00', 'COURS'),
('Séance 6', '15:00', '16:00', 'COURS'),
('Séance 7', '16:00', '17:00', 'COURS');
