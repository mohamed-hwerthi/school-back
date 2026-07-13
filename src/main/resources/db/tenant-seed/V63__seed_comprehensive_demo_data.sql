-- ================================================================
-- P1-DATA: Comprehensive realistic test data for ALL modules
-- Idempotent: uses ON CONFLICT DO NOTHING throughout
-- ================================================================

-- ── 1. NIVEAUX (3) ─────────────────────────────────────────────
-- V11 already inserts 6 niveaux; these overlap with IDs 1-3.
-- If they exist, DO NOTHING.
INSERT INTO niveaux (name) VALUES
    ('1ère année'),
    ('2ème année'),
    ('3ème année')
ON CONFLICT (name) DO NOTHING;

-- ── 2. CLASSES (6) ──────────────────────────────────────────────
-- V11 already creates some classes; add any missing ones.
INSERT INTO classes (niveau_id, letter)
SELECT n.id, v.letter
FROM (VALUES ('1ère année','A'),('1ère année','B'),
             ('2ème année','A'),('2ème année','B'),
             ('3ème année','A'),('3ème année','B')) AS v(niveau_name, letter)
JOIN niveaux n ON n.name = v.niveau_name
ON CONFLICT (niveau_id, letter) DO NOTHING;

-- ── 3. MODULES / MATIERES (8 per niveau 1ère année) ────────────
-- We create 8 core modules for '1ère année'.
-- Using a DO block to look up the niveau_id dynamically.
DO $$
DECLARE
    v_niveau_1_id UUID;
    v_niveau_2_id UUID;
BEGIN
    SELECT id INTO v_niveau_1_id FROM niveaux WHERE name = '1ère année';
    SELECT id INTO v_niveau_2_id FROM niveaux WHERE name = '2ème année';

    IF v_niveau_1_id IS NOT NULL THEN
        INSERT INTO modules (name, coeff_etatique, coeff_prive, ordre_etatique, ordre_prive, niveau_id, version_etatique, version_privee)
        VALUES
            ('Mathématiques',          2, 3, 1, 1, v_niveau_1_id, true, true),
            ('Français',               2, 2, 2, 2, v_niveau_1_id, true, true),
            ('Arabe',                  2, 2, 3, 3, v_niveau_1_id, true, true),
            ('Sciences',               1, 1, 4, 4, v_niveau_1_id, true, true),
            ('Histoire-Géographie',    1, 1, 5, 5, v_niveau_1_id, true, true),
            ('Éducation Islamique',    1, 1, 6, 6, v_niveau_1_id, true, true),
            ('Éducation Physique',     1, 1, 7, 7, v_niveau_1_id, true, true),
            ('Anglais',                1, 2, 8, 8, v_niveau_1_id, true, true)
        ON CONFLICT DO NOTHING;
    END IF;

    -- Same 8 modules for 2ème année
    IF v_niveau_2_id IS NOT NULL THEN
        INSERT INTO modules (name, coeff_etatique, coeff_prive, ordre_etatique, ordre_prive, niveau_id, version_etatique, version_privee)
        VALUES
            ('Mathématiques',          2, 3, 1, 1, v_niveau_2_id, true, true),
            ('Français',               2, 2, 2, 2, v_niveau_2_id, true, true),
            ('Arabe',                  2, 2, 3, 3, v_niveau_2_id, true, true),
            ('Sciences',               1, 1, 4, 4, v_niveau_2_id, true, true),
            ('Histoire-Géographie',    1, 1, 5, 5, v_niveau_2_id, true, true),
            ('Éducation Islamique',    1, 1, 6, 6, v_niveau_2_id, true, true),
            ('Éducation Physique',     1, 1, 7, 7, v_niveau_2_id, true, true),
            ('Anglais',                1, 2, 8, 8, v_niveau_2_id, true, true)
        ON CONFLICT DO NOTHING;
    END IF;
END $$;

-- ── 4. TEACHERS (10) ───────────────────────────────────────────
INSERT INTO teachers (first_name, last_name, email, specialization, sexe, telephone, date_naissance, date_embauche, statut)
VALUES
    ('Hedi',     'Ben Amor',     'hedi.benamor@school.tn',     'Mathématiques',       'M', '20 100 200', '1980-03-15', '2005-09-01', 'Actif'),
    ('Najet',    'Trabelsi',     'najet.trabelsi@school.tn',   'Français',            'F', '20 100 201', '1985-07-22', '2010-09-01', 'Actif'),
    ('Tarek',    'Gharbi',       'tarek.gharbi@school.tn',     'Arabe',               'M', '20 100 202', '1978-11-05', '2003-09-01', 'Actif'),
    ('Sonia',    'Hammami',      'sonia.hammami@school.tn',    'Sciences',            'F', '20 100 203', '1982-01-18', '2007-09-01', 'Actif'),
    ('Ridha',    'Jebali',       'ridha.jebali@school.tn',     'Histoire-Géographie', 'M', '20 100 204', '1975-06-30', '2000-09-01', 'Actif'),
    ('Amel',     'Karoui',       'amel.karoui@school.tn',      'Éducation Islamique', 'F', '20 100 205', '1988-04-12', '2012-09-01', 'Actif'),
    ('Nabil',    'Meddeb',       'nabil.meddeb@school.tn',     'Éducation Physique',  'M', '20 100 206', '1983-09-25', '2008-09-01', 'Actif'),
    ('Olfa',     'Chaabane',     'olfa.chaabane@school.tn',    'Anglais',             'F', '20 100 207', '1990-12-03', '2015-09-01', 'Actif'),
    ('Kamel',    'Sfaxi',        'kamel.sfaxi@school.tn',      'Mathématiques',       'M', '20 100 208', '1977-08-14', '2002-09-01', 'Actif'),
    ('Lamia',    'Bouazizi',     'lamia.bouazizi@school.tn',   'Français',            'F', '20 100 209', '1986-02-28', '2011-09-01', 'Actif')
ON CONFLICT DO NOTHING;

