-- Seed demo users for development/testing (BCrypt-hashed passwords)
-- These should be removed or replaced before production deployment

INSERT INTO users (email, password_hash, first_name, last_name, role, tenant_id, is_active, created_at, updated_at)
VALUES
    ('admin@school.dev', '$2a$10$dIfc.Z61dtNQjcOv7E70MOBqMXVShnPbeSO0W8y28crCDem6hEgYK', 'Admin', 'Demo', 'SUPER_ADMIN', NULL, TRUE, NOW(), NOW()),
    ('directeur@school.dev', '$2a$10$NB5YQtnJ8Hgq6W3QgSeBRO5bIbh9UdYPUYyxlDPHZ7/DjQU293HKO', 'Mohamed', 'Directeur', 'DIRECTEUR', 'demo-school', TRUE, NOW(), NOW()),
    ('prof@school.dev', '$2a$10$WKsBZaz8D/hKjdPKWTY.G.i3P./kvAtHe0JzgujyU20Xk50adQZBG', 'Fatma', 'Enseignante', 'ENSEIGNANT', 'demo-school', TRUE, NOW(), NOW()),
    ('comptable@school.dev', '$2a$10$jhHmoyvBbSairwGvoQUUZOXHa4MbtQEUN/sUTZOGL2g/JoYK7easa', 'Ali', 'Comptable', 'COMPTABLE', 'demo-school', TRUE, NOW(), NOW()),
    ('parent@school.dev', '$2a$10$Ab9Zn7c6uXwidHlPY.6dzupVIzXnj0lDhO9qq9fSaZJBkXARnQDCC', 'Leila', 'Parent', 'PARENT', 'demo-school', TRUE, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;
