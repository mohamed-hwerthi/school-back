-- Vitrine (showcase website) tables for each school tenant.

-- Global vitrine configuration (one row per school)
CREATE TABLE vitrine_config (
    id                  UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    school_display_name VARCHAR(255),
    slogan              VARCHAR(500),
    logo_url            VARCHAR(500),
    hero_image_url      VARCHAR(500),
    primary_color       VARCHAR(7)      NOT NULL DEFAULT '#1e40af',
    secondary_color     VARCHAR(7)      NOT NULL DEFAULT '#f59e0b',
    accent_color        VARCHAR(7)      NOT NULL DEFAULT '#10b981',
    theme_template      VARCHAR(20)     NOT NULL DEFAULT 'modern',
    contact_phone       VARCHAR(50),
    contact_email       VARCHAR(255),
    contact_address     TEXT,
    facebook_url        VARCHAR(500),
    instagram_url       VARCHAR(500),
    whatsapp_number     VARCHAR(50),
    meta_description    VARCHAR(500),
    is_published        BOOLEAN         NOT NULL DEFAULT false,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Dynamic pages (Accueil, A propos, Contact, etc.)
CREATE TABLE vitrine_pages (
    id              UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(255)    NOT NULL,
    slug            VARCHAR(255)    NOT NULL UNIQUE,
    display_order   INT             NOT NULL DEFAULT 0,
    visible         BOOLEAN         NOT NULL DEFAULT true,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Sections within pages (hero banner, text blocks, gallery, stats, etc.)
CREATE TABLE vitrine_sections (
    id              UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    page_id         UUID          NOT NULL REFERENCES vitrine_pages(id) ON DELETE CASCADE,
    section_type    VARCHAR(50)     NOT NULL CHECK (section_type IN ('hero','text','gallery','stats','cta','testimonials','map','announcements')),
    title           VARCHAR(255),
    content         JSONB,
    display_order   INT             NOT NULL DEFAULT 0,
    visible         BOOLEAN         NOT NULL DEFAULT true,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Image gallery
CREATE TABLE vitrine_gallery (
    id              UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    image_url       VARCHAR(500)    NOT NULL,
    caption         VARCHAR(500),
    category        VARCHAR(100),
    display_order   INT             NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Public announcements / news
CREATE TABLE vitrine_announcements (
    id              UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(255)    NOT NULL,
    body            TEXT,
    pinned          BOOLEAN         NOT NULL DEFAULT false,
    published       BOOLEAN         NOT NULL DEFAULT true,
    expires_at      TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed default vitrine config
INSERT INTO vitrine_config (school_display_name, slogan, is_published) VALUES ('Notre École', 'L''excellence au service de l''éducation', false);

-- Seed default Accueil page with hero section
INSERT INTO vitrine_pages (title, slug, display_order, visible) VALUES ('Accueil', 'accueil', 0, true);
INSERT INTO vitrine_pages (title, slug, display_order, visible) VALUES ('À propos', 'a-propos', 1, true);
INSERT INTO vitrine_pages (title, slug, display_order, visible) VALUES ('Contact', 'contact', 2, true);

INSERT INTO vitrine_sections (page_id, section_type, title, content, display_order, visible)
VALUES (
    (SELECT id FROM vitrine_pages WHERE slug = 'accueil'),
    'hero',
    'Bienvenue',
    '{"subtitle": "Découvrez notre établissement", "buttonText": "En savoir plus", "buttonLink": "/a-propos"}'::jsonb,
    0,
    true
);

INSERT INTO vitrine_sections (page_id, section_type, title, content, display_order, visible)
VALUES (
    (SELECT id FROM vitrine_pages WHERE slug = 'a-propos'),
    'text',
    'Notre histoire',
    '{"body": "<p>Bienvenue dans notre établissement scolaire. Nous sommes dédiés à offrir une éducation de qualité.</p>"}'::jsonb,
    0,
    true
);

INSERT INTO vitrine_sections (page_id, section_type, title, content, display_order, visible)
VALUES (
    (SELECT id FROM vitrine_pages WHERE slug = 'contact'),
    'map',
    'Nous contacter',
    '{"latitude": 33.5731, "longitude": -7.5898}'::jsonb,
    0,
    true
);
