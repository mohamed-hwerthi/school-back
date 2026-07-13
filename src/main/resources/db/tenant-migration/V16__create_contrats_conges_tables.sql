CREATE TABLE IF NOT EXISTS contrats_enseignant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    enseignant_id UUID NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
    type_contrat VARCHAR(20) NOT NULL CHECK (type_contrat IN ('CDI','CDD','VACATAIRE')),
    date_debut DATE NOT NULL,
    date_fin DATE,
    salaire_base DECIMAL(10,2),
    document_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS conges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    enseignant_id UUID NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
    type_conge VARCHAR(30) NOT NULL CHECK (type_conge IN ('ANNUEL','MALADIE','MATERNITE','EXCEPTIONNEL','SANS_SOLDE')),
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    motif TEXT,
    statut VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE' CHECK (statut IN ('EN_ATTENTE','APPROUVE','REFUSE')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_contrats_enseignant ON contrats_enseignant(enseignant_id);
CREATE INDEX idx_conges_enseignant ON conges(enseignant_id);
CREATE INDEX idx_conges_statut ON conges(statut);
