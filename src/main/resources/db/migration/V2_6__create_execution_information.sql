CREATE TABLE IF NOT EXISTS execution_information
(
    municipality_id           VARCHAR(4)  NOT NULL PRIMARY KEY,
    last_successful_execution DATETIME(6) NULL
);
