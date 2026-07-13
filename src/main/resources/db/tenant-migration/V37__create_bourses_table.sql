CREATE TABLE bourses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES students(id),
    type VARCHAR(50) NOT NULL CHECK (type IN ('BOURSE', 'AIDE', 'EXONERATION')),
    label VARCHAR(200) NOT NULL,
    montant NUMERIC(12,2) NOT NULL,
    pourcentage NUMERIC(5,2),
    annee_scolaire VARCHAR(20) NOT NULL,
    statut VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (statut IN ('ACTIVE', 'SUSPENDUE', 'TERMINEE')),
    date_debut DATE,
    date_fin DATE,
    motif TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
