-- V81: introduce a typed FK from emploi_du_temps to classrooms.
-- The legacy "salle" VARCHAR column is kept during the transition so the
-- existing UI keeps rendering; phase 3 will populate classroom_id
-- via the solver and a later migration will drop "salle".

ALTER TABLE emploi_du_temps
    ADD COLUMN IF NOT EXISTS classroom_id UUID REFERENCES classrooms(id);

CREATE INDEX IF NOT EXISTS idx_edt_classroom ON emploi_du_temps(classroom_id);
