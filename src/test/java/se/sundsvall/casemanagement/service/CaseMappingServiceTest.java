package se.sundsvall.casemanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;

@ExtendWith(MockitoExtension.class)
class CaseMappingServiceTest {

	@InjectMocks
	private CaseMappingService caseMappingService;

	@Mock
	private CaseMappingRepository caseMappingRepository;

	@Test
	void testPostCaseMapping() {
		final var caseMappingInput = new CaseMapping();
		caseMappingInput.setExternalCaseId(UUID.randomUUID().toString());

		when(caseMappingRepository.findAllByExternalCaseId(caseMappingInput.getExternalCaseId())).thenReturn(List.of());

		caseMappingService.postCaseMapping(caseMappingInput);

		verify(caseMappingRepository, times(1)).save(caseMappingInput);
	}

	@Test
	void testPostCaseMappingAlreadyExists() {
		final CaseMapping caseMappingInput = new CaseMapping();
		caseMappingInput.setExternalCaseId(UUID.randomUUID().toString());

		when(caseMappingRepository.findAllByExternalCaseId(caseMappingInput.getExternalCaseId())).thenReturn(List.of(caseMappingInput));

		assertThatThrownBy(() -> caseMappingService.postCaseMapping(caseMappingInput))
			.isInstanceOf(ThrowableProblem.class)
			.hasMessage(MessageFormat.format("Bad Request: A resources already exists with the same externalCaseId: {0}", caseMappingInput.getExternalCaseId()))
			.hasFieldOrPropertyWithValue("status", Status.BAD_REQUEST);

		verify(caseMappingRepository, times(0)).save(caseMappingInput);
	}

	@Test
	void testGetCaseMappingWithExternalCaseId() {
		final CaseMapping caseMappingInput = new CaseMapping();
		caseMappingInput.setExternalCaseId(UUID.randomUUID().toString());

		when(caseMappingRepository.findAllByExternalCaseIdOrCaseId(caseMappingInput.getExternalCaseId(), null))
			.thenReturn(List.of(caseMappingInput));

		final var result = caseMappingService.getCaseMapping(caseMappingInput.getExternalCaseId());
		assertThat(result.getExternalCaseId()).isEqualTo(caseMappingInput.getExternalCaseId());
	}


	@Test
	void getAllCaseMappings() {
		when(caseMappingRepository.findAll()).thenReturn(List.of(CaseMapping.builder()
				.withCaseId("caseId")
				.withExternalCaseId("externalCaseId")
				.withCaseType(CaseType.REGISTRERING_AV_LIVSMEDEL.toString())
				.withServiceName("serviceName")
				.withTimestamp(LocalDateTime.now())
				.build(),
			CaseMapping.builder()
				.withCaseId("caseId2")
				.withExternalCaseId("externalCaseId2")
				.withCaseType(CaseType.LOST_PARKING_PERMIT.toString())
				.withServiceName("serviceName2")
				.withTimestamp(LocalDateTime.now())
				.build()));

		final var result = caseMappingService.getAllCaseMappings();

		verify(caseMappingRepository, times(1)).findAll();

		assertThat(result).hasSize(2);
		assertThat(result.getFirst().getCaseId()).isEqualTo("caseId");
		assertThat(result.getFirst().getExternalCaseId()).isEqualTo("externalCaseId");
		assertThat(result.getFirst().getCaseType()).isEqualTo(CaseType.REGISTRERING_AV_LIVSMEDEL.toString());
		assertThat(result.getFirst().getServiceName()).isEqualTo("serviceName");
		assertThat(result.getFirst().getTimestamp()).isNotNull();

		assertThat(result.get(1).getCaseId()).isEqualTo("caseId2");
		assertThat(result.get(1).getExternalCaseId()).isEqualTo("externalCaseId2");
		assertThat(result.get(1).getCaseType()).isEqualTo(CaseType.LOST_PARKING_PERMIT.toString());
		assertThat(result.get(1).getServiceName()).isEqualTo("serviceName2");
		assertThat(result.get(1).getTimestamp()).isNotNull();

	}

