CREATE TABLE parent_students (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_user_id UUID NOT NULL,
    student_id UUID NOT NULL REFERENCES students(id),
    relation VARCHAR(30) DEFAULT 'PARENT' CHECK (relation IN ('PARENT', 'TUTEUR', 'AUTRE')),
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(parent_user_id, student_id)
);
