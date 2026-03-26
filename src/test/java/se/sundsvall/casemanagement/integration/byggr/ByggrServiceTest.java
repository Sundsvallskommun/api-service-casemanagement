package se.sundsvall.casemanagement.integration.byggr;

import arendeexport.AbstractArendeObjekt;
import arendeexport.Arende;
import arendeexport.Arende2;
import arendeexport.ArendeFastighet;
import arendeexport.ArendeIntressent;
import arendeexport.ArrayOfArende1;
import arendeexport.ArrayOfHandelse;
import arendeexport.ArrayOfHandling;
import arendeexport.GetArende;
import arendeexport.GetArendeResponse;
import arendeexport.GetRelateradeArendenByPersOrgNrAndRole;
import arendeexport.GetRelateradeArendenByPersOrgNrAndRoleResponse;
import arendeexport.Handelse;
import arendeexport.HandelseHandling;
import arendeexport.SaveNewArende;
import arendeexport.SaveNewArendeMessage;
import arendeexport.SaveNewArendeResponse2;
import arendeexport.SaveNewHandelse;
import arendeexport.SaveNewHandelseMessage;
import generated.client.oep_integrator.ConfirmDeliveryRequest;
import generated.client.oep_integrator.InstanceType;
import generated.client.party.PartyType;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casemanagement.TestUtil;
import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;
import se.sundsvall.casemanagement.api.model.CaseStatusDTO;
import se.sundsvall.casemanagement.api.model.FacilityDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.StakeholderDTO;
import se.sundsvall.casemanagement.api.model.enums.AddressCategory;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.casemanagement.api.model.enums.FacilityType;
import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.api.model.enums.StakeholderType;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.ByggrCaseTypeConfigRepository;
import se.sundsvall.casemanagement.integration.db.CaseRepository;
import se.sundsvall.casemanagement.integration.db.CaseTypeRepository;
import se.sundsvall.casemanagement.integration.db.model.ByggrCaseTypeConfigEntity;
import se.sundsvall.casemanagement.integration.db.model.CaseEntity;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.integration.messaging.MessagingIntegration;
import se.sundsvall.casemanagement.integration.oepintegrator.OepIntegratorClient;
import se.sundsvall.casemanagement.integration.party.PartyIntegration;
import se.sundsvall.casemanagement.service.ByggrSystemConfigProvider;
import se.sundsvall.casemanagement.service.CaseMappingService;
import se.sundsvall.casemanagement.service.FbService;
import se.sundsvall.casemanagement.util.Constants;
import se.sundsvall.casemanagement.util.EnvironmentUtil;
import se.sundsvall.dept44.problem.ThrowableProblem;

import static generated.client.party.PartyType.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static se.sundsvall.casemanagement.TestUtil.FNR;
import static se.sundsvall.casemanagement.TestUtil.createByggRCaseDTO;
import static se.sundsvall.casemanagement.TestUtil.createStakeholderDTO;
import static se.sundsvall.casemanagement.TestUtil.setUpCaseTypes;
import static se.sundsvall.casemanagement.util.Constants.BYGGR;
import static se.sundsvall.casemanagement.util.Constants.ERRAND_NR;

@ExtendWith(MockitoExtension.class)
class ByggrServiceTest {

	private static final String BYGG_CASE_ID = "Inskickat";

	private static final String MUNICIPALITY_ID = "2281";

	@Mock
	private ByggrCaseTypeConfigRepository byggrCaseTypeConfigRepository;

	@Mock
	private CaseTypeRepository caseTypeRepositoryMock;

	private ByggrService byggrService;

	@Mock
	private FbService fbServiceMock;

	@Mock
	private PartyIntegration partyIntegrationMock;

	@Mock
	private CaseMappingService caseMappingServiceMock;

	@Mock
	private OepIntegratorClient oepIntegratorClientMock;

	@Mock
	private ArendeExportClient arendeExportClientMock;

	@Mock
	private CaseRepository caseRepositoryMock;

	@Mock
	private MessagingIntegration messagingIntegrationMock;

	@Mock
	private EnvironmentUtil environmentUtilMock;

	@Mock
	private ByggrSystemConfigProvider byggrSystemConfigProvider;

	private Map<String, ByggrUpdateHandler> updateHandlersMock;

	private static void assertCaseStatus(final String dnr, final String caseId, final String externalCaseID, final String caseType, final String serviceName, final String status, final LocalDateTime dateTime, final CaseStatusDTO getStatusResult) {
		assertThat(getStatusResult.getCaseId()).isEqualTo(dnr);
		assertThat(getStatusResult.getErrandNumber()).isEqualTo(caseId);
		assertThat(getStatusResult.getExternalCaseId()).isEqualTo(externalCaseID);
		assertThat(getStatusResult.getSystem()).isEqualTo(SystemType.BYGGR);
		assertThat(getStatusResult.getCaseType()).isEqualTo(caseType);
		assertThat(getStatusResult.getServiceName()).isEqualTo(serviceName);
		assertThat(getStatusResult.getStatus()).isEqualTo(status);
		assertThat(getStatusResult.getTimestamp()).isEqualTo(dateTime);
	}

	private static void assertHandelse(final String dnr, final SaveNewHandelseMessage saveNewHandelseMessage, final List<String> notesToContain, final String handelseRubrik, final String handelstyp, final String handelseslag,
		final List<AttachmentDTO> attachments) {
		assertThat(saveNewHandelseMessage.getDnr()).isEqualTo(dnr);
		assertThat(saveNewHandelseMessage.getHandlaggarSign()).isEqualTo("SYSTEM");
		assertThat(saveNewHandelseMessage.getHandelse().getRubrik()).isEqualTo(handelseRubrik);
		assertThat(saveNewHandelseMessage.getHandelse().getRiktning()).isEqualTo("In");
		assertThat(saveNewHandelseMessage.getHandelse().getHandelsetyp()).isEqualTo(handelstyp);
		assertThat(saveNewHandelseMessage.getHandelse().getHandelseslag()).isEqualTo(handelseslag);
		assertThat(saveNewHandelseMessage.getHandelse().getStartDatum()).isNotNull();
		notesToContain.forEach(text -> assertThat(saveNewHandelseMessage.getHandelse().getAnteckning()).contains(text));

		if (attachments != null) {
			assertHandlingar(attachments, saveNewHandelseMessage.getHandlingar().getHandling());
		}

	}

	private static void assertHandlingar(final List<AttachmentDTO> attachments, final List<HandelseHandling> handlingList) {
		assertThat(handlingList).hasSameSizeAs(attachments);
		attachments.forEach(attachmentDTO -> {
			assertThat(handlingList.stream().filter(handelseHandling -> handelseHandling.getTyp().equals(attachmentDTO.getCategory())).count()).isEqualTo(1);
			assertThat(handlingList.stream().filter(handelseHandling -> handelseHandling.getAnteckning().equals(attachmentDTO.getName())).count()).isEqualTo(1);
			assertThat(handlingList.stream().filter(handelseHandling -> handelseHandling.getDokument().getNamn().equals(attachmentDTO.getName())).count()).isEqualTo(1);
			assertThat(handlingList.stream().filter(handelseHandling -> handelseHandling.getDokument().getBeskrivning().equals(attachmentDTO.getNote())).count()).isEqualTo(1);
			assertThat(handlingList.stream().filter(handelseHandling -> handelseHandling.getDokument().getFil().getFilAndelse().equals(attachmentDTO.getExtension().toLowerCase())).count()).isEqualTo(1);
		});

		handlingList.forEach(handling -> {
			assertThat(handling.getDokument().getFil().getFilBuffer()).isNotNull();
			assertThat(handling.getStatus()).isEqualTo("Inkommen");
		});
	}

	private static void assertOrganizationDTO(final OrganizationDTO organizationDTO, final ArendeIntressent arendeIntressent) {
		assertThat(arendeIntressent.isArForetag()).isTrue();
		assertThat(arendeIntressent.getNamn()).isEqualTo(organizationDTO.getOrganizationName());
		assertThat(arendeIntressent.getPersOrgNr()).isEqualTo(organizationDTO.getOrganizationNumber());

		assertThat(arendeIntressent.getRollLista().getRoll()).containsAll(organizationDTO.getRoles().stream().map((String t) -> StakeholderRole.valueOf(t).getText()).toList());

		assertCommunication(organizationDTO, arendeIntressent);
		assertAddress(organizationDTO, arendeIntressent);
	}

