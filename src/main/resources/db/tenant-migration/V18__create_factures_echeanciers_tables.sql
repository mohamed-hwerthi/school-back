CREATE TABLE IF NOT EXISTS factures (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero VARCHAR(20) NOT NULL UNIQUE,
    eleve_id UUID NOT NULL REFERENCES students(id),
    date_emission DATE NOT NULL DEFAULT CURRENT_DATE,
    date_echeance DATE,
    montant_total DECIMAL(10,2) NOT NULL,
    montant_remise DECIMAL(10,2) NOT NULL DEFAULT 0,
    montant_net DECIMAL(10,2) NOT NULL,
    statut VARCHAR(20) NOT NULL DEFAULT 'NON_PAYEE' CHECK (statut IN ('NON_PAYEE','PARTIELLEMENT_PAYEE','PAYEE','ANNULEE')),
    pdf_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS facture_lignes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    facture_id UUID NOT NULL REFERENCES factures(id) ON DELETE CASCADE,
    type_frais_id UUID REFERENCES types_frais(id),
    description VARCHAR(255) NOT NULL,
    quantite INTEGER NOT NULL DEFAULT 1,
    prix_unitaire DECIMAL(10,2) NOT NULL,
    montant DECIMAL(10,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS echeanciers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    eleve_id UUID NOT NULL REFERENCES students(id),
    type_frais_id UUID REFERENCES types_frais(id),
    montant_total DECIMAL(10,2) NOT NULL,
    nb_mensualites INTEGER NOT NULL,
    date_debut DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS echeances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    echeancier_id UUID NOT NULL REFERENCES echeanciers(id) ON DELETE CASCADE,
    numero INTEGER NOT NULL,
    montant DECIMAL(10,2) NOT NULL,
    date_echeance DATE NOT NULL,
    statut VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE' CHECK (statut IN ('EN_ATTENTE','PAYEE','EN_RETARD')),
    paiement_id UUID REFERENCES paiements(id),
    UNIQUE(echeancier_id, numero)
);
CREATE INDEX idx_factures_eleve ON factures(eleve_id);
CREATE INDEX idx_echeances_statut ON echeances(statut);
CREATE INDEX idx_echeances_date ON echeances(date_echeance);
