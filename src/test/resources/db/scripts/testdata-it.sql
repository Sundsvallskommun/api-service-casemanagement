-- Static case types (with data-driven flags)
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
    ('ANMALAN_ELDSTAD', 'BYGGR', 0, 0, NULL),
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
    ('INFORMATION_OM_UPPHORANDE_AV_VERKSAMHET', 'ECOS', 0, 0, NULL),
    ('EXTRA_SACK', 'EDPFUTURE', 0, 0, NULL);

-- Byggr case type config (metadata for Byggr types)
INSERT INTO byggr_case_type_config (case_type_name, arende_slag, arende_grupp, arende_typ, handelse_typ, handelse_rubrik, handelse_slag, arende_mening) VALUES
    ('NYBYGGNAD_ANSOKAN_OM_BYGGLOV', 'A', 'LOV', 'BL', 'ANSÖKAN', 'Bygglov', 'Bygglov', 'Bygglov för nybyggnad av'),
    ('TILLBYGGNAD_ANSOKAN_OM_BYGGLOV', 'B', 'LOV', 'BL', 'ANSÖKAN', 'Bygglov', 'Bygglov', 'Bygglov för tillbyggnad av'),
    ('UPPSATTANDE_SKYLT', 'L', 'LOV', 'BL', 'ANSÖKAN', 'Bygglov', 'Bygglov', 'Bygglov för uppsättande av '),
    ('ANDRING_ANSOKAN_OM_BYGGLOV', NULL, 'LOV', 'BL', 'ANSÖKAN', 'Bygglov', 'Bygglov', 'Bygglov för'),
    ('NYBYGGNAD_FORHANDSBESKED', 'A', 'LOV', 'FÖRF', 'ANSÖKAN', 'Förhandsbesked', 'Förhand', 'Förhandsbesked för nybyggnad av'),
    ('STRANDSKYDD_NYBYGGNAD', 'NYB', 'STRA', 'DI', 'ANSÖKAN', 'Strandskyddsdispens', 'Strand', 'Strandskyddsdispens för nybyggnad av'),
    ('STRANDSKYDD_ANLAGGANDE', 'A1', 'STRA', 'DI', 'ANSÖKAN', 'Strandskyddsdispens', 'Strand', 'Strandskyddsdispens för anläggande av'),
    ('STRANDSKYDD_ANORDNANDE', 'AO', 'STRA', 'DI', 'ANSÖKAN', 'Strandskyddsdispens', 'Strand', 'Strandskyddsdispens för anordnare av'),
    ('STRANDSKYDD_ANDRAD_ANVANDNING', 'ÄNDR', 'STRA', 'DI', 'ANSÖKAN', 'Strandskyddsdispens', 'Strand', 'Strandskyddsdispens för ändrad användning av'),
    ('STRANDSKYDD_OVRIGT', 'ÖVR', 'STRA', 'DI', 'ANSÖKAN', 'Strandskyddsdispens', 'Strand', 'Strandskyddsdispens för övrigt'),
    ('ANMALAN_ATTEFALL', NULL, 'LOV', 'ATTANM', 'ANM', 'Anmälan Attefall', 'ANMATT', NULL),
    ('ANMALAN_ELDSTAD', NULL, 'LOV', 'ANM', 'ANM', NULL, NULL, NULL),
    ('ANDRING_VENTILATION', 'K', 'LOV', 'ANM', 'ANM', 'Anmälan', 'ANM', 'Anmälan för ändring av ventilation'),
    ('INSTALLATION_VENTILATION', 'J', 'LOV', 'ANM', 'ANM', 'Anmälan', 'ANM', 'Anmälan för installation av ventilation'),
    ('ANDRING_VA', 'I', 'LOV', 'ANM', 'ANM', 'Anmälan', 'ANM', 'Anmälan för ändring av VA'),
    ('INSTALLATION_VA', 'H', 'LOV', 'ANM', 'ANM', 'Anmälan', 'ANM', 'Anmälan för installation av VA'),
    ('ANDRING_PLANLOSNING', 'ÄNDP', 'LOV', 'ANM', 'ANM', 'Anmälan', 'ANM', 'Anmälan för ändring av planlösning'),
    ('ANDRING_BARANDE_KONSTRUKTION', 'L', 'LOV', 'ANM', 'ANM', 'Anmälan', 'ANM', 'Anmälan för ändring av bärande konstruktion'),
    ('ANDRING_BRANDSKYDD', 'VÄS', 'LOV', 'ANM', 'ANM', 'Anmälan', 'ANM', 'Anmälan för väsentlig ändring av brandskydd'),
    ('INSTALLLATION_HISS', 'G', 'LOV', 'ANM', 'ANM', 'Anmälan', 'ANM', 'Anmälan för installation av hiss'),
    ('MARKLOV_SCHAKTNING', 'SCH', 'LOV', 'MARK', 'ANSÖKAN', 'Marklov', 'Marklov', 'Marklov för schaktning'),
    ('MARKLOV_FYLL', 'FYL', 'LOV', 'MARK', 'ANSÖKAN', 'Marklov', 'Marklov', 'Marklov för fyll'),
    ('MARKLOV_TRADFALLNING', 'TRÄD', 'LOV', 'MARK', 'ANSÖKAN', 'Marklov', 'Marklov', 'Marklov för trädfällning'),
    ('MARKLOV_OVRIGT', 'ÖVR', 'LOV', 'MARK', 'ANSÖKAN', 'Marklov', 'Marklov', 'Marklov för övrigt'),
    ('ANSOKAN_RIVNINGSLOV', NULL, 'LOV', 'RIVL', 'ANSÖKAN', 'Rivningslov', 'Rivlov', 'Rivningslov'),
    ('ANMALAN_RIVNING', NULL, 'LOV', 'ANM', 'ANM', 'Anmälan', 'ANM', 'Anmälan för rivning');

