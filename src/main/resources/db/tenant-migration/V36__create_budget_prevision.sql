CREATE TABLE budgets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    annee_scolaire VARCHAR(20) NOT NULL,
    label VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('RECETTE', 'DEPENSE')),
    categorie VARCHAR(100),
    montant_prevu NUMERIC(12,2) NOT NULL DEFAULT 0,
    montant_realise NUMERIC(12,2) NOT NULL DEFAULT 0,
    mois INTEGER,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
