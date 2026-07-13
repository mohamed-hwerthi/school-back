CREATE TABLE vehicules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    immatriculation VARCHAR(20) NOT NULL UNIQUE,
    marque VARCHAR(100),
    modele VARCHAR(100),
    capacite INTEGER NOT NULL,
    chauffeur_nom VARCHAR(200),
    chauffeur_telephone VARCHAR(20),
    date_assurance DATE,
    date_controle_technique DATE,
    statut VARCHAR(20) DEFAULT 'ACTIF' CHECK (statut IN ('ACTIF', 'EN_PANNE', 'EN_MAINTENANCE')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE circuits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nom VARCHAR(200) NOT NULL,
    description TEXT,
    vehicule_id UUID REFERENCES vehicules(id),
    heure_depart TIME,
    heure_retour TIME,
    distance_km NUMERIC(6,2),
    cout_mensuel NUMERIC(10,2),
    actif BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE arrets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    circuit_id UUID NOT NULL REFERENCES circuits(id) ON DELETE CASCADE,
    nom VARCHAR(200) NOT NULL,
    adresse TEXT,
    ordre INTEGER NOT NULL,
    heure_passage TIME,
    latitude NUMERIC(10,7),
    longitude NUMERIC(10,7)
);
