CREATE TABLE IF NOT EXISTS annees_scolaires (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    label VARCHAR(20) NOT NULL UNIQUE,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT FALSE,
    cloturee BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS trimestres (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    annee_scolaire_id UUID NOT NULL REFERENCES annees_scolaires(id) ON DELETE CASCADE,
    numero INTEGER NOT NULL CHECK (numero BETWEEN 1 AND 3),
    label VARCHAR(50) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    saisie_notes_ouverte BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE(annee_scolaire_id, numero)
);

CREATE TABLE IF NOT EXISTS vacances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    annee_scolaire_id UUID NOT NULL REFERENCES annees_scolaires(id) ON DELETE CASCADE,
    label VARCHAR(100) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS jours_feries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    annee_scolaire_id UUID NOT NULL REFERENCES annees_scolaires(id) ON DELETE CASCADE,
    label VARCHAR(100) NOT NULL,
    date DATE NOT NULL
);

-- Seed current year
INSERT INTO annees_scolaires (label, date_debut, date_fin, active) VALUES
('2025-2026', '2025-09-01', '2026-06-30', TRUE);

INSERT INTO trimestres (annee_scolaire_id, numero, label, date_debut, date_fin) VALUES
((SELECT id FROM annees_scolaires WHERE label = '2025-2026'), 1, 'Trimestre 1', '2025-09-01', '2025-12-19'),
((SELECT id FROM annees_scolaires WHERE label = '2025-2026'), 2, 'Trimestre 2', '2026-01-05', '2026-03-27'),
((SELECT id FROM annees_scolaires WHERE label = '2025-2026'), 3, 'Trimestre 3', '2026-04-13', '2026-06-30');