	private static void assertPersonDTO(final PersonDTO personDTO, final ArendeIntressent arendeIntressent) {
		assertThat(arendeIntressent.isArForetag()).isFalse();
		assertThat(arendeIntressent.getFornamn()).isEqualTo(personDTO.getFirstName());
		assertThat(arendeIntressent.getEfternamn()).isEqualTo(personDTO.getLastName());
		assertThat(arendeIntressent.getPersOrgNr()).isEqualTo(personDTO.getPersonalNumber());
		assertThat(arendeIntressent.getRollLista().getRoll())
			.containsAll(personDTO.getRoles().stream().map((String t) -> StakeholderRole.valueOf(t).getText()).toList());

		assertCommunication(personDTO, arendeIntressent);
		assertAddress(personDTO, arendeIntressent);
	}

	private static void assertCommunication(final StakeholderDTO stakeholderDTO, final ArendeIntressent arendeIntressent) {
		final var stakeholderContacts = Arrays.asList(stakeholderDTO.getEmailAddress(), stakeholderDTO.getCellphoneNumber(), stakeholderDTO.getPhoneNumber());
		arendeIntressent.getIntressentKommunikationLista().getIntressentKommunikation()
			.forEach(kom -> assertThat(stakeholderContacts).contains(kom.getBeskrivning()));
	}

	private static void assertAddress(final StakeholderDTO stakeholderDTO, final ArendeIntressent arendeIntressent) {
		final var postalAddress = stakeholderDTO.getAddresses().stream().filter(addressDTO -> addressDTO.getAddressCategories().contains(AddressCategory.POSTAL_ADDRESS)).findFirst().orElseThrow();

		assertThat(arendeIntressent.getAdress()).isEqualTo(postalAddress.getStreet() + " " + postalAddress.getHouseNumber());
		assertThat(arendeIntressent.getPostNr()).isEqualTo(postalAddress.getPostalCode());
		assertThat(arendeIntressent.getOrt()).isEqualTo(postalAddress.getCity());
		assertThat(arendeIntressent.getLand()).isEqualTo(postalAddress.getCountry());

		final var invoiceAddress = stakeholderDTO.getAddresses().stream().filter(addressDTO -> addressDTO.getAddressCategories().contains(AddressCategory.INVOICE_ADDRESS)).findFirst();
		if (invoiceAddress.isPresent()) {
			assertThat(arendeIntressent.getFakturaAdress().getAdress()).isEqualTo(invoiceAddress.get().getStreet() + " " + invoiceAddress.get().getHouseNumber());
			assertThat(arendeIntressent.getFakturaAdress().getPostNr()).isEqualTo(invoiceAddress.get().getPostalCode());
			assertThat(arendeIntressent.getFakturaAdress().getOrt()).isEqualTo(invoiceAddress.get().getCity());
			assertThat(arendeIntressent.getFakturaAdress().getLand()).isEqualTo(invoiceAddress.get().getCountry());
			assertThat(arendeIntressent.getFakturaAdress().getAttention()).isEqualTo(invoiceAddress.get().getAttention());
		}
	}

	private static void assertThatArendeIsEqual(final Arende2 arende, final String arendeTyp, final FacilityDTO inputFacility) {
		assertThat(arende.getArendetyp()).isEqualTo(arendeTyp);

		if ("DI".equals(arendeTyp)) {
			assertThat(arende.getArendegrupp()).isEqualTo("STRA");
		} else {
			assertThat(arende.getArendegrupp()).isEqualTo("LOV");
		}
		assertThat(arende.getNamndkod()).isEqualTo("SBN");
		assertThat(arende.getEnhetkod()).isEqualTo("SBK");
		assertThat(arende.getKommun()).isEqualTo("2281");
		assertThat(arende.getHandlaggare().getSignatur()).isEqualTo("SYSTEM");
		assertThat(arende.isArInomplan()).isEqualTo(inputFacility.getAddress().getIsZoningPlanArea());
		assertThat(arende.getAnkomstDatum()).isNotNull();
	}

	private static void assertThatHandlingIsEqual(final ArrayOfHandling handlingar, final AttachmentDTO inputAttachment) {

		assertThat(handlingar.getHandling()).hasSize(1).element(0).satisfies(handling -> {
			assertThat(handling.getAnteckning()).isEqualTo(inputAttachment.getName());
			assertThat(handling.getDokument().getFil().getFilBuffer()).isNotNull();
			assertThat(handling.getDokument().getFil().getFilAndelse()).isEqualTo(inputAttachment.getExtension().toLowerCase());
			assertThat(handling.getDokument().getNamn()).isEqualTo(inputAttachment.getName());
			assertThat(handling.getDokument().getBeskrivning()).isEqualTo(inputAttachment.getNote());
			assertThat(handling.getStatus()).isEqualTo("Inkommen");
			assertThat(handling.getTyp()).isEqualTo(inputAttachment.getCategory());
		});
	}

	@BeforeEach
	void setup() {
		// Status mapping stubs for toByggrStatus tests
		lenient().when(byggrSystemConfigProvider.resolveHandelseStatus("BESLUT", "SLU", "Handelsutfall 2")).thenReturn("SLU");
		lenient().when(byggrSystemConfigProvider.resolveHandelseStatus("BESLUT", "SLU", "Handelsutfall 1")).thenReturn("SLU");
		lenient().when(byggrSystemConfigProvider.resolveHandelseStatus("BESLUT", "SLU", null)).thenReturn("SLU");
		lenient().when(byggrSystemConfigProvider.resolveHandelseStatus("HANDLING", "KOMPL", null)).thenReturn("KOMPL");

		updateHandlersMock = new HashMap<>();
		updateHandlersMock.put("neighborhoodResponse", mock(ByggrUpdateHandler.class));
		updateHandlersMock.put("addInspector", mock(ByggrUpdateHandler.class));
		updateHandlersMock.put("addDocuments", mock(ByggrUpdateHandler.class));
		byggrService = new ByggrService(
			fbServiceMock,
			partyIntegrationMock,
			caseMappingServiceMock,
			environmentUtilMock,
			arendeExportClientMock,
			oepIntegratorClientMock,
			byggrCaseTypeConfigRepository,
			caseTypeRepositoryMock,
			caseRepositoryMock,
			messagingIntegrationMock,
			byggrSystemConfigProvider,
			updateHandlersMock);

		lenient().when(byggrCaseTypeConfigRepository.findAll()).thenReturn(setUpCaseTypes());

		// Stub update_handler lookups for update case types
		lenient().when(byggrCaseTypeConfigRepository.findById("NEIGHBORHOOD_NOTIFICATION"))
			.thenReturn(Optional.of(ByggrCaseTypeConfigEntity.builder().withCaseTypeName("NEIGHBORHOOD_NOTIFICATION").withUpdateHandler("neighborhoodResponse").build()));
		lenient().when(byggrCaseTypeConfigRepository.findById("PROPERTY_OWNER_NOTIFICATION"))
			.thenReturn(Optional.of(ByggrCaseTypeConfigEntity.builder().withCaseTypeName("PROPERTY_OWNER_NOTIFICATION").withUpdateHandler("neighborhoodResponse").build()));
		lenient().when(byggrCaseTypeConfigRepository.findById("BYGGR_ADD_CERTIFIED_INSPECTOR"))
			.thenReturn(Optional.of(ByggrCaseTypeConfigEntity.builder().withCaseTypeName("BYGGR_ADD_CERTIFIED_INSPECTOR").withUpdateHandler("addInspector").build()));
		lenient().when(byggrCaseTypeConfigRepository.findById("BYGGR_ADDITIONAL_DOCUMENTS"))
			.thenReturn(Optional.of(ByggrCaseTypeConfigEntity.builder().withCaseTypeName("BYGGR_ADDITIONAL_DOCUMENTS").withUpdateHandler("addDocuments").build()));

		TestUtil.standardMockFb(fbServiceMock);
		TestUtil.standardMockArendeExport(arendeExportClientMock);
		TestUtil.standardMockCitizen(partyIntegrationMock);
	}

