DROP TABLE IF EXISTS casetypedata;

CREATE TABLE casetypedata
(
    value           VARCHAR(255),
    arendeslag     VARCHAR(255),
    arendegrupp    VARCHAR(255),
    arendetyp      VARCHAR(255),
    handelsetyp    VARCHAR(255),
    handelserubrik VARCHAR(255),
    handelseslag   VARCHAR(255),
    arendemening   VARCHAR(255)
);

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
       ('ANMALAN_ELDSTAD', NULL, 'LOV', 'ANM', 'ANM', NULL, NULL, NULL);