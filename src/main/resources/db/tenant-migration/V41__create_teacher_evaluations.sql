CREATE TABLE teacher_evaluations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_id UUID NOT NULL,
    evaluator_id UUID,
    evaluator_name VARCHAR(100),
    annee_scolaire VARCHAR(20) NOT NULL,
    trimestre INTEGER NOT NULL,
    ponctualite INTEGER CHECK (ponctualite BETWEEN 1 AND 5),
    pedagogie INTEGER CHECK (pedagogie BETWEEN 1 AND 5),
    discipline INTEGER CHECK (discipline BETWEEN 1 AND 5),
    communication INTEGER CHECK (communication BETWEEN 1 AND 5),
    implication INTEGER CHECK (implication BETWEEN 1 AND 5),
    note_globale NUMERIC(3,1),
    commentaire TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);
