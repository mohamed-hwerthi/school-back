CREATE TABLE IF NOT EXISTS baremes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    label VARCHAR(100) NOT NULL,
    note_max DECIMAL(5,2) NOT NULL DEFAULT 20.0,
    note_min DECIMAL(5,2) NOT NULL DEFAULT 0.0,
    note_passage DECIMAL(5,2) NOT NULL DEFAULT 10.0,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
INSERT INTO baremes (label, note_max, note_min, note_passage, actif) VALUES ('Barème /20', 20.0, 0.0, 10.0, true);
