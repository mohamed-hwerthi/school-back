CREATE TABLE inscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    date_naissance DATE NOT NULL,
    lieu_naissance VARCHAR(200),
    sexe VARCHAR(1) CHECK (sexe IN ('M', 'F')),
    adresse TEXT,
    telephone_parent VARCHAR(20),
    email_parent VARCHAR(200),
    nom_parent VARCHAR(200),
    prenom_parent VARCHAR(200),
    niveau_id UUID,
    annee_scolaire VARCHAR(20) NOT NULL,
    statut VARCHAR(20) NOT NULL DEFAULT 'SOUMISE' CHECK (statut IN ('SOUMISE', 'EN_REVISION', 'ACCEPTEE', 'REFUSEE', 'EN_ATTENTE', 'LISTE_ATTENTE')),
    commentaire TEXT,
    numero_dossier VARCHAR(50) UNIQUE,
    documents_paths TEXT[],
    montant_frais NUMERIC(10,2) DEFAULT 0,
    frais_paye BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_inscriptions_statut ON inscriptions(statut);
CREATE INDEX idx_inscriptions_annee ON inscriptions(annee_scolaire);
