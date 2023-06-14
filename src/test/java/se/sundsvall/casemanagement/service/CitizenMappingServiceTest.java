package se.sundsvall.casemanagement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casemanagement.TestUtil;
import se.sundsvall.casemanagement.integration.rest.citizenmapping.CitizenMappingClient;
import se.sundsvall.casemanagement.util.Constants;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class CitizenMappingServiceTest {

    @InjectMocks
    private CitizenMappingService citizenMappingService;
    @Mock
    private CitizenMappingClient citizenMappingClient;

    @Test
    void testGetPersonalNumber() {
        String personalNumberMock = TestUtil.generateRandomPersonalNumber();
        String personId = UUID.randomUUID().toString();
        doReturn(personalNumberMock).when(citizenMappingClient).getPersonalNumber(personId);

        String personalNumber = citizenMappingService.getPersonalNumber(personId);
        assertEquals(personalNumberMock, personalNumber);
    }

    @Test
    void testGetPersonalNumberWithNull() {
        String personalNumber = citizenMappingService.getPersonalNumber(null);
        assertNull(personalNumber);
    }

    @Test
    void testGetPersonalNumberWithEmptyString() {
        String personalNumber = citizenMappingService.getPersonalNumber("");
        assertNull(personalNumber);
    }

    @Test
    void testNotFound() {
        String personId = UUID.randomUUID().toString();
        doThrow(Problem.valueOf(Status.NOT_FOUND)).when(citizenMappingClient).getPersonalNumber(personId);
        var problem = assertThrows(ThrowableProblem.class, () -> citizenMappingService.getPersonalNumber(personId));
        assertEquals(Status.BAD_REQUEST, problem.getStatus());
        assertEquals(Constants.ERR_MSG_PERSONAL_NUMBER_NOT_FOUND_WITH_PERSON_ID(personId), problem.getDetail());
    }

}
