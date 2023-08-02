package se.sundsvall.casemanagement.service;

import static org.zalando.problem.Status.NOT_FOUND;

import java.text.MessageFormat;
import java.util.List;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.util.Constants;

@Service
public class CaseMappingService {

    private final CaseMappingRepository caseMappingRepository;

    public CaseMappingService(CaseMappingRepository caseMappingRepository) {
        this.caseMappingRepository = caseMappingRepository;
    }

    public void postCaseMapping(CaseMapping caseMapping) {
        validateUniqueCase(caseMapping.getExternalCaseId());
        caseMappingRepository.save(caseMapping);
    }

    public List<CaseMapping> getCaseMapping(String externalCaseId, String caseId) {
        return caseMappingRepository.findAllByExternalCaseIdOrCaseId(externalCaseId, caseId);
    }

    public CaseMapping getCaseMapping(String externalCaseId) {
        List<CaseMapping> caseMappingList = getCaseMapping(externalCaseId, null);

        if (caseMappingList.isEmpty()) {
            throw Problem.valueOf(NOT_FOUND, Constants.ERR_MSG_CASES_NOT_FOUND);
        } else if (caseMappingList.size() > 1) {
            throw Problem.valueOf(NOT_FOUND, MessageFormat.format("More than one case was found with the same externalCaseId: \"{0}\". This should not be possible.", externalCaseId));
        } else {
            return caseMappingList.get(0);
        }
    }

    public List<CaseMapping> getAllCaseMappings() {
        List<CaseMapping> caseMappingList = caseMappingRepository.findAll();
        if (caseMappingList.isEmpty()) {
            throw Problem.valueOf(NOT_FOUND, Constants.ERR_MSG_CASES_NOT_FOUND);
        } else {
            return caseMappingList;
        }
    }

    public void validateUniqueCase(String externalCaseId) {
        if (externalCaseId != null) {

            List<CaseMapping> caseMappingList = caseMappingRepository.findAllByExternalCaseId(externalCaseId);

            if (!caseMappingList.isEmpty()) {
                throw Problem.valueOf(Status.BAD_REQUEST, MessageFormat.format("A resources already exists with the same externalCaseId: {0}", externalCaseId));
            }
        }
    }
}
