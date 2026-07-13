CREATE TABLE quiz (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titre VARCHAR(300) NOT NULL,
    description TEXT,
    module_id UUID,
    classe_id UUID,
    enseignant_id UUID,
    duree_minutes INTEGER NOT NULL DEFAULT 60,
    note_totale NUMERIC(5,2) DEFAULT 20,
    melanger_questions BOOLEAN DEFAULT false,
    melanger_reponses BOOLEAN DEFAULT false,
    afficher_resultats BOOLEAN DEFAULT true,
    tentatives_max INTEGER DEFAULT 1,
    date_debut TIMESTAMP,
    date_fin TIMESTAMP,
    statut VARCHAR(20) DEFAULT 'BROUILLON' CHECK (statut IN ('BROUILLON', 'PUBLIE', 'EN_COURS', 'TERMINE')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_quiz_classe ON quiz(classe_id);
CREATE INDEX idx_quiz_statut ON quiz(statut);
