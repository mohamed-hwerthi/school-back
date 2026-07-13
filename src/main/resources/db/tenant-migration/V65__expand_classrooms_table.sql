-- Add missing columns to classrooms table for full room management
ALTER TABLE classrooms ADD COLUMN IF NOT EXISTS type       VARCHAR(50)  DEFAULT 'Salle de classe';
ALTER TABLE classrooms ADD COLUMN IF NOT EXISTS floor      INTEGER      DEFAULT 0;
ALTER TABLE classrooms ADD COLUMN IF NOT EXISTS equipment  TEXT;
ALTER TABLE classrooms ADD COLUMN IF NOT EXISTS status     VARCHAR(30)  DEFAULT 'Disponible';
