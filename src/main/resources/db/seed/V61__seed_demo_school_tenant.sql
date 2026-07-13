-- ================================================================
-- P1-DATA: Seed the 'demo_school' tenant in the PUBLIC schema
-- and link demo users to it.
-- ================================================================

-- Create the demo_school tenant (separate from the default school_1)
INSERT INTO tenants (name, schema_name, contact_email, active, slug, plan, max_students, max_teachers, onboarding_completed)
VALUES (
    'École Primaire Demo',
    'demo_school',
    'contact@demo-school.tn',
    true,
    'demo-school',
    'PREMIUM',
    200,
    30,
    true
)
ON CONFLICT (schema_name) DO NOTHING;

-- Update the existing demo users to point to 'demo_school' tenant_id
-- (V60 created them with tenant_id = 'demo-school'; ensure consistency)
UPDATE users SET tenant_id = 'demo_school'
WHERE email IN ('directeur@school.dev', 'prof@school.dev', 'comptable@school.dev', 'parent@school.dev')
  AND tenant_id IS DISTINCT FROM 'demo_school';
