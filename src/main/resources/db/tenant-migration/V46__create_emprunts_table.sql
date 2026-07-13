CREATE TABLE emprunts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    livre_id UUID NOT NULL REFERENCES livres(id),
    eleve_id UUID NOT NULL,
    date_emprunt DATE NOT NULL DEFAULT CURRENT_DATE,
    date_retour_prevue DATE NOT NULL,
    date_retour_effective DATE,
    statut VARCHAR(20) NOT NULL DEFAULT 'EN_COURS' CHECK (statut IN ('EN_COURS', 'RETOURNE', 'EN_RETARD', 'PERDU')),
    penalite NUMERIC(10,2) DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_emprunts_eleve ON emprunts(eleve_id);
CREATE INDEX idx_emprunts_livre ON emprunts(livre_id);
CREATE INDEX idx_emprunts_statut ON emprunts(statut);
