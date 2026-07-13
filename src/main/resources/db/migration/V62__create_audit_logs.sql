-- V62: Create audit_logs in the PUBLIC schema.
-- Auth/security events (LOGIN, LOGOUT, failed attempts) are audited while
-- the connection runs in the public schema — before any tenant context is
-- set — so audit_logs must also exist there, not only in tenant schemas.

CREATE TABLE IF NOT EXISTS audit_logs (
    id          UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    username    VARCHAR(200),
    action      VARCHAR(50)  NOT NULL,
    entity_type VARCHAR(100),
    entity_id   UUID,
    details     TEXT,
    ip_address  VARCHAR(50),
    timestamp   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_username  ON audit_logs(username);
CREATE INDEX IF NOT EXISTS idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity    ON audit_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action    ON audit_logs(action);
