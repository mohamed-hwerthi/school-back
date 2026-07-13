-- Add statut column to notes (PRESENT, ABSENT, EXEMPT)
ALTER TABLE notes
    ADD COLUMN statut VARCHAR(20) NOT NULL DEFAULT 'PRESENT'
        CHECK (statut IN ('PRESENT', 'ABSENT', 'EXEMPT'));