-- ── 5. STUDENTS (50) ────────────────────────────────────────────
-- Distributed across 6 classes: 1A(9), 1B(9), 2A(8), 2B(8), 3A(8), 3B(8)
-- Using IDs > 100 to avoid conflicts with existing seed data (V11 uses 1-10)
INSERT INTO students (first_name, last_name, first_name_ar, last_name_ar, sex, date_of_birth, birth_place, address, classe, niveau, enrollment_date, status, is_blocked, parent_last_name, parent_first_name, parent_phone, parent_email, matricule)
VALUES
    -- ═══ Classe 1A (9 students) ═══
    ('Yassine',  'Ben Ali',      'ياسين',   'بن علي',      'M', '2016-03-12', 'Tunis',     '10 Rue de la Liberté, Tunis',    '1A', '1ère année', '2025-09-15', 'Actif',   false, 'Ben Ali',    'Mourad',   '20 200 001', 'mourad.benali@email.tn',    'ELV-2026-00001'),
    ('Meriem',   'Laabidi',      'مريم',    'العبيدي',     'F', '2016-05-24', 'Sfax',      '22 Av Habib Bourguiba, Sfax',    '1A', '1ère année', '2025-09-15', 'Actif',   false, 'Laabidi',    'Ahmed',    '20 200 002', 'ahmed.laabidi@email.tn',    'ELV-2026-00002'),
    ('Aziz',     'Cherif',       'عزيز',    'الشريف',      'M', '2015-11-08', 'Sousse',    '5 Rue Ibn Khaldoun, Sousse',     '1A', '1ère année', '2025-09-15', 'Actif',   false, 'Cherif',     'Salah',    '20 200 003', 'salah.cherif@email.tn',     'ELV-2026-00003'),
    ('Rania',    'Khelifi',      'رانية',   'الخليفي',     'F', '2016-07-19', 'Bizerte',   '8 Rue de Carthage, Bizerte',     '1A', '1ère année', '2025-09-15', 'Actif',   false, 'Khelifi',    'Hichem',   '20 200 004', 'hichem.khelifi@email.tn',   'ELV-2026-00004'),
    ('Amine',    'Mejri',        'أمين',    'المجري',      'M', '2016-01-30', 'Tunis',     '15 Rue du Lac, Les Berges',      '1A', '1ère année', '2025-09-15', 'Actif',   false, 'Mejri',      'Karim',    '20 200 005', 'karim.mejri@email.tn',      'ELV-2026-00005'),
    ('Nour',     'Sassi',        'نور',     'الساسي',      'F', '2015-09-03', 'Nabeul',    '3 Rue des Jasmins, Nabeul',      '1A', '1ère année', '2025-09-15', 'Actif',   false, 'Sassi',      'Habib',    '20 200 006', 'habib.sassi@email.tn',      'ELV-2026-00006'),
    ('Bilel',    'Ghanmi',       'بلال',    'الغنمي',      'M', '2016-04-17', 'Monastir',  '20 Av de la République, Monastir','1A','1ère année', '2025-09-15', 'Actif',   false, 'Ghanmi',     'Nabil',    '20 200 007', 'nabil.ghanmi@email.tn',     'ELV-2026-00007'),
    ('Eya',      'Riahi',        'آية',     'الرياحي',     'F', '2016-08-26', 'Tunis',     '12 Rue de Marseille, Tunis',     '1A', '1ère année', '2025-09-15', 'Inactif', false, 'Riahi',      'Fethi',    '20 200 008', 'fethi.riahi@email.tn',      'ELV-2026-00008'),
    ('Zied',     'Hamza',        'زياد',    'حمزة',        'M', '2015-12-22', 'Kairouan',  '7 Rue Okba Ibn Nafaa, Kairouan', '1A', '1ère année', '2025-09-15', 'Actif',   false, 'Hamza',      'Ali',      '20 200 009', 'ali.hamza@email.tn',        'ELV-2026-00009'),

    -- ═══ Classe 1B (9 students) ═══
    ('Emna',     'Bouzid',       'آمنة',    'بوزيد',       'F', '2016-02-14', 'Tunis',     '18 Rue de Rome, Tunis',          '1B', '1ère année', '2025-09-15', 'Actif',   false, 'Bouzid',     'Slim',     '20 200 010', 'slim.bouzid@email.tn',      'ELV-2026-00010'),
    ('Hamdi',    'Ayari',        'حمدي',    'العياري',     'M', '2015-10-07', 'Gabes',     '4 Rue El Hana, Gabes',           '1B', '1ère année', '2025-09-15', 'Actif',   false, 'Ayari',      'Mohamed',  '20 200 011', 'mohamed.ayari@email.tn',    'ELV-2026-00011'),
    ('Rahma',    'Tounsi',       'رحمة',    'التونسي',     'F', '2016-06-11', 'Ariana',    '9 Rue de la Paix, Ariana',       '1B', '1ère année', '2025-09-15', 'Actif',   false, 'Tounsi',     'Lotfi',    '20 200 012', 'lotfi.tounsi@email.tn',     'ELV-2026-00012'),
    ('Wael',     'Maalej',       'وائل',    'المعالج',     'M', '2016-03-28', 'Sfax',      '14 Rue de Sfax, Sfax',           '1B', '1ère année', '2025-09-15', 'Actif',   false, 'Maalej',     'Anouar',   '20 200 013', 'anouar.maalej@email.tn',    'ELV-2026-00013'),
    ('Salma',    'Nasri',        'سلمى',    'النصري',      'F', '2015-08-15', 'Tozeur',    '2 Rue des Palmes, Tozeur',       '1B', '1ère année', '2025-09-15', 'Actif',   false, 'Nasri',      'Raouf',    '20 200 014', 'raouf.nasri@email.tn',      'ELV-2026-00014'),
    ('Houssem',  'Dridi',        'حسام',    'الدريدي',     'M', '2016-09-01', 'Tunis',     '25 Av de Paris, Tunis',          '1B', '1ère année', '2025-09-15', 'Actif',   false, 'Dridi',      'Sami',     '20 200 015', 'sami.dridi@email.tn',       'ELV-2026-00015'),
    ('Asma',     'Jerbi',        'أسماء',   'الجربي',      'F', '2016-01-05', 'Djerba',    '6 Rue du Souk, Djerba',          '1B', '1ère année', '2025-09-15', 'Actif',   false, 'Jerbi',      'Ridha',    '20 200 016', 'ridha.jerbi@email.tn',      'ELV-2026-00016'),
    ('Fares',    'Lakhdhar',     'فارس',    'الأخضر',      'M', '2015-07-20', 'Manouba',   '11 Rue Ibn Sina, Manouba',       '1B', '1ère année', '2025-09-15', 'Inactif', false, 'Lakhdhar',   'Bechir',   '20 200 017', 'bechir.lakhdhar@email.tn',  'ELV-2026-00017'),
    ('Lina',     'Kchaou',       'لينا',    'القشاو',      'F', '2016-11-13', 'Sousse',    '16 Rue du Port, Sousse',         '1B', '1ère année', '2025-09-15', 'Actif',   false, 'Kchaou',     'Fathi',    '20 200 018', 'fathi.kchaou@email.tn',     'ELV-2026-00018'),

    -- ═══ Classe 2A (8 students) ═══
    ('Adam',     'Haddad',       'آدم',     'الحداد',      'M', '2015-04-09', 'Tunis',     '30 Rue Alain Savary, Tunis',     '2A', '2ème année', '2024-09-15', 'Actif',   false, 'Haddad',     'Jamel',    '20 200 019', 'jamel.haddad@email.tn',     'ELV-2026-00019'),
    ('Cyrine',   'Mansour',      'سيرين',   'المنصور',     'F', '2015-06-22', 'Bizerte',   '7 Rue du Lac, Bizerte',          '2A', '2ème année', '2024-09-15', 'Actif',   false, 'Mansour',    'Khaled',   '20 200 020', 'khaled.mansour@email.tn',   'ELV-2026-00020'),
    ('Seif',     'Tlili',        'سيف',     'التليلي',     'M', '2015-01-17', 'Sousse',    '21 Rue de la Médina, Sousse',    '2A', '2ème année', '2024-09-15', 'Actif',   false, 'Tlili',      'Omar',     '20 200 021', 'omar.tlili@email.tn',       'ELV-2026-00021'),
    ('Malek',    'Ferchichi',    'مالك',    'الفرشيشي',    'M', '2015-08-30', 'Monastir',  '13 Av de la Corniche, Monastir', '2A', '2ème année', '2024-09-15', 'Actif',   false, 'Ferchichi',  'Adel',     '20 200 022', 'adel.ferchichi@email.tn',   'ELV-2026-00022'),
    ('Aya',      'Belhadj',      'آية',     'بالحاج',      'F', '2015-03-05', 'Nabeul',    '9 Rue des Orangers, Nabeul',     '2A', '2ème année', '2024-09-15', 'Actif',   false, 'Belhadj',    'Tahar',    '20 200 023', 'tahar.belhadj@email.tn',    'ELV-2026-00023'),
    ('Rayen',    'Ouerghi',      'رايان',   'الورغي',      'M', '2015-11-28', 'Tunis',     '17 Rue Charles de Gaulle, Tunis','2A', '2ème année', '2024-09-15', 'Actif',   false, 'Ouerghi',    'Mondher',  '20 200 024', 'mondher.ouerghi@email.tn',  'ELV-2026-00024'),
    ('Israa',    'Romdhane',     'إسراء',   'رمضان',       'F', '2015-05-14', 'Sfax',      '5 Rue El Amine, Sfax',           '2A', '2ème année', '2024-09-15', 'Actif',   false, 'Romdhane',   'Abdelkader','20 200 025','abdelkader.romdhane@email.tn','ELV-2026-00025'),
    ('Youssef',  'Zouari',       'يوسف',    'الزواري',     'M', '2015-09-19', 'Gabes',     '3 Rue El Kef, Gabes',            '2A', '2ème année', '2024-09-15', 'Actif',   false, 'Zouari',     'Hassen',   '20 200 026', 'hassen.zouari@email.tn',    'ELV-2026-00026'),

    -- ═══ Classe 2B (8 students) ═══
    ('Tasnim',   'Mhiri',        'تسنيم',   'المهيري',     'F', '2015-02-08', 'Tunis',     '28 Rue de Hollande, Tunis',      '2B', '2ème année', '2024-09-15', 'Actif',   false, 'Mhiri',      'Walid',    '20 200 027', 'walid.mhiri@email.tn',      'ELV-2026-00027'),
    ('Ghaith',   'Bouslama',     'غيث',     'بوسلامة',     'M', '2015-07-11', 'Ariana',    '19 Rue du Soleil, Ariana',       '2B', '2ème année', '2024-09-15', 'Actif',   false, 'Bouslama',   'Noureddine','20 200 028','noureddine.bouslama@email.tn','ELV-2026-00028'),
    ('Jana',     'Saidi',        'جنى',     'السعيدي',     'F', '2015-10-25', 'Kairouan',  '8 Rue de Tunis, Kairouan',       '2B', '2ème année', '2024-09-15', 'Actif',   false, 'Saidi',      'Youssef',  '20 200 029', 'youssef.saidi@email.tn',    'ELV-2026-00029'),
    ('Mohamed',  'Ghorbel',      'محمد',     'الغربال',     'M', '2015-04-03', 'Sfax',      '14 Rue Habib Thameur, Sfax',     '2B', '2ème année', '2024-09-15', 'Actif',   false, 'Ghorbel',    'Faouzi',   '20 200 030', 'faouzi.ghorbel@email.tn',   'ELV-2026-00030'),
    ('Yasmine',  'Khemiri',      'ياسمين',  'الخميري',     'F', '2015-12-16', 'Tunis',     '6 Rue du 1er Juin, Tunis',       '2B', '2ème année', '2024-09-15', 'Actif',   false, 'Khemiri',    'Moncef',   '20 200 031', 'moncef.khemiri@email.tn',   'ELV-2026-00031'),
    ('Adem',     'Chouchene',    'آدم',     'الشوشان',     'M', '2015-06-29', 'Sousse',    '11 Rue de Paris, Sousse',        '2B', '2ème année', '2024-09-15', 'Actif',   false, 'Chouchene',  'Imed',     '20 200 032', 'imed.chouchene@email.tn',   'ELV-2026-00032'),
    ('Chaima',   'Mbarki',       'شيماء',   'المباركي',    'F', '2015-01-22', 'Monastir',  '23 Rue du Commerce, Monastir',   '2B', '2ème année', '2024-09-15', 'Inactif', false, 'Mbarki',     'Anis',     '20 200 033', 'anis.mbarki@email.tn',      'ELV-2026-00033'),
    ('Louay',    'Bejaoui',      'لؤي',     'البجاوي',     'M', '2015-08-07', 'Beja',      '4 Rue de Beja, Beja',            '2B', '2ème année', '2024-09-15', 'Actif',   false, 'Bejaoui',    'Nizar',    '20 200 034', 'nizar.bejaoui@email.tn',    'ELV-2026-00034'),

    -- ═══ Classe 3A (8 students) ═══
    ('Rayane',   'Souissi',      'ريان',    'السويسي',     'M', '2014-03-21', 'Tunis',     '32 Rue Mohamed V, Tunis',        '3A', '3ème année', '2023-09-15', 'Actif',   false, 'Souissi',    'Mehdi',    '20 200 035', 'mehdi.souissi@email.tn',    'ELV-2026-00035'),
    ('Malak',    'Karray',       'ملاك',    'القراي',      'F', '2014-06-14', 'Sfax',      '15 Rue Hédi Chaker, Sfax',       '3A', '3ème année', '2023-09-15', 'Actif',   false, 'Karray',     'Brahim',   '20 200 036', 'brahim.karray@email.tn',    'ELV-2026-00036'),
    ('Oussama',  'Hadj Ali',     'أسامة',   'حاج علي',     'M', '2014-09-08', 'Bizerte',   '7 Rue du Marché, Bizerte',       '3A', '3ème année', '2023-09-15', 'Actif',   false, 'Hadj Ali',   'Rachid',   '20 200 037', 'rachid.hadjali@email.tn',   'ELV-2026-00037'),
    ('Ghada',    'Ammar',        'غادة',    'عمار',        'F', '2014-01-30', 'Nabeul',    '19 Rue de Hammamet, Nabeul',     '3A', '3ème année', '2023-09-15', 'Actif',   false, 'Ammar',      'Taoufik',  '20 200 038', 'taoufik.ammar@email.tn',    'ELV-2026-00038'),
    ('Anis',     'Baccouche',    'أنيس',    'بكوش',        'M', '2014-11-17', 'Tunis',     '26 Rue de Liège, Tunis',         '3A', '3ème année', '2023-09-15', 'Actif',   false, 'Baccouche',  'Mongi',    '20 200 039', 'mongi.baccouche@email.tn',  'ELV-2026-00039'),
    ('Hela',     'Gueddiche',    'هالة',    'القديش',      'F', '2014-04-05', 'Ariana',    '10 Rue de Mégrine, Ariana',      '3A', '3ème année', '2023-09-15', 'Actif',   false, 'Gueddiche',  'Hafedh',   '20 200 040', 'hafedh.gueddiche@email.tn', 'ELV-2026-00040'),
    ('Skander',  'Maamouri',     'سكندر',   'المعموري',    'M', '2014-07-23', 'Sousse',    '3 Rue de la Gare, Sousse',       '3A', '3ème année', '2023-09-15', 'Actif',   false, 'Maamouri',   'Chokri',   '20 200 041', 'chokri.maamouri@email.tn',  'ELV-2026-00041'),
    ('Sirine',   'Hannachi',     'سيرين',   'الحناشي',     'F', '2014-10-11', 'Manouba',   '8 Rue de Carthage, Manouba',     '3A', '3ème année', '2023-09-15', 'Inactif', false, 'Hannachi',   'Abdelhamid','20 200 042','abdelhamid.hannachi@email.tn','ELV-2026-00042'),

    -- ═══ Classe 3B (8 students) ═══
    ('Islem',    'Mathlouthi',   'إسلام',   'المثلوثي',    'M', '2014-02-19', 'Tunis',     '14 Rue de Londres, Tunis',       '3B', '3ème année', '2023-09-15', 'Actif',   false, 'Mathlouthi', 'Moez',     '20 200 043', 'moez.mathlouthi@email.tn',  'ELV-2026-00043'),
    ('Ines',     'Mekni',        'إيناس',   'المكني',      'F', '2014-05-28', 'Sfax',      '22 Rue de la Médina, Sfax',      '3B', '3ème année', '2023-09-15', 'Actif',   false, 'Mekni',      'Fethi',    '20 200 044', 'fethi.mekni@email.tn',      'ELV-2026-00044'),
    ('Fedi',     'Trabelsi',     'فادي',    'الطرابلسي',   'M', '2014-08-06', 'Gabes',     '5 Av Farhat Hached, Gabes',      '3B', '3ème année', '2023-09-15', 'Actif',   false, 'Trabelsi',   'Riadh',    '20 200 045', 'riadh.trabelsi@email.tn',   'ELV-2026-00045'),
    ('Rym',      'Dhaouadi',     'ريم',     'الذوادي',     'F', '2014-12-15', 'Kairouan',  '17 Rue de la Mosquée, Kairouan', '3B', '3ème année', '2023-09-15', 'Actif',   false, 'Dhaouadi',   'Lassaad',  '20 200 046', 'lassaad.dhaouadi@email.tn', 'ELV-2026-00046'),
    ('Moez',     'Achour',       'معز',     'عاشور',       'M', '2014-04-27', 'Tunis',     '29 Rue du 18 Janvier, Tunis',    '3B', '3ème année', '2023-09-15', 'Actif',   false, 'Achour',     'Hedi',     '20 200 047', 'hedi.achour@email.tn',      'ELV-2026-00047'),
    ('Dorra',    'Belhaj',       'درة',     'بالحاج',      'F', '2014-07-09', 'Sousse',    '6 Rue de la Plage, Sousse',      '3B', '3ème année', '2023-09-15', 'Actif',   false, 'Belhaj',     'Noureddine','20 200 048','noureddine.belhaj@email.tn','ELV-2026-00048'),
    ('Ayoub',    'Turki',        'أيوب',    'التركي',      'M', '2014-10-31', 'Bizerte',   '12 Rue du Port, Bizerte',        '3B', '3ème année', '2023-09-15', 'Actif',   false, 'Turki',      'Wissem',   '20 200 049', 'wissem.turki@email.tn',     'ELV-2026-00049'),
    ('Farah',    'Gasmi',        'فرح',     'القاسمي',     'F', '2014-01-14', 'Ariana',    '20 Rue du Jardin, Ariana',       '3B', '3ème année', '2023-09-15', 'Actif',   false, 'Gasmi',      'Khaled',   '20 200 050', 'khaled.gasmi@email.tn',     'ELV-2026-00050')
