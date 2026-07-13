-- Users table in PUBLIC schema (needed before tenant resolution)
CREATE TABLE IF NOT EXISTS users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    role            VARCHAR(30)  NOT NULL DEFAULT 'ENSEIGNANT',
    tenant_id       VARCHAR(100),
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_user_role CHECK (role IN (
        'SUPER_ADMIN', 'ADMIN', 'DIRECTEUR', 'ENSEIGNANT', 'COMPTABLE', 'PARENT'
    ))
);

-- Refresh tokens for JWT rotation
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(500) NOT NULL UNIQUE,
    expires_at  TIMESTAMP    NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_tenant ON users(tenant_id);

-- Seed a default super admin (password: admin123)
INSERT INTO users (email, password_hash, first_name, last_name, role, tenant_id)
VALUES (
    'admin@school.com',
    '$2a$10$1WvFtnApuZtZOpVrKU9uj.x/Jkk.vrNLfBZV4yhBb.gZ/YepSEXa.',
    'Admin',
    'System',
    'SUPER_ADMIN',
    'school_1'
);
