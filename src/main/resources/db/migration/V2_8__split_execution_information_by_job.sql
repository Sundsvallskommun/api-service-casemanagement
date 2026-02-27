ALTER TABLE execution_information
    DROP PRIMARY KEY;

ALTER TABLE execution_information
    ADD COLUMN job_name VARCHAR(50) NOT NULL DEFAULT 'UNKNOWN';

UPDATE execution_information
SET job_name = 'BYGGR_STATUS'
where job_name is null;

INSERT INTO execution_information (municipality_id, last_successful_execution, job_name)
SELECT municipality_id, last_successful_execution, 'ECOS_STATUS'
FROM execution_information
WHERE job_name = 'BYGGR_STATUS';

ALTER TABLE execution_information
    ADD COLUMN id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;

ALTER TABLE execution_information
    ADD UNIQUE KEY uq_execution_information_municipality_job (municipality_id, job_name);
