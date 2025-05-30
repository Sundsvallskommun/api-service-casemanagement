package se.sundsvall.casemanagement.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.integration.db.model.CaseMappingId;

@CircuitBreaker(name = "caseMappingRepository")
public interface CaseMappingRepository extends JpaRepository<CaseMapping, CaseMappingId> {

	List<CaseMapping> findAllByMunicipalityIdAndExternalCaseIdOrCaseId(String municipalityId, String externalCaseId, String caseId);

	List<CaseMapping> findAllByExternalCaseIdAndMunicipalityId(String externalCaseId, String municipalityId);

	boolean existsByExternalCaseIdAndMunicipalityId(String externalCaseId, String municipalityId);

}
