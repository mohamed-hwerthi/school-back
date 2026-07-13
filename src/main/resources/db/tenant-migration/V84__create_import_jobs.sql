-- V84: Track bulk-import jobs so the UI can poll progress for large datasets.
-- Each row records one user-initiated import (students, teachers, etc.) and the
-- per-row outcome is stored as a JSON blob so we don't need a child table.

CREATE TABLE IF NOT EXISTS import_jobs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type            VARCHAR(40)  NOT NULL,                 -- "STUDENTS", "TEACHERS", ...
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING', 'RUNNING', 'DONE', 'FAILED')),
    strategy        VARCHAR(20)  NOT NULL DEFAULT 'SKIP'
                    CHECK (strategy IN ('SKIP', 'UPDATE')), -- on duplicates
    total_rows      INTEGER      NOT NULL DEFAULT 0,
    processed       INTEGER      NOT NULL DEFAULT 0,
    success_count   INTEGER      NOT NULL DEFAULT 0,
    skipped_count   INTEGER      NOT NULL DEFAULT 0,
    error_count     INTEGER      NOT NULL DEFAULT 0,
    errors_json     TEXT,                                  -- JSON-serialised list of {row,field,message,code}
    created_by      VARCHAR(100),                          -- user id / email
    started_at      TIMESTAMP,
    finished_at     TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_import_jobs_status      ON import_jobs(status);
CREATE INDEX IF NOT EXISTS idx_import_jobs_type        ON import_jobs(type);
CREATE INDEX IF NOT EXISTS idx_import_jobs_created_at  ON import_jobs(created_at DESC);
