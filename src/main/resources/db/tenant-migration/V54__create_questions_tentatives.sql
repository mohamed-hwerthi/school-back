CREATE TABLE questions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quiz_id UUID NOT NULL REFERENCES quiz(id) ON DELETE CASCADE,
    texte TEXT NOT NULL,
    type_question VARCHAR(20) NOT NULL CHECK (type_question IN ('QCM', 'VRAI_FAUX', 'TEXTE_LIBRE', 'REPONSE_COURTE')),
    points NUMERIC(5,2) NOT NULL DEFAULT 1,
    ordre INTEGER NOT NULL,
    explication TEXT,
    image_url VARCHAR(500),
    obligatoire BOOLEAN DEFAULT true
);

CREATE TABLE choix_reponse (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    texte VARCHAR(500) NOT NULL,
    correct BOOLEAN DEFAULT false,
    ordre INTEGER NOT NULL
);

CREATE TABLE tentatives (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quiz_id UUID NOT NULL REFERENCES quiz(id),
    eleve_id UUID NOT NULL,
    date_debut TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_fin TIMESTAMP,
    score NUMERIC(5,2),
    score_pourcentage NUMERIC(5,2),
    statut VARCHAR(20) DEFAULT 'EN_COURS' CHECK (statut IN ('EN_COURS', 'SOUMISE', 'CORRIGEE')),
    temps_passe_secondes INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE reponses_eleve (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tentative_id UUID NOT NULL REFERENCES tentatives(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES questions(id),
    choix_id UUID REFERENCES choix_reponse(id),
    reponse_texte TEXT,
    correct BOOLEAN,
    points_obtenus NUMERIC(5,2) DEFAULT 0
);

CREATE INDEX idx_tentatives_quiz ON tentatives(quiz_id);
CREATE INDEX idx_tentatives_eleve ON tentatives(eleve_id);
CREATE INDEX idx_reponses_tentative ON reponses_eleve(tentative_id);
