-- MOB-FUNC-023 : bibliothèque de modèles d'appréciations
-- réutilisables lors de la saisie des appréciations de bulletin.
-- Scope : chaque enseignant possède sa propre liste.

CREATE TABLE appreciation_templates (
    id            UUID         PRIMARY KEY,
    enseignant_id UUID         NOT NULL,
    libelle       VARCHAR(60)  NOT NULL,
    contenu       TEXT         NOT NULL,
    tag           VARCHAR(20)  NOT NULL DEFAULT 'NEUTRE',
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at    TIMESTAMP,
    CONSTRAINT appreciation_templates_tag_check
        CHECK (tag IN ('POSITIF', 'NEUTRE', 'NEGATIF'))
);

CREATE INDEX idx_appreciation_templates_enseignant
    ON appreciation_templates (enseignant_id)
    WHERE deleted = FALSE;
