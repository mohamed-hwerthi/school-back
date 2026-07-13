CREATE TABLE liste_attente (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inscription_id UUID NOT NULL REFERENCES inscriptions(id),
    niveau_id UUID NOT NULL,
    position INTEGER NOT NULL,
    annee_scolaire VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_liste_attente_niveau ON liste_attente(niveau_id, annee_scolaire);
