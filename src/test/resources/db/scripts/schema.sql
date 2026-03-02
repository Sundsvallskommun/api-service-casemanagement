
    create table CaseEntity (
        created datetime(6),
        id varchar(255) not null,
        municipalityId varchar(255),
        request_id varchar(255),
        deliveryStatus varchar(255),
        dto longtext,
        primary key (id)
    ) engine=InnoDB;

    create table CaseMapping (
        timestamp datetime(6),
        caseId varchar(255) not null,
        caseType varchar(255) not null,
        externalCaseId varchar(255) not null,
        municipalityId varchar(255),
        serviceName varchar(255),
        system varchar(255) not null,
        primary key (caseId, externalCaseId)
    ) engine=InnoDB;

    create table casetypedata (
        arendeGrupp varchar(255),
        arendeMening varchar(255),
        arendeSlag varchar(255),
        arendeTyp varchar(255),
        handelseRubrik varchar(255),
        handelseSlag varchar(255),
        handelseTyp varchar(255),
        value varchar(255) not null,
        primary key (value)
    ) engine=InnoDB;

    create table execution_information (
        municipality_id varchar(4) not null,
        id bigint not null auto_increment,
        last_successful_execution datetime(6),
        job_name varchar(50) not null,
        primary key (id)
    ) engine=InnoDB;

    create index case_entity_municipality_id_idx 
       on CaseEntity (municipalityId);

    create index case_mapping_municipality_id_idx 
       on CaseMapping (municipalityId);

    alter table if exists CaseMapping 
       add constraint UK9x4rtmb794uqwa8fwh2jjm5ol unique (externalCaseId);

    alter table if exists execution_information 
       add constraint uq_execution_information_municipality_job unique (municipality_id, job_name);
