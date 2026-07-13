-- Localisation : coordonnees GPS + horaires d'ouverture configurables
-- pour la carte et le bloc Contact du site vitrine.

ALTER TABLE vitrine_config
    ADD COLUMN contact_latitude  NUMERIC(10, 7),
    ADD COLUMN contact_longitude NUMERIC(10, 7),
    ADD COLUMN contact_hours     VARCHAR(120);

-- Defaults sensibles (Casablanca, jours ouvres standard) pour les tenants existants
UPDATE vitrine_config SET
    contact_latitude  = COALESCE(contact_latitude,  33.5731),
    contact_longitude = COALESCE(contact_longitude, -7.5898),
    contact_hours     = COALESCE(contact_hours,     'Lun - Ven : 8h - 17h');
