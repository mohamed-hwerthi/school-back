-- V78: enrich modules table with timetable solver attributes.
-- These columns are consumed by the OptaPlanner solver to enforce
-- room-type matching, double-period requirements, and morning preferences.

ALTER TABLE modules
    ADD COLUMN IF NOT EXISTS salle_type_requise VARCHAR(30) NOT NULL DEFAULT 'NORMAL',
    ADD COLUMN IF NOT EXISTS duree_min_seance   INTEGER     NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS duree_max_seance   INTEGER     NOT NULL DEFAULT 2,
    ADD COLUMN IF NOT EXISTS preference_horaire VARCHAR(20) NOT NULL DEFAULT 'INDIFFERENT';

ALTER TABLE modules
    ADD CONSTRAINT modules_salle_type_requise_chk
        CHECK (salle_type_requise IN ('NORMAL', 'LABO_SVT', 'LABO_PHYSIQUE', 'INFO', 'GYMNASE', 'ARTS', 'MUSIQUE')),
    ADD CONSTRAINT modules_preference_horaire_chk
        CHECK (preference_horaire IN ('MATIN', 'APRES_MIDI', 'INDIFFERENT')),
    ADD CONSTRAINT modules_duree_seance_chk
        CHECK (duree_min_seance >= 1 AND duree_max_seance >= duree_min_seance);