ON CONFLICT DO NOTHING;

-- ── 6. CRENEAUX ─────────────────────────────────────────────────
-- V13 already seeds creneaux. We just ensure idempotency.
-- The creneaux from V13 are sufficient (9 slots including pause/rec).

-- ── 7. EXAMENS (12 across Maths+Francais for classes 1A, 2A) ───
DO $$
DECLARE
    v_classe_1a_id UUID;
    v_classe_2a_id UUID;
    v_mod_maths_n1 UUID;
    v_mod_franc_n1 UUID;
    v_mod_maths_n2 UUID;
    v_mod_franc_n2 UUID;
    v_teacher_maths UUID;
    v_teacher_franc UUID;
    -- students arrays
    v_student_ids_1a UUID[];
    v_student_ids_2a UUID[];
    v_sid UUID;
    v_eid UUID;
    v_val DOUBLE PRECISION;
    v_counter INTEGER;
    -- examen IDs
    v_ex_ctrl1_t1_maths_1a UUID;
    v_ex_exam_t1_maths_1a UUID;
    v_ex_ctrl1_t2_maths_1a UUID;
    v_ex_exam_t2_maths_1a UUID;
    v_ex_ctrl1_t3_maths_1a UUID;
    v_ex_exam_t3_maths_1a UUID;
    v_ex_ctrl1_t1_franc_1a UUID;
    v_ex_exam_t1_franc_1a UUID;
    v_ex_ctrl1_t2_franc_1a UUID;
    v_ex_exam_t2_franc_1a UUID;
    v_ex_ctrl1_t3_franc_1a UUID;
    v_ex_exam_t3_franc_1a UUID;
    -- 2A examen IDs
    v_ex_ctrl1_t1_maths_2a UUID;
    v_ex_exam_t1_maths_2a UUID;
    v_ex_ctrl1_t1_franc_2a UUID;
    v_ex_exam_t1_franc_2a UUID;
    -- all examen IDs for notes generation
    v_all_examens UUID[];
    v_trimestres INTEGER[];
