package se.sundsvall.casemanagement.integration.db;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.integration.db.model.CaseTypeData;

public interface CaseTypeDataRepository extends JpaRepository<CaseTypeData, CaseType> {

}
