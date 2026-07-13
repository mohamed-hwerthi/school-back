-- Fix: V63 seeded students with matricules ELV-YYYY-00001..00050 but did not
-- advance matricule_seq, so StudentService.generateMatricule() produced
-- colliding values on the first new inserts. Advance the sequence past the
-- highest existing matricule in this tenant.
SELECT setval('matricule_seq',
    GREATEST((SELECT last_value FROM matricule_seq),
             COALESCE((SELECT MAX(CAST(SUBSTRING(matricule FROM 'ELV-\d{4}-(\d+)') AS INTEGER))
                       FROM students), 0)));
