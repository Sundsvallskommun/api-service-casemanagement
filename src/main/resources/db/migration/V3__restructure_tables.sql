-- =============================================================================
-- V3: Restructure tables to snake_case, normalize case type storage,
--     and make all routing/validation fully data-driven.
-- =============================================================================

-- Step 1: Rename existing tables to snake_case
ALTER TABLE CaseEntity RENAME TO case_entity;
ALTER TABLE CaseMapping RENAME TO case_mapping;

-- Step 2: Create the case_type table (stores all static type→system mappings + behavior flags)
CREATE TABLE case_type (
    name VARCHAR(255) NOT NULL,
    system_type VARCHAR(50) NOT NULL,
    nullable_facility_type TINYINT(1) NOT NULL DEFAULT 0,
    nullable_facility TINYINT(1) NOT NULL DEFAULT 0,
    facility_type_rule VARCHAR(50),
    PRIMARY KEY (name)
) engine=InnoDB;

-- Step 3: Rename casetypedata → byggr_case_type_config and rename columns to snake_case
ALTER TABLE casetypedata RENAME TO byggr_case_type_config;
ALTER TABLE byggr_case_type_config CHANGE COLUMN value case_type_name VARCHAR(255) NOT NULL;
ALTER TABLE byggr_case_type_config CHANGE COLUMN arendeSlag arende_slag VARCHAR(255);
ALTER TABLE byggr_case_type_config CHANGE COLUMN arendeGrupp arende_grupp VARCHAR(255);
ALTER TABLE byggr_case_type_config CHANGE COLUMN arendeTyp arende_typ VARCHAR(255);
ALTER TABLE byggr_case_type_config CHANGE COLUMN handelseTyp handelse_typ VARCHAR(255);
ALTER TABLE byggr_case_type_config CHANGE COLUMN handelseRubrik handelse_rubrik VARCHAR(255);
ALTER TABLE byggr_case_type_config CHANGE COLUMN handelseSlag handelse_slag VARCHAR(255);
ALTER TABLE byggr_case_type_config CHANGE COLUMN arendeMening arende_mening VARCHAR(255);
ALTER TABLE byggr_case_type_config ADD COLUMN update_handler VARCHAR(50);

-- Step 4: Create ecos_case_type_config table
CREATE TABLE ecos_case_type_config (
    case_type_name VARCHAR(255) NOT NULL,
    diary_plan_id VARCHAR(255),
    process_type_id VARCHAR(255),
    facility_handler VARCHAR(50),
    PRIMARY KEY (case_type_name)
) engine=InnoDB;

-- Step 5: Populate case_type with all Byggr types
INSERT INTO case_type (name, system_type, nullable_facility_type, nullable_facility, facility_type_rule) VALUES
    ('NYBYGGNAD_ANSOKAN_OM_BYGGLOV', 'BYGGR', 0, 0, 'ATTEFALL_REJECTED'),
    ('TILLBYGGNAD_ANSOKAN_OM_BYGGLOV', 'BYGGR', 0, 0, NULL),
    ('UPPSATTANDE_SKYLT', 'BYGGR', 0, 0, NULL),
    ('ANDRING_ANSOKAN_OM_BYGGLOV', 'BYGGR', 0, 0, NULL),
    ('ANDRING_VINDKRAFTVERK', 'BYGGR', 0, 0, NULL),
    ('NYBYGGNAD_VINDKRAFTVERK', 'BYGGR', 0, 0, NULL),
    ('NYBYGGNAD_FORHANDSBESKED', 'BYGGR', 0, 0, NULL),
    ('NEIGHBORHOOD_NOTIFICATION', 'BYGGR', 0, 1, NULL),
    ('PROPERTY_OWNER_NOTIFICATION', 'BYGGR', 0, 1, NULL),
    ('BYGGR_ADD_CERTIFIED_INSPECTOR', 'BYGGR', 0, 1, NULL),
    ('BYGGR_ADDITIONAL_DOCUMENTS', 'BYGGR', 0, 1, NULL),
    ('STRANDSKYDD_NYBYGGNAD', 'BYGGR', 0, 0, NULL),
    ('STRANDSKYDD_ANLAGGANDE', 'BYGGR', 0, 0, NULL),
    ('STRANDSKYDD_ANORDNANDE', 'BYGGR', 0, 0, NULL),
    ('STRANDSKYDD_ANDRAD_ANVANDNING', 'BYGGR', 0, 0, NULL),
    ('STRANDSKYDD_OVRIGT', 'BYGGR', 1, 0, NULL),
    ('ANDRING_VENTILATION', 'BYGGR', 0, 0, NULL),
    ('INSTALLATION_VENTILATION', 'BYGGR', 0, 0, NULL),
    ('ANDRING_VA', 'BYGGR', 0, 0, NULL),
    ('INSTALLATION_VA', 'BYGGR', 0, 0, NULL),
    ('ANDRING_PLANLOSNING', 'BYGGR', 0, 0, NULL),
    ('ANDRING_BARANDE_KONSTRUKTION', 'BYGGR', 0, 0, NULL),
    ('ANDRING_BRANDSKYDD', 'BYGGR', 0, 0, NULL),
    ('INSTALLLATION_HISS', 'BYGGR', 0, 0, NULL),
    ('ANSOKAN_RIVNINGSLOV', 'BYGGR', 0, 0, NULL),
    ('ANMALAN_RIVNING', 'BYGGR', 0, 0, NULL),
    ('MARKLOV_SCHAKTNING', 'BYGGR', 1, 0, NULL),
    ('MARKLOV_FYLL', 'BYGGR', 1, 0, NULL),
    ('MARKLOV_TRADFALLNING', 'BYGGR', 1, 0, NULL),
    ('MARKLOV_OVRIGT', 'BYGGR', 1, 0, NULL),
    ('ANMALAN_ATTEFALL', 'BYGGR', 0, 0, 'ATTEFALL_REQUIRED'),
    ('ANMALAN_ELDSTAD', 'BYGGR', 0, 0, NULL);

