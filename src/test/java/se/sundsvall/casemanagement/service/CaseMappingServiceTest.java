package se.sundsvall.casemanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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

import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;

@ExtendWith(MockitoExtension.class)
class CaseMappingServiceTest {

	private static final String MUNICIPALITY_ID = "2281";

	@Mock
	private CaseMappingRepository caseMappingRepository;

	@InjectMocks
	private CaseMappingService caseMappingService;

	@Test
	void testPostCaseMapping() {
		//Arrange
		final var caseId = UUID.randomUUID().toString();
		final var caseDTO = new OtherCaseDTO();
		final var externalCaseId = "externalCaseId";
		caseDTO.setExternalCaseId(externalCaseId);
		//Mock
		when(caseMappingRepository.existsByExternalCaseIdAndMunicipalityId(any(), eq(MUNICIPALITY_ID))).thenReturn(false);
		//Act
		caseMappingService.postCaseMapping(caseDTO, caseId, SystemType.CASE_DATA, MUNICIPALITY_ID);
		//Assert
		verify(caseMappingRepository).save(any(CaseMapping.class));
	}

	@Test
	void testPostCaseMappingAlreadyExists() {
		//Arrange
		final var caseId = UUID.randomUUID().toString();
		final var caseDTO = new OtherCaseDTO();
		final var externalCaseId = "externalCaseId";
		caseDTO.setExternalCaseId(externalCaseId);
		//Mock
		when(caseMappingRepository.existsByExternalCaseIdAndMunicipalityId(caseDTO.getExternalCaseId(), MUNICIPALITY_ID)).thenReturn(true);

		//Act && Assert
		assertThatThrownBy(() -> caseMappingService.postCaseMapping(caseDTO, caseId, SystemType.ECOS, MUNICIPALITY_ID))
			.isInstanceOf(ThrowableProblem.class)
			.hasMessage(MessageFormat.format("Bad Request: A resources already exists with the same externalCaseId: {0}", externalCaseId))
			.hasFieldOrPropertyWithValue("status", Status.BAD_REQUEST);

		verify(caseMappingRepository, never()).save(any());
	}

	@Test
	void testGetCaseMappingWithExternalCaseId() {
		final var caseMappingInput = CaseMapping.builder()
			.withExternalCaseId(UUID.randomUUID().toString())
			.build();

		when(caseMappingRepository.findAllByMunicipalityIdAndExternalCaseIdOrCaseId(MUNICIPALITY_ID, caseMappingInput.getExternalCaseId(), null)).thenReturn(List.of(caseMappingInput));

		final var result = caseMappingService.getCaseMapping(caseMappingInput.getExternalCaseId(), MUNICIPALITY_ID);
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

		verify(caseMappingRepository).findAll();

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
		final var caseId = UUID.randomUUID().toString();

		when(caseMappingRepository.findAllByMunicipalityIdAndExternalCaseIdOrCaseId(MUNICIPALITY_ID, caseId, null)).thenReturn(List.of());

		assertThatThrownBy(() -> caseMappingService.getCaseMapping(caseId, MUNICIPALITY_ID))
			.isInstanceOf(ThrowableProblem.class)
			.hasMessage("Not Found: Case not found")
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND);
	}

	@Test
	void testGetCaseMappingWithExternalCaseIdMoreThanOneCase() {
		final var caseMappingInput = CaseMapping.builder()
			.withExternalCaseId(UUID.randomUUID().toString())
			.build();

		when(caseMappingRepository.findAllByMunicipalityIdAndExternalCaseIdOrCaseId(MUNICIPALITY_ID, caseMappingInput.getExternalCaseId(), null)).thenReturn(List.of(caseMappingInput, caseMappingInput));

		final var externalCaseId = caseMappingInput.getExternalCaseId();
		assertThatThrownBy(() -> caseMappingService.getCaseMapping(externalCaseId, MUNICIPALITY_ID))
			.isInstanceOf(ThrowableProblem.class)
			.hasMessage(MessageFormat.format("Not Found: More than one case was found with the same externalCaseId: \"{0}\". This should not be possible.", externalCaseId))
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND);
	}

	@Test
	void testGetCaseMappingWithCaseId() {
		final var caseMappingInput = CaseMapping.builder()
			.withExternalCaseId(UUID.randomUUID().toString())
			.build();

		when(caseMappingRepository.findAllByMunicipalityIdAndExternalCaseIdOrCaseId(MUNICIPALITY_ID, null, caseMappingInput.getCaseId())).thenReturn(List.of(caseMappingInput));

		final var result = caseMappingService.getCaseMapping(null, caseMappingInput.getCaseId(), MUNICIPALITY_ID);
		assertThat(result.getFirst().getCaseId()).isEqualTo(caseMappingInput.getCaseId());
	}

	@Test
	void testGetCaseMappingWithExternalCaseIdAndCaseId() {
		final var caseMappingInput = CaseMapping.builder()
			.withCaseId(UUID.randomUUID().toString())
			.withExternalCaseId(UUID.randomUUID().toString())
			.build();

		when(caseMappingRepository.findAllByMunicipalityIdAndExternalCaseIdOrCaseId(MUNICIPALITY_ID, caseMappingInput.getExternalCaseId(), caseMappingInput.getCaseId())).thenReturn(List.of(caseMappingInput));

		final var result = caseMappingService.getCaseMapping(caseMappingInput.getExternalCaseId(), caseMappingInput.getCaseId(), MUNICIPALITY_ID);
		assertThat(result.getFirst().getCaseId()).isEqualTo(caseMappingInput.getCaseId());
		assertThat(result.getFirst().getExternalCaseId()).isEqualTo(caseMappingInput.getExternalCaseId());
	}

	@Test
	void testValidateUniqueCase() {
		final var byggRCaseDTO = new ByggRCaseDTO();
		final var externalCaseId = UUID.randomUUID().toString();
		byggRCaseDTO.setExternalCaseId(externalCaseId);
		final var caseMappingInput = CaseMapping.builder()
			.withCaseId(UUID.randomUUID().toString())
			.withExternalCaseId(externalCaseId)
			.build();

		when(caseMappingRepository.existsByExternalCaseIdAndMunicipalityId(externalCaseId, MUNICIPALITY_ID)).thenReturn(true);

		assertThatThrownBy(() -> caseMappingService.validateUniqueCase(byggRCaseDTO, MUNICIPALITY_ID))
			.isInstanceOf(ThrowableProblem.class)
			.hasMessage(MessageFormat.format("Bad Request: A resources already exists with the same externalCaseId: {0}", caseMappingInput.getExternalCaseId()))
			.hasFieldOrPropertyWithValue("status", Status.BAD_REQUEST);
	}

}
