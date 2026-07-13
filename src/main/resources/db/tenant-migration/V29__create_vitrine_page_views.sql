-- Vitrine page view analytics (simple, no external dependencies)
CREATE TABLE vitrine_page_views (
    id              UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    page_slug       VARCHAR(255)    NOT NULL,
    visitor_hash    VARCHAR(64),
    user_agent      VARCHAR(500),
    referer         VARCHAR(500),
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_vitrine_page_views_slug ON vitrine_page_views(page_slug);
CREATE INDEX idx_vitrine_page_views_created ON vitrine_page_views(created_at);
