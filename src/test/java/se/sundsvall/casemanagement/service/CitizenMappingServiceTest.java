package se.sundsvall.casemanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.casemanagement.TestUtil;
import se.sundsvall.casemanagement.integration.citizenmapping.CitizenMappingClient;
import se.sundsvall.casemanagement.util.Constants;

@ExtendWith(MockitoExtension.class)
class CitizenMappingServiceTest {

	@InjectMocks
	private CitizenMappingService citizenMappingService;
	@Mock
	private CitizenMappingClient citizenMappingClient;

	@Test
	void testGetPersonalNumber() {
		final String personalNumberMock = TestUtil.generateRandomPersonalNumber();
		final String personId = UUID.randomUUID().toString();
		doReturn(personalNumberMock).when(citizenMappingClient).getPersonalNumber(personId);

		final String personalNumber = citizenMappingService.getPersonalNumber(personId);
		assertEquals(personalNumberMock, personalNumber);
	}

	@Test
	void testGetPersonalNumberWithNull() {
		final String personalNumber = citizenMappingService.getPersonalNumber(null);
		assertNull(personalNumber);
	}

	@Test
	void testGetPersonalNumberWithEmptyString() {
		final String personalNumber = citizenMappingService.getPersonalNumber("");
		assertNull(personalNumber);
	}

	@Test
	void testNotFound() {
		final String personId = UUID.randomUUID().toString();
		doThrow(Problem.valueOf(Status.NOT_FOUND)).when(citizenMappingClient).getPersonalNumber(personId);
		final var problem = assertThrows(ThrowableProblem.class, () -> citizenMappingService.getPersonalNumber(personId));
		assertEquals(Status.BAD_REQUEST, problem.getStatus());
		assertEquals(String.format(Constants.ERR_MSG_PERSONAL_NUMBER_NOT_FOUND_WITH_PERSON_ID, personId), problem.getDetail());
	}

}
