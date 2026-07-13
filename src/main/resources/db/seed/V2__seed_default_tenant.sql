-- Seed a default school for development/testing
INSERT INTO tenants (name, schema_name, contact_email, active)
VALUES ('École Primaire Ibn Khaldoun', 'school_1', 'admin@ibnkhaldoun.tn', true)
ON CONFLICT (schema_name) DO NOTHING;
