alter table CaseEntity
    add column municipalityId varchar(255);

alter table CaseMapping
    add column municipalityId varchar(255);

create index case_entity_municipality_id_idx
    on CaseEntity (municipalityId);

create index case_mapping_municipality_id_idx
    on CaseMapping (municipalityId);
