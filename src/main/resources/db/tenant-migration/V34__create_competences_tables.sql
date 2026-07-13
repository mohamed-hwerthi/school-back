CREATE TABLE IF NOT EXISTS competences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    module_id UUID REFERENCES modules(id) ON DELETE CASCADE,
    label VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS evaluations_competences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    eleve_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    competence_id UUID NOT NULL REFERENCES competences(id) ON DELETE CASCADE,
    examen_id UUID NOT NULL REFERENCES examens(id) ON DELETE CASCADE,
    niveau VARCHAR(20) NOT NULL CHECK (niveau IN ('NON_ATTEINT', 'EN_COURS', 'ATTEINT', 'DEPASSE')),
    commentaire TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(eleve_id, competence_id, examen_id)
);
