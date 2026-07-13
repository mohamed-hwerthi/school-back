CREATE TABLE IF NOT EXISTS remplacements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    emploi_du_temps_id UUID NOT NULL REFERENCES emploi_du_temps(id) ON DELETE CASCADE,
    enseignant_remplacant_id UUID NOT NULL REFERENCES teachers(id),
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    motif VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
