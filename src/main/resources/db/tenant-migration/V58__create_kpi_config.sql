CREATE TABLE kpi_config (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nom VARCHAR(100) NOT NULL,
    description TEXT,
    type VARCHAR(20) NOT NULL CHECK (type IN ('TAUX_REUSSITE', 'TAUX_PRESENCE', 'TAUX_PAIEMENT', 'MOYENNE_GENERALE', 'CUSTOM')),
    valeur_cible NUMERIC(10,2),
    seuil_alerte NUMERIC(10,2),
    actif BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO kpi_config (nom, type, valeur_cible, seuil_alerte) VALUES
('Taux de réussite', 'TAUX_REUSSITE', 80, 60),
('Taux de présence', 'TAUX_PRESENCE', 95, 85),
('Taux de paiement', 'TAUX_PAIEMENT', 90, 70),
('Moyenne générale', 'MOYENNE_GENERALE', 12, 10);
