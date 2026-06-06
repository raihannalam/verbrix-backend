INSERT INTO role (name)
VALUES
    ('CLIENT'),
    ('INTERPRETER'),
    ('ADMIN')
    ON CONFLICT (name) DO NOTHING;

-- =============================================================================
-- 1. POPULATE SPECIALIZATIONS ENTITY
-- =============================================================================
INSERT INTO specializations (name) VALUES
    ('GENERAL_PRACTICE'),
    ('CARDIOLOGY'),
    ('ONCOLOGY'),
    ('PEDIATRICS'),
    ('NEUROLOGY'),
    ('ORTHOPEDICS'),
    ('OBSTETRICS_GYNECOLOGY'),
    ('PSYCHIATRY'),
    ('DERMATOLOGY'),
    ('GASTROENTEROLOGY'),
    ('EMERGENCY_MEDICINE'),
    ('SURGERY'),
    ('IMMUNOLOGY'),
    ('ENDOCRINOLOGY'),
    ('UROLOGY'),
    ('NEPHROLOGY'),
    ('OPHTHALMOLOGY'),
    ('OTOLARYNGOLOGY'),
    ('RHEUMATOLOGY'),
    ('GERIATRICS'),
    ('PALLIATIVE_CARE'),
    ('REHABILITATION'),
    ('RADIOLOGY'),
    ('GENETICS'),
    ('INFECTIOUS_DISEASES'),
    ('DENTISTRY'),
    ('CLINICAL_TRIALS'),
    ('PHARMACEUTICALS'),
    ('MEDICAL_DEVICES'),
    ('TELEHEALTH')
ON CONFLICT (name) DO NOTHING;

-- =============================================================================
-- 2. POPULATE LANGUAGE ENTITY
-- =============================================================================
INSERT INTO languages (code, name) VALUES
    ('EN', 'EN'),
    ('ES', 'ES'),
    ('FR', 'FR'),
    ('AR', 'AR'),
    ('ZH', 'ZH'),
    ('DE', 'DE'),
    ('PT', 'PT'),
    ('RU', 'RU'),
    ('JA', 'JA'),
    ('KO', 'KO'),
    ('TR', 'TR'),
    ('VI', 'VI'),
    ('IT', 'IT'),
    ('NL', 'NL'),
    ('PL', 'PL'),
    ('HI', 'HI'),
    ('BN', 'BN'),
    ('TE', 'TE'),
    ('MR', 'MR'),
    ('TA', 'TA'),
    ('UR', 'UR'),
    ('GU', 'GU'),
    ('KN', 'KN'),
    ('ML', 'ML'),
    ('PA', 'PA'),
    ('AS', 'AS'),
    ('ASL', 'ASL'),
    ('ISL', 'ISL')
ON CONFLICT (code) DO NOTHING;