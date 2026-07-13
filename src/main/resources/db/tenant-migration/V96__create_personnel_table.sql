-- Non-teaching staff (secrétaire, comptable, gardien, femme de ménage, etc.).
-- HR record only — no login account is provisioned for these employees.
CREATE TABLE personnel (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    fonction VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    sexe VARCHAR(1),
    telephone VARCHAR(50),
    date_naissance DATE,
    date_embauche DATE DEFAULT CURRENT_DATE,
    statut VARCHAR(20) NOT NULL DEFAULT 'Actif',
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_personnel_fonction ON personnel(fonction) WHERE deleted = FALSE;
CREATE INDEX idx_personnel_statut ON personnel(statut) WHERE deleted = FALSE;
