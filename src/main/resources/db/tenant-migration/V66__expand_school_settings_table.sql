-- Add missing columns to school_settings for full school info management
ALTER TABLE school_settings ADD COLUMN IF NOT EXISTS logo TEXT;
ALTER TABLE school_settings ADD COLUMN IF NOT EXISTS ville VARCHAR(255);
ALTER TABLE school_settings ADD COLUMN IF NOT EXISTS ville_ar VARCHAR(255);
ALTER TABLE school_settings ADD COLUMN IF NOT EXISTS email VARCHAR(255);
ALTER TABLE school_settings ADD COLUMN IF NOT EXISTS site_web VARCHAR(255);
ALTER TABLE school_settings ADD COLUMN IF NOT EXISTS annee_creation VARCHAR(10);
ALTER TABLE school_settings ADD COLUMN IF NOT EXISTS description TEXT;
