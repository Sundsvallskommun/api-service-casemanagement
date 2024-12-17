package se.sundsvall.casemanagement.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseEntity;

@CircuitBreaker(name = "caseRepository")
public interface CaseRepository extends JpaRepository<CaseEntity, String> {

	Optional<CaseEntity> findByIdAndMunicipalityId(String id, String municipalityId);

}
