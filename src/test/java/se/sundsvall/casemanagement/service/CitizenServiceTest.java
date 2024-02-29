package se.sundsvall.casemanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

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
import se.sundsvall.casemanagement.integration.citizen.CitizenClient;

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
		when(citizenClient.getPersonalNumber(personId)).thenReturn(personalNumberMock);

		final String personalNumber = citizenService.getPersonalNumber(personId);
		assertThat(personalNumber).isEqualTo(personalNumberMock);
	}

	@Test
	void testGetPersonalNumberWithNull() {
		final String personalNumber = citizenService.getPersonalNumber(null);
		assertThat(personalNumber).isNull();
	}

	@Test
	void testGetPersonalNumberWithEmptyString() {
		final String personalNumber = citizenService.getPersonalNumber("");
		assertThat(personalNumber).isNull();
	}

	@Test
	void testNotFound() {
		final String personId = UUID.randomUUID().toString();
		doThrow(Problem.valueOf(Status.NOT_FOUND)).when(citizenClient).getPersonalNumber(personId);

		assertThatThrownBy(() -> citizenService.getPersonalNumber(personId))
			.isInstanceOf(ThrowableProblem.class)
			.hasMessage("Bad Request: No personalNumber was found in CitizenMapping with personId: %s", personId)
			.hasFieldOrPropertyWithValue("status", Status.BAD_REQUEST);

	}

}
