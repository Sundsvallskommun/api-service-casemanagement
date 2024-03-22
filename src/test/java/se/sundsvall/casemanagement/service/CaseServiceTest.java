package se.sundsvall.casemanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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
import se.sundsvall.casemanagement.api.model.EcosCaseDTO;
import se.sundsvall.casemanagement.api.model.FacilityDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.integration.db.CaseRepository;
import se.sundsvall.casemanagement.service.event.IncomingByggrCase;
import se.sundsvall.casemanagement.service.event.IncomingEcosCase;
import se.sundsvall.casemanagement.service.event.IncomingOtherCase;
import se.sundsvall.casemanagement.service.util.Validator;

@ExtendWith(MockitoExtension.class)
class CaseServiceTest {

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
		final var pCase = new ByggRCaseDTO();
		pCase.setStakeholders(List.of());
		pCase.setFacilities(List.of());
		pCase.setCaseType(CaseType.ANDRING_ANSOKAN_OM_BYGGLOV.toString());
		// Act
		caseService.handleCase(pCase);
		// Assert
		verify(validator, times(1)).validateByggrErrand(pCase);
		verify(eventPublisher, times(1)).publishEvent(byggrCaseCaptor.capture());
		verify(caseRepository, times(1)).save(any());
		final var incomingByggrCase = byggrCaseCaptor.getValue();
		assertThat(incomingByggrCase.getSource()).isEqualTo(caseService);
		assertThat(incomingByggrCase.getPayload()).isEqualTo(pCase);
	}

	@ParameterizedTest
	@EnumSource(value = CaseType.class,
		names = {"MARKLOV_FYLL",
			"MARKLOV_SCHAKTNING", "MARKLOV_TRADFALLNING", "MARKLOV_OVRIGT",
			"STRANDSKYDD_OVRIGT"})
	void testHandleByggRCaseNoFacilityTypeAllowed(final CaseType caseType) {
		// Arrange
		final var adress = new AddressDTO();
		adress.setPropertyDesignation("propertyDesignation");
		adress.setAddressCategories(List.of());

		final var facility = new FacilityDTO();
		facility.setAddress(adress);

		final var pCase = new ByggRCaseDTO();
		pCase.setStakeholders(List.of());
		pCase.setFacilities(List.of(facility));
		pCase.setCaseType(caseType.toString());

		// Act
		caseService.handleCase(pCase);
		// Assert
		verify(validator, times(1)).validateByggrErrand(pCase);
		verify(eventPublisher, times(1)).publishEvent(byggrCaseCaptor.capture());
		verify(caseRepository, times(1)).save(any());

		final var incomingByggrCase = byggrCaseCaptor.getValue();
		assertThat(incomingByggrCase.getSource()).isEqualTo(caseService);
		assertThat(incomingByggrCase.getPayload()).isEqualTo(pCase);
	}

	@ParameterizedTest
	@EnumSource(value = CaseType.class,
		names = {"NYBYGGNAD_ANSOKAN_OM_BYGGLOV",
			"TILLBYGGNAD_ANSOKAN_OM_BYGGLOV", "STRANDSKYDD_ANDRAD_ANVANDNING"})
	void testHandleByggRCaseNoFacilityType_notAllowed(final CaseType caseType) {
		// Arrange
		final var adress = new AddressDTO();
		adress.setPropertyDesignation("propertyDesignation");
		adress.setAddressCategories(List.of());

		final var facility = new FacilityDTO();
		facility.setAddress(adress);

		final var pCase = new ByggRCaseDTO();
		pCase.setStakeholders(List.of());
		pCase.setFacilities(List.of(facility));
		pCase.setCaseType(caseType.toString());
		// Act && assert
		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> caseService.handleCase(pCase))
			.withMessage("Bad Request: FacilityType is not allowed to be null for CaseType " + caseType);

		verify(validator, times(1)).validateByggrErrand(pCase);
		verifyNoInteractions(eventPublisher);
		verifyNoInteractions(caseRepository);
	}

	@Test
	void testHandleEcosCase() {
		// Arrange
		final var eCase = new EcosCaseDTO();
		eCase.setStakeholders(List.of());
		eCase.setCaseType(CaseType.REGISTRERING_AV_LIVSMEDEL.toString());
		eCase.setFacilities(List.of());
		// Act
		caseService.handleCase(eCase);
		// Assert
		verify(validator, times(1)).validateEcosErrand(eCase);
		verify(eventPublisher, times(1)).publishEvent(ecosCaseCaptor.capture());
		verify(caseRepository, times(1)).save(any());

		final var incomingEcosCase = ecosCaseCaptor.getValue();
		assertThat(incomingEcosCase.getSource()).isEqualTo(caseService);
		assertThat(incomingEcosCase.getPayload()).isEqualTo(eCase);
	}

	@Test
	void testHandleOtherCase() {
		final var oCase = new OtherCaseDTO();
		// Act
		caseService.handleCase(oCase);
		// Assert
		verify(eventPublisher, times(1)).publishEvent(otherCaseCaptor.capture());
		verify(caseRepository, times(1)).save(any());

		final var incomingOtherCase = otherCaseCaptor.getValue();
		assertThat(incomingOtherCase.getSource()).isEqualTo(caseService);
		assertThat(incomingOtherCase.getPayload()).isEqualTo(oCase);
	}

}