-- ByggR update case types (with update_handler)
INSERT INTO byggr_case_type_config (case_type_name, update_handler) VALUES
    ('NEIGHBORHOOD_NOTIFICATION', 'neighborhoodResponse'),
    ('PROPERTY_OWNER_NOTIFICATION', 'neighborhoodResponse'),
    ('BYGGR_ADD_CERTIFIED_INSPECTOR', 'addInspector'),
    ('BYGGR_ADDITIONAL_DOCUMENTS', 'addDocuments');

-- Ecos case type config
INSERT INTO ecos_case_type_config (case_type_name, diary_plan_id, process_type_id, facility_handler) VALUES
    ('REGISTRERING_AV_LIVSMEDEL', '73B90981-D7AE-49E3-8AB7-3AED778ABDB4', 'A764A86B-7327-445B-98C5-C26543D6F705', 'FOOD'),
    ('ANMALAN_INSTALLATION_VARMEPUMP', '91470D60-FCDE-418D-A2B9-601FC1850B63', '38C76611-DFE0-4358-864A-31C320712F69', 'HEAT_PUMP'),
    ('ANSOKAN_TILLSTAND_VARMEPUMP', '91470D60-FCDE-418D-A2B9-601FC1850B63', 'BDFE8FBB-18D5-45FC-A9E7-DE43E42F6218', 'HEAT_PUMP'),
    ('ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP', '91470D60-FCDE-418D-A2B9-601FC1850B63', '50B6FA5B-23E2-4ABA-B393-0B3ADEFC6C9F', 'INDIVIDUAL_SEWAGE'),
    ('ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC', '91470D60-FCDE-418D-A2B9-601FC1850B63', '294F547E-C1C9-445E-87F9-8829D0FB1ED6', 'INDIVIDUAL_SEWAGE'),
    ('ANMALAN_ANDRING_AVLOPPSANLAGGNING', '91470D60-FCDE-418D-A2B9-601FC1850B63', '9511B1D1-4BAA-4FC1-92FD-84622AD8A4C8', 'INDIVIDUAL_SEWAGE'),
    ('ANMALAN_ANDRING_AVLOPPSANORDNING', '91470D60-FCDE-418D-A2B9-601FC1850B63', '11428429-E292-44B5-B03A-A4FE6CEBAAD7', 'INDIVIDUAL_SEWAGE'),
    ('ANMALAN_HALSOSKYDDSVERKSAMHET', '86100879-6451-4310-AAB2-9C1F9F663F69', 'AF6D94EC-94FB-4C0A-AF39-CC5E4C732D4B', 'HEALTH_PROTECTION'),
    ('UPPDATERING_RISKKLASSNING', '73B90981-D7AE-49E3-8AB7-3AED778ABDB4', 'E8E389D4-FE45-4195-A790-C58AE4DC96BF', 'RISK_CLASS_UPDATE'),
    ('ANMALAN_KOMPOSTERING', '91470D60-FCDE-418D-A2B9-601FC1850B63', '87C496A2-6877-4ED8-9CB0-4937F09F4DB9', 'NONE'),
    ('ANMALAN_AVHJALPANDEATGARD_FORORENING', '91470D60-FCDE-418D-A2B9-601FC1850B63', '7AA1A70E-4842-4CF0-A15A-EEF99707811E', 'NONE'),
    ('ANDRING_AV_LIVSMEDELSVERKSAMHET', '73B90981-D7AE-49E3-8AB7-3AED778ABDB4', '54617585-692A-432F-86BE-08A9364D8F40', 'EXISTING_FACILITY'),
    ('INFORMATION_OM_UPPHORANDE_AV_VERKSAMHET', '73B90981-D7AE-49E3-8AB7-3AED778ABDB4', '20BB6BDA-75A2-4728-8EDF-589E86F4446F', 'EXISTING_FACILITY');

-- Byggr status mapping rules
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

-- Case mappings
INSERT INTO case_mapping(caseId, externalCaseId, caseType, serviceName, system, timestamp, municipalityId) VALUES
    ('BYGG 2021-000200', '3522', 'NYBYGGNAD_ANSOKAN_OM_BYGGLOV', 'Ansökan - strandskyddsdispens', 'BYGGR', '2023-05-12 14:53:58.672027', '2281'),
    ('e19981ad-34b2-4e14-88f5-133f61ca85aa', '2222', 'REGISTRERING_AV_LIVSMEDEL', 'Registrering av livsmedelsanläggning', 'ECOS', '2023-05-12 14:53:58.672027', '2281'),
    ('e19981ad-34b2-4e14-88f5-133f61ca85aa', '2223', 'REGISTRERING_AV_LIVSMEDEL', 'Registrering av livsmedelsanläggning', 'ECOS', '2023-05-12 14:53:58.672027', '2281'),
    ('24', '231', 'PARKING_PERMIT', 'Parkeringstillstånd', 'CASE_DATA', '2023-05-12 14:53:58.672027', '2281'),
    ('BYGG 2022-000003', '231156', 'NYBYGGNAD_ANSOKAN_OM_BYGGLOV', 'Ansökan - nybyggnad', 'BYGGR', '2023-05-12 14:53:58.672027', '2281');

-- Execution information
TRUNCATE TABLE execution_information;
INSERT INTO execution_information (municipality_id, job_name, last_successful_execution) VALUES
    ('2281', 'BYGGR_STATUS', '2020-01-01 00:00:00.000000'),
    ('2281', 'ECOS_STATUS', '2020-01-01 00:00:00.000000');