BEGIN
    -- ── Look up class IDs ──
    SELECT c.id INTO v_classe_1a_id
    FROM classes c JOIN niveaux n ON c.niveau_id = n.id
    WHERE n.name = '1ère année' AND c.letter = 'A';

    SELECT c.id INTO v_classe_2a_id
    FROM classes c JOIN niveaux n ON c.niveau_id = n.id
    WHERE n.name = '2ème année' AND c.letter = 'A';

    -- ── Look up module IDs ──
    SELECT m.id INTO v_mod_maths_n1
    FROM modules m JOIN niveaux n ON m.niveau_id = n.id
    WHERE m.name = 'Mathématiques' AND n.name = '1ère année'
    LIMIT 1;

    SELECT m.id INTO v_mod_franc_n1
    FROM modules m JOIN niveaux n ON m.niveau_id = n.id
    WHERE m.name = 'Français' AND n.name = '1ère année'
    LIMIT 1;

    SELECT m.id INTO v_mod_maths_n2
    FROM modules m JOIN niveaux n ON m.niveau_id = n.id
    WHERE m.name = 'Mathématiques' AND n.name = '2ème année'
    LIMIT 1;

    SELECT m.id INTO v_mod_franc_n2
    FROM modules m JOIN niveaux n ON m.niveau_id = n.id
    WHERE m.name = 'Français' AND n.name = '2ème année'
    LIMIT 1;

    -- ── Look up teacher IDs ──
    SELECT id INTO v_teacher_maths FROM teachers WHERE specialization = 'Mathématiques' LIMIT 1;
    SELECT id INTO v_teacher_franc FROM teachers WHERE specialization = 'Français' LIMIT 1;

    -- Guard: skip if data not found
    IF v_classe_1a_id IS NULL OR v_mod_maths_n1 IS NULL THEN
        RAISE NOTICE 'Required base data not found, skipping examen/note seeding';
        RETURN;
    END IF;

    -- ══════════════════════════════════════════════════════════════
    -- EXAMENS for class 1A: Maths (3 trimestres x 2 = 6) + Francais (6) = 12
    -- ══════════════════════════════════════════════════════════════

    -- Maths 1A
    INSERT INTO examens (name, coeff_etatique, coeff_prive, ordre_etatique, ordre_prive, classe_id, teacher_id, module_id, date_limite_saisie, version_etatique, version_privee)
    VALUES ('Contrôle 1 T1', 1, 1, 1, 1, v_classe_1a_id, v_teacher_maths, v_mod_maths_n1, '2025-11-15', true, true)
    RETURNING id INTO v_ex_ctrl1_t1_maths_1a;

    INSERT INTO examens (name, coeff_etatique, coeff_prive, ordre_etatique, ordre_prive, classe_id, teacher_id, module_id, date_limite_saisie, version_etatique, version_privee)
    VALUES ('Examen T1', 2, 2, 2, 2, v_classe_1a_id, v_teacher_maths, v_mod_maths_n1, '2025-12-20', true, true)
    RETURNING id INTO v_ex_exam_t1_maths_1a;

    INSERT INTO examens (name, coeff_etatique, coeff_prive, ordre_etatique, ordre_prive, classe_id, teacher_id, module_id, date_limite_saisie, version_etatique, version_privee)
    VALUES ('Contrôle 1 T2', 1, 1, 1, 1, v_classe_1a_id, v_teacher_maths, v_mod_maths_n1, '2026-02-15', true, true)
    RETURNING id INTO v_ex_ctrl1_t2_maths_1a;

    INSERT INTO examens (name, coeff_etatique, coeff_prive, ordre_etatique, ordre_prive, classe_id, teacher_id, module_id, date_limite_saisie, version_etatique, version_privee)
    VALUES ('Examen T2', 2, 2, 2, 2, v_classe_1a_id, v_teacher_maths, v_mod_maths_n1, '2026-03-20', true, true)
    RETURNING id INTO v_ex_exam_t2_maths_1a;

    INSERT INTO examens (name, coeff_etatique, coeff_prive, ordre_etatique, ordre_prive, classe_id, teacher_id, module_id, date_limite_saisie, version_etatique, version_privee)
    VALUES ('Contrôle 1 T3', 1, 1, 1, 1, v_classe_1a_id, v_teacher_maths, v_mod_maths_n1, '2026-05-10', true, true)
    RETURNING id INTO v_ex_ctrl1_t3_maths_1a;

    INSERT INTO examens (name, coeff_etatique, coeff_prive, ordre_etatique, ordre_prive, classe_id, teacher_id, module_id, date_limite_saisie, version_etatique, version_privee)
    VALUES ('Examen T3', 2, 2, 2, 2, v_classe_1a_id, v_teacher_maths, v_mod_maths_n1, '2026-06-15', true, true)
    RETURNING id INTO v_ex_exam_t3_maths_1a;

    -- Francais 1A
    INSERT INTO examens (name, coeff_etatique, coeff_prive, ordre_etatique, ordre_prive, classe_id, teacher_id, module_id, date_limite_saisie, version_etatique, version_privee)
    VALUES ('Contrôle 1 T1', 1, 1, 1, 1, v_classe_1a_id, v_teacher_franc, v_mod_franc_n1, '2025-11-15', true, true)
    RETURNING id INTO v_ex_ctrl1_t1_franc_1a;

    INSERT INTO examens (name, coeff_etatique, coeff_prive, ordre_etatique, ordre_prive, classe_id, teacher_id, module_id, date_limite_saisie, version_etatique, version_privee)
    VALUES ('Examen T1', 2, 2, 2, 2, v_classe_1a_id, v_teacher_franc, v_mod_franc_n1, '2025-12-20', true, true)
    RETURNING id INTO v_ex_exam_t1_franc_1a;

    INSERT INTO examens (name, coeff_etatique, coeff_prive, ordre_etatique, ordre_prive, classe_id, teacher_id, module_id, date_limite_saisie, version_etatique, version_privee)
    VALUES ('Contrôle 1 T2', 1, 1, 1, 1, v_classe_1a_id, v_teacher_franc, v_mod_franc_n1, '2026-02-15', true, true)
    RETURNING id INTO v_ex_ctrl1_t2_franc_1a;

    INSERT INTO examens (name, coeff_etatique, coeff_prive, ordre_etatique, ordre_prive, classe_id, teacher_id, module_id, date_limite_saisie, version_etatique, version_privee)
    VALUES ('Examen T2', 2, 2, 2, 2, v_classe_1a_id, v_teacher_franc, v_mod_franc_n1, '2026-03-20', true, true)
    RETURNING id INTO v_ex_exam_t2_franc_1a;

    INSERT INTO examens (name, coeff_etatique, coeff_prive, ordre_etatique, ordre_prive, classe_id, teacher_id, module_id, date_limite_saisie, version_etatique, version_privee)
    VALUES ('Contrôle 1 T3', 1, 1, 1, 1, v_classe_1a_id, v_teacher_franc, v_mod_franc_n1, '2026-05-10', true, true)
    RETURNING id INTO v_ex_ctrl1_t3_franc_1a;

    INSERT INTO examens (name, coeff_etatique, coeff_prive, ordre_etatique, ordre_prive, classe_id, teacher_id, module_id, date_limite_saisie, version_etatique, version_privee)
    VALUES ('Examen T3', 2, 2, 2, 2, v_classe_1a_id, v_teacher_franc, v_mod_franc_n1, '2026-06-15', true, true)
    RETURNING id INTO v_ex_exam_t3_franc_1a;

    -- Maths 2A (T1 only for variety)
    IF v_classe_2a_id IS NOT NULL AND v_mod_maths_n2 IS NOT NULL THEN
        INSERT INTO examens (name, coeff_etatique, coeff_prive, ordre_etatique, ordre_prive, classe_id, teacher_id, module_id, date_limite_saisie, version_etatique, version_privee)
        VALUES ('Contrôle 1 T1', 1, 1, 1, 1, v_classe_2a_id, v_teacher_maths, v_mod_maths_n2, '2025-11-15', true, true)
        RETURNING id INTO v_ex_ctrl1_t1_maths_2a;

        INSERT INTO examens (name, coeff_etatique, coeff_prive, ordre_etatique, ordre_prive, classe_id, teacher_id, module_id, date_limite_saisie, version_etatique, version_privee)
        VALUES ('Examen T1', 2, 2, 2, 2, v_classe_2a_id, v_teacher_maths, v_mod_maths_n2, '2025-12-20', true, true)
        RETURNING id INTO v_ex_exam_t1_maths_2a;
    END IF;

    -- Francais 2A (T1 only)
    IF v_classe_2a_id IS NOT NULL AND v_mod_franc_n2 IS NOT NULL THEN
        INSERT INTO examens (name, coeff_etatique, coeff_prive, ordre_etatique, ordre_prive, classe_id, teacher_id, module_id, date_limite_saisie, version_etatique, version_privee)
        VALUES ('Contrôle 1 T1', 1, 1, 1, 1, v_classe_2a_id, v_teacher_franc, v_mod_franc_n2, '2025-11-15', true, true)
        RETURNING id INTO v_ex_ctrl1_t1_franc_2a;

        INSERT INTO examens (name, coeff_etatique, coeff_prive, ordre_etatique, ordre_prive, classe_id, teacher_id, module_id, date_limite_saisie, version_etatique, version_privee)
        VALUES ('Examen T1', 2, 2, 2, 2, v_classe_2a_id, v_teacher_franc, v_mod_franc_n2, '2025-12-20', true, true)
        RETURNING id INTO v_ex_exam_t1_franc_2a;
    END IF;

    -- ══════════════════════════════════════════════════════════════
    -- NOTES (200+) for students in classes 1A and 2A
    -- ══════════════════════════════════════════════════════════════

    -- Get student IDs for class 1A
    SELECT ARRAY_AGG(id ORDER BY id) INTO v_student_ids_1a
    FROM students WHERE classe = '1A';

    -- Get student IDs for class 2A
    SELECT ARRAY_AGG(id ORDER BY id) INTO v_student_ids_2a
    FROM students WHERE classe = '2A';

    -- ── Notes for 1A students: T1 Maths (Controle + Examen) ──
    IF v_student_ids_1a IS NOT NULL THEN
        FOREACH v_sid IN ARRAY v_student_ids_1a LOOP
            -- Trimestre 1 Maths
            v_val := 8 + (random() * 11)::int; -- 8-19
            INSERT INTO notes (student_id, examen_id, trimestre, valeur, observation)
            VALUES (v_sid, v_ex_ctrl1_t1_maths_1a, 1, v_val, NULL) ON CONFLICT DO NOTHING;

            v_val := 8 + (random() * 11)::int;
            INSERT INTO notes (student_id, examen_id, trimestre, valeur, observation)
            VALUES (v_sid, v_ex_exam_t1_maths_1a, 1, v_val, NULL) ON CONFLICT DO NOTHING;

            -- Trimestre 1 Francais
            v_val := 8 + (random() * 11)::int;
            INSERT INTO notes (student_id, examen_id, trimestre, valeur, observation)
            VALUES (v_sid, v_ex_ctrl1_t1_franc_1a, 1, v_val, NULL) ON CONFLICT DO NOTHING;

            v_val := 8 + (random() * 11)::int;
            INSERT INTO notes (student_id, examen_id, trimestre, valeur, observation)
            VALUES (v_sid, v_ex_exam_t1_franc_1a, 1, v_val, NULL) ON CONFLICT DO NOTHING;

            -- Trimestre 2 Maths
            v_val := 8 + (random() * 11)::int;
            INSERT INTO notes (student_id, examen_id, trimestre, valeur, observation)
            VALUES (v_sid, v_ex_ctrl1_t2_maths_1a, 2, v_val, NULL) ON CONFLICT DO NOTHING;

            v_val := 8 + (random() * 11)::int;
            INSERT INTO notes (student_id, examen_id, trimestre, valeur, observation)
            VALUES (v_sid, v_ex_exam_t2_maths_1a, 2, v_val, NULL) ON CONFLICT DO NOTHING;

            -- Trimestre 2 Francais
            v_val := 8 + (random() * 11)::int;
            INSERT INTO notes (student_id, examen_id, trimestre, valeur, observation)
            VALUES (v_sid, v_ex_ctrl1_t2_franc_1a, 2, v_val, NULL) ON CONFLICT DO NOTHING;

            v_val := 8 + (random() * 11)::int;
            INSERT INTO notes (student_id, examen_id, trimestre, valeur, observation)
            VALUES (v_sid, v_ex_exam_t2_franc_1a, 2, v_val, NULL) ON CONFLICT DO NOTHING;

            -- Trimestre 3 Maths
            v_val := 8 + (random() * 11)::int;
            INSERT INTO notes (student_id, examen_id, trimestre, valeur, observation)
            VALUES (v_sid, v_ex_ctrl1_t3_maths_1a, 3, v_val, NULL) ON CONFLICT DO NOTHING;

            v_val := 8 + (random() * 11)::int;
            INSERT INTO notes (student_id, examen_id, trimestre, valeur, observation)
            VALUES (v_sid, v_ex_exam_t3_maths_1a, 3, v_val, NULL) ON CONFLICT DO NOTHING;

            -- Trimestre 3 Francais
            v_val := 8 + (random() * 11)::int;
            INSERT INTO notes (student_id, examen_id, trimestre, valeur, observation)
            VALUES (v_sid, v_ex_ctrl1_t3_franc_1a, 3, v_val, NULL) ON CONFLICT DO NOTHING;

            v_val := 8 + (random() * 11)::int;
            INSERT INTO notes (student_id, examen_id, trimestre, valeur, observation)
            VALUES (v_sid, v_ex_exam_t3_franc_1a, 3, v_val, NULL) ON CONFLICT DO NOTHING;
        END LOOP;
    END IF;

    -- ── Notes for 2A students: T1 Maths + Francais ──
    IF v_student_ids_2a IS NOT NULL AND v_ex_ctrl1_t1_maths_2a IS NOT NULL THEN
        FOREACH v_sid IN ARRAY v_student_ids_2a LOOP
            v_val := 8 + (random() * 11)::int;
            INSERT INTO notes (student_id, examen_id, trimestre, valeur, observation)
            VALUES (v_sid, v_ex_ctrl1_t1_maths_2a, 1, v_val, NULL) ON CONFLICT DO NOTHING;

            v_val := 8 + (random() * 11)::int;
            INSERT INTO notes (student_id, examen_id, trimestre, valeur, observation)
            VALUES (v_sid, v_ex_exam_t1_maths_2a, 1, v_val, NULL) ON CONFLICT DO NOTHING;

            v_val := 8 + (random() * 11)::int;
            INSERT INTO notes (student_id, examen_id, trimestre, valeur, observation)
            VALUES (v_sid, v_ex_ctrl1_t1_franc_2a, 1, v_val, NULL) ON CONFLICT DO NOTHING;

            v_val := 8 + (random() * 11)::int;
            INSERT INTO notes (student_id, examen_id, trimestre, valeur, observation)
            VALUES (v_sid, v_ex_exam_t1_franc_2a, 1, v_val, NULL) ON CONFLICT DO NOTHING;
        END LOOP;
    END IF;

    -- ══════════════════════════════════════════════════════════════
    -- ABSENCES (30) - mix of justified/unjustified, ABSENCE/RETARD
    -- ══════════════════════════════════════════════════════════════

    IF v_student_ids_1a IS NOT NULL AND array_length(v_student_ids_1a, 1) >= 9 THEN
        INSERT INTO absences (eleve_id, date, type, seance, heure_arrivee, justifie, motif, enseignant_id)
        VALUES
            -- Student 1 (1A) - 3 absences
            (v_student_ids_1a[1], '2025-10-07', 'ABSENCE', 'Séance 1', NULL,    true,  'Visite médicale',                       v_teacher_maths),
            (v_student_ids_1a[1], '2025-11-14', 'RETARD',  'Séance 1', '08:25', false, NULL,                                    v_teacher_maths),
            (v_student_ids_1a[1], '2026-01-20', 'ABSENCE', 'Séance 3', NULL,    true,  'Maladie - certificat médical',          v_teacher_franc),
            -- Student 2 (1A) - 2 absences
            (v_student_ids_1a[2], '2025-10-15', 'ABSENCE', 'Séance 2', NULL,    false, NULL,                                    v_teacher_franc),
            (v_student_ids_1a[2], '2025-12-03', 'RETARD',  'Séance 1', '08:15', true,  'Panne de bus scolaire',                 v_teacher_maths),
            -- Student 3 (1A) - 4 absences
            (v_student_ids_1a[3], '2025-10-21', 'ABSENCE', 'Séance 4', NULL,    false, NULL,                                    v_teacher_franc),
            (v_student_ids_1a[3], '2025-11-04', 'ABSENCE', 'Séance 1', NULL,    false, NULL,                                    v_teacher_maths),
            (v_student_ids_1a[3], '2025-12-16', 'RETARD',  'Séance 2', '09:20', false, NULL,                                    v_teacher_franc),
            (v_student_ids_1a[3], '2026-01-13', 'ABSENCE', 'Séance 3', NULL,    true,  'Rendez-vous dentiste',                  v_teacher_maths),
            -- Student 4 (1A) - 1 absence
            (v_student_ids_1a[4], '2025-11-25', 'ABSENCE', 'Séance 1', NULL,    true,  'Fête familiale',                        v_teacher_maths),
            -- Student 5 (1A) - 3 absences
            (v_student_ids_1a[5], '2025-10-09', 'RETARD',  'Séance 1', '08:30', false, NULL,                                    v_teacher_maths),
            (v_student_ids_1a[5], '2025-11-18', 'ABSENCE', 'Séance 5', NULL,    false, NULL,                                    v_teacher_franc),
            (v_student_ids_1a[5], '2026-02-03', 'ABSENCE', 'Séance 2', NULL,    true,  'Maladie - grippe',                      v_teacher_franc),
            -- Student 6 (1A) - 2 absences
            (v_student_ids_1a[6], '2025-12-01', 'RETARD',  'Séance 1', '08:10', true,  'Embouteillage route scolaire',          v_teacher_maths),
            (v_student_ids_1a[6], '2026-01-27', 'ABSENCE', 'Séance 4', NULL,    false, NULL,                                    v_teacher_franc),
            -- Student 7 (1A) - 5 absences (problematic student)
            (v_student_ids_1a[7], '2025-10-02', 'ABSENCE', 'Séance 1', NULL,    false, NULL,                                    v_teacher_maths),
            (v_student_ids_1a[7], '2025-10-16', 'ABSENCE', 'Séance 2', NULL,    false, NULL,                                    v_teacher_franc),
            (v_student_ids_1a[7], '2025-11-06', 'RETARD',  'Séance 1', '08:45', false, NULL,                                    v_teacher_maths),
            (v_student_ids_1a[7], '2025-12-09', 'ABSENCE', 'Séance 3', NULL,    false, NULL,                                    v_teacher_franc),
            (v_student_ids_1a[7], '2026-01-22', 'ABSENCE', 'Séance 1', NULL,    true,  'Hospitalisation',                       v_teacher_maths),
            -- Student 8 (1A) - 1 absence
            (v_student_ids_1a[8], '2025-11-12', 'RETARD',  'Séance 1', '08:20', true,  'Problème de transport',                 v_teacher_franc)
        ON CONFLICT DO NOTHING;
    END IF;

    -- More absences for 2A students
    IF v_student_ids_2a IS NOT NULL AND array_length(v_student_ids_2a, 1) >= 8 THEN
        INSERT INTO absences (eleve_id, date, type, seance, heure_arrivee, justifie, motif, enseignant_id)
        VALUES
            (v_student_ids_2a[1], '2025-10-14', 'ABSENCE', 'Séance 1', NULL,    true,  'Maladie',                               v_teacher_maths),
            (v_student_ids_2a[1], '2025-12-10', 'RETARD',  'Séance 2', '09:15', false, NULL,                                    v_teacher_franc),
            (v_student_ids_2a[2], '2025-11-03', 'ABSENCE', 'Séance 4', NULL,    false, NULL,                                    v_teacher_franc),
            (v_student_ids_2a[3], '2025-10-28', 'RETARD',  'Séance 1', '08:35', false, NULL,                                    v_teacher_maths),
            (v_student_ids_2a[3], '2026-01-15', 'ABSENCE', 'Séance 3', NULL,    true,  'Consultation ophtalmologue',            v_teacher_maths),
            (v_student_ids_2a[4], '2025-11-20', 'ABSENCE', 'Séance 2', NULL,    false, NULL,                                    v_teacher_franc),
            (v_student_ids_2a[5], '2025-12-18', 'RETARD',  'Séance 1', '08:40', true,  'Panne de voiture du père',              v_teacher_maths),
            (v_student_ids_2a[6], '2026-02-05', 'ABSENCE', 'Séance 1', NULL,    false, NULL,                                    v_teacher_franc),
            (v_student_ids_2a[7], '2025-10-23', 'ABSENCE', 'Séance 5', NULL,    true,  'Voyage familial autorisé',              v_teacher_franc)
        ON CONFLICT DO NOTHING;
    END IF;

    -- ══════════════════════════════════════════════════════════════
    -- EMPLOI DU TEMPS (30 entries for 1A and 2A, Mon-Fri)
    -- ══════════════════════════════════════════════════════════════

    -- Look up creneau IDs by label
    -- creneaux from V13: Séance 1(08-09), Séance 2(09-10), Récréation(10-10:15),
    --   Séance 3(10:15-11:15), Séance 4(11:15-12:15), Pause déjeuner,
    --   Séance 5(14-15), Séance 6(15-16), Séance 7(16-17)

    -- Look up module IDs for 1ère année
    DECLARE
        v_cr1 UUID; v_cr2 UUID; v_cr3 UUID; v_cr4 UUID;
        v_cr5 UUID; v_cr6 UUID;
        v_mod_arabe_n1 UUID; v_mod_sciences_n1 UUID;
        v_mod_histgeo_n1 UUID; v_mod_edislam_n1 UUID;
        v_mod_edphys_n1 UUID; v_mod_anglais_n1 UUID;
        v_mod_arabe_n2 UUID; v_mod_sciences_n2 UUID;
        v_mod_histgeo_n2 UUID; v_mod_edislam_n2 UUID;
        v_mod_edphys_n2 UUID; v_mod_anglais_n2 UUID;
        v_teacher_arabe UUID; v_teacher_sciences UUID;
        v_teacher_histgeo UUID; v_teacher_edislam UUID;
        v_teacher_edphys UUID; v_teacher_anglais UUID;
    BEGIN
        SELECT id INTO v_cr1 FROM creneaux WHERE label = 'Séance 1' LIMIT 1;
        SELECT id INTO v_cr2 FROM creneaux WHERE label = 'Séance 2' LIMIT 1;
        SELECT id INTO v_cr3 FROM creneaux WHERE label = 'Séance 3' LIMIT 1;
        SELECT id INTO v_cr4 FROM creneaux WHERE label = 'Séance 4' LIMIT 1;
        SELECT id INTO v_cr5 FROM creneaux WHERE label = 'Séance 5' LIMIT 1;
        SELECT id INTO v_cr6 FROM creneaux WHERE label = 'Séance 6' LIMIT 1;

        IF v_cr1 IS NULL THEN
            RAISE NOTICE 'Creneaux not found, skipping emploi du temps';
            RETURN;
        END IF;

        -- Modules for niveau 1
        SELECT m.id INTO v_mod_arabe_n1     FROM modules m JOIN niveaux n ON m.niveau_id = n.id WHERE m.name = 'Arabe'               AND n.name = '1ère année' LIMIT 1;
        SELECT m.id INTO v_mod_sciences_n1  FROM modules m JOIN niveaux n ON m.niveau_id = n.id WHERE m.name = 'Sciences'            AND n.name = '1ère année' LIMIT 1;
        SELECT m.id INTO v_mod_histgeo_n1   FROM modules m JOIN niveaux n ON m.niveau_id = n.id WHERE m.name = 'Histoire-Géographie' AND n.name = '1ère année' LIMIT 1;
        SELECT m.id INTO v_mod_edislam_n1   FROM modules m JOIN niveaux n ON m.niveau_id = n.id WHERE m.name = 'Éducation Islamique' AND n.name = '1ère année' LIMIT 1;
        SELECT m.id INTO v_mod_edphys_n1    FROM modules m JOIN niveaux n ON m.niveau_id = n.id WHERE m.name = 'Éducation Physique'  AND n.name = '1ère année' LIMIT 1;
        SELECT m.id INTO v_mod_anglais_n1   FROM modules m JOIN niveaux n ON m.niveau_id = n.id WHERE m.name = 'Anglais'             AND n.name = '1ère année' LIMIT 1;

        -- Modules for niveau 2
        SELECT m.id INTO v_mod_arabe_n2     FROM modules m JOIN niveaux n ON m.niveau_id = n.id WHERE m.name = 'Arabe'               AND n.name = '2ème année' LIMIT 1;
        SELECT m.id INTO v_mod_sciences_n2  FROM modules m JOIN niveaux n ON m.niveau_id = n.id WHERE m.name = 'Sciences'            AND n.name = '2ème année' LIMIT 1;
        SELECT m.id INTO v_mod_histgeo_n2   FROM modules m JOIN niveaux n ON m.niveau_id = n.id WHERE m.name = 'Histoire-Géographie' AND n.name = '2ème année' LIMIT 1;
        SELECT m.id INTO v_mod_edislam_n2   FROM modules m JOIN niveaux n ON m.niveau_id = n.id WHERE m.name = 'Éducation Islamique' AND n.name = '2ème année' LIMIT 1;
        SELECT m.id INTO v_mod_edphys_n2    FROM modules m JOIN niveaux n ON m.niveau_id = n.id WHERE m.name = 'Éducation Physique'  AND n.name = '2ème année' LIMIT 1;
        SELECT m.id INTO v_mod_anglais_n2   FROM modules m JOIN niveaux n ON m.niveau_id = n.id WHERE m.name = 'Anglais'             AND n.name = '2ème année' LIMIT 1;

        -- Teachers by specialization
        SELECT id INTO v_teacher_arabe    FROM teachers WHERE specialization = 'Arabe'               LIMIT 1;
        SELECT id INTO v_teacher_sciences FROM teachers WHERE specialization = 'Sciences'            LIMIT 1;
        SELECT id INTO v_teacher_histgeo  FROM teachers WHERE specialization = 'Histoire-Géographie' LIMIT 1;
        SELECT id INTO v_teacher_edislam  FROM teachers WHERE specialization = 'Éducation Islamique' LIMIT 1;
        SELECT id INTO v_teacher_edphys   FROM teachers WHERE specialization = 'Éducation Physique'  LIMIT 1;
        SELECT id INTO v_teacher_anglais  FROM teachers WHERE specialization = 'Anglais'             LIMIT 1;

        -- ── Classe 1A schedule (Mon=1 to Fri=5) ──
        -- Monday
        INSERT INTO emploi_du_temps (classe_id, creneau_id, jour_semaine, module_id, enseignant_id, salle) VALUES
            (v_classe_1a_id, v_cr1, 1, v_mod_maths_n1,    v_teacher_maths,    'Salle 101'),
            (v_classe_1a_id, v_cr2, 1, v_mod_franc_n1,    v_teacher_franc,    'Salle 101'),
            (v_classe_1a_id, v_cr3, 1, v_mod_arabe_n1,    v_teacher_arabe,    'Salle 101')
        ON CONFLICT DO NOTHING;
        -- Tuesday
        INSERT INTO emploi_du_temps (classe_id, creneau_id, jour_semaine, module_id, enseignant_id, salle) VALUES
            (v_classe_1a_id, v_cr1, 2, v_mod_sciences_n1, v_teacher_sciences, 'Salle 102'),
            (v_classe_1a_id, v_cr2, 2, v_mod_histgeo_n1,  v_teacher_histgeo,  'Salle 101'),
            (v_classe_1a_id, v_cr3, 2, v_mod_maths_n1,    v_teacher_maths,    'Salle 101')
        ON CONFLICT DO NOTHING;
        -- Wednesday
        INSERT INTO emploi_du_temps (classe_id, creneau_id, jour_semaine, module_id, enseignant_id, salle) VALUES
            (v_classe_1a_id, v_cr1, 3, v_mod_franc_n1,    v_teacher_franc,    'Salle 101'),
            (v_classe_1a_id, v_cr2, 3, v_mod_edislam_n1,  v_teacher_edislam,  'Salle 101'),
            (v_classe_1a_id, v_cr3, 3, v_mod_anglais_n1,  v_teacher_anglais,  'Salle 103')
        ON CONFLICT DO NOTHING;
        -- Thursday
        INSERT INTO emploi_du_temps (classe_id, creneau_id, jour_semaine, module_id, enseignant_id, salle) VALUES
            (v_classe_1a_id, v_cr1, 4, v_mod_arabe_n1,    v_teacher_arabe,    'Salle 101'),
            (v_classe_1a_id, v_cr2, 4, v_mod_maths_n1,    v_teacher_maths,    'Salle 101'),
            (v_classe_1a_id, v_cr5, 4, v_mod_edphys_n1,   v_teacher_edphys,   'Terrain Sport')
        ON CONFLICT DO NOTHING;
        -- Friday
        INSERT INTO emploi_du_temps (classe_id, creneau_id, jour_semaine, module_id, enseignant_id, salle) VALUES
            (v_classe_1a_id, v_cr1, 5, v_mod_franc_n1,    v_teacher_franc,    'Salle 101'),
            (v_classe_1a_id, v_cr2, 5, v_mod_sciences_n1, v_teacher_sciences, 'Salle 102'),
            (v_classe_1a_id, v_cr3, 5, v_mod_arabe_n1,    v_teacher_arabe,    'Salle 101')
        ON CONFLICT DO NOTHING;

        -- ── Classe 2A schedule (Mon=1 to Fri=5) ──
        IF v_classe_2a_id IS NOT NULL THEN
            -- Monday
            INSERT INTO emploi_du_temps (classe_id, creneau_id, jour_semaine, module_id, enseignant_id, salle) VALUES
                (v_classe_2a_id, v_cr3, 1, v_mod_maths_n2,    v_teacher_maths,    'Salle 201'),
                (v_classe_2a_id, v_cr4, 1, v_mod_franc_n2,    v_teacher_franc,    'Salle 201'),
                (v_classe_2a_id, v_cr5, 1, v_mod_arabe_n2,    v_teacher_arabe,    'Salle 201')
            ON CONFLICT DO NOTHING;
            -- Tuesday
            INSERT INTO emploi_du_temps (classe_id, creneau_id, jour_semaine, module_id, enseignant_id, salle) VALUES
                (v_classe_2a_id, v_cr3, 2, v_mod_sciences_n2, v_teacher_sciences, 'Salle 202'),
                (v_classe_2a_id, v_cr4, 2, v_mod_histgeo_n2,  v_teacher_histgeo,  'Salle 201'),
                (v_classe_2a_id, v_cr5, 2, v_mod_maths_n2,    v_teacher_maths,    'Salle 201')
            ON CONFLICT DO NOTHING;
            -- Wednesday
            INSERT INTO emploi_du_temps (classe_id, creneau_id, jour_semaine, module_id, enseignant_id, salle) VALUES
                (v_classe_2a_id, v_cr3, 3, v_mod_franc_n2,    v_teacher_franc,    'Salle 201'),
                (v_classe_2a_id, v_cr4, 3, v_mod_edislam_n2,  v_teacher_edislam,  'Salle 201'),
                (v_classe_2a_id, v_cr5, 3, v_mod_anglais_n2,  v_teacher_anglais,  'Salle 203')
            ON CONFLICT DO NOTHING;
            -- Thursday
            INSERT INTO emploi_du_temps (classe_id, creneau_id, jour_semaine, module_id, enseignant_id, salle) VALUES
                (v_classe_2a_id, v_cr3, 4, v_mod_arabe_n2,    v_teacher_arabe,    'Salle 201'),
                (v_classe_2a_id, v_cr4, 4, v_mod_maths_n2,    v_teacher_maths,    'Salle 201'),
                (v_classe_2a_id, v_cr6, 4, v_mod_edphys_n2,   v_teacher_edphys,   'Terrain Sport')
            ON CONFLICT DO NOTHING;
            -- Friday
            INSERT INTO emploi_du_temps (classe_id, creneau_id, jour_semaine, module_id, enseignant_id, salle) VALUES
                (v_classe_2a_id, v_cr3, 5, v_mod_franc_n2,    v_teacher_franc,    'Salle 201'),
                (v_classe_2a_id, v_cr4, 5, v_mod_sciences_n2, v_teacher_sciences, 'Salle 202'),
                (v_classe_2a_id, v_cr5, 5, v_mod_arabe_n2,    v_teacher_arabe,    'Salle 201')
            ON CONFLICT DO NOTHING;
        END IF;
    END;

