CREATE TABLE IF NOT EXISTS passages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES students(id),
    ancien_niveau VARCHAR(100),
    nouveau_niveau VARCHAR(100),
    ancienne_classe VARCHAR(50),
    nouvelle_classe VARCHAR(50),
    decision VARCHAR(20) NOT NULL CHECK (decision IN ('PASSAGE', 'REDOUBLEMENT', 'EXCLUSION', 'TRANSFERT')),
    annee_scolaire VARCHAR(20) NOT NULL,
    motif TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);
