-- ============================================================
-- V10 : Gestion de Caisse
-- ============================================================

CREATE TABLE IF NOT EXISTS caisses (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    date_ouverture    DATE          NOT NULL,
    date_fermeture    DATE,
    statut            VARCHAR(10)   NOT NULL CHECK (statut IN ('OUVERTE','FERMEE')),
    solde_ouverture   NUMERIC(10,2) NOT NULL DEFAULT 0,
    solde_fermeture   NUMERIC(10,2),
    total_entrees     NUMERIC(10,2) NOT NULL DEFAULT 0,
    total_sorties     NUMERIC(10,2) NOT NULL DEFAULT 0,
    annee_scolaire    VARCHAR(9)    NOT NULL,
    notes             TEXT,
    ouvert_par        VARCHAR(255),
    ferme_par         VARCHAR(255),
    created_at        TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE INDEX idx_caisses_annee  ON caisses(annee_scolaire);
CREATE INDEX idx_caisses_statut ON caisses(statut);

CREATE TABLE IF NOT EXISTS mouvements_caisse (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    caisse_id           UUID        NOT NULL REFERENCES caisses(id) ON DELETE CASCADE,
    type                VARCHAR(10)   NOT NULL CHECK (type IN ('ENTREE','SORTIE')),
    categorie           VARCHAR(25)   NOT NULL,
    montant             NUMERIC(10,2) NOT NULL CHECK (montant > 0),
    libelle             VARCHAR(255)  NOT NULL,
    reference_paiement  VARCHAR(255),
    notes               TEXT,
    created_at          TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE INDEX idx_mouvements_caisse ON mouvements_caisse(caisse_id);
