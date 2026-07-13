CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(30) NOT NULL DEFAULT 'INFO' CHECK (type IN ('INFO', 'WARNING', 'ALERT', 'FINANCE', 'ABSENCE', 'NOTE', 'DISCIPLINE')),
    is_read BOOLEAN DEFAULT FALSE,
    link VARCHAR(500),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE annonces (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titre VARCHAR(200) NOT NULL,
    contenu TEXT NOT NULL,
    type VARCHAR(30) NOT NULL DEFAULT 'GENERAL' CHECK (type IN ('GENERAL', 'URGENT', 'EVENEMENT', 'REUNION')),
    destinataires VARCHAR(50) NOT NULL DEFAULT 'TOUS' CHECK (destinataires IN ('TOUS', 'PARENTS', 'ENSEIGNANTS', 'ELEVES', 'CLASSE')),
    classe_id UUID,
    auteur_id UUID,
    auteur_name VARCHAR(100),
    date_publication TIMESTAMP DEFAULT NOW(),
    date_expiration TIMESTAMP,
    actif BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);
