-- Add slug column to tenants table for public vitrine URLs.
-- The slug is derived from the school name (lowercase, hyphens).

ALTER TABLE tenants ADD COLUMN slug VARCHAR(255) UNIQUE;

-- Populate slug from existing schema_name for any pre-existing tenants
UPDATE tenants SET slug = REPLACE(schema_name, '_', '-') WHERE slug IS NULL;

ALTER TABLE tenants ALTER COLUMN slug SET NOT NULL;