END $$;

-- ── 8. TYPES DE FRAIS (3) ──────────────────────────────────────
INSERT INTO types_frais (nom, montant, frequence, description, actif)
VALUES
    ('Frais de scolarité', 450.00, 'MENSUEL',     'Frais mensuels de scolarité',           true),
    ('Frais de cantine',   200.00, 'MENSUEL',     'Frais mensuels de cantine scolaire',    true),
    ('Frais de transport',  150.00, 'MENSUEL',     'Frais mensuels de transport scolaire',  true)
ON CONFLICT DO NOTHING;

-- ── 9. PAIEMENTS (20) ──────────────────────────────────────────
-- For students in class 1A, various months of the 2025-2026 year
DO $$
DECLARE
    v_student_ids_1a UUID[];
    v_tf_scolarite UUID;
    v_tf_cantine UUID;
    v_tf_transport UUID;
BEGIN
    SELECT ARRAY_AGG(id ORDER BY id) INTO v_student_ids_1a FROM students WHERE classe = '1A';
    SELECT id INTO v_tf_scolarite FROM types_frais WHERE nom = 'Frais de scolarité' LIMIT 1;
    SELECT id INTO v_tf_cantine   FROM types_frais WHERE nom = 'Frais de cantine'   LIMIT 1;
    SELECT id INTO v_tf_transport FROM types_frais WHERE nom = 'Frais de transport'  LIMIT 1;

    IF v_student_ids_1a IS NULL OR v_tf_scolarite IS NULL THEN
        RAISE NOTICE 'Required data not found for paiements, skipping';
        RETURN;
    END IF;

    -- Student 1: scolarite Sept-Dec (4 payments, all PAYE)
    INSERT INTO paiements (student_id, type_frais_id, mois, annee_scolaire, montant_du, montant_paye, date_paiement, mode_paiement, statut, reference)
    VALUES
        (v_student_ids_1a[1], v_tf_scolarite, 'Septembre', '2025-2026', 450.00, 450.00, '2025-09-05', 'ESPECES',  'PAYE', 'PAY-2025-09-001'),
        (v_student_ids_1a[1], v_tf_scolarite, 'Octobre',   '2025-2026', 450.00, 450.00, '2025-10-03', 'VIREMENT', 'PAYE', 'PAY-2025-10-001'),
        (v_student_ids_1a[1], v_tf_scolarite, 'Novembre',  '2025-2026', 450.00, 450.00, '2025-11-04', 'ESPECES',  'PAYE', 'PAY-2025-11-001'),
        (v_student_ids_1a[1], v_tf_scolarite, 'Décembre',  '2025-2026', 450.00, 450.00, '2025-12-02', 'ESPECES',  'PAYE', 'PAY-2025-12-001')
    ON CONFLICT DO NOTHING;

    -- Student 2: scolarite Sept-Nov + cantine Sept (4 payments, mix)
    INSERT INTO paiements (student_id, type_frais_id, mois, annee_scolaire, montant_du, montant_paye, date_paiement, mode_paiement, statut, reference)
    VALUES
        (v_student_ids_1a[2], v_tf_scolarite, 'Septembre', '2025-2026', 450.00, 450.00, '2025-09-06', 'CHEQUE',   'PAYE',       'PAY-2025-09-002'),
        (v_student_ids_1a[2], v_tf_scolarite, 'Octobre',   '2025-2026', 450.00, 450.00, '2025-10-05', 'ESPECES',  'PAYE',       'PAY-2025-10-002'),
        (v_student_ids_1a[2], v_tf_scolarite, 'Novembre',  '2025-2026', 450.00, 200.00, NULL,         NULL,        'PARTIEL',    'PAY-2025-11-002'),
        (v_student_ids_1a[2], v_tf_cantine,   'Septembre', '2025-2026', 200.00, 200.00, '2025-09-06', 'ESPECES',  'PAYE',       'PAY-2025-09-002C')
    ON CONFLICT DO NOTHING;

    -- Student 3: scolarite Sept + transport Sept-Oct (3 payments)
    INSERT INTO paiements (student_id, type_frais_id, mois, annee_scolaire, montant_du, montant_paye, date_paiement, mode_paiement, statut, reference)
    VALUES
        (v_student_ids_1a[3], v_tf_scolarite, 'Septembre', '2025-2026', 450.00, 450.00, '2025-09-08', 'ESPECES',  'PAYE',       'PAY-2025-09-003'),
        (v_student_ids_1a[3], v_tf_transport, 'Septembre', '2025-2026', 150.00, 150.00, '2025-09-08', 'ESPECES',  'PAYE',       'PAY-2025-09-003T'),
        (v_student_ids_1a[3], v_tf_transport, 'Octobre',   '2025-2026', 150.00, 150.00, '2025-10-06', 'ESPECES',  'PAYE',       'PAY-2025-10-003T')
    ON CONFLICT DO NOTHING;

    -- Student 4: scolarite Sept-Oct, Oct EN_ATTENTE (2 payments)
    INSERT INTO paiements (student_id, type_frais_id, mois, annee_scolaire, montant_du, montant_paye, date_paiement, mode_paiement, statut, reference)
    VALUES
        (v_student_ids_1a[4], v_tf_scolarite, 'Septembre', '2025-2026', 450.00, 450.00, '2025-09-10', 'VIREMENT', 'PAYE',       'PAY-2025-09-004'),
        (v_student_ids_1a[4], v_tf_scolarite, 'Octobre',   '2025-2026', 450.00, 0.00,   NULL,         NULL,        'EN_ATTENTE', 'PAY-2025-10-004')
    ON CONFLICT DO NOTHING;

    -- Student 5: scolarite Sept + cantine Sept-Oct (3 payments)
    INSERT INTO paiements (student_id, type_frais_id, mois, annee_scolaire, montant_du, montant_paye, date_paiement, mode_paiement, statut, reference)
    VALUES
        (v_student_ids_1a[5], v_tf_scolarite, 'Septembre', '2025-2026', 450.00, 450.00, '2025-09-12', 'ESPECES',  'PAYE',       'PAY-2025-09-005'),
        (v_student_ids_1a[5], v_tf_cantine,   'Septembre', '2025-2026', 200.00, 200.00, '2025-09-12', 'ESPECES',  'PAYE',       'PAY-2025-09-005C'),
        (v_student_ids_1a[5], v_tf_cantine,   'Octobre',   '2025-2026', 200.00, 0.00,   NULL,         NULL,        'EN_ATTENTE', 'PAY-2025-10-005C')
    ON CONFLICT DO NOTHING;

    -- Student 6: scolarite EN_RETARD for Sept (1 payment)
    INSERT INTO paiements (student_id, type_frais_id, mois, annee_scolaire, montant_du, montant_paye, date_paiement, mode_paiement, statut, reference)
    VALUES
        (v_student_ids_1a[6], v_tf_scolarite, 'Septembre', '2025-2026', 450.00, 0.00, NULL, NULL, 'EN_RETARD', 'PAY-2025-09-006')
    ON CONFLICT DO NOTHING;

