-- Audit / traçabilité des notifications envoyées (parents, enseignants…).
-- Une ligne par tentative d'envoi (un envoi multi-canal = plusieurs lignes).

CREATE TABLE IF NOT EXISTS notification_logs (
    id                    UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_type        VARCHAR(20)  NOT NULL,
        -- PARENT, TEACHER, STUDENT, ADMIN
    recipient_id          UUID,
        -- ID de l'élève (si PARENT, on log le student_id pour pouvoir filtrer)
    recipient_address     VARCHAR(255) NOT NULL,
        -- numéro de tel ou email ciblé
    channel               VARCHAR(20)  NOT NULL CHECK (channel IN ('SMS', 'EMAIL', 'PUSH')),
    event_type            VARCHAR(50)  NOT NULL,
        -- NOTE_PUBLIEE, ABSENCE_NON_JUSTIFIEE, BULLETIN_DISPONIBLE, MANUAL, …
    subject               VARCHAR(255),
    body                  TEXT         NOT NULL,
    status                VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'SKIPPED')),
    provider_message_id   VARCHAR(255),
    error_message         TEXT,
    related_entity_type   VARCHAR(50),
        -- NOTE, ABSENCE, BULLETIN… pour pouvoir remonter à l'objet métier
    related_entity_id     UUID,
    triggered_by_user_id  UUID,
    retry_count           INTEGER      NOT NULL DEFAULT 0,
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    sent_at               TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notif_logs_recipient
    ON notification_logs (recipient_type, recipient_id);
CREATE INDEX IF NOT EXISTS idx_notif_logs_status
    ON notification_logs (status);
CREATE INDEX IF NOT EXISTS idx_notif_logs_event
    ON notification_logs (event_type);
CREATE INDEX IF NOT EXISTS idx_notif_logs_related
    ON notification_logs (related_entity_type, related_entity_id);
CREATE INDEX IF NOT EXISTS idx_notif_logs_created
    ON notification_logs (created_at DESC);
