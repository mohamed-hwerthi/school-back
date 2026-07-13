CREATE TABLE IF NOT EXISTS bulletin_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nom VARCHAR(100) NOT NULL,
    logo_url VARCHAR(500),
    nom_ecole_fr VARCHAR(255),
    nom_ecole_ar VARCHAR(255),
    adresse VARCHAR(500),
    telephone VARCHAR(50),
    email VARCHAR(100),
    header_color VARCHAR(7) DEFAULT '#1e40af',
    show_logo BOOLEAN DEFAULT TRUE,
    show_photo_eleve BOOLEAN DEFAULT FALSE,
    show_appreciation BOOLEAN DEFAULT TRUE,
    show_rang BOOLEAN DEFAULT TRUE,
    footer_text TEXT,
    actif BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
INSERT INTO bulletin_templates (nom, nom_ecole_fr, nom_ecole_ar, actif) VALUES ('Template par défaut', 'École', 'المدرسة', true);
