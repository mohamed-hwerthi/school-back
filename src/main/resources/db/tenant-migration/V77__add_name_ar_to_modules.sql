-- Ajout du nom arabe sur les modules — affiché dans le bulletin (RTL).

ALTER TABLE modules ADD COLUMN IF NOT EXISTS name_ar VARCHAR(255);

-- Mapping des noms les plus courants (Tunisie, primaire). Ne touche pas
-- les valeurs déjà saisies.
UPDATE modules SET name_ar = CASE name
    WHEN 'Mathématiques'        THEN 'الرياضيات'
    WHEN 'Français'             THEN 'الفرنسية'
    WHEN 'Arabe'                THEN 'العربية'
    WHEN 'Sciences'             THEN 'العلوم'
    WHEN 'Histoire-Géographie'  THEN 'التاريخ والجغرافيا'
    WHEN 'Histoire'             THEN 'التاريخ'
    WHEN 'Géographie'           THEN 'الجغرافيا'
    WHEN 'Éducation Islamique'  THEN 'التربية الإسلامية'
    WHEN 'Éducation Physique'   THEN 'التربية البدنية'
    WHEN 'Anglais'              THEN 'الإنجليزية'
    WHEN 'Éducation Civique'    THEN 'التربية المدنية'
    WHEN 'Éducation Musicale'   THEN 'التربية الموسيقية'
    WHEN 'Éducation Plastique'  THEN 'التربية التشكيلية'
    WHEN 'Éveil Scientifique'   THEN 'الإيقاظ العلمي'
    WHEN 'Production Écrite'    THEN 'الإنتاج الكتابي'
    WHEN 'Lecture'              THEN 'القراءة'
    WHEN 'Écriture'             THEN 'الخط والإملاء'
    ELSE name_ar
END
WHERE name_ar IS NULL;
