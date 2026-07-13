-- Délégation régionale de l'éducation (ex : "باجة" / "Béja")
-- Affichée en haut du bulletin officiel et obligatoire à la création
-- d'un nouvel établissement par le super-admin.
ALTER TABLE school_settings
    ADD COLUMN IF NOT EXISTS delegation_regionale       VARCHAR(120),
    ADD COLUMN IF NOT EXISTS delegation_regionale_ar    VARCHAR(120);
