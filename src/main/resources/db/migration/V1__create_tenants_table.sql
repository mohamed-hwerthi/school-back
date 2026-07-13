-- Tenants table lives in the PUBLIC schema.
-- It stores the registry of all schools and their schema names.

CREATE TABLE IF NOT EXISTS tenants (
    id              UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255)    NOT NULL,
    schema_name     VARCHAR(63)     NOT NULL UNIQUE,
    contact_email   VARCHAR(255)    NOT NULL UNIQUE,
    active          BOOLEAN         NOT NULL DEFAULT true,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
