CREATE TABLE menus (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    date_menu DATE NOT NULL,
    jour_semaine VARCHAR(10) NOT NULL,
    entree VARCHAR(300),
    plat_principal VARCHAR(300) NOT NULL,
    accompagnement VARCHAR(300),
    dessert VARCHAR(300),
    allergenes TEXT[],
    type_regime VARCHAR(20) DEFAULT 'STANDARD' CHECK (type_regime IN ('STANDARD', 'VEGETARIEN', 'SANS_GLUTEN', 'HALAL')),
    semaine INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_menus_date ON menus(date_menu);
CREATE INDEX idx_menus_semaine ON menus(semaine);
