package se.sundsvall.casemanagement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.service.exceptions.ApplicationException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static se.sundsvall.casemanagement.util.Constants.ERR_MSG_CASES_NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class CaseMappingServiceTest {

    @InjectMocks
    private CaseMappingService caseMappingService;

    @Mock
    private CaseMappingRepository caseMappingRepository;

    @Test
    void testPostCaseMapping() {
        CaseMapping caseMappingInput = new CaseMapping();
        caseMappingInput.setExternalCaseId(UUID.randomUUID().toString());

        doReturn(new ArrayList<>()).when(caseMappingRepository).findAllByExternalCaseId(caseMappingInput.getExternalCaseId());

        caseMappingService.postCaseMapping(caseMappingInput);

        verify(caseMappingRepository, times(1)).save(caseMappingInput);
    }

    @Test
    void testPostCaseMappingAlreadyExists() {
        CaseMapping caseMappingInput = new CaseMapping();
        caseMappingInput.setExternalCaseId(UUID.randomUUID().toString());

        doReturn(List.of(caseMappingInput)).when(caseMappingRepository).findAllByExternalCaseId(caseMappingInput.getExternalCaseId());

        var problem = assertThrows(ThrowableProblem.class, () -> caseMappingService.postCaseMapping(caseMappingInput));
        assertEquals(Status.BAD_REQUEST, problem.getStatus());
        assertEquals(MessageFormat.format("A resources already exists with the same externalCaseId: {0}", caseMappingInput.getExternalCaseId()), problem.getDetail());

        verify(caseMappingRepository, times(0)).save(caseMappingInput);
    }

    @Test
    void testGetCaseMappingWithExternalCaseId() throws ApplicationException {
        CaseMapping caseMappingInput = new CaseMapping();
        caseMappingInput.setExternalCaseId(UUID.randomUUID().toString());

        doReturn(List.of(caseMappingInput)).when(caseMappingRepository).findAllByExternalCaseIdOrCaseId(caseMappingInput.getExternalCaseId(), null);

        var result = caseMappingService.getCaseMapping(caseMappingInput.getExternalCaseId());
        assertEquals(caseMappingInput.getExternalCaseId(), result.getExternalCaseId());
    }

    @Test
    void testGetCaseMappingWithExternalCaseIdNotFound() {
        CaseMapping caseMappingInput = new CaseMapping();
        String externalCaseId = UUID.randomUUID().toString();
        caseMappingInput.setExternalCaseId(externalCaseId);

        doReturn(new ArrayList<>()).when(caseMappingRepository).findAllByExternalCaseIdOrCaseId(externalCaseId, null);

        var problem = assertThrows(ThrowableProblem.class, () -> caseMappingService.getCaseMapping(externalCaseId));
        assertEquals(Status.NOT_FOUND, problem.getStatus());
        assertEquals(ERR_MSG_CASES_NOT_FOUND, problem.getDetail());
    }

    @Test
    void testGetCaseMappingWithExternalCaseIdMoreThanOneCase() {
        CaseMapping caseMappingInput = new CaseMapping();
        caseMappingInput.setExternalCaseId(UUID.randomUUID().toString());

        doReturn(List.of(caseMappingInput, caseMappingInput)).when(caseMappingRepository).findAllByExternalCaseIdOrCaseId(caseMappingInput.getExternalCaseId(), null);

        var exception = assertThrows(ApplicationException.class, () -> caseMappingService.getCaseMapping(caseMappingInput.getExternalCaseId()));
        assertEquals(MessageFormat.format("More than one case was found with the same externalCaseId: \"{0}\". This should not be possible.", caseMappingInput.getExternalCaseId()), exception.getMessage());
    }

    @Test
    void testGetCaseMappingWithCaseId() {
        CaseMapping caseMappingInput = new CaseMapping();
        caseMappingInput.setCaseId(UUID.randomUUID().toString());

        doReturn(List.of(caseMappingInput)).when(caseMappingRepository).findAllByExternalCaseIdOrCaseId(null, caseMappingInput.getCaseId());

        var result = caseMappingService.getCaseMapping(null, caseMappingInput.getCaseId());
        assertEquals(caseMappingInput.getCaseId(), result.get(0).getCaseId());
    }

    @Test
    void testGetCaseMappingWithExternalCaseIdAndCaseId() {
        CaseMapping caseMappingInput = new CaseMapping();
        caseMappingInput.setCaseId(UUID.randomUUID().toString());
        caseMappingInput.setExternalCaseId(UUID.randomUUID().toString());

        doReturn(List.of(caseMappingInput)).when(caseMappingRepository).findAllByExternalCaseIdOrCaseId(caseMappingInput.getExternalCaseId(), caseMappingInput.getCaseId());

        var result = caseMappingService.getCaseMapping(caseMappingInput.getExternalCaseId(), caseMappingInput.getCaseId());
        assertEquals(caseMappingInput.getCaseId(), result.get(0).getCaseId());
        assertEquals(caseMappingInput.getExternalCaseId(), result.get(0).getExternalCaseId());
    }

    @Test
    void testValidateUniqueCase() {
        CaseMapping caseMappingInput = new CaseMapping();
        caseMappingInput.setCaseId(UUID.randomUUID().toString());
        String externalCaseId = UUID.randomUUID().toString();
        caseMappingInput.setExternalCaseId(externalCaseId);

        doReturn(List.of(caseMappingInput)).when(caseMappingRepository).findAllByExternalCaseId(externalCaseId);

        var problem = assertThrows(ThrowableProblem.class, () -> caseMappingService.validateUniqueCase(externalCaseId));
        assertEquals(Status.BAD_REQUEST, problem.getStatus());
        assertEquals(MessageFormat.format("A resources already exists with the same externalCaseId: {0}", caseMappingInput.getExternalCaseId()), problem.getDetail());
    }

}
