CREATE TABLE IF NOT EXISTS CaseEntity
(
    id             VARCHAR(255) NOT NULL PRIMARY KEY,
    deliveryStatus VARCHAR(255) NULL,
    dto            LONGTEXT     NULL

);

CREATE TABLE IF NOT EXISTS CaseMapping
(
    caseId         VARCHAR(255) NULL,
    externalCaseId VARCHAR(255) NULL,
    caseType       VARCHAR(255) NULL,
    serviceName    VARCHAR(255) NULL,
    `system`       VARCHAR(255) NULL,
    timestamp      DATETIME(6)  NULL,
    PRIMARY KEY (caseId, externalCaseId),
    UNIQUE INDEX externalCaseId (externalCaseId)
);

CREATE TABLE IF NOT EXISTS casetypedata
(
    value          VARCHAR(255) NULL,
    arendeslag     VARCHAR(255) NULL,
    arendegrupp    VARCHAR(255) NULL,
    arendetyp      VARCHAR(255) NULL,
    handelsetyp    VARCHAR(255) NULL,
    handelserubrik VARCHAR(255) NULL,
    handelseslag   VARCHAR(255) NULL,
    arendemening   VARCHAR(255) NULL
);
