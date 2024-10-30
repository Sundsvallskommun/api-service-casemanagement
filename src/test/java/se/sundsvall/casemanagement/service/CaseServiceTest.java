package se.sundsvall.casemanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static se.sundsvall.casemanagement.TestUtil.createFacilityDTO;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;
import se.sundsvall.casemanagement.api.model.EcosCaseDTO;
import se.sundsvall.casemanagement.api.model.FacilityDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.integration.db.CaseRepository;
import se.sundsvall.casemanagement.service.event.IncomingByggrCase;
import se.sundsvall.casemanagement.service.event.IncomingEcosCase;
import se.sundsvall.casemanagement.service.event.IncomingOtherCase;
import se.sundsvall.casemanagement.service.util.Validator;

@ExtendWith(MockitoExtension.class)
class CaseServiceTest {

	private static final String MUNICIPALITY_ID = "2281";

	@Spy
	private Validator validator;

	@Mock
	private CaseRepository caseRepository;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@InjectMocks
	private CaseService caseService;

	@Captor
	private ArgumentCaptor<IncomingByggrCase> byggrCaseCaptor;

	@Captor
	private ArgumentCaptor<IncomingEcosCase> ecosCaseCaptor;

	@Captor
	private ArgumentCaptor<IncomingOtherCase> otherCaseCaptor;

	@Test
	void testHandleByggRCase() {
		// Arrange
		final var byggRCase = ByggRCaseDTO.builder()
			.withStakeholders(List.of())
			.withCaseType(CaseType.ANDRING_ANSOKAN_OM_BYGGLOV.toString())
			.withFacilities(List.of(createFacilityDTO(CaseType.ANDRING_ANSOKAN_OM_BYGGLOV)))
			.build();
		// Act
		caseService.handleCase(byggRCase, MUNICIPALITY_ID);
		// Assert
		verify(validator).validateByggrErrand(byggRCase);
		verify(eventPublisher).publishEvent(byggrCaseCaptor.capture());
		verify(caseRepository).save(any());
		final var incomingByggrCase = byggrCaseCaptor.getValue();
		assertThat(incomingByggrCase.getSource()).isEqualTo(caseService);
		assertThat(incomingByggrCase.getPayload()).isEqualTo(byggRCase);
	}

	@ParameterizedTest
	@EnumSource(value = CaseType.class,
		names = {
			"MARKLOV_FYLL",
			"MARKLOV_SCHAKTNING", "MARKLOV_TRADFALLNING", "MARKLOV_OVRIGT",
			"STRANDSKYDD_OVRIGT"
		})
	void testHandleByggRCaseNoFacilityTypeAllowed(final CaseType caseType) {
		// Arrange
		final var address = AddressDTO.builder()
			.withAddressCategories(List.of())
			.withPropertyDesignation("propertyDesignation")
			.build();

		final var facility = FacilityDTO.builder()
			.withAddress(address)
			.build();

		final var byggRCase = ByggRCaseDTO.builder()
			.withStakeholders(List.of())
			.withFacilities(List.of(facility))
			.withCaseType(caseType.toString())
			.build();

		// Act
		caseService.handleCase(byggRCase, MUNICIPALITY_ID);
		// Assert
		verify(validator).validateByggrErrand(byggRCase);
		verify(eventPublisher).publishEvent(byggrCaseCaptor.capture());
		verify(caseRepository).save(any());

		final var incomingByggrCase = byggrCaseCaptor.getValue();
		assertThat(incomingByggrCase.getSource()).isEqualTo(caseService);
		assertThat(incomingByggrCase.getPayload()).isEqualTo(byggRCase);
	}

	@ParameterizedTest
	@EnumSource(value = CaseType.class,
		names = {
			"NYBYGGNAD_ANSOKAN_OM_BYGGLOV",
			"TILLBYGGNAD_ANSOKAN_OM_BYGGLOV", "STRANDSKYDD_ANDRAD_ANVANDNING"
		})
	void testHandleByggRCaseNoFacilityType_notAllowed(final CaseType caseType) {
		// Arrange
		final var address = AddressDTO.builder()
			.withPropertyDesignation("propertyDesignation")
			.withAddressCategories(List.of())
			.build();

		final var facility = FacilityDTO.builder()
			.withAddress(address)
			.build();

		final var byggRCase = ByggRCaseDTO.builder()
			.withStakeholders(List.of())
			.withFacilities(List.of(facility))
			.withCaseType(caseType.toString())
			.build();

		// Act && assert
		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> caseService.handleCase(byggRCase, MUNICIPALITY_ID))
			.withMessage("Bad Request: FacilityType is not allowed to be null for CaseType " + caseType);

		verify(validator).validateByggrErrand(byggRCase);
		verifyNoInteractions(eventPublisher);
		verifyNoInteractions(caseRepository);
	}

	@Test
	void testHandleEcosCase() {
		// Arrange
		final var ecosCaseDTO = EcosCaseDTO.builder()
			.withStakeholders(List.of())
			.withCaseType(CaseType.REGISTRERING_AV_LIVSMEDEL.toString())
			.withFacilities(List.of())
			.build();

		// Act
		caseService.handleCase(ecosCaseDTO, MUNICIPALITY_ID);
		// Assert
		verify(validator).validateEcosErrand(ecosCaseDTO);
		verify(eventPublisher).publishEvent(ecosCaseCaptor.capture());
		verify(caseRepository).save(any());

		final var incomingEcosCase = ecosCaseCaptor.getValue();
		assertThat(incomingEcosCase.getSource()).isEqualTo(caseService);
		assertThat(incomingEcosCase.getPayload()).isEqualTo(ecosCaseDTO);
	}

	@Test
	void testHandleOtherCase() {
		final var otherCaseDTO = OtherCaseDTO.builder().build();
		// Act
		caseService.handleCase(otherCaseDTO, MUNICIPALITY_ID);
		// Assert
		verify(eventPublisher).publishEvent(otherCaseCaptor.capture());
		verify(caseRepository).save(any());

		final var incomingOtherCase = otherCaseCaptor.getValue();
		assertThat(incomingOtherCase.getSource()).isEqualTo(caseService);
		assertThat(incomingOtherCase.getPayload()).isEqualTo(otherCaseDTO);
	}

}
