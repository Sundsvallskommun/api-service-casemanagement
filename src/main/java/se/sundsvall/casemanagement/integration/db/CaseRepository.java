package se.sundsvall.casemanagement.integration.db;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.casemanagement.integration.db.model.CaseEntity;

public interface CaseRepository extends JpaRepository<CaseEntity, String> {

	Optional<CaseEntity> findByIdAndMunicipalityId(String id, String municipalityId);

}
