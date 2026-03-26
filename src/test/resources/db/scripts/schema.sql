    create table byggr_case_type_config (
        arende_grupp varchar(255),
        arende_mening varchar(255),
        arende_slag varchar(255),
        arende_typ varchar(255),
        case_type_name varchar(255) not null,
        handelse_rubrik varchar(255),
        handelse_slag varchar(255),
        handelse_typ varchar(255),
        update_handler varchar(255),
        primary key (case_type_name)
    ) engine=InnoDB;

    create table byggr_status_mapping (
        id bigint not null auto_increment,
        handelse_slag varchar(255),
        handelse_typ varchar(255),
        handelse_utfall varchar(255),
        return_field varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table case_entity (
        created datetime(6),
        id varchar(255) not null,
        municipalityId varchar(255),
        request_id varchar(255),
        deliveryStatus varchar(255),
        dto longtext,
        primary key (id)
    ) engine=InnoDB;

    create table case_mapping (
        timestamp datetime(6),
        caseId varchar(255) not null,
        caseType varchar(255) not null,
        externalCaseId varchar(255) not null,
        municipalityId varchar(255),
        serviceName varchar(255),
        system varchar(255) not null,
        primary key (caseId, externalCaseId)
    ) engine=InnoDB;

    create table case_type (
        nullable_facility bit,
        nullable_facility_type bit,
        facility_type_rule varchar(255),
        name varchar(255) not null,
        system_type enum ('ALKT','BYGGR','CASE_DATA','ECOS','EDPFUTURE') not null,
        primary key (name)
    ) engine=InnoDB;

    create table ecos_case_type_config (
        case_type_name varchar(255) not null,
        diary_plan_id varchar(255),
        process_type_id varchar(255),
        facility_handler enum ('EXISTING_FACILITY','FOOD','HEALTH_PROTECTION','HEAT_PUMP','INDIVIDUAL_SEWAGE','NONE','RISK_CLASS_UPDATE'),
        primary key (case_type_name)
    ) engine=InnoDB;

    create table execution_information (
        municipality_id varchar(4) not null,
        id bigint not null auto_increment,
        last_successful_execution datetime(6),
        job_name varchar(50) not null,
        primary key (id)
    ) engine=InnoDB;

    create index case_entity_municipality_id_idx 
       on case_entity (municipalityId);

    create index case_mapping_municipality_id_idx 
       on case_mapping (municipalityId);

    alter table if exists case_mapping 
       add constraint UKqfce0ayqm3nkmeteyrwmadgxe unique (externalCaseId);

    alter table if exists execution_information 
       add constraint uq_execution_information_municipality_job unique (municipality_id, job_name);