	// ANSOKAN_OM_BYGGLOV
	@ParameterizedTest
	@ValueSource(strings = {
		"STRANDSKYDD_NYBYGGNAD",
		"STRANDSKYDD_ANLAGGANDE", "STRANDSKYDD_ANORDNANDE",
		"STRANDSKYDD_ANDRAD_ANVANDNING"
	})
	void testStrandskyddCaseType(final String caseType) {
		final var caseTypes = Map.of(
			"STRANDSKYDD_NYBYGGNAD", "Strandskyddsdispens för nybyggnad av",
			"STRANDSKYDD_ANLAGGANDE", "Strandskyddsdispens för anläggande av",
			"STRANDSKYDD_ANORDNANDE", "Strandskyddsdispens för anordnare av",
			"STRANDSKYDD_ANDRAD_ANVANDNING", "Strandskyddsdispens för ändrad användning av");

		final var input = createByggRCaseDTO(caseType, AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		final var inputFacility = input.getFacilities().getFirst();
		final var inputAttachment = input.getAttachments().getFirst();
		final var response = byggrService.saveNewCase(input, MUNICIPALITY_ID);

		assertThat(response.getDnr()).isEqualTo(BYGG_CASE_ID);

		final var saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
		verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());

		final var saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
		final var arende = saveNewArendeMessage.getArende();
		final var handelse = saveNewArendeMessage.getHandelse();
		final var handlingar = saveNewArendeMessage.getHandlingar();

		// SaveNewArendeMessage
		assertThat(saveNewArendeMessage.getHandlaggarSign()).isEqualTo("SYSTEM");

		// Arende
		assertThatArendeIsEqual(arende, "DI", inputFacility);
		assertThat(arende.getArendeklass()).isEqualTo(FacilityType.valueOf(inputFacility.getFacilityType()).getValue());
		assertThat(arende.getBeskrivning()).isEqualTo("%s %s samt %s".formatted(caseTypes.get(caseType), FacilityType.valueOf(inputFacility.getFacilityType()).getDescription(), input.getCaseTitleAddition().trim().toLowerCase()));
		assertThat(arende.getProjektnr()).isEqualTo(input.getStakeholders().getFirst().getAddresses().getFirst().getInvoiceMarking());
		// Intressenter
		assertThat(arende.getIntressentLista().getIntressent()).hasSize(3);

		// ArendeObjekt
		assertThat(arende.getObjektLista().getAbstractArendeObjekt()).hasSize(1).element(0).satisfies(
			abstractArendeObjekt -> {
				final var arendeFastighet = (ArendeFastighet) abstractArendeObjekt;
				assertThat(arendeFastighet.isArHuvudObjekt()).isEqualTo(inputFacility.isMainFacility());
				assertThat(arendeFastighet.getFastighet().getFnr()).isEqualTo(FNR);
			});

		// Handlingar
		assertThat(handlingar.getHandling()).hasSize(1);
		final var handling = handlingar.getHandling().getFirst();
		assertThat(inputAttachment.getName()).isEqualTo(handling.getAnteckning());
		assertThat(handling.getDokument().getFil().getFilBuffer()).isNotNull();
		assertThat(handling.getDokument().getFil().getFilAndelse()).isEqualTo(inputAttachment.getExtension().toLowerCase());
		assertThat(handling.getDokument().getNamn()).isEqualTo(inputAttachment.getName());
		assertThat(handling.getDokument().getBeskrivning()).isEqualTo(inputAttachment.getNote());
		assertThat(handling.getStatus()).isEqualTo("Inkommen");
		assertThat(handling.getTyp()).isEqualTo(inputAttachment.getCategory());

		// Handelser
		assertThat(handelse.getStartDatum()).isNotNull();
		assertThat(handelse.getRiktning()).isEqualTo("In");
		assertThat(handelse.getRubrik()).isEqualTo("Strandskyddsdispens");
		assertThat(handelse.getHandelsetyp()).isEqualTo("ANSÖKAN");
		assertThat(handelse.getHandelseslag()).isEqualTo("Strand");
	}

