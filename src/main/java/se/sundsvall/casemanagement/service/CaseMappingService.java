package se.sundsvall.casemanagement.service;

import java.text.MessageFormat;
import java.util.List;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;
import se.sundsvall.casemanagement.api.model.CaseDTO;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;

import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casemanagement.service.mapper.CaseMappingMapper.toCaseMapping;
import static se.sundsvall.casemanagement.util.Constants.ERR_MSG_CASES_NOT_FOUND;

@Service
public class CaseMappingService {

	private final CaseMappingRepository caseMappingRepository;

	public CaseMappingService(final CaseMappingRepository caseMappingRepository) {
		this.caseMappingRepository = caseMappingRepository;
	}

	public void postCaseMapping(final CaseDTO caseInput, final String caseId, final SystemType systemType, final String municipalityId) {
		validateUniqueCase(caseInput, municipalityId);
		caseMappingRepository.save(toCaseMapping(caseInput, caseId, systemType, municipalityId));
	}

	public List<CaseMapping> getCaseMapping(final String externalCaseId, final String caseId, final String municipalityId) {
		return caseMappingRepository.findAllByMunicipalityIdAndExternalCaseIdOrCaseId(municipalityId, externalCaseId, caseId);
	}

	public CaseMapping getCaseMapping(final String externalCaseId, final String municipalityId) {
		final List<CaseMapping> caseMappingList = getCaseMapping(externalCaseId, null, municipalityId);

		if (caseMappingList.isEmpty()) {
			throw Problem.valueOf(NOT_FOUND, ERR_MSG_CASES_NOT_FOUND);
		}
		if (caseMappingList.size() > 1) {
			throw Problem.valueOf(NOT_FOUND, MessageFormat.format("More than one case was found with the same externalCaseId: \"{0}\". This should not be possible.", externalCaseId));
		}

		return caseMappingList.getFirst();
	}

	public List<CaseMapping> getAllCaseMappings() {
		final List<CaseMapping> caseMappingList = caseMappingRepository.findAll();

		if (caseMappingList.isEmpty()) {
			throw Problem.valueOf(NOT_FOUND, ERR_MSG_CASES_NOT_FOUND);
		}

		return caseMappingList;
	}

	public void validateUniqueCase(final CaseDTO caseDTO, final String municipalityId) {

		final var externalCaseId = caseDTO.getExternalCaseId();
		if (caseDTO instanceof final ByggRCaseDTO byggRCaseDTO
			&& byggRCaseDTO.getExtraParameters() != null
			&& "PUT".equalsIgnoreCase(byggRCaseDTO.getExtraParameters().get("oepAction"))) {
			return;
		}

		if (externalCaseId != null && caseMappingRepository.existsByExternalCaseIdAndMunicipalityId(externalCaseId, municipalityId)) {
			throw Problem.valueOf(Status.BAD_REQUEST, MessageFormat.format("A resources already exists with the same externalCaseId: {0}", externalCaseId));
		}
	}

}
