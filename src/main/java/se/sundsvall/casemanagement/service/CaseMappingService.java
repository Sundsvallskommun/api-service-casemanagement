package se.sundsvall.casemanagement.service;

import static org.zalando.problem.Status.NOT_FOUND;

import java.text.MessageFormat;
import java.util.List;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.casemanagement.api.model.CaseDTO;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.util.Constants;

@Service
public class CaseMappingService {

	private final CaseMappingRepository caseMappingRepository;

	public CaseMappingService(final CaseMappingRepository caseMappingRepository) {
		this.caseMappingRepository = caseMappingRepository;
	}

	public void postCaseMapping(final CaseDTO caseInput, final String caseId, final SystemType systemType) {
		validateUniqueCase(caseId);
		caseMappingRepository.save(CaseMappingMapper.toCaseMapping(caseInput, caseId, systemType));
	}

	public List<CaseMapping> getCaseMapping(final String externalCaseId, final String caseId) {
		return caseMappingRepository.findAllByExternalCaseIdOrCaseId(externalCaseId, caseId);
	}

	public CaseMapping getCaseMapping(final String externalCaseId) {
		final List<CaseMapping> caseMappingList = getCaseMapping(externalCaseId, null);

		if (caseMappingList.isEmpty()) {
			throw Problem.valueOf(NOT_FOUND, Constants.ERR_MSG_CASES_NOT_FOUND);
		}
		if (caseMappingList.size() > 1) {
			throw Problem.valueOf(NOT_FOUND, MessageFormat.format("More than one case was found with the same externalCaseId: \"{0}\". This should not be possible.", externalCaseId));
		} else {
			return caseMappingList.getFirst();
		}
	}

	public List<CaseMapping> getAllCaseMappings() {
		final List<CaseMapping> caseMappingList = caseMappingRepository.findAll();
		if (caseMappingList.isEmpty()) {
			throw Problem.valueOf(NOT_FOUND, Constants.ERR_MSG_CASES_NOT_FOUND);
		}
		return caseMappingList;
	}

	public void validateUniqueCase(final String externalCaseId) {
		if (externalCaseId != null) {

			final List<CaseMapping> caseMappingList = caseMappingRepository.findAllByExternalCaseId(externalCaseId);

			if (!caseMappingList.isEmpty()) {
				throw Problem.valueOf(Status.BAD_REQUEST, MessageFormat.format("A resources already exists with the same externalCaseId: {0}", externalCaseId));
			}
		}
	}


}
