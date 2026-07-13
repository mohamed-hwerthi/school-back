ALTER TABLE refresh_tokens ADD COLUMN device_name VARCHAR(255);
ALTER TABLE refresh_tokens ADD COLUMN ip_address VARCHAR(45);
ALTER TABLE refresh_tokens ADD COLUMN last_used_at TIMESTAMP DEFAULT NOW();
