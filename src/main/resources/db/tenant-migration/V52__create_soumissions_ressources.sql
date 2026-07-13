CREATE TABLE soumissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    devoir_id UUID NOT NULL REFERENCES devoirs(id),
    eleve_id UUID NOT NULL,
    contenu TEXT,
    fichier_url VARCHAR(500),
    date_soumission TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    note NUMERIC(5,2),
    commentaire_correction TEXT,
    corrige BOOLEAN DEFAULT false,
    en_retard BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_soumissions_devoir ON soumissions(devoir_id);
CREATE INDEX idx_soumissions_eleve ON soumissions(eleve_id);

CREATE TABLE ressources_pedagogiques (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titre VARCHAR(300) NOT NULL,
    description TEXT,
    module_id UUID,
    type VARCHAR(20) DEFAULT 'DOCUMENT' CHECK (type IN ('DOCUMENT', 'VIDEO', 'LIEN', 'IMAGE', 'AUDIO')),
    fichier_url VARCHAR(500),
    lien_externe VARCHAR(500),
    enseignant_id UUID,
    taille_fichier BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_ressources_module ON ressources_pedagogiques(module_id);
