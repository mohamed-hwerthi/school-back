CREATE TABLE fiches_paie (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employe_id UUID NOT NULL,
    employe_type VARCHAR(20) NOT NULL,
    mois INTEGER NOT NULL,
    annee INTEGER NOT NULL,
    salaire_base NUMERIC(10,2) NOT NULL,
    primes NUMERIC(10,2) DEFAULT 0,
    retenues NUMERIC(10,2) DEFAULT 0,
    salaire_net NUMERIC(10,2) NOT NULL,
    date_paiement DATE,
    paye BOOLEAN DEFAULT false,
    commentaire TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_fiches_paie_employe ON fiches_paie(employe_id, annee, mois);

CREATE TABLE formations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titre VARCHAR(300) NOT NULL,
    description TEXT,
    formateur VARCHAR(200),
    date_debut DATE NOT NULL,
    date_fin DATE,
    lieu VARCHAR(200),
    nombre_heures INTEGER,
    cout NUMERIC(10,2),
    statut VARCHAR(20) DEFAULT 'PLANIFIEE' CHECK (statut IN ('PLANIFIEE', 'EN_COURS', 'TERMINEE', 'ANNULEE')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE formation_participants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    formation_id UUID NOT NULL REFERENCES formations(id),
    employe_id UUID NOT NULL,
    employe_type VARCHAR(20) NOT NULL,
    present BOOLEAN DEFAULT false,
    certificat_obtenu BOOLEAN DEFAULT false
);
