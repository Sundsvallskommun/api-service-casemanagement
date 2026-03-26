package se.sundsvall.casemanagement.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.casemanagement.integration.db.model.ByggrStatusMappingEntity;

@CircuitBreaker(name = "byggrStatusMappingRepository")
public interface ByggrStatusMappingRepository extends JpaRepository<ByggrStatusMappingEntity, Long> {

}
