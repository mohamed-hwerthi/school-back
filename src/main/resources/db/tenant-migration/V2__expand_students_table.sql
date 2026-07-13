-- Expand students table with all fields required by the admin frontend.

ALTER TABLE students ADD COLUMN IF NOT EXISTS first_name_ar VARCHAR(100);
ALTER TABLE students ADD COLUMN IF NOT EXISTS last_name_ar  VARCHAR(100);
ALTER TABLE students ADD COLUMN IF NOT EXISTS sex           VARCHAR(1)   NOT NULL DEFAULT 'M';
ALTER TABLE students ADD COLUMN IF NOT EXISTS birth_place   VARCHAR(255);
ALTER TABLE students ADD COLUMN IF NOT EXISTS address       TEXT;
ALTER TABLE students ADD COLUMN IF NOT EXISTS classe        VARCHAR(50);
ALTER TABLE students ADD COLUMN IF NOT EXISTS niveau        VARCHAR(100);
ALTER TABLE students ADD COLUMN IF NOT EXISTS enrollment_date DATE       DEFAULT CURRENT_DATE;
ALTER TABLE students ADD COLUMN IF NOT EXISTS status        VARCHAR(20)  NOT NULL DEFAULT 'Actif';
ALTER TABLE students ADD COLUMN IF NOT EXISTS is_blocked    BOOLEAN      NOT NULL DEFAULT false;
ALTER TABLE students ADD COLUMN IF NOT EXISTS parent_last_name  VARCHAR(100);
ALTER TABLE students ADD COLUMN IF NOT EXISTS parent_first_name VARCHAR(100);
ALTER TABLE students ADD COLUMN IF NOT EXISTS parent_phone  VARCHAR(20);
ALTER TABLE students ADD COLUMN IF NOT EXISTS parent_email  VARCHAR(255);
ALTER TABLE students ADD COLUMN IF NOT EXISTS notes         TEXT;

-- email was required before but students don't have personal email, make it optional
ALTER TABLE students ALTER COLUMN email DROP NOT NULL;