	@Test
	void testGetCaseMappingWithExternalCaseIdNotFound() {
		final CaseMapping caseMappingInput = new CaseMapping();
		final String externalCaseId = UUID.randomUUID().toString();
		caseMappingInput.setExternalCaseId(externalCaseId);

		when(caseMappingRepository.findAllByExternalCaseIdOrCaseId(externalCaseId, null)).thenReturn(List.of());


		assertThatThrownBy(() -> caseMappingService.getCaseMapping(externalCaseId))
			.isInstanceOf(ThrowableProblem.class)
			.hasMessage("Not Found: Case not found")
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND);
	}

	@Test
	void testGetCaseMappingWithExternalCaseIdMoreThanOneCase() {
		final CaseMapping caseMappingInput = new CaseMapping();
		caseMappingInput.setExternalCaseId(UUID.randomUUID().toString());

		when(caseMappingRepository.findAllByExternalCaseIdOrCaseId(caseMappingInput.getExternalCaseId(), null)).thenReturn(List.of(caseMappingInput, caseMappingInput));

		final var externalCaseId = caseMappingInput.getExternalCaseId();
		assertThatThrownBy(() -> caseMappingService.getCaseMapping(externalCaseId))
			.isInstanceOf(ThrowableProblem.class)
			.hasMessage(MessageFormat.format("Not Found: More than one case was found with the same externalCaseId: \"{0}\". This should not be possible.", caseMappingInput.getExternalCaseId()))
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND);
	}

	@Test
	void testGetCaseMappingWithCaseId() {
		final CaseMapping caseMappingInput = new CaseMapping();
		caseMappingInput.setCaseId(UUID.randomUUID().toString());

		when(caseMappingRepository.findAllByExternalCaseIdOrCaseId(null, caseMappingInput.getCaseId())).thenReturn(List.of(caseMappingInput));

		final var result = caseMappingService.getCaseMapping(null, caseMappingInput.getCaseId());
		assertThat(result.getFirst().getCaseId()).isEqualTo(caseMappingInput.getCaseId());
	}

	@Test
	void testGetCaseMappingWithExternalCaseIdAndCaseId() {
		final CaseMapping caseMappingInput = new CaseMapping();
		caseMappingInput.setCaseId(UUID.randomUUID().toString());
		caseMappingInput.setExternalCaseId(UUID.randomUUID().toString());

		when(caseMappingRepository.findAllByExternalCaseIdOrCaseId(caseMappingInput.getExternalCaseId(), caseMappingInput.getCaseId())).thenReturn(List.of(caseMappingInput));

		final var result = caseMappingService.getCaseMapping(caseMappingInput.getExternalCaseId(), caseMappingInput.getCaseId());
		assertThat(result.getFirst().getCaseId()).isEqualTo(caseMappingInput.getCaseId());
		assertThat(result.getFirst().getExternalCaseId()).isEqualTo(caseMappingInput.getExternalCaseId());
	}

	@Test
	void testValidateUniqueCase() {
		final CaseMapping caseMappingInput = new CaseMapping();
		caseMappingInput.setCaseId(UUID.randomUUID().toString());
		final String externalCaseId = UUID.randomUUID().toString();
		caseMappingInput.setExternalCaseId(externalCaseId);

		when(caseMappingRepository.findAllByExternalCaseId(externalCaseId)).thenReturn(List.of(caseMappingInput));

		assertThatThrownBy(() -> caseMappingService.validateUniqueCase(externalCaseId))
			.isInstanceOf(ThrowableProblem.class)
			.hasMessage(MessageFormat.format("Bad Request: A resources already exists with the same externalCaseId: {0}", caseMappingInput.getExternalCaseId()))
			.hasFieldOrPropertyWithValue("status", Status.BAD_REQUEST);
	}

}
