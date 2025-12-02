ALTER TABLE CaseEntity
    ADD COLUMN request_id VARCHAR(255);

ALTER TABLE CaseEntity
    ADD COLUMN created DATETIME(6);
