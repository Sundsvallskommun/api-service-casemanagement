INSERT INTO casetypedata
VALUES ('NYBYGGNAD_ANSOKAN_OM_BYGGLOV', 'A', 'LOV', 'BL', 'ANSÖKAN', 'Bygglov', 'Bygglov',
        'Bygglov för nybyggnad av'),
       ('TILLBYGGNAD_ANSOKAN_OM_BYGGLOV', 'B', 'LOV', 'BL', 'ANSÖKAN', 'Bygglov', 'Bygglov',
        'Bygglov för tillbyggnad av'),
       ('UPPSATTANDE_SKYLT', 'L', 'LOV', 'BL', 'ANSÖKAN', 'Bygglov', 'Bygglov',
        'Bygglov för uppsättande av '),
       ('ANDRING_ANSOKAN_OM_BYGGLOV', NULL, 'LOV', 'BL', 'ANSÖKAN', 'Bygglov', 'Bygglov',
        'Bygglov för'),
       ('NYBYGGNAD_FORHANDSBESKED', 'A', 'LOV', 'FÖRF', 'ANSÖKAN', 'Förhandsbesked', 'Förhand',
        'Förhandsbesked för nybyggnad av'),
       ('STRANDSKYDD_NYBYGGNAD', 'NYB', 'STRA', 'DI', 'ANSÖKAN', 'Strandskyddsdispens', 'Strand',
        'Strandskyddsdispens för nybyggnad av'),
       ('STRANDSKYDD_ANLAGGANDE', 'A1', 'STRA', 'DI', 'ANSÖKAN', 'Strandskyddsdispens', 'Strand',
        'Strandskyddsdispens för anläggande av'),
       ('STRANDSKYDD_ANORDNANDE', 'AO', 'STRA', 'DI', 'ANSÖKAN', 'Strandskyddsdispens', 'Strand',
        'Strandskyddsdispens för anordnare av'),
       ('STRANDSKYDD_ANDRAD_ANVANDNING', 'ÄNDR', 'STRA', 'DI', 'ANSÖKAN', 'Strandskyddsdispens',
        'Strand', 'Strandskyddsdispens för ändrad användning av'),
       ('ANMALAN_ATTEFALL', NULL, 'LOV', 'ATTANM', 'ANM', 'Anmälan Attefall', 'ANMATT', NULL),
       ('ANMALAN_ELDSTAD', NULL, 'LOV', 'ANM', 'ANM', NULL, NULL, NULL),
       ('ANDRING_VENTILATION', 'K', 'LOV', 'ANM', 'ANM', 'Anmälan', 'ANM',
        'Anmälan för ändring av ventilation'),
       ('INSTALLATION_VENTILATION', 'J', 'LOV', 'ANM', 'ANM', 'Anmälan', 'ANM',
        'Anmälan för installation av ventilation'),
       ('ANDRING_VA', 'I', 'LOV', 'ANM', 'ANM', 'Anmälan', 'ANM', 'Anmälan för ändring av VA'),
       ('INSTALLATION_VA', 'H', 'LOV', 'ANM', 'ANM', 'Anmälan', 'ANM',
        'Anmälan för installation av VA'),
       ('ANDRING_PLANLOSNING', 'ÄNDP', 'LOV', 'ANM', 'ANM', 'Anmälan', 'ANM',
        'Anmälan för ändring av planlösning'),
       ('ANDRING_BARANDE_KONSTRUKTION', 'L', 'LOV', 'ANM', 'ANM', 'Anmälan', 'ANM',
        'Anmälan för ändring av bärande konstruktion'),
       ('ANDRING_BRANDSKYDD', 'VÄS', 'LOV', 'ANM', 'ANM', 'Anmälan', 'ANM',
        'Anmälan för väsentlig ändring av brandskydd'),
       ('INSTALLLATION_HISS', 'G', 'LOV', 'ANM', 'ANM', 'Anmälan', 'ANM',
        'Anmälan för installation av hiss'),
       ('MARKLOV_SCHAKTNING', 'SCH', 'LOV', 'MARK', 'ANSÖKAN', 'Marklov', 'Marklov',
        'Marklov för schaktning'),
       ('MARKLOV_FYLL', 'FYL', 'LOV', 'MARK', 'ANSÖKAN', 'Marklov', 'Marklov', 'Marklov för fyll'),
       ('MARKLOV_TRADFALLNING', 'TRÄD', 'LOV', 'MARK', 'ANSÖKAN', 'Marklov', 'Marklov',
        'Marklov för trädfällning'),
       ('MARKLOV_OVRIGT', 'ÖVR', 'LOV', 'MARK', 'ANSÖKAN', 'Marklov', 'Marklov',
        'Marklov för övrigt'),
       ('STRANDSKYDD_OVRIGT', 'ÖVR', 'STRA', 'DI', 'ANSÖKAN', 'Strandskyddsdispens', 'Strand',
        'Strandskyddsdispens för övrigt');


INSERT INTO CaseMapping(caseId, externalCaseId, caseType, serviceName, system, timestamp,
                        municipalityId)
VALUES ('BYGG 2021-000200', '3522', 'NYBYGGNAD_ANSOKAN_OM_BYGGLOV',
        'Ansökan - strandskyddsdispens', 'BYGGR', '2023-05-12 14:53:58.672027', '2281'),


       ('e19981ad-34b2-4e14-88f5-133f61ca85aa', '2222', 'REGISTRERING_AV_LIVSMEDEL',
        'Registrering av livsmedelsanläggning', 'ECOS', '2023-05-12 14:53:58.672027', '2281'),


       ('e19981ad-34b2-4e14-88f5-133f61ca85aa', '2223', 'REGISTRERING_AV_LIVSMEDEL',
        'Registrering av livsmedelsanläggning', 'ECOS', '2023-05-12 14:53:58.672027', '2281'),
    

       ('24', '231', 'PARKING_PERMIT', 'Parkeringstillstånd', 'CASE_DATA',
        '2023-05-12 14:53:58.672027', '2281');
