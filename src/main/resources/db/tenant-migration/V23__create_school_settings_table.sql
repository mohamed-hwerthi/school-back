CREATE TABLE IF NOT EXISTS school_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_name VARCHAR(255) NOT NULL DEFAULT 'École',
    school_name_ar VARCHAR(255),
    annee_scolaire VARCHAR(50) NOT NULL DEFAULT '2025 / 2026',
    adresse TEXT,
    telephone VARCHAR(50),
    directeur_name VARCHAR(255),
    directeur_name_ar VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Seed default school settings
INSERT INTO school_settings (school_name, school_name_ar, annee_scolaire, adresse, telephone, directeur_name, directeur_name_ar)
VALUES (
    'École Primaire Ibn Khaldoun',
    'المدرسة الابتدائية ابن خلدون',
    '2025 / 2026',
    'Rue de la République, 1000 Tunis',
    '+216 71 234 567',
    'Mohamed Ben Ali',
    'محمد بن علي'
)
ON CONFLICT DO NOTHING;
