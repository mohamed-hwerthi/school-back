CREATE TABLE abonnements_cantine (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    eleve_id UUID NOT NULL,
    type_abonnement VARCHAR(20) NOT NULL CHECK (type_abonnement IN ('JOURNALIER', 'HEBDOMADAIRE', 'MENSUEL', 'ANNUEL')),
    date_debut DATE NOT NULL,
    date_fin DATE,
    montant NUMERIC(10,2) NOT NULL,
    actif BOOLEAN DEFAULT true,
    allergies TEXT,
    regime VARCHAR(20) DEFAULT 'STANDARD',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE pointages_repas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    eleve_id UUID NOT NULL,
    date_repas DATE NOT NULL DEFAULT CURRENT_DATE,
    type_repas VARCHAR(20) DEFAULT 'DEJEUNER' CHECK (type_repas IN ('PETIT_DEJEUNER', 'DEJEUNER', 'GOUTER')),
    present BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_pointages_repas_date ON pointages_repas(date_repas);
CREATE INDEX idx_pointages_repas_eleve ON pointages_repas(eleve_id);
