-- V89: traçabilité de l'auteur (created_by / updated_by) sur les entités
-- sensibles — notes, paiements, incidents et sanctions (SEC-025).
-- Les colonnes référencent public.users.id ; pas de contrainte FK
-- inter-schéma, ce sont des colonnes d'audit informatives.

ALTER TABLE notes      ADD COLUMN IF NOT EXISTS created_by UUID;
ALTER TABLE notes      ADD COLUMN IF NOT EXISTS updated_by UUID;

ALTER TABLE paiements  ADD COLUMN IF NOT EXISTS created_by UUID;
ALTER TABLE paiements  ADD COLUMN IF NOT EXISTS updated_by UUID;

ALTER TABLE incidents  ADD COLUMN IF NOT EXISTS created_by UUID;
ALTER TABLE incidents  ADD COLUMN IF NOT EXISTS updated_by UUID;

ALTER TABLE sanctions  ADD COLUMN IF NOT EXISTS created_by UUID;
ALTER TABLE sanctions  ADD COLUMN IF NOT EXISTS updated_by UUID;
