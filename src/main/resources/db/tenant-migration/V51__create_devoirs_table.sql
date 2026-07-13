CREATE TABLE devoirs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titre VARCHAR(300) NOT NULL,
    description TEXT,
    module_id UUID,
    classe_id UUID,
    enseignant_id UUID,
    date_publication DATE NOT NULL DEFAULT CURRENT_DATE,
    date_limite DATE NOT NULL,
    type VARCHAR(20) DEFAULT 'DEVOIR' CHECK (type IN ('DEVOIR', 'EXERCICE', 'PROJET', 'EXPOSE')),
    points_max INTEGER DEFAULT 20,
    fichier_url VARCHAR(500),
    statut VARCHAR(20) DEFAULT 'PUBLIE' CHECK (statut IN ('BROUILLON', 'PUBLIE', 'FERME')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_devoirs_classe ON devoirs(classe_id);
CREATE INDEX idx_devoirs_module ON devoirs(module_id);
CREATE INDEX idx_devoirs_date_limite ON devoirs(date_limite);
