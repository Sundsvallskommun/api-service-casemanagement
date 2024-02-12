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
import se.sundsvall.casemanagement.integration.citizen.CitizenClient;
import se.sundsvall.casemanagement.util.Constants;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class CitizenServiceTest {

	@InjectMocks
	private CitizenService citizenService;
	@Mock
	private CitizenClient citizenClient;

	@Test
	void testGetPersonalNumber() {
		final String personalNumberMock = TestUtil.generateRandomPersonalNumber();
		final String personId = UUID.randomUUID().toString();
		doReturn(personalNumberMock).when(citizenClient).getPersonalNumber(personId);

		final String personalNumber = citizenService.getPersonalNumber(personId);
		assertEquals(personalNumberMock, personalNumber);
	}

	@Test
	void testGetPersonalNumberWithNull() {
		final String personalNumber = citizenService.getPersonalNumber(null);
		assertNull(personalNumber);
	}

	@Test
	void testGetPersonalNumberWithEmptyString() {
		final String personalNumber = citizenService.getPersonalNumber("");
		assertNull(personalNumber);
	}

	@Test
	void testNotFound() {
		final String personId = UUID.randomUUID().toString();
		doThrow(Problem.valueOf(Status.NOT_FOUND)).when(citizenClient).getPersonalNumber(personId);
		final var problem = assertThrows(ThrowableProblem.class, () -> citizenService.getPersonalNumber(personId));
		assertEquals(Status.BAD_REQUEST, problem.getStatus());
		assertEquals(String.format(Constants.ERR_MSG_PERSONAL_NUMBER_NOT_FOUND_WITH_PERSON_ID, personId), problem.getDetail());
	}

}
