-- Catégories de dépenses
CREATE TABLE IF NOT EXISTS categories_depense (
    id              UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    nom             VARCHAR(255)    NOT NULL,
    type            VARCHAR(10)     NOT NULL DEFAULT 'VARIABLE' CHECK (type IN ('FIXE', 'VARIABLE')),
    description     TEXT,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Dépenses (sorties financières)
CREATE TABLE IF NOT EXISTS depenses (
    id              UUID       PRIMARY KEY DEFAULT gen_random_uuid(),
    categorie_id    UUID          NOT NULL REFERENCES categories_depense(id) ON DELETE RESTRICT,
    libelle         VARCHAR(255)    NOT NULL,
    montant         DECIMAL(10,2)   NOT NULL,
    date_depense    DATE            NOT NULL,
    mode_paiement   VARCHAR(20)     CHECK (mode_paiement IN ('ESPECES', 'VIREMENT', 'CHEQUE', 'CARTE_BANCAIRE', 'PRELEVEMENT')),
    fournisseur     VARCHAR(255),
    reference       VARCHAR(255)    UNIQUE,
    recurrente      BOOLEAN         NOT NULL DEFAULT false,
    notes           TEXT,
    annee_scolaire  VARCHAR(9)      NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Index
CREATE INDEX idx_depenses_annee_scolaire ON depenses(annee_scolaire);
CREATE INDEX idx_depenses_categorie_id ON depenses(categorie_id);
CREATE INDEX idx_depenses_date ON depenses(date_depense);

-- Catégories prédéfinies
INSERT INTO categories_depense (nom, type, description) VALUES
    ('Salaires enseignants', 'FIXE', 'Rémunération mensuelle des enseignants'),
    ('Salaires personnel', 'FIXE', 'Rémunération du personnel administratif et technique'),
    ('Loyer & charges', 'FIXE', 'Loyer des locaux scolaires'),
    ('Électricité / Eau / Internet', 'FIXE', 'Factures des services publics'),
    ('Matériel pédagogique', 'VARIABLE', 'Achat de livres, tableaux, matériel didactique'),
    ('Maintenance locaux', 'VARIABLE', 'Réparations et entretien des bâtiments'),
    ('Transport scolaire', 'FIXE', 'Carburant, entretien bus, chauffeur'),
    ('Assurances', 'FIXE', 'Assurance scolaire et responsabilité civile'),
    ('Fournitures bureau', 'VARIABLE', 'Papeterie, impressions, consommables'),
    ('Autres', 'VARIABLE', 'Dépenses diverses non catégorisées');