	@ParameterizedTest
	@CsvSource({
		"NEIGHBORHOOD_NOTIFICATION, neighborhoodResponse",
		"PROPERTY_OWNER_NOTIFICATION, neighborhoodResponse",
		"BYGGR_ADD_CERTIFIED_INSPECTOR, addInspector",
		"BYGGR_ADDITIONAL_DOCUMENTS, addDocuments"
	})
	void updateByggRCaseSuccess(final String caseType, final String handlerName) {
		final var byggRCaseDto = createByggRCaseDTO(caseType, AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		final var confirmDeliveryRequest = new ConfirmDeliveryRequest().delivered(true).caseId(byggRCaseDto.getExtraParameters().get(ERRAND_NR)).system(BYGGR);
		when(caseRepositoryMock.findByIdAndMunicipalityId(byggRCaseDto.getExternalCaseId(), MUNICIPALITY_ID)).thenReturn(Optional.of(CaseEntity.builder().build()));

		byggrService.updateByggRCase(byggRCaseDto, MUNICIPALITY_ID);

		verify(updateHandlersMock.get(handlerName)).handle(byggRCaseDto);
		verify(oepIntegratorClientMock).confirmDelivery(MUNICIPALITY_ID, InstanceType.EXTERNAL, byggRCaseDto.getExternalCaseId(), confirmDeliveryRequest);
		verify(caseRepositoryMock).findByIdAndMunicipalityId(byggRCaseDto.getExternalCaseId(), MUNICIPALITY_ID);
		verify(caseRepositoryMock).delete(any());
	}

	@ParameterizedTest
	@CsvSource({
		"NEIGHBORHOOD_NOTIFICATION, neighborhoodResponse",
		"PROPERTY_OWNER_NOTIFICATION, neighborhoodResponse",
		"BYGGR_ADD_CERTIFIED_INSPECTOR, addInspector",
		"BYGGR_ADDITIONAL_DOCUMENTS, addDocuments"
	})
	void updateByggRCaseException(final String caseType, final String handlerName) {
		final var byggRCaseDto = createByggRCaseDTO(caseType, AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		final var subject = "Incident from CaseManagement[JUnit]";
		final var message = "[%s][BYGGR] Could not update case with externalCaseId: %s. Exception: %s ".formatted(MUNICIPALITY_ID, byggRCaseDto.getExternalCaseId(), null);
		doThrow(RuntimeException.class).when(updateHandlersMock.get(handlerName)).handle(byggRCaseDto);
		when(environmentUtilMock.extractEnvironment()).thenReturn("JUnit");

		byggrService.updateByggRCase(byggRCaseDto, MUNICIPALITY_ID);

		verify(updateHandlersMock.get(handlerName)).handle(byggRCaseDto);
		verify(messagingIntegrationMock).sendSlack(message, MUNICIPALITY_ID);
		verify(messagingIntegrationMock).sendMail(subject, message, MUNICIPALITY_ID);
	}

	// ANSOKAN_OM_BYGGLOV
	@ParameterizedTest
	@ValueSource(strings = {
		"NYBYGGNAD_ANSOKAN_OM_BYGGLOV",
		"ANDRING_ANSOKAN_OM_BYGGLOV", "TILLBYGGNAD_ANSOKAN_OM_BYGGLOV"
	})
	void testPostNybyggnad(final String caseType) {
		// Arrange
		final var input = createByggRCaseDTO(caseType, AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		final var inputFacility = input.getFacilities().getFirst();
		final var inputAttachment = input.getAttachments().getFirst();

		// Act
		final var response = byggrService.saveNewCase(input, MUNICIPALITY_ID);

		// Assert
		assertThat(response.getDnr()).isEqualTo(BYGG_CASE_ID);

		final var saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
		verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());

		final var saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
		final var arende = saveNewArendeMessage.getArende();
		final var handelse = saveNewArendeMessage.getHandelse();
		final var handlingar = saveNewArendeMessage.getHandlingar();

		// SaveNewArendeMessage
		assertThat(saveNewArendeMessage.getHandlaggarSign()).isEqualTo("SYSTEM");

		// Arende

		if (!"ANDRING_ANSOKAN_OM_BYGGLOV".equals(caseType)) {
			assertThat(arende.getArendeklass()).isEqualTo(FacilityType.valueOf(inputFacility.getFacilityType()).getValue());
		} else {
			assertThat(arende.getArendeslag()).isEqualTo(FacilityType.valueOf(inputFacility.getFacilityType()).getValue());
		}

		assertThatArendeIsEqual(arende, "BL", inputFacility);

		// Intressenter
		assertThat(arende.getIntressentLista().getIntressent()).hasSize(3);

		// ArendeObjekt
		assertThat(arende.getObjektLista().getAbstractArendeObjekt()).hasSize(1).element(0).satisfies(abstractArendeObjekt -> {
			final var arendeFastighet = (ArendeFastighet) abstractArendeObjekt;
			assertThat(arendeFastighet.isArHuvudObjekt()).isEqualTo(inputFacility.isMainFacility());
			assertThat(arendeFastighet.getFastighet().getFnr()).isEqualTo(FNR);
		});
		final var arendeFastighet = (ArendeFastighet) arende.getObjektLista().getAbstractArendeObjekt().getFirst();
		assertThat(arendeFastighet.isArHuvudObjekt()).isEqualTo(inputFacility.isMainFacility());
		assertThat(arendeFastighet.getFastighet().getFnr()).isEqualTo(FNR);

		// Handlingar
		assertThatHandlingIsEqual(handlingar, inputAttachment);

		// Handelser
		assertThat(handelse.getStartDatum()).isNotNull();
		assertThat(handelse.getRiktning()).isEqualTo("In");
		assertThat(handelse.getRubrik()).isEqualTo("Bygglov");
		assertThat(handelse.getHandelsetyp()).isEqualTo("ANSÖKAN");
		assertThat(handelse.getHandelseslag()).isEqualTo("Bygglov");
	}

	// ANMALAN_ATTEFALL
	@Test
	void testPostAttefall() {

		final ByggRCaseDTO input = createByggRCaseDTO("ANMALAN_ATTEFALL", AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		// Set facilityType to a compatible value
		input.getFacilities().getFirst().setFacilityType(FacilityType.EXTENSION.toString());
		// Set addressCategory to not be INVOICE_ADDRESS, so we can test projektnummer to be propertyDesignation
		input.getStakeholders().getFirst().getAddresses().getFirst().setAddressCategories(List.of(AddressCategory.POSTAL_ADDRESS));
		final var inputFacility = input.getFacilities().getFirst();
		final AttachmentDTO inputAttachment = input.getAttachments().getFirst();
		final SaveNewArendeResponse2 response = byggrService.saveNewCase(input, MUNICIPALITY_ID);

		assertThat(response.getDnr()).isEqualTo(BYGG_CASE_ID);

		final ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
		verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
		final SaveNewArendeMessage saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
		final Arende2 arende = saveNewArendeMessage.getArende();
		final Handelse handelse = saveNewArendeMessage.getHandelse();
		final ArrayOfHandling handlingar = saveNewArendeMessage.getHandlingar();

		// SaveNewArendeMessage
		assertThat(saveNewArendeMessage.getHandlaggarSign()).isEqualTo("SYSTEM");

		// Arende
		assertThatArendeIsEqual(arende, "ATTANM", inputFacility);
		assertThat(arende.getArendeslag()).isEqualTo(FacilityType.valueOf(inputFacility.getFacilityType()).getValue());
		assertThat(arende.getArendeklass()).isNull();
		// Intressenter
		assertThat(arende.getIntressentLista().getIntressent()).hasSize(3);

		// ArendeObjekt
		assertThat(arende.getObjektLista().getAbstractArendeObjekt()).hasSize(1);
		final ArendeFastighet arendeFastighet = (ArendeFastighet) arende.getObjektLista().getAbstractArendeObjekt().getFirst();
		assertThat(arendeFastighet.isArHuvudObjekt()).isEqualTo(inputFacility.isMainFacility());
		assertThat(arendeFastighet.getFastighet().getFnr()).isEqualTo(FNR);

		// Handlingar
		assertThatHandlingIsEqual(handlingar, inputAttachment);

		// Handelser
		assertThat(handelse.getStartDatum()).isNotNull();
		assertThat(handelse.getRiktning()).isEqualTo("In");
		assertThat(handelse.getRubrik()).isEqualTo("Anmälan Attefall");
		assertThat(handelse.getHandelsetyp()).isEqualTo("ANM");
		assertThat(handelse.getHandelseslag()).isEqualTo("ANMATT");
	}

	// ANMALAN_ELDSTAD
	@Test
	void testPostEldstad() {

		final ByggRCaseDTO input = createByggRCaseDTO("ANMALAN_ELDSTAD",
			AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		// Set facilityType to a compatible value
		input.getFacilities().getFirst().setFacilityType(FacilityType.FIREPLACE.toString());
		// Set addressCategory to not be INVOICE_ADDRESS, so we can test projektnummer to be propertyDesignation
		input.getStakeholders().getFirst().getAddresses().getFirst().setAddressCategories(List.of(AddressCategory.POSTAL_ADDRESS));
		final var inputFacility = input.getFacilities().getFirst();
		final AttachmentDTO inputAttachment = input.getAttachments().getFirst();
		final SaveNewArendeResponse2 response = byggrService.saveNewCase(input, MUNICIPALITY_ID);

		assertThat(response.getDnr()).isEqualTo(BYGG_CASE_ID);

		final ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
		verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
		final SaveNewArendeMessage saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
		final Arende2 arende = saveNewArendeMessage.getArende();
		final Handelse handelse = saveNewArendeMessage.getHandelse();
		final ArrayOfHandling handlingar = saveNewArendeMessage.getHandlingar();

		// SaveNewArendeMessage
		assertThat(saveNewArendeMessage.getHandlaggarSign()).isEqualTo("SYSTEM");

		// Arende
		assertThatArendeIsEqual(arende, "ANM", inputFacility);
		assertThat(arende.getArendeslag()).isEqualTo(FacilityType.valueOf(inputFacility.getFacilityType()).getValue());
		assertThat(arende.getArendeklass()).isNull();

		// Intressenter
		assertThat(arende.getIntressentLista().getIntressent()).hasSize(3);

		// ArendeObjekt
		assertThat(arende.getObjektLista().getAbstractArendeObjekt()).hasSize(1);
		final ArendeFastighet arendeFastighet = (ArendeFastighet) arende.getObjektLista().getAbstractArendeObjekt().getFirst();
		assertThat(arendeFastighet.isArHuvudObjekt()).isEqualTo(inputFacility.isMainFacility());
		assertThat(arendeFastighet.getFastighet().getFnr()).isEqualTo(FNR);

		// Handlingar
		assertThatHandlingIsEqual(handlingar, inputAttachment);

		// Handelser
		assertThat(handelse.getStartDatum()).isNotNull();
		assertThat(handelse.getRiktning()).isEqualTo("In");
		assertThat(handelse.getRubrik()).isEqualTo("Eldstad");
		assertThat(handelse.getHandelsetyp()).isEqualTo("ANM");
		assertThat(handelse.getHandelseslag()).isEqualTo("ELD1");
	}

	// ANMALAN_ELDSTAD_SMOKE
	@Test
	void testPostEldstadRokkanal() {

		// Arrange
		final var input = createByggRCaseDTO("ANMALAN_ELDSTAD", AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		// Set facilityType to a compatible value
		input.getFacilities().getFirst().setFacilityType(FacilityType.FIREPLACE_SMOKECHANNEL.toString());
		// Set addressCategory to not be INVOICE_ADDRESS, so we can test projektnummer to be propertyDesignation
		input.getStakeholders().getFirst().getAddresses().getFirst().setAddressCategories(List.of(AddressCategory.POSTAL_ADDRESS));
		final var inputFacility = input.getFacilities().getFirst();
		final var inputAttachment = input.getAttachments().getFirst();

		// Act
		final SaveNewArendeResponse2 response = byggrService.saveNewCase(input, MUNICIPALITY_ID);

		// Assert
		assertThat(response.getDnr()).isEqualTo(BYGG_CASE_ID);

		final ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
		verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
		final var saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
		final var arende = saveNewArendeMessage.getArende();
		final var handelse = saveNewArendeMessage.getHandelse();

		// SaveNewArendeMessage
		assertThat(saveNewArendeMessage.getHandlaggarSign()).isEqualTo("SYSTEM");

		// Arende
		assertThatArendeIsEqual(arende, "ANM", inputFacility);
		assertThat(arende.getArendeslag()).isEqualTo(FacilityType.valueOf(inputFacility.getFacilityType()).getValue());
		assertThat(arende.getArendeklass()).isNull();

		// Intressenter
		assertThat(arende.getIntressentLista().getIntressent()).hasSize(3);

		// ArendeObjekt
		assertThat(arende.getObjektLista().getAbstractArendeObjekt()).hasSize(1);
		final var arendeFastighet = (ArendeFastighet) arende.getObjektLista().getAbstractArendeObjekt().getFirst();
		assertThat(arendeFastighet.isArHuvudObjekt()).isEqualTo(inputFacility.isMainFacility());
		assertThat(arendeFastighet.getFastighet().getFnr()).isEqualTo(FNR);

		// Handlingar
		assertThatHandlingIsEqual(saveNewArendeMessage.getHandlingar(), inputAttachment);

		// Handelser
		assertThat(handelse.getStartDatum()).isNotNull();
		assertThat(handelse.getRiktning()).isEqualTo("In");
		assertThat(handelse.getRubrik()).isEqualTo("Eldstad/Rökkanal");
		assertThat(handelse.getHandelsetyp()).isEqualTo("ANM");
		assertThat(handelse.getHandelseslag()).isEqualTo("ELD");
	}

	@Test
	void testCallToCaseMapping() {
		final ByggRCaseDTO input = createByggRCaseDTO("NYBYGGNAD_ANSOKAN_OM_BYGGLOV", AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		input.getExtraParameters().put(Constants.SERVICE_NAME, "Test service name");
		final PersonDTO applicant = (PersonDTO) TestUtil.createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT.toString()));
		input.setStakeholders(List.of(applicant));

		final var postResult = byggrService.saveNewCase(input, MUNICIPALITY_ID);

		verify(caseMappingServiceMock, times(1)).postCaseMapping(input, postResult.getDnr(), SystemType.BYGGR, MUNICIPALITY_ID);
	}

	// Test no duplicates of arendeFastighet
	@Test
	void testNoDuplicateFacilities() {
		final ByggRCaseDTO input = createByggRCaseDTO("NYBYGGNAD_ANSOKAN_OM_BYGGLOV", AttachmentCategory.BUILDING_PERMIT_APPLICATION);

		final String propertyDesignation = "Sundsvall test 123:123";
		final var facility1 = TestUtil.createFacilityDTO(true);
		final var facility2 = TestUtil.createFacilityDTO(false);
		final var facility3 = TestUtil.createFacilityDTO(false);
		facility1.getAddress().setPropertyDesignation(propertyDesignation);
		facility2.getAddress().setPropertyDesignation(propertyDesignation);
		facility3.getAddress().setPropertyDesignation(propertyDesignation);
		// Add some facilities
		input.setFacilities(List.of(facility1, facility2, facility3));

		byggrService.saveNewCase(input, MUNICIPALITY_ID);

		final ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
		verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
		final SaveNewArendeMessage saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
		final Arende2 arende = saveNewArendeMessage.getArende();

		// ArendeObjekt
		assertThat(arende.getObjektLista().getAbstractArendeObjekt()).hasSize(1);
	}

	// Test getMainOrTheOnlyFacility
	@Test
	void testGetMainOrTheOnlyFacility() {
		final ByggRCaseDTO input = createByggRCaseDTO("NYBYGGNAD_ANSOKAN_OM_BYGGLOV", AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		// Set addressCategory to not be INVOICE_ADDRESS, so we can test projektnummer to be propertyDesignation
		input.getStakeholders().getFirst().getAddresses().getFirst().setAddressCategories(List.of(AddressCategory.POSTAL_ADDRESS));

		final var mainFacility = TestUtil.createFacilityDTO(true);
		final var randomFacility1 = TestUtil.createFacilityDTO(false);
		randomFacility1.getAddress().setPropertyDesignation("Sundsvall test 1:1");
		final var randomFacility2 = TestUtil.createFacilityDTO(false);
		randomFacility2.getAddress().setPropertyDesignation("Sundsvall test 2:2");
		// Add some facilities
		input.setFacilities(List.of(randomFacility1, mainFacility, randomFacility2));

		byggrService.saveNewCase(input, MUNICIPALITY_ID);

		final ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
		verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
		final SaveNewArendeMessage saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
		final Arende2 arende = saveNewArendeMessage.getArende();

		// Arende
		assertThat(arende.getArendeklass()).isEqualTo(FacilityType.valueOf(mainFacility.getFacilityType()).getValue());
		assertThat(arende.isArInomplan()).isEqualTo(mainFacility.getAddress().getIsZoningPlanArea());
		// Remove SUNDSVALL from propertyDesignation
		final String propertyDesignation = mainFacility.getAddress().getPropertyDesignation().substring(mainFacility.getAddress().getPropertyDesignation().indexOf(" ") + 1);
		assertThat(arende.getProjektnr()).isEqualTo(propertyDesignation);

		// ArendeObjekt
		assertThat(arende.getObjektLista().getAbstractArendeObjekt()).hasSize(3);
		final Long nrOfMainFacilities = arende.getObjektLista().getAbstractArendeObjekt().stream().map(ArendeFastighet.class::cast).filter(AbstractArendeObjekt::isArHuvudObjekt).count();
		assertThat(nrOfMainFacilities).isEqualTo(1);
	}

	@Test
	void testSetPersonInvoiceAddressError() {
		final ByggRCaseDTO input = createByggRCaseDTO("NYBYGGNAD_ANSOKAN_OM_BYGGLOV", AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		final PersonDTO applicant = (PersonDTO) TestUtil.createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT.toString()));
		applicant.setAddresses(List.of(TestUtil.createAddressDTO(List.of(AddressCategory.INVOICE_ADDRESS))));
		input.setStakeholders(List.of(applicant));

		assertThatThrownBy(
			() -> byggrService.saveNewCase(input, MUNICIPALITY_ID))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", BAD_REQUEST)
			.hasFieldOrPropertyWithValue("detail", Constants.ERR_MSG_PERSON_INVOICE_ADDRESS);
	}

	@Test
	void testSetPersonFields() {

		// Arrange
		final ByggRCaseDTO input = createByggRCaseDTO("NYBYGGNAD_ANSOKAN_OM_BYGGLOV", AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		final PersonDTO applicant = (PersonDTO) TestUtil.createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT.toString()));
		input.setStakeholders(List.of(applicant));

		byggrService.saveNewCase(input, MUNICIPALITY_ID);

		final ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);

		verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
		final SaveNewArendeMessage saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
		final Arende2 arende = saveNewArendeMessage.getArende();
		final var intressenter = arende.getIntressentLista().getIntressent();

		assertThat(intressenter).hasSize(1);

		final var applicants = intressenter.stream()
			.filter(intressent -> intressent.getRollLista().getRoll()
				.contains(StakeholderRole.APPLICANT.getText())).toList();
		assertThat(applicants).hasSize(1);
		assertPersonDTO(applicant, applicants.getFirst());
	}

	@Test
	void testSetOrganisationFields() {
		final ByggRCaseDTO input = createByggRCaseDTO("NYBYGGNAD_ANSOKAN_OM_BYGGLOV", AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		final OrganizationDTO applicant = (OrganizationDTO) TestUtil.createStakeholderDTO(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT.toString()));
		input.setStakeholders(List.of(applicant));

		byggrService.saveNewCase(input, MUNICIPALITY_ID);

		final ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
		verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
		final SaveNewArendeMessage saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
		final Arende2 arende = saveNewArendeMessage.getArende();
		final var intressenter = arende.getIntressentLista().getIntressent();

		assertThat(intressenter).hasSize(1);
		final var applicants = intressenter.stream().filter(intressent -> intressent.getRollLista().getRoll().contains(StakeholderRole.APPLICANT.getText())).toList();
		assertThat(applicants).hasSize(1);
		assertOrganizationDTO(applicant, applicants.getFirst());
	}

	// 1 applicant and 1 propertyOwner
	@Test
	void testPopulateStakeholderListWithPropertyOwners1() {
		final ByggRCaseDTO input = createByggRCaseDTO("NYBYGGNAD_ANSOKAN_OM_BYGGLOV", AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		final PersonDTO applicant = (PersonDTO) TestUtil.createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT.toString()));
		final PersonDTO propertyOwner = (PersonDTO) TestUtil.createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.PROPERTY_OWNER.toString()));
		input.setStakeholders(List.of(applicant));

