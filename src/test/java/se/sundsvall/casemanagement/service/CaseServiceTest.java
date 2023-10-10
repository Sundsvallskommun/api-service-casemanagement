package se.sundsvall.casemanagement.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertSame;
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
import se.sundsvall.casemanagement.api.model.EnvironmentalCaseDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.api.model.PlanningPermissionCaseDTO;
import se.sundsvall.casemanagement.api.model.PlanningPermissionFacilityDTO;
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
		final var pCase = new PlanningPermissionCaseDTO();
		pCase.setStakeholders(List.of());
		pCase.setFacilities(List.of());
		pCase.setCaseType(CaseType.ANDRING_ANSOKAN_OM_BYGGLOV);
		caseService.handleCase(pCase);

		verify(validator, times(1)).validateByggrErrand(pCase);
		verify(eventPublisher, times(1)).publishEvent(byggrCaseCaptor.capture());
		verify(caseRepository, times(1)).save(any());

		final var incomingByggrCase = byggrCaseCaptor.getValue();
		assertSame(caseService, incomingByggrCase.getSource());
		assertSame(pCase, incomingByggrCase.getPayload());
	}

	@ParameterizedTest
	@EnumSource(value = CaseType.class,
		names = { "MARKLOV_FYLL",
			"MARKLOV_SCHAKTNING", "MARKLOV_TRADFALLNING", "MARKLOV_OVRIGT",
			"STRANDSKYDD_OVRIGT" })
	void testHandleByggRCaseNoFacilityTypeAllowed(CaseType caseType) {
		final var pCase = new PlanningPermissionCaseDTO();

		final var facility = new PlanningPermissionFacilityDTO();
		final var adress = new AddressDTO();
		adress.setPropertyDesignation("propertyDesignation");
		adress.setAddressCategories(List.of());
		facility.setAddress(adress);
		pCase.setStakeholders(List.of());
		pCase.setFacilities(List.of(facility));
		pCase.setCaseType(caseType);
		caseService.handleCase(pCase);

		verify(validator, times(1)).validateByggrErrand(pCase);
		verify(eventPublisher, times(1)).publishEvent(byggrCaseCaptor.capture());
		verify(caseRepository, times(1)).save(any());

		final var incomingByggrCase = byggrCaseCaptor.getValue();
		assertSame(caseService, incomingByggrCase.getSource());
		assertSame(pCase, incomingByggrCase.getPayload());
	}

	@ParameterizedTest
	@EnumSource(value = CaseType.class,
		names = { "NYBYGGNAD_ANSOKAN_OM_BYGGLOV",
			"TILLBYGGNAD_ANSOKAN_OM_BYGGLOV", "STRANDSKYDD_ANDRAD_ANVANDNING" })
	void testHandleByggRCaseNoFacilityType_notAllowed(CaseType caseType) {
		final var pCase = new PlanningPermissionCaseDTO();

		final var facility = new PlanningPermissionFacilityDTO();
		final var adress = new AddressDTO();
		adress.setPropertyDesignation("propertyDesignation");
		adress.setAddressCategories(List.of());
		facility.setAddress(adress);
		pCase.setStakeholders(List.of());
		pCase.setFacilities(List.of(facility));
		pCase.setCaseType(caseType);

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> caseService.handleCase(pCase))
			.withMessage("Bad Request: FacilityType is not allowed to be null for CaseType " + caseType);

		verify(validator, times(1)).validateByggrErrand(pCase);
		verifyNoInteractions(eventPublisher);
		verifyNoInteractions(caseRepository);
	}

	@Test
	void testHandleEcosCase() {
		final var eCase = new EnvironmentalCaseDTO();
		eCase.setStakeholders(List.of());
		eCase.setCaseType(CaseType.REGISTRERING_AV_LIVSMEDEL);
		eCase.setFacilities(List.of());
		caseService.handleCase(eCase);

		verify(validator, times(1)).validateEcosErrand(eCase);
		verify(eventPublisher, times(1)).publishEvent(ecosCaseCaptor.capture());
		verify(caseRepository, times(1)).save(any());

		final var incomingEcosCase = ecosCaseCaptor.getValue();
		assertSame(caseService, incomingEcosCase.getSource());
		assertSame(eCase, incomingEcosCase.getPayload());
	}

	@Test
	void testHandleOtherCase() {
		final var oCase = new OtherCaseDTO();
		caseService.handleCase(oCase);

		verify(eventPublisher, times(1)).publishEvent(otherCaseCaptor.capture());
		verify(caseRepository, times(1)).save(any());

		final var incomingOtherCase = otherCaseCaptor.getValue();
		assertSame(caseService, incomingOtherCase.getSource());
		assertSame(oCase, incomingOtherCase.getPayload());
	}
}