-- Step 6: Populate case_type with Ecos types
INSERT INTO case_type (name, system_type, nullable_facility_type, nullable_facility, facility_type_rule) VALUES
    ('REGISTRERING_AV_LIVSMEDEL', 'ECOS', 0, 0, NULL),
    ('ANMALAN_INSTALLATION_VARMEPUMP', 'ECOS', 0, 0, NULL),
    ('ANSOKAN_TILLSTAND_VARMEPUMP', 'ECOS', 0, 0, NULL),
    ('ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP', 'ECOS', 0, 0, NULL),
    ('ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC', 'ECOS', 0, 0, NULL),
    ('ANMALAN_ANDRING_AVLOPPSANLAGGNING', 'ECOS', 0, 0, NULL),
    ('ANMALAN_ANDRING_AVLOPPSANORDNING', 'ECOS', 0, 0, NULL),
    ('ANMALAN_HALSOSKYDDSVERKSAMHET', 'ECOS', 0, 0, NULL),
    ('UPPDATERING_RISKKLASSNING', 'ECOS', 0, 0, NULL),
    ('ANMALAN_KOMPOSTERING', 'ECOS', 1, 0, NULL),
    ('ANMALAN_AVHJALPANDEATGARD_FORORENING', 'ECOS', 1, 0, NULL),
    ('ANDRING_AV_LIVSMEDELSVERKSAMHET', 'ECOS', 0, 0, NULL),
    ('INFORMATION_OM_UPPHORANDE_AV_VERKSAMHET', 'ECOS', 0, 0, NULL);

-- Step 7: Populate case_type with EdpFuture types
INSERT INTO case_type (name, system_type) VALUES
    ('EXTRA_SACK', 'EDPFUTURE');

