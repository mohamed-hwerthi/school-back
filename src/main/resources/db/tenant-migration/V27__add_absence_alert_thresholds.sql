CREATE TABLE IF NOT EXISTS absence_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seuil_alerte_jaune INT NOT NULL DEFAULT 3,
    seuil_alerte_rouge INT NOT NULL DEFAULT 5,
    notification_auto BOOLEAN NOT NULL DEFAULT TRUE,
    notification_email BOOLEAN NOT NULL DEFAULT TRUE,
    notification_sms BOOLEAN NOT NULL DEFAULT FALSE
);
INSERT INTO absence_settings (seuil_alerte_jaune, seuil_alerte_rouge) VALUES (3, 5);