END $$;

-- ── 10. ANNONCES (5) ───────────────────────────────────────────
INSERT INTO annonces (titre, contenu, type, destinataires, auteur_name, date_publication, actif)
VALUES
    ('Rentrée scolaire 2025-2026',
     'Chers parents, nous avons le plaisir de vous informer que la rentrée scolaire aura lieu le lundi 15 septembre 2025. Les portes ouvriront à 7h30. Nous vous prions de veiller à ce que vos enfants soient munis de leurs fournitures scolaires.',
     'GENERAL', 'TOUS', 'Mohamed Directeur', '2025-09-01 08:00:00', true),

    ('Réunion parents-enseignants',
     'Une réunion parents-enseignants se tiendra le samedi 18 octobre 2025 à 9h00 dans la salle polyvalente. Votre présence est importante pour discuter du suivi pédagogique de vos enfants. Merci de confirmer votre participation.',
     'REUNION', 'PARENTS', 'Mohamed Directeur', '2025-10-05 10:00:00', true),

    ('Fête scolaire de fin de trimestre',
     'L''école organise une fête de fin de premier trimestre le vendredi 19 décembre 2025. Au programme : spectacles des élèves, exposition de travaux artistiques et goûter collectif. Tous les parents sont cordialement invités.',
     'EVENEMENT', 'TOUS', 'Mohamed Directeur', '2025-12-01 09:00:00', true),

    ('Vacances d''hiver',
     'Les vacances d''hiver se dérouleront du samedi 20 décembre 2025 au dimanche 4 janvier 2026. La reprise des cours est prévue le lundi 5 janvier 2026. Nous souhaitons à toutes les familles de bonnes fêtes.',
     'GENERAL', 'TOUS', 'Mohamed Directeur', '2025-12-15 08:00:00', true),

    ('Inscription cantine - 2ème trimestre',
     'Les inscriptions à la cantine pour le 2ème trimestre sont ouvertes. Le tarif mensuel est de 200 TND. Merci de vous rapprocher du secrétariat avant le 10 janvier 2026 pour effectuer l''inscription.',
     'GENERAL', 'PARENTS', 'Ali Comptable', '2025-12-20 14:00:00', true)
ON CONFLICT DO NOTHING;

