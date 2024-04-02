package se.sundsvall.casemanagement.service.mapper;

import static se.sundsvall.casemanagement.util.Constants.SERVICE_NAME;

import java.util.Map;
import java.util.Optional;

import se.sundsvall.casemanagement.api.model.CaseDTO;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;

public class CaseMappingMapper {

	private CaseMappingMapper() {
		// Intentionally empty
	}

	public static CaseMapping toCaseMapping(final CaseDTO caseInput, final String caseId, final SystemType systemType) {
		return CaseMapping.builder()
			.withExternalCaseId(caseInput.getExternalCaseId())
			.withCaseId(caseId)
			.withSystem(systemType)
			.withCaseType(caseInput.getCaseType())
			.withServiceName(Optional.ofNullable(caseInput.getExtraParameters())
				.orElse(Map.of())
				.get(SERVICE_NAME))
			.build();
	}
}
