package se.sundsvall.casemanagement.integration.db;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.integration.db.model.CaseMappingId;

import java.util.List;

public interface CaseMappingRepository extends JpaRepository<CaseMapping, CaseMappingId> {

    List<CaseMapping> findAllByExternalCaseIdOrCaseId(String externalCaseId, String caseId);

    List<CaseMapping> findAllByExternalCaseId(String externalCaseId);
}