-- Step 8: Populate ecos_case_type_config
INSERT INTO ecos_case_type_config (case_type_name, diary_plan_id, process_type_id, facility_handler) VALUES
    ('REGISTRERING_AV_LIVSMEDEL',                    '73B90981-D7AE-49E3-8AB7-3AED778ABDB4', 'A764A86B-7327-445B-98C5-C26543D6F705', 'FOOD'),
    ('ANMALAN_INSTALLATION_VARMEPUMP',                '91470D60-FCDE-418D-A2B9-601FC1850B63', '38C76611-DFE0-4358-864A-31C320712F69', 'HEAT_PUMP'),
    ('ANSOKAN_TILLSTAND_VARMEPUMP',                   '91470D60-FCDE-418D-A2B9-601FC1850B63', 'BDFE8FBB-18D5-45FC-A9E7-DE43E42F6218', 'HEAT_PUMP'),
    ('ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP',           '91470D60-FCDE-418D-A2B9-601FC1850B63', '50B6FA5B-23E2-4ABA-B393-0B3ADEFC6C9F', 'INDIVIDUAL_SEWAGE'),
    ('ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC',    '91470D60-FCDE-418D-A2B9-601FC1850B63', '294F547E-C1C9-445E-87F9-8829D0FB1ED6', 'INDIVIDUAL_SEWAGE'),
    ('ANMALAN_ANDRING_AVLOPPSANLAGGNING',             '91470D60-FCDE-418D-A2B9-601FC1850B63', '9511B1D1-4BAA-4FC1-92FD-84622AD8A4C8', 'INDIVIDUAL_SEWAGE'),
    ('ANMALAN_ANDRING_AVLOPPSANORDNING',              '91470D60-FCDE-418D-A2B9-601FC1850B63', '11428429-E292-44B5-B03A-A4FE6CEBAAD7', 'INDIVIDUAL_SEWAGE'),
    ('ANMALAN_HALSOSKYDDSVERKSAMHET',                 '86100879-6451-4310-AAB2-9C1F9F663F69', 'AF6D94EC-94FB-4C0A-AF39-CC5E4C732D4B', 'HEALTH_PROTECTION'),
    ('UPPDATERING_RISKKLASSNING',                     '73B90981-D7AE-49E3-8AB7-3AED778ABDB4', 'E8E389D4-FE45-4195-A790-C58AE4DC96BF', 'RISK_CLASS_UPDATE'),
    ('ANMALAN_KOMPOSTERING',                          '91470D60-FCDE-418D-A2B9-601FC1850B63', '87C496A2-6877-4ED8-9CB0-4937F09F4DB9', 'NONE'),
    ('ANMALAN_AVHJALPANDEATGARD_FORORENING',          '91470D60-FCDE-418D-A2B9-601FC1850B63', '7AA1A70E-4842-4CF0-A15A-EEF99707811E', 'NONE'),
    ('ANDRING_AV_LIVSMEDELSVERKSAMHET',               '73B90981-D7AE-49E3-8AB7-3AED778ABDB4', '54617585-692A-432F-86BE-08A9364D8F40', 'EXISTING_FACILITY'),
    ('INFORMATION_OM_UPPHORANDE_AV_VERKSAMHET',       '73B90981-D7AE-49E3-8AB7-3AED778ABDB4', '20BB6BDA-75A2-4728-8EDF-589E86F4446F', 'EXISTING_FACILITY');

-- Step 9: Add FK constraints
ALTER TABLE byggr_case_type_config
    ADD CONSTRAINT fk_byggr_config_case_type FOREIGN KEY (case_type_name) REFERENCES case_type (name);

ALTER TABLE ecos_case_type_config
    ADD CONSTRAINT fk_ecos_config_case_type FOREIGN KEY (case_type_name) REFERENCES case_type (name);

-- Step 10: Populate update handlers for ByggR update case types
INSERT INTO byggr_case_type_config (case_type_name, update_handler) VALUES
    ('NEIGHBORHOOD_NOTIFICATION', 'neighborhoodResponse'),
    ('PROPERTY_OWNER_NOTIFICATION', 'neighborhoodResponse'),
    ('BYGGR_ADD_CERTIFIED_INSPECTOR', 'addInspector'),
    ('BYGGR_ADDITIONAL_DOCUMENTS', 'addDocuments');

-- Step 11: Create byggr_status_mapping table for data-driven status resolution
CREATE TABLE byggr_status_mapping (
    id BIGINT NOT NULL AUTO_INCREMENT,
    handelse_typ VARCHAR(50),
    handelse_slag VARCHAR(50),
    handelse_utfall VARCHAR(50),
    return_field VARCHAR(10) NOT NULL,
    PRIMARY KEY (id)
) engine=InnoDB;

INSERT INTO byggr_status_mapping (handelse_typ, handelse_slag, handelse_utfall, return_field) VALUES
    ('ANM', NULL, NULL, 'TYP'),
    ('ANSÖKAN', NULL, NULL, 'TYP'),
    ('UNDER', 'Med', NULL, 'TYP'),
    ('UNDER', 'Utan', NULL, 'TYP'),
    ('KOMP', NULL, NULL, 'TYP'),
    ('KOMP1', NULL, NULL, 'TYP'),
    ('BESLUT', 'SLU', NULL, 'SLAG'),
    ('BESLUT', 'UAB', NULL, 'SLAG'),
    ('HANDLING', 'KOMPL', NULL, 'SLAG'),
    (NULL, 'KOMPBYGG', NULL, 'SLAG'),
    (NULL, 'KOMPTEK', NULL, 'SLAG'),
    (NULL, 'KOMPREV', NULL, 'SLAG'),
    ('REMISS', 'UTSKICK', NULL, 'SLAG'),
    ('Atom', 'Kv', 'Kv2', 'UTFALL');
