CREATE TABLE vitrine_contacts (
    id              UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255)    NOT NULL,
    email           VARCHAR(255)    NOT NULL,
    phone           VARCHAR(50),
    subject         VARCHAR(500),
    message         TEXT            NOT NULL,
    is_read         BOOLEAN         NOT NULL DEFAULT false,
    replied         BOOLEAN         NOT NULL DEFAULT false,
    reply_text      TEXT,
    replied_at      TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_vitrine_contacts_read ON vitrine_contacts(is_read);
CREATE INDEX idx_vitrine_contacts_created ON vitrine_contacts(created_at DESC);
