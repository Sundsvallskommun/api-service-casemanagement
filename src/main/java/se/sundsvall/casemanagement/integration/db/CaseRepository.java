package se.sundsvall.casemanagement.integration.db;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.casemanagement.integration.db.model.CaseEntity;

public interface CaseRepository extends JpaRepository<CaseEntity, String> {
}