		when(fbServiceMock.getPropertyOwnerByPropertyDesignation(anyString())).thenReturn(List.of(propertyOwner));

		byggrService.saveNewCase(input, MUNICIPALITY_ID);

		final ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
		verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
		final SaveNewArendeMessage saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
		final Arende2 arende = saveNewArendeMessage.getArende();
		final var intressenter = arende.getIntressentLista().getIntressent();

		assertThat(intressenter).hasSize(2);

		final var propertyOwners = intressenter.stream().filter(intressent -> intressent.getRollLista().getRoll().contains(StakeholderRole.PROPERTY_OWNER.getText())).toList();
		assertThat(propertyOwners).hasSize(1);
		assertPersonDTO(propertyOwner, propertyOwners.getFirst());

		final var applicants = intressenter.stream().filter(intressent -> intressent.getRollLista().getRoll().contains(StakeholderRole.APPLICANT.getText())).toList();
		assertThat(applicants).hasSize(1);
		assertPersonDTO(applicant, applicants.getFirst());
	}

	// same as testPopulateStakeholderListWithPropertyOwners1 but for organization
	@Test
	void testPopulateStakeholderListWithPropertyOwners11() {
		final ByggRCaseDTO input = createByggRCaseDTO("NYBYGGNAD_ANSOKAN_OM_BYGGLOV", AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		final OrganizationDTO applicant = (OrganizationDTO) TestUtil.createStakeholderDTO(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT.toString()));
		final OrganizationDTO propertyOwner = (OrganizationDTO) TestUtil.createStakeholderDTO(StakeholderType.ORGANIZATION, List.of(StakeholderRole.PROPERTY_OWNER.toString()));
		input.setStakeholders(List.of(applicant));

		when(fbServiceMock.getPropertyOwnerByPropertyDesignation(anyString())).thenReturn(List.of(propertyOwner));

		byggrService.saveNewCase(input, MUNICIPALITY_ID);

		final ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
		verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
		final SaveNewArendeMessage saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
		final Arende2 arende = saveNewArendeMessage.getArende();
		final var intressenter = arende.getIntressentLista().getIntressent();

		assertThat(intressenter).hasSize(2);

		final var propertyOwners = intressenter.stream().filter(intressent -> intressent.getRollLista().getRoll().contains(StakeholderRole.PROPERTY_OWNER.getText())).toList();
		assertThat(propertyOwners).hasSize(1);
		assertOrganizationDTO(propertyOwner, propertyOwners.getFirst());

		final var applicants = intressenter.stream().filter(intressent -> intressent.getRollLista().getRoll().contains(StakeholderRole.APPLICANT.getText())).toList();
		assertThat(applicants).hasSize(1);
		assertOrganizationDTO(applicant, applicants.getFirst());
	}

	// 1 applicant that is also a propertyOwner + 1 more propertyOwner
	@Test
	void testPopulateStakeholderListWithPropertyOwners2() {
		final var input = createByggRCaseDTO("NYBYGGNAD_ANSOKAN_OM_BYGGLOV", AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		final var applicant = (PersonDTO) TestUtil.createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT.toString()));
		input.setStakeholders(List.of(applicant));

		final var propertyOwner = (PersonDTO) TestUtil.createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.PROPERTY_OWNER.toString()));

		when(fbServiceMock.getPropertyOwnerByPropertyDesignation(anyString())).thenReturn(List.of(applicant, propertyOwner));

		byggrService.saveNewCase(input, MUNICIPALITY_ID);

		final ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
		verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
		final SaveNewArendeMessage saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
		final Arende2 arende = saveNewArendeMessage.getArende();
		final var intressenter = arende.getIntressentLista().getIntressent();

		assertThat(intressenter).hasSize(2);

		final var propertyOwners = intressenter.stream().filter(intressent -> intressent.getRollLista().getRoll().contains(StakeholderRole.PROPERTY_OWNER.getText())).toList();
		assertThat(propertyOwners).hasSize(2);

		final var applicants = intressenter.stream().filter(intressent -> intressent.getRollLista().getRoll().contains(StakeholderRole.APPLICANT.getText())).toList();
		assertThat(applicants).hasSize(1);
	}

	// Case does not contain PropertyOwner
	@Test
	void testContainsPropertyOwner() {
		final ByggRCaseDTO input = createByggRCaseDTO("NYBYGGNAD_ANSOKAN_OM_BYGGLOV", AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		final PersonDTO applicant = (PersonDTO) TestUtil.createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT.toString()));
		input.setStakeholders(List.of(applicant));

		when(fbServiceMock.getPropertyOwnerByPropertyDesignation(anyString())).thenReturn(Collections.emptyList());

		final var postResult = byggrService.saveNewCase(input, MUNICIPALITY_ID);
		final ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
		verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
		final SaveNewArendeMessage saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
		final Arende2 arende = saveNewArendeMessage.getArende();
		final var intressenter = arende.getIntressentLista().getIntressent();

		assertThat(intressenter).hasSize(1);

		final ArgumentCaptor<SaveNewHandelse> saveNewHandelseRequestCaptor = ArgumentCaptor.forClass(SaveNewHandelse.class);
		verify(arendeExportClientMock, times(1)).saveNewHandelse(saveNewHandelseRequestCaptor.capture());

		assertHandelse(postResult.getDnr(),
			saveNewHandelseRequestCaptor.getValue().getMessage(),
			List.of("- Fastighetsägare kunde inte registreras maskinellt.",
				"Du måste registrera ovanstående punkter manuellt. Det inkomna ärendet hittar du i handlingen \"Ansökan om bygglov\"."),
			"Manuell hantering",
			"STATUS",
			"MANHANT",
			null);
	}

	@Test
	void testControlOfficial() {
		final ByggRCaseDTO input = createByggRCaseDTO("NYBYGGNAD_ANSOKAN_OM_BYGGLOV", AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		final PersonDTO controlOfficial = (PersonDTO) TestUtil.createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.CONTROL_OFFICIAL.toString()));
		final PersonDTO applicant = (PersonDTO) TestUtil.createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT.toString()));
		input.setStakeholders(List.of(applicant, controlOfficial));

		final var postResult = byggrService.saveNewCase(input, MUNICIPALITY_ID);

		final ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
		verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
		final SaveNewArendeMessage saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
		final Arende2 arende = saveNewArendeMessage.getArende();
		final var intressenter = arende.getIntressentLista().getIntressent();

		// The control official should be excluded
		assertThat(intressenter).hasSize(1);

		final ArgumentCaptor<SaveNewHandelse> saveNewHandelseRequestCaptor = ArgumentCaptor.forClass(SaveNewHandelse.class);
		verify(arendeExportClientMock, times(1)).saveNewHandelse(saveNewHandelseRequestCaptor.capture());

		assertHandelse(postResult.getDnr(),
			saveNewHandelseRequestCaptor.getValue().getMessage(),
			List.of("- Det finns uppgifter om kontrollansvarig i den inkomna ansökan. Detta går inte att registrera maskinellt.",
				"Du måste registrera ovanstående punkter manuellt. Det inkomna ärendet hittar du i handlingen \"Ansökan om bygglov\"."),
			"Manuell hantering",
			"STATUS",
			"MANHANT",
			null);
	}

	// Test two persons with the same personId - should generate handelse
	@Test
	void testDoublePersonId() {
		final String personId = UUID.randomUUID().toString();
		final ByggRCaseDTO input = createByggRCaseDTO("NYBYGGNAD_ANSOKAN_OM_BYGGLOV", AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		final PersonDTO paymentPerson = (PersonDTO) TestUtil.createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.PAYMENT_PERSON.toString()));
		paymentPerson.setPersonId(personId);
		final PersonDTO applicant = (PersonDTO) TestUtil.createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT.toString()));
		applicant.setPersonId(personId);
		input.setStakeholders(List.of(applicant, paymentPerson));

		final var postResult = byggrService.saveNewCase(input, MUNICIPALITY_ID);

		final ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
		verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
		final SaveNewArendeMessage saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
		final Arende2 arende = saveNewArendeMessage.getArende();
		final var intressenter = arende.getIntressentLista().getIntressent();

		// Both should be excluded
		assertThat(intressenter).isEmpty();

		final ArgumentCaptor<SaveNewHandelse> saveNewHandelseRequestCaptor = ArgumentCaptor.forClass(SaveNewHandelse.class);
		verify(arendeExportClientMock, times(1)).saveNewHandelse(saveNewHandelseRequestCaptor.capture());

		assertHandelse(postResult.getDnr(),
			saveNewHandelseRequestCaptor.getValue().getMessage(),
			List.of("- Det finns flera intressenter med samma personnummer i den inkomna ansökan. Detta går inte att registrera maskinellt.",
				"Du måste registrera ovanstående punkter manuellt. Det inkomna ärendet hittar du i handlingen \"Ansökan om bygglov\"."),
			"Manuell hantering",
			"STATUS", "MANHANT",
			null);
	}

	// Test saveNewIncomingAttachmentHandelse
	@Test
	void testSaveNewIncomingAttachmentHandelse() {
		final String dnr = UUID.randomUUID().toString();
		final List<AttachmentDTO> attachments = List.of(TestUtil.createAttachmentDTO((AttachmentCategory) TestUtil.getRandomOfEnum(AttachmentCategory.class)));
		byggrService.saveNewIncomingAttachmentHandelse(dnr, attachments);

		final ArgumentCaptor<SaveNewHandelse> saveNewHandelseRequestCaptor = ArgumentCaptor.forClass(SaveNewHandelse.class);
		verify(arendeExportClientMock, times(1)).saveNewHandelse(saveNewHandelseRequestCaptor.capture());

		assertHandelse(
			dnr,
			saveNewHandelseRequestCaptor.getValue().getMessage(),
			List.of("Inkomna kompletteringar via e-tjänst."),
			"Komplettering till Admin",
			"HANDLING",
			"KOMPADM",
			attachments);
	}

	// Test getByggRStatus
	@Test
	void testGetByggRStatus() {
		final String caseId = MessageFormat.format("BYGG-2022-{0}", new Random().nextInt(100000));
		final String externalCaseID = UUID.randomUUID().toString();
		final Integer arendeId = 123456;

		// Mock caseMappingServiceMock
		final List<CaseMapping> caseMappingList = new ArrayList<>();

		caseMappingList.add(CaseMapping.builder()
			.withExternalCaseId(externalCaseID)
			.withCaseId(caseId)
			.withSystem(SystemType.BYGGR)
			.withCaseType("NYBYGGNAD_ANSOKAN_OM_BYGGLOV")
			.withServiceName("Test service")
			.build());

		// Mock arendeExportClientMock
		final GetArendeResponse getArendeResponse = new GetArendeResponse();
		final Arende arende = new Arende();
		arende.setDnr(caseId);
		arende.setArendeId(arendeId);
		arende.setStatus("Pågående");
		final ArrayOfHandelse arrayOfHandelse = new ArrayOfHandelse();
		final Handelse handelse1 = new Handelse();
		handelse1.setStartDatum(LocalDateTime.now().minusDays(5));
		handelse1.setHandelsetyp("Handelstyp 1");
		handelse1.setHandelseslag("Handelsbeslag 1");
		handelse1.setHandelseutfall("Handelsutfall 1");
		final Handelse handelse2 = new Handelse();
		handelse2.setStartDatum(LocalDateTime.now().minusDays(2));
		handelse2.setHandelsetyp("BESLUT");
		handelse2.setHandelseslag("SLU");
		handelse2.setHandelseutfall("Handelsutfall 2");
		final Handelse handelse3 = new Handelse();
		handelse3.setStartDatum(LocalDateTime.now().minusDays(10));
		handelse3.setHandelsetyp("Handelstyp 3");
		handelse3.setHandelseslag("Handelsslag 3");
		handelse3.setHandelseutfall("Handelsutfall 3");
		arrayOfHandelse.getHandelse().add(handelse1);
		arrayOfHandelse.getHandelse().add(handelse2);
		arrayOfHandelse.getHandelse().add(handelse3);
		arende.setHandelseLista(arrayOfHandelse);
		getArendeResponse.setGetArendeResult(arende);
		when(arendeExportClientMock.getArende(any())).thenReturn(getArendeResponse);

		// Act
		final var getStatusResult = byggrService.toByggrStatus(caseMappingList.getFirst());

		assertCaseStatus(caseId, caseId, externalCaseID, caseMappingList.getFirst().getCaseType(), caseMappingList.getFirst().getServiceName(), handelse2.getHandelseslag(), handelse2.getStartDatum(), getStatusResult);

		final ArgumentCaptor<GetArende> getArendeRequestCaptor = ArgumentCaptor.forClass(GetArende.class);
		verify(arendeExportClientMock, times(1)).getArende(getArendeRequestCaptor.capture());
		assertThat(getArendeRequestCaptor.getValue().getDnr()).isEqualTo(caseId);
	}

	// Test getByggRStatusByOrgNr
	@Test
	void testGetByggRStatusByOrgNr() throws ExecutionException, InterruptedException {

		final String caseId1 = MessageFormat.format("BYGG-2021-{0}", new Random().nextInt(100000));
		final String caseId2 = MessageFormat.format("BYGG-2022-{0}", new Random().nextInt(100000));
		final Integer arendeId1 = 123456;
		final Integer arendeId2 = 654321;
		final String externalCaseID1 = UUID.randomUUID().toString();
		final String externalCaseID2 = UUID.randomUUID().toString();

		// Mock caseMappingServiceMock
		final List<CaseMapping> caseMappingList1 = new ArrayList<>();
		caseMappingList1.add(CaseMapping.builder()
			.withExternalCaseId(externalCaseID1)
			.withCaseId(caseId1)
			.withSystem(SystemType.BYGGR)
			.withCaseType("NYBYGGNAD_ANSOKAN_OM_BYGGLOV")
			.withServiceName("Test service")
			.build());

		when(caseMappingServiceMock.getCaseMapping(null, caseId1, MUNICIPALITY_ID)).thenReturn(caseMappingList1);
		when(caseMappingServiceMock.getCaseMapping(externalCaseID1, caseId1, MUNICIPALITY_ID)).thenReturn(caseMappingList1);

		final List<CaseMapping> caseMappingList2 = new ArrayList<>();

		caseMappingList2.add(CaseMapping.builder()
			.withExternalCaseId(externalCaseID2)
			.withCaseId(caseId2)
			.withSystem(SystemType.BYGGR)
			.withCaseType("ANMALAN_ATTEFALL")
			.withServiceName("Test service 2")
			.build());

		when(caseMappingServiceMock.getCaseMapping(null, caseId2, MUNICIPALITY_ID)).thenReturn(caseMappingList2);
		when(caseMappingServiceMock.getCaseMapping(externalCaseID2, caseId2, MUNICIPALITY_ID)).thenReturn(caseMappingList2);

		// Mock ArendeExportClientMock
		final GetRelateradeArendenByPersOrgNrAndRoleResponse getRelateradeArendenByPersOrgNrAndRoleResponse = new GetRelateradeArendenByPersOrgNrAndRoleResponse();
		final ArrayOfArende1 arrayOfArende = new ArrayOfArende1();
		final Arende arende1 = new Arende();
		arende1.setDnr(caseId1);
		arende1.setArendeId(arendeId1);
		arende1.setStatus("Pågående");
		final ArrayOfHandelse arrayOfHandelse1 = new ArrayOfHandelse();
		final Handelse handelse1 = new Handelse();
		handelse1.setStartDatum(LocalDateTime.now().minusDays(5));
		handelse1.setHandelsetyp("HANDLING");
		handelse1.setHandelseslag("KOMPL");
		final Handelse handelse2 = new Handelse();
		handelse2.setStartDatum(LocalDateTime.now().minusDays(2));
		handelse2.setHandelsetyp("BESLUT");
		handelse2.setHandelseslag("SLU");
		arrayOfHandelse1.getHandelse().add(handelse1);
		arrayOfHandelse1.getHandelse().add(handelse2);
		arende1.setHandelseLista(arrayOfHandelse1);
		arrayOfArende.getArende().add(arende1);

		final Arende arende2 = new Arende();
		arende2.setDnr(caseId2);
		arende2.setArendeId(arendeId2);
		arende2.setStatus("Pågående");
		final ArrayOfHandelse arrayOfHandelse2 = new ArrayOfHandelse();
		final Handelse handelse21 = new Handelse();
		handelse21.setStartDatum(LocalDateTime.now().minusDays(5));
		handelse21.setHandelsetyp("HANDLING");
		handelse21.setHandelseslag("KOMPL");
		final Handelse handelse22 = new Handelse();
		handelse22.setStartDatum(LocalDateTime.now().minusDays(10));
		handelse22.setHandelsetyp("BESLUT");
		handelse22.setHandelseslag("SLU");
		arrayOfHandelse2.getHandelse().add(handelse21);
		arrayOfHandelse2.getHandelse().add(handelse22);
		arende2.setHandelseLista(arrayOfHandelse2);
		arrayOfArende.getArende().add(arende2);
		getRelateradeArendenByPersOrgNrAndRoleResponse.setGetRelateradeArendenByPersOrgNrAndRoleResult(arrayOfArende);
		when(arendeExportClientMock.getRelateradeArendenByPersOrgNrAndRole(any())).thenReturn(getRelateradeArendenByPersOrgNrAndRoleResponse);

		final String orgnr = TestUtil.generateRandomOrganizationNumber();
		final var getStatusResult = byggrService.getByggrStatusByLegalId(orgnr, PartyType.ENTERPRISE, MUNICIPALITY_ID).get();

		assertThat(getStatusResult).hasSize(2);
		assertCaseStatus(caseId1, caseId1, externalCaseID1, caseMappingList1.getFirst().getCaseType(), caseMappingList1.getFirst().getServiceName(), handelse2.getHandelseslag(), handelse2.getStartDatum(), getStatusResult.getFirst());
		assertCaseStatus(caseId2, caseId2, externalCaseID2, caseMappingList2.getFirst().getCaseType(), caseMappingList2.getFirst().getServiceName(), handelse21.getHandelseslag(), handelse21.getStartDatum(), getStatusResult.get(1));

		final ArgumentCaptor<GetRelateradeArendenByPersOrgNrAndRole> getRelateradeArendenByPersOrgNrAndRoleRequestCaptor = ArgumentCaptor.forClass(GetRelateradeArendenByPersOrgNrAndRole.class);
		verify(arendeExportClientMock).getRelateradeArendenByPersOrgNrAndRole(getRelateradeArendenByPersOrgNrAndRoleRequestCaptor.capture());
		assertThat(getRelateradeArendenByPersOrgNrAndRoleRequestCaptor.getValue().getPersOrgNr()).isEqualTo(orgnr);
		assertThat(getRelateradeArendenByPersOrgNrAndRoleRequestCaptor.getValue().getArendeIntressentRoller().getString()).contains(StakeholderRole.APPLICANT.getText());
		assertThat(getRelateradeArendenByPersOrgNrAndRoleRequestCaptor.getValue().getHandelseIntressentRoller().getString()).contains(StakeholderRole.APPLICANT.getText());
	}

	/**
	 * Test scenario where there is a 1-person stakeholder. Should retrieve and return personal number from citizen service.
	 */
	@Test
	void extractStakeholderId1() {
		final var personStakeholder = (PersonDTO) createStakeholderDTO(StakeholderType.PERSON, List.of("Granne"));
		final var personalNumber = "200001011234";
		final List<StakeholderDTO> stakeholders = List.of(personStakeholder);
		when(partyIntegrationMock.getLegalIdByPartyId(MUNICIPALITY_ID, personStakeholder.getPersonId())).thenReturn(Map.of(PRIVATE, personalNumber));

		final var result = byggrService.extractStakeholderId(stakeholders, MUNICIPALITY_ID);

		assertThat(result).isEqualTo("20000101-1234");
		verify(partyIntegrationMock).getLegalIdByPartyId(MUNICIPALITY_ID, personStakeholder.getPersonId());
	}

	/**
	 * Test a scenario where there are 2 stakeholders, one person, and one organization. Should return the organization
	 * stakeholders organization number.
	 */
	@Test
	void extractStakeholderId2() {
		final var personStakeholder = (PersonDTO) createStakeholderDTO(StakeholderType.PERSON, List.of("Granne"));
		final var organizationStakeholder = (OrganizationDTO) createStakeholderDTO(StakeholderType.ORGANIZATION, List.of("Granne"));
		final var organizationNumberOepFormat = "123456781234";
		organizationStakeholder.setOrganizationNumber(organizationNumberOepFormat);

		final var stakeholders = List.of(personStakeholder, organizationStakeholder);

		final var result = byggrService.extractStakeholderId(stakeholders, MUNICIPALITY_ID);

		assertThat(result).isEqualTo("12345678-1234");
		verifyNoInteractions(partyIntegrationMock);
	}

	/**
	 * Test scenario where there are 2 stakeholders, one person, and one organization and the organization number is in
	 * 10-digit format. Should add the "16" prefix to the organization stakeholders organization number and return.
	 */
	@Test
	void extractStakeholderId3() {
		final var personStakeholder = (PersonDTO) createStakeholderDTO(StakeholderType.PERSON, List.of("Granne"));
		final var organizationStakeholder = (OrganizationDTO) createStakeholderDTO(StakeholderType.ORGANIZATION, List.of("Granne"));
		final var organizationNumberOepFormat = "1234561234";
		organizationStakeholder.setOrganizationNumber(organizationNumberOepFormat);

		final var stakeholders = List.of(personStakeholder, organizationStakeholder);

		final var result = byggrService.extractStakeholderId(stakeholders, MUNICIPALITY_ID);

		assertThat(result).isEqualTo("16123456-1234");
		verifyNoInteractions(partyIntegrationMock);
	}

	@Test
	void getByggrIntressenter_withSundsvallsKommunAsPropertyOwner() {
		// Arrange
		final var byggRCase = TestUtil.createByggRCaseDTO("NYBYGGNAD_ANSOKAN_OM_BYGGLOV", AttachmentCategory.BUILDING_PERMIT_APPLICATION);

		// Create a property owner organization that is Sundsvalls kommun
		final var sundsvallsKommunPropertyOwner = new OrganizationDTO();
		sundsvallsKommunPropertyOwner.setOrganizationName("Sundsvalls kommun");
		sundsvallsKommunPropertyOwner.setOrganizationNumber("212000-2411");
		sundsvallsKommunPropertyOwner.setRoles(List.of(StakeholderRole.PROPERTY_OWNER.toString()));

		byggRCase.getStakeholders().add(sundsvallsKommunPropertyOwner);

		// Mock GetIntressent response from ByggR
		final var intressentFromByggr = new arendeexport.Intressent();
		intressentFromByggr.setIntressentId(107445);
		intressentFromByggr.setIntressentVersionId(214462);

		final var getIntressentResponse2 = new arendeexport.GetIntressentResponse2();
		final var arrayOfIntressent = new arendeexport.ArrayOfIntressent();
		arrayOfIntressent.getIntressent().add(intressentFromByggr);
		getIntressentResponse2.setIntressent(arrayOfIntressent);

		final var getIntressentResponse = new arendeexport.GetIntressentResponse();
		getIntressentResponse.setGetIntressentResult(getIntressentResponse2);

		when(arendeExportClientMock.getIntressent(any())).thenReturn(getIntressentResponse);

		// Act
		final var result = byggrService.getByggrIntressenter(byggRCase);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getIntressent()).isNotEmpty();

		// Find the Sundsvalls kommun intressent in the result
		final var sundsvallsKommunIntressent = result.getIntressent().stream()
			.filter(i -> i.getIntressentId() != null && i.getIntressentId().equals(107445))
			.findFirst();

		assertThat(sundsvallsKommunIntressent).isPresent();
		assertThat(sundsvallsKommunIntressent.get().getIntressentId()).isEqualTo(107445);
		assertThat(sundsvallsKommunIntressent.get().getIntressentVersionId()).isEqualTo(214462);

		// Verify that GetIntressent was called
		verify(arendeExportClientMock).getIntressent(any(arendeexport.GetIntressent.class));
	}

	@Test
	void getByggrIntressenter_withSundsvallsKommunAsPropertyOwner_fallbackWhenGetIntressentFails() {
		// Arrange
		final var byggRCase = TestUtil.createByggRCaseDTO("NYBYGGNAD_ANSOKAN_OM_BYGGLOV", AttachmentCategory.BUILDING_PERMIT_APPLICATION);

		// Create a property owner organization that is Sundsvalls kommun
		final var sundsvallsKommunPropertyOwner = new OrganizationDTO();
		sundsvallsKommunPropertyOwner.setOrganizationName("Sundsvalls kommun");
		sundsvallsKommunPropertyOwner.setOrganizationNumber("16212000-2411");
		sundsvallsKommunPropertyOwner.setRoles(List.of(StakeholderRole.PROPERTY_OWNER.toString()));

		byggRCase.getStakeholders().add(sundsvallsKommunPropertyOwner);

		// Mock GetIntressent to return an empty result (no intressent found)
		final var getIntressentResponse = new arendeexport.GetIntressentResponse();
		when(arendeExportClientMock.getIntressent(any())).thenReturn(getIntressentResponse);

		// Act
		final var result = byggrService.getByggrIntressenter(byggRCase);

		// Assert - should fall back to using organization number
		assertThat(result).isNotNull();
		assertThat(result.getIntressent()).isNotEmpty();

		// Find the Sundsvalls kommun intressent in the result
		final var sundsvallsKommunIntressent = result.getIntressent().stream()
			.filter(i -> "16212000-2411".equals(i.getPersOrgNr()))
			.findFirst();

		assertThat(sundsvallsKommunIntressent).isPresent();
		assertThat(sundsvallsKommunIntressent.get().getPersOrgNr()).isEqualTo("16212000-2411");

		// Verify that GetIntressent was called
		verify(arendeExportClientMock).getIntressent(any(arendeexport.GetIntressent.class));
	}

}
