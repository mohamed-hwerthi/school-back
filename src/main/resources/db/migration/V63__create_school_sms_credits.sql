CREATE TABLE IF NOT EXISTS school_sms_credits (
    tenant_id VARCHAR(255) PRIMARY KEY,
    total_credits INTEGER NOT NULL DEFAULT 0,
    used_credits INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMP DEFAULT NOW()
);
