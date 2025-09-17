package se.sundsvall.casemanagement.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseTypeData;

@CircuitBreaker(name = "caseTypeDataRepository")
public interface CaseTypeDataRepository extends JpaRepository<CaseTypeData, String> {

}
