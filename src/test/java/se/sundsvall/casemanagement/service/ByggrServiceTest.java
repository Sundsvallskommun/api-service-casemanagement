package se.sundsvall.casemanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.casemanagement.TestUtil.FNR;
import static se.sundsvall.casemanagement.TestUtil.setUpCaseTypes;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.ANDRING_ANSOKAN_OM_BYGGLOV;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.STRANDSKYDD_ANDRAD_ANVANDNING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.STRANDSKYDD_ANLAGGANDE;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.STRANDSKYDD_ANORDNANDE;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.STRANDSKYDD_NYBYGGNAD;
import static se.sundsvall.casemanagement.util.Constants.ATTEFALL;
import static se.sundsvall.casemanagement.util.Constants.BYGGLOV_FOR;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_ARENDEMENING_STRANDSKYDD_FOR_ANDRAD_ANVANDNING;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_ARENDEMENING_STRANDSKYDD_FOR_ANLAGGANDE;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_ARENDEMENING_STRANDSKYDD_FOR_ANORDNANDE;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_ARENDEMENING_STRANDSKYDD_FOR_NYBYGGNAD;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSESLAG_KOMPLETTERANDE_HANDLINGAR;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSESLAG_SLUTBESKED;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSETYP_BESLUT;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSETYP_HANDLING;
import static se.sundsvall.casemanagement.util.Constants.HANDELSESLAG_ANMALAN_ATTEFALL;
import static se.sundsvall.casemanagement.util.Constants.HANDELSESLAG_BYGGLOV;
import static se.sundsvall.casemanagement.util.Constants.HANDELSESLAG_STRANDSKYDD;
import static se.sundsvall.casemanagement.util.Constants.HANDELSETYP_ANMALAN;
import static se.sundsvall.casemanagement.util.Constants.HANDELSETYP_ANSOKAN;
import static se.sundsvall.casemanagement.util.Constants.RUBRIK_ANMALAN_ATTEFALL;
import static se.sundsvall.casemanagement.util.Constants.RUBRIK_BYGGLOV;
import static se.sundsvall.casemanagement.util.Constants.RUBRIK_STRANDSKYDD;
import static se.sundsvall.casemanagement.util.Constants.STRANDSKYDD;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.casemanagement.TestUtil;
import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.api.model.CaseStatusDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.PlanningPermissionCaseDTO;
import se.sundsvall.casemanagement.api.model.PlanningPermissionFacilityDTO;
import se.sundsvall.casemanagement.api.model.StakeholderDTO;
import se.sundsvall.casemanagement.api.model.enums.AddressCategory;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.FacilityType;
import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.api.model.enums.StakeholderType;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.byggr.ArendeExportClient;
import se.sundsvall.casemanagement.integration.byggr.ByggrService;
import se.sundsvall.casemanagement.integration.db.CaseTypeRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.util.Constants;

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

@ExtendWith(MockitoExtension.class)
class ByggrServiceTest {

	private static final String BYGG_CASE_ID = "Inskickat";

	@Mock
	private CaseTypeRepository caseTypeRepository;

	@InjectMocks
	private ByggrService byggrService;

	@Mock
	private FbService fbServiceMock;

	@Mock
	private CitizenService citizenServiceMock;

	@Mock
	private CaseMappingService caseMappingServiceMock;

	@Mock
	private ArendeExportClient arendeExportClientMock;

	private static void assertCaseStatus(final String caseId, final String externalCaseID, final CaseType caseType, final String serviceName, final String status, final LocalDateTime dateTime, final CaseStatusDTO getStatusResult) {
		assertThat(getStatusResult.getCaseId()).isEqualTo(caseId);
		assertThat(getStatusResult.getExternalCaseId()).isEqualTo(externalCaseID);
		assertThat(getStatusResult.getSystem()).isEqualTo(SystemType.BYGGR);
		assertThat(getStatusResult.getCaseType()).isEqualTo(caseType.toString());
		assertThat(getStatusResult.getServiceName()).isEqualTo(serviceName);
		assertThat(getStatusResult.getStatus()).isEqualTo(status);
		assertThat(getStatusResult.getTimestamp()).isEqualTo(dateTime);
	}

	private static void assertHandelse(final String dnr, final SaveNewHandelseMessage saveNewHandelseMessage, final List<String> notesToContain, final String handelseRubrik, final String handelstyp, final String handelseslag, final List<AttachmentDTO> attachments) {
		assertThat(saveNewHandelseMessage.getDnr()).isEqualTo(dnr);
		assertThat(saveNewHandelseMessage.getHandlaggarSign()).isEqualTo(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN);
		assertThat(saveNewHandelseMessage.getHandelse().getRubrik()).isEqualTo(handelseRubrik);
		assertThat(saveNewHandelseMessage.getHandelse().getRiktning()).isEqualTo(Constants.BYGGR_HANDELSE_RIKTNING_IN);
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
			assertThat(handling.getStatus()).isEqualTo(Constants.BYGGR_HANDLING_STATUS_INKOMMEN);
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

	private static void assertThatArendeIsEqual(final Arende2 arende, final String arendeTyp, final PlanningPermissionFacilityDTO inputFacility) {
		assertThat(arende.getArendetyp()).isEqualTo(arendeTyp);

		if (arendeTyp.equals(STRANDSKYDD)) {
			assertThat(arende.getArendegrupp()).isEqualTo(Constants.BYGGR_ARENDEGRUPP_STRANDSKYDD);
		} else {
			assertThat(arende.getArendegrupp()).isEqualTo(Constants.BYGGR_ARENDEGRUPP_LOV_ANMALNINGSARENDE);
		}
		assertThat(arende.getNamndkod()).isEqualTo(Constants.BYGGR_NAMNDKOD_STADSBYGGNADSNAMNDEN);
		assertThat(arende.getEnhetkod()).isEqualTo(Constants.BYGGR_ENHETKOD_STADSBYGGNADSKONTORET);
		assertThat(arende.getKommun()).isEqualTo(Constants.BYGGR_KOMMUNKOD_SUNDSVALL_KOMMUN);
		assertThat(arende.getHandlaggare().getSignatur()).isEqualTo(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN);
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
			assertThat(handling.getStatus()).isEqualTo(Constants.BYGGR_HANDLING_STATUS_INKOMMEN);
			assertThat(handling.getTyp()).isEqualTo(inputAttachment.getCategory());
		});
	}

	@BeforeEach
	public void setup() {
		lenient().when(caseTypeRepository.findAll()).thenReturn(setUpCaseTypes());
		TestUtil.standardMockFb(fbServiceMock);
		TestUtil.standardMockArendeExport(arendeExportClientMock);
		TestUtil.standardMockCitizen(citizenServiceMock);
	}

	//ANSOKAN_OM_BYGGLOV
	@ParameterizedTest
	@EnumSource(value = CaseType.class, names = {"STRANDSKYDD_NYBYGGNAD",
		"STRANDSKYDD_ANLAGGANDE", "STRANDSKYDD_ANORDNANDE",
		"STRANDSKYDD_ANDRAD_ANVANDNING"})
	void testStrandskyddCaseType(final CaseType caseType) {
		final var caseTypes = Map.of(
			STRANDSKYDD_NYBYGGNAD, BYGGR_ARENDEMENING_STRANDSKYDD_FOR_NYBYGGNAD,
			STRANDSKYDD_ANLAGGANDE, BYGGR_ARENDEMENING_STRANDSKYDD_FOR_ANLAGGANDE,
			STRANDSKYDD_ANORDNANDE, BYGGR_ARENDEMENING_STRANDSKYDD_FOR_ANORDNANDE,
			STRANDSKYDD_ANDRAD_ANVANDNING, BYGGR_ARENDEMENING_STRANDSKYDD_FOR_ANDRAD_ANVANDNING
		);

		final var input = TestUtil.createPlanningPermissionCaseDTO(caseType, AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		final var inputFacility = input.getFacilities().getFirst();
		final var inputAttachment = input.getAttachments().getFirst();
		final var response = byggrService.postCase(input);

		assertThat(response.getDnr()).isEqualTo(BYGG_CASE_ID);

		final var saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
		verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());

		final var saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
		final var arende = saveNewArendeMessage.getArende();
		final var handelse = saveNewArendeMessage.getHandelse();
		final var handlingar = saveNewArendeMessage.getHandlingar();

		// SaveNewArendeMessage
		assertThat(saveNewArendeMessage.getHandlaggarSign()).isEqualTo(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN);

		// Arende
		assertThatArendeIsEqual(arende, STRANDSKYDD, inputFacility);
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
			}
		);


		// Handlingar
		assertThat(handlingar.getHandling()).hasSize(1);
		final var handling = handlingar.getHandling().getFirst();
		assertThat(inputAttachment.getName()).isEqualTo(handling.getAnteckning());
		assertThat(handling.getDokument().getFil().getFilBuffer()).isNotNull();
		assertThat(handling.getDokument().getFil().getFilAndelse()).isEqualTo(inputAttachment.getExtension().toLowerCase());
		assertThat(handling.getDokument().getNamn()).isEqualTo(inputAttachment.getName());
		assertThat(handling.getDokument().getBeskrivning()).isEqualTo(inputAttachment.getNote());
		assertThat(handling.getStatus()).isEqualTo(Constants.BYGGR_HANDLING_STATUS_INKOMMEN);
		assertThat(handling.getTyp()).isEqualTo(inputAttachment.getCategory());

		// Handelser
		assertThat(handelse.getStartDatum()).isNotNull();
		assertThat(handelse.getRiktning()).isEqualTo(Constants.BYGGR_HANDELSE_RIKTNING_IN);
		assertThat(handelse.getRubrik()).isEqualTo(RUBRIK_STRANDSKYDD);
		assertThat(handelse.getHandelsetyp()).isEqualTo(HANDELSETYP_ANSOKAN);
		assertThat(handelse.getHandelseslag()).isEqualTo(HANDELSESLAG_STRANDSKYDD);
	}

	//ANSOKAN_OM_BYGGLOV
	@ParameterizedTest
	@EnumSource(value = CaseType.class, names = {"NYBYGGNAD_ANSOKAN_OM_BYGGLOV",
		"ANDRING_ANSOKAN_OM_BYGGLOV", "TILLBYGGNAD_ANSOKAN_OM_BYGGLOV"})
	void testPostNybyggnad(final CaseType caseType) {
		// Arrange
		final var input = TestUtil.createPlanningPermissionCaseDTO(caseType, AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		final var inputFacility = input.getFacilities().getFirst();
		final var inputAttachment = input.getAttachments().getFirst();

		// Act
		final var response = byggrService.postCase(input);

		// Assert
		assertThat(response.getDnr()).isEqualTo(BYGG_CASE_ID);

		final var saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
		verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());

		final var saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
		final var arende = saveNewArendeMessage.getArende();
		final var handelse = saveNewArendeMessage.getHandelse();
		final var handlingar = saveNewArendeMessage.getHandlingar();

		// SaveNewArendeMessage
		assertThat(saveNewArendeMessage.getHandlaggarSign()).isEqualTo(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN);

		// Arende

		if (!caseType.equals(ANDRING_ANSOKAN_OM_BYGGLOV)) {
			assertThat(arende.getArendeklass()).isEqualTo(FacilityType.valueOf(inputFacility.getFacilityType()).getValue());
		} else {
			assertThat(arende.getArendeslag()).isEqualTo(FacilityType.valueOf(inputFacility.getFacilityType()).getValue());
		}

		assertThatArendeIsEqual(arende, BYGGLOV_FOR, inputFacility);

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
		assertThat(handelse.getRiktning()).isEqualTo(Constants.BYGGR_HANDELSE_RIKTNING_IN);
		assertThat(handelse.getRubrik()).isEqualTo(RUBRIK_BYGGLOV);
		assertThat(handelse.getHandelsetyp()).isEqualTo(HANDELSETYP_ANSOKAN);
		assertThat(handelse.getHandelseslag()).isEqualTo(HANDELSESLAG_BYGGLOV);
	}

	// ANMALAN_ATTEFALL
	@Test
	void testPostAttefall() {

		final PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.ANMALAN_ATTEFALL, AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		// Set facilityType to a compatible value
		input.getFacilities().getFirst().setFacilityType(FacilityType.EXTENSION.toString());
		// Set addressCategory to not be INVOICE_ADDRESS, so we can test projektnummer to be propertyDesignation
		input.getStakeholders().getFirst().getAddresses().getFirst().setAddressCategories(List.of(AddressCategory.POSTAL_ADDRESS));
		final PlanningPermissionFacilityDTO inputFacility = input.getFacilities().getFirst();
		final AttachmentDTO inputAttachment = input.getAttachments().getFirst();
		final SaveNewArendeResponse2 response = byggrService.postCase(input);

		assertThat(response.getDnr()).isEqualTo(BYGG_CASE_ID);

		final ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
		verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
		final SaveNewArendeMessage saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
		final Arende2 arende = saveNewArendeMessage.getArende();
		final Handelse handelse = saveNewArendeMessage.getHandelse();
		final ArrayOfHandling handlingar = saveNewArendeMessage.getHandlingar();

		// SaveNewArendeMessage
		assertThat(saveNewArendeMessage.getHandlaggarSign()).isEqualTo(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN);

		// Arende
		assertThatArendeIsEqual(arende, ATTEFALL, inputFacility);
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
		assertThat(handelse.getRiktning()).isEqualTo(Constants.BYGGR_HANDELSE_RIKTNING_IN);
		assertThat(handelse.getRubrik()).isEqualTo(RUBRIK_ANMALAN_ATTEFALL);
		assertThat(handelse.getHandelsetyp()).isEqualTo(HANDELSETYP_ANMALAN);
		assertThat(handelse.getHandelseslag()).isEqualTo(HANDELSESLAG_ANMALAN_ATTEFALL);
	}

	// ANMALAN_ELDSTAD
	@Test
	void testPostEldstad() {

		final PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.ANMALAN_ELDSTAD,
			AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		// Set facilityType to a compatible value
		input.getFacilities().getFirst().setFacilityType(FacilityType.FIREPLACE.toString());
		// Set addressCategory to not be INVOICE_ADDRESS, so we can test projektnummer to be propertyDesignation
		input.getStakeholders().getFirst().getAddresses().getFirst().setAddressCategories(List.of(AddressCategory.POSTAL_ADDRESS));
		final PlanningPermissionFacilityDTO inputFacility = input.getFacilities().getFirst();
		final AttachmentDTO inputAttachment = input.getAttachments().getFirst();
		final SaveNewArendeResponse2 response = byggrService.postCase(input);

		assertThat(response.getDnr()).isEqualTo(BYGG_CASE_ID);

		final ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
		verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
		final SaveNewArendeMessage saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
		final Arende2 arende = saveNewArendeMessage.getArende();
		final Handelse handelse = saveNewArendeMessage.getHandelse();
		final ArrayOfHandling handlingar = saveNewArendeMessage.getHandlingar();

		// SaveNewArendeMessage
		assertThat(saveNewArendeMessage.getHandlaggarSign()).isEqualTo(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN);

		// Arende
		assertThatArendeIsEqual(arende, HANDELSETYP_ANMALAN, inputFacility);
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
		assertThat(handelse.getRiktning()).isEqualTo(Constants.BYGGR_HANDELSE_RIKTNING_IN);
		assertThat(handelse.getRubrik()).isEqualTo(Constants.BYGGR_HANDELSE_RUBRIK_ELDSTAD);
		assertThat(handelse.getHandelsetyp()).isEqualTo(HANDELSETYP_ANMALAN);
		assertThat(handelse.getHandelseslag()).isEqualTo(Constants.BYGGR_HANDELSESLAG_ELDSTAD);
	}

	// ANMALAN_ELDSTAD_SMOKE
	@Test
	void testPostEldstadRokkanal() {

		// Arrange
		final var input = TestUtil.createPlanningPermissionCaseDTO(CaseType.ANMALAN_ELDSTAD, AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		// Set facilityType to a compatible value
		input.getFacilities().getFirst().setFacilityType(FacilityType.FIREPLACE_SMOKECHANNEL.toString());
		// Set addressCategory to not be INVOICE_ADDRESS, so we can test projektnummer to be propertyDesignation
		input.getStakeholders().getFirst().getAddresses().getFirst().setAddressCategories(List.of(AddressCategory.POSTAL_ADDRESS));
		final var inputFacility = input.getFacilities().getFirst();
		final var inputAttachment = input.getAttachments().getFirst();

		// Act
		final SaveNewArendeResponse2 response = byggrService.postCase(input);

		// Assert
		assertThat(response.getDnr()).isEqualTo(BYGG_CASE_ID);

		final ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
		verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
		final var saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
		final var arende = saveNewArendeMessage.getArende();
		final var handelse = saveNewArendeMessage.getHandelse();


		// SaveNewArendeMessage
		assertThat(saveNewArendeMessage.getHandlaggarSign()).isEqualTo(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN);

		// Arende
		assertThatArendeIsEqual(arende, HANDELSETYP_ANMALAN, inputFacility);
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
		assertThat(handelse.getRiktning()).isEqualTo(Constants.BYGGR_HANDELSE_RIKTNING_IN);
		assertThat(handelse.getRubrik()).isEqualTo(Constants.BYGGR_HANDELSE_RUBRIK_ELDSTAD_ROKKANAL);
		assertThat(handelse.getHandelsetyp()).isEqualTo(HANDELSETYP_ANMALAN);
		assertThat(handelse.getHandelseslag()).isEqualTo(Constants.BYGGR_HANDELSESLAG_ELDSTAD_ROKKANAL);
	}

	@Test
	void testCallToCaseMapping() {
		final PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		input.getExtraParameters().put(Constants.SERVICE_NAME, "Test service name");
		final PersonDTO applicant = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT.toString()));
		input.setStakeholders(List.of(applicant));

		final var postResult = byggrService.postCase(input);

		final CaseMapping caseMapping = CaseMapping.builder().withExternalCaseId(input.getExternalCaseId())
			.withCaseId(postResult.getDnr())
			.withSystem(SystemType.BYGGR)
			.withCaseType(input.getCaseType())
			.withServiceName(input.getExtraParameters().get(Constants.SERVICE_NAME))
			.build();

		verify(caseMappingServiceMock, times(1)).postCaseMapping(caseMapping);
	}

	// Test no duplicates of arendeFastighet
	@Test
	void testNoDuplicateFacilities() {
		final PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.BUILDING_PERMIT_APPLICATION);

		final String propertyDesignation = "Sundsvall test 123:123";
		final var facility1 = TestUtil.createPlanningPermissionFacilityDTO(true);
		final var facility2 = TestUtil.createPlanningPermissionFacilityDTO(false);
		final var facility3 = TestUtil.createPlanningPermissionFacilityDTO(false);
		facility1.getAddress().setPropertyDesignation(propertyDesignation);
		facility2.getAddress().setPropertyDesignation(propertyDesignation);
		facility3.getAddress().setPropertyDesignation(propertyDesignation);
		// Add some facilities
		input.setFacilities(List.of(facility1, facility2, facility3));

		byggrService.postCase(input);

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
		final PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		// Set addressCategory to not be INVOICE_ADDRESS, so we can test projektnummer to be propertyDesignation
		input.getStakeholders().getFirst().getAddresses().getFirst().setAddressCategories(List.of(AddressCategory.POSTAL_ADDRESS));

		final var mainFacility = TestUtil.createPlanningPermissionFacilityDTO(true);
		final var randomFacility_1 = TestUtil.createPlanningPermissionFacilityDTO(false);
		randomFacility_1.getAddress().setPropertyDesignation("Sundsvall test 1:1");
		final var randomFacility_2 = TestUtil.createPlanningPermissionFacilityDTO(false);
		randomFacility_2.getAddress().setPropertyDesignation("Sundsvall test 2:2");
		// Add some facilities
		input.setFacilities(List.of(randomFacility_1, mainFacility, randomFacility_2));

		byggrService.postCase(input);

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
		final PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		final PersonDTO applicant = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT.toString()));
		applicant.setAddresses(List.of(TestUtil.createAddressDTO(List.of(AddressCategory.INVOICE_ADDRESS))));
		input.setStakeholders(List.of(applicant));

		assertThatThrownBy(
			() -> byggrService.postCase(input))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.BAD_REQUEST)
			.hasFieldOrPropertyWithValue("detail", Constants.ERR_MSG_PERSON_INVOICE_ADDRESS);
	}

	@Test
	void testSetPersonFields() {

		//Arrange
		final PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		final PersonDTO applicant = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT.toString()));
		input.setStakeholders(List.of(applicant));

		byggrService.postCase(input);

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
		final PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		final OrganizationDTO applicant = (OrganizationDTO) TestUtil.createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT.toString()));
		input.setStakeholders(List.of(applicant));

		byggrService.postCase(input);

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
	void testPopulateStakeholderListWithPropertyOwners_1() {
		final PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		final PersonDTO applicant = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT.toString()));
		final PersonDTO propertyOwner = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.PROPERTY_OWNER.toString()));
		input.setStakeholders(List.of(applicant));

		when(fbServiceMock.getPropertyOwnerByPropertyDesignation(anyString())).thenReturn(List.of(propertyOwner));

		byggrService.postCase(input);

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

	// same as testPopulateStakeholderListWithPropertyOwners_1 but for organization
	@Test
	void testPopulateStakeholderListWithPropertyOwners_1_1() {
		final PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		final OrganizationDTO applicant = (OrganizationDTO) TestUtil.createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT.toString()));
		final OrganizationDTO propertyOwner = (OrganizationDTO) TestUtil.createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.PROPERTY_OWNER.toString()));
		input.setStakeholders(List.of(applicant));

		when(fbServiceMock.getPropertyOwnerByPropertyDesignation(anyString())).thenReturn(List.of(propertyOwner));

		byggrService.postCase(input);

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

	// 1 applicant that is also propertyOwner + 1 more propertyOwner
	@Test
	void testPopulateStakeholderListWithPropertyOwners_2() {
		final var input = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		final var applicant = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT.toString()));
		input.setStakeholders(List.of(applicant));

		final var propertyOwner = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.PROPERTY_OWNER.toString()));

		when(fbServiceMock.getPropertyOwnerByPropertyDesignation(anyString())).thenReturn(List.of(applicant, propertyOwner));

		byggrService.postCase(input);

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
		final PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		final PersonDTO applicant = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT.toString()));
		input.setStakeholders(List.of(applicant));

		when(fbServiceMock.getPropertyOwnerByPropertyDesignation(anyString())).thenReturn(Collections.emptyList());

		final var postResult = byggrService.postCase(input);
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
			List.of(Constants.BYGGR_HANDELSE_ANTECKNING_FASTIGHETSAGARE,
				Constants.BYGGR_HANDELSE_ANTECKNING_DU_MASTE_REGISTRERA_DETTA_MANUELLT),
			Constants.BYGGR_HANDELSE_RUBRIK_MANUELL_HANTERING,
			Constants.BYGGR_HANDELSETYP_STATUS,
			Constants.BYGGR_HANDELSESLAG_MANUELL_HANTERING_KRAVS,
			null);
	}

	@Test
	void testControlOfficial() {
		final PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		final PersonDTO controlOfficial = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.CONTROL_OFFICIAL.toString()));
		final PersonDTO applicant = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT.toString()));
		input.setStakeholders(List.of(applicant, controlOfficial));

		final var postResult = byggrService.postCase(input);

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
			List.of(Constants.BYGGR_HANDELSE_ANTECKNING_KONTROLLANSVARIG,
				Constants.BYGGR_HANDELSE_ANTECKNING_DU_MASTE_REGISTRERA_DETTA_MANUELLT),
			Constants.BYGGR_HANDELSE_RUBRIK_MANUELL_HANTERING,
			Constants.BYGGR_HANDELSETYP_STATUS,
			Constants.BYGGR_HANDELSESLAG_MANUELL_HANTERING_KRAVS,
			null);
	}

	// Test two persons with same personId - should generate handelse
	@Test
	void testDoublePersonId() {
		final String personId = UUID.randomUUID().toString();
		final PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		final PersonDTO paymentPerson = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.PAYMENT_PERSON.toString()));
		paymentPerson.setPersonId(personId);
		final PersonDTO applicant = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT.toString()));
		applicant.setPersonId(personId);
		input.setStakeholders(List.of(applicant, paymentPerson));

		final var postResult = byggrService.postCase(input);

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
			List.of(Constants.BYGGR_HANDELSE_ANTECKNING_INTRESSENT_KUNDE_INTE_REGISTRERAS,
				Constants.BYGGR_HANDELSE_ANTECKNING_DU_MASTE_REGISTRERA_DETTA_MANUELLT),
			Constants.BYGGR_HANDELSE_RUBRIK_MANUELL_HANTERING,
			Constants.BYGGR_HANDELSETYP_STATUS, Constants.BYGGR_HANDELSESLAG_MANUELL_HANTERING_KRAVS,
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
			List.of(Constants.BYGGR_HANDELSE_ANTECKNING),
			Constants.BYGGR_HANDELSE_RUBRIK_KOMPLETTERING_TILL_ADMIN,
			BYGGR_HANDELSETYP_HANDLING,
			Constants.BYGGR_HANDELSESLAG_KOMPLETTERING_TILL_ADMIN,
			attachments);
	}

	// Test getByggRStatus
	@Test
	void testGetByggRStatus() {
		final String caseId = MessageFormat.format("BYGG-2022-{0}", new Random().nextInt(100000));
		final String externalCaseID = UUID.randomUUID().toString();

		// Mock caseMappingServiceMock
		final List<CaseMapping> caseMappingList = new ArrayList<>();

		caseMappingList.add(CaseMapping.builder()
			.withExternalCaseId(externalCaseID)
			.withCaseId(caseId)
			.withSystem(SystemType.BYGGR)
			.withCaseType(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV.toString())
			.withServiceName("Test service")
			.build());

		when(caseMappingServiceMock.getCaseMapping(externalCaseID, caseId)).thenReturn(caseMappingList);

		// Mock arendeExportClientMock
		final GetArendeResponse getArendeResponse = new GetArendeResponse();
		final Arende arende = new Arende();
		arende.setDnr(caseId);
		arende.setStatus("Pågående");
		final ArrayOfHandelse arrayOfHandelse = new ArrayOfHandelse();
		final Handelse handelse_1 = new Handelse();
		handelse_1.setStartDatum(LocalDateTime.now().minusDays(5));
		handelse_1.setHandelsetyp("Handelstyp 1");
		handelse_1.setHandelseslag("Handelsbeslag 1");
		handelse_1.setHandelseutfall("Handelsutfall 1");
		final Handelse handelse_2 = new Handelse();
		handelse_2.setStartDatum(LocalDateTime.now().minusDays(2));
		handelse_2.setHandelsetyp(BYGGR_HANDELSETYP_BESLUT);
		handelse_2.setHandelseslag(BYGGR_HANDELSESLAG_SLUTBESKED);
		handelse_2.setHandelseutfall("Handelsutfall 2");
		final Handelse handelse_3 = new Handelse();
		handelse_3.setStartDatum(LocalDateTime.now().minusDays(10));
		handelse_3.setHandelsetyp("Handelstyp 3");
		handelse_3.setHandelseslag("Handelsslag 3");
		handelse_3.setHandelseutfall("Handelsutfall 3");
		arrayOfHandelse.getHandelse().add(handelse_1);
		arrayOfHandelse.getHandelse().add(handelse_2);
		arrayOfHandelse.getHandelse().add(handelse_3);
		arende.setHandelseLista(arrayOfHandelse);
		getArendeResponse.setGetArendeResult(arende);
		when(arendeExportClientMock.getArende(any())).thenReturn(getArendeResponse);

		// Let's go
		final var getStatusResult = byggrService.getByggrStatus(caseId, externalCaseID);

		assertCaseStatus(caseId, externalCaseID, CaseType.valueOf(caseMappingList.getFirst().getCaseType()), caseMappingList.getFirst().getServiceName(), handelse_2.getHandelseslag(), handelse_2.getStartDatum(), getStatusResult);

		final ArgumentCaptor<GetArende> getArendeRequestCaptor = ArgumentCaptor.forClass(GetArende.class);
		verify(arendeExportClientMock, times(1)).getArende(getArendeRequestCaptor.capture());
		assertThat(getArendeRequestCaptor.getValue().getDnr()).isEqualTo(caseId);
	}

	// Test getByggRStatusByOrgNr
	@Test
	void testGetByggRStatusByOrgNr() {

		final String caseId_1 = MessageFormat.format("BYGG-2021-{0}", new Random().nextInt(100000));
		final String caseId_2 = MessageFormat.format("BYGG-2022-{0}", new Random().nextInt(100000));
		final String externalCaseID_1 = UUID.randomUUID().toString();
		final String externalCaseID_2 = UUID.randomUUID().toString();

		// Mock caseMappingServiceMock
		final List<CaseMapping> caseMappingList_1 = new ArrayList<>();
		caseMappingList_1.add(CaseMapping.builder()
			.withExternalCaseId(externalCaseID_1)
			.withCaseId(caseId_1)
			.withSystem(SystemType.BYGGR)
			.withCaseType(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV.toString())
			.withServiceName("Test service")
			.build());

		when(caseMappingServiceMock.getCaseMapping(null, caseId_1)).thenReturn(caseMappingList_1);
		when(caseMappingServiceMock.getCaseMapping(externalCaseID_1, caseId_1)).thenReturn(caseMappingList_1);

		final List<CaseMapping> caseMappingList_2 = new ArrayList<>();


		caseMappingList_2.add(CaseMapping.builder()
			.withExternalCaseId(externalCaseID_2)
			.withCaseId(caseId_2)
			.withSystem(SystemType.BYGGR)
			.withCaseType(CaseType.ANMALAN_ATTEFALL.toString())
			.withServiceName("Test service 2")
			.build());

		when(caseMappingServiceMock.getCaseMapping(null, caseId_2)).thenReturn(caseMappingList_2);
		when(caseMappingServiceMock.getCaseMapping(externalCaseID_2, caseId_2)).thenReturn(caseMappingList_2);

		// Mock ArendeExportClientMock
		final GetRelateradeArendenByPersOrgNrAndRoleResponse getRelateradeArendenByPersOrgNrAndRoleResponse = new GetRelateradeArendenByPersOrgNrAndRoleResponse();
		final ArrayOfArende1 arrayOfArende = new ArrayOfArende1();
		final Arende arende_1 = new Arende();
		arende_1.setDnr(caseId_1);
		arende_1.setStatus("Pågående");
		final ArrayOfHandelse arrayOfHandelse_1 = new ArrayOfHandelse();
		final Handelse handelse_1 = new Handelse();
		handelse_1.setStartDatum(LocalDateTime.now().minusDays(5));
		handelse_1.setHandelsetyp(BYGGR_HANDELSETYP_HANDLING);
		handelse_1.setHandelseslag(BYGGR_HANDELSESLAG_KOMPLETTERANDE_HANDLINGAR);
		final Handelse handelse_2 = new Handelse();
		handelse_2.setStartDatum(LocalDateTime.now().minusDays(2));
		handelse_2.setHandelsetyp(BYGGR_HANDELSETYP_BESLUT);
		handelse_2.setHandelseslag(BYGGR_HANDELSESLAG_SLUTBESKED);
		arrayOfHandelse_1.getHandelse().add(handelse_1);
		arrayOfHandelse_1.getHandelse().add(handelse_2);
		arende_1.setHandelseLista(arrayOfHandelse_1);
		arrayOfArende.getArende().add(arende_1);

		final Arende arende_2 = new Arende();
		arende_2.setDnr(caseId_2);
		arende_2.setStatus("Pågående");
		final ArrayOfHandelse arrayOfHandelse_2 = new ArrayOfHandelse();
		final Handelse handelse_2_1 = new Handelse();
		handelse_2_1.setStartDatum(LocalDateTime.now().minusDays(5));
		handelse_2_1.setHandelsetyp(BYGGR_HANDELSETYP_HANDLING);
		handelse_2_1.setHandelseslag(BYGGR_HANDELSESLAG_KOMPLETTERANDE_HANDLINGAR);
		final Handelse handelse_2_2 = new Handelse();
		handelse_2_2.setStartDatum(LocalDateTime.now().minusDays(10));
		handelse_2_2.setHandelsetyp(BYGGR_HANDELSETYP_BESLUT);
		handelse_2_2.setHandelseslag(BYGGR_HANDELSESLAG_SLUTBESKED);
		arrayOfHandelse_2.getHandelse().add(handelse_2_1);
		arrayOfHandelse_2.getHandelse().add(handelse_2_2);
		arende_2.setHandelseLista(arrayOfHandelse_2);
		arrayOfArende.getArende().add(arende_2);
		getRelateradeArendenByPersOrgNrAndRoleResponse.setGetRelateradeArendenByPersOrgNrAndRoleResult(arrayOfArende);
		when(arendeExportClientMock.getRelateradeArendenByPersOrgNrAndRole(any())).thenReturn(getRelateradeArendenByPersOrgNrAndRoleResponse);

		final String orgnr = TestUtil.generateRandomOrganizationNumber();
		final var getStatusResult = byggrService.getByggrStatusByOrgNr(orgnr);

		assertThat(getStatusResult).hasSize(2);
		assertCaseStatus(caseId_1, externalCaseID_1, CaseType.valueOf(caseMappingList_1.getFirst().getCaseType()), caseMappingList_1.getFirst().getServiceName(), handelse_2.getHandelseslag(), handelse_2.getStartDatum(), getStatusResult.getFirst());
		assertCaseStatus(caseId_2, externalCaseID_2, CaseType.valueOf(caseMappingList_2.getFirst().getCaseType()), caseMappingList_2.getFirst().getServiceName(), handelse_2_1.getHandelseslag(), handelse_2_1.getStartDatum(), getStatusResult.get(1));

		final ArgumentCaptor<GetRelateradeArendenByPersOrgNrAndRole> getRelateradeArendenByPersOrgNrAndRoleRequestCaptor = ArgumentCaptor.forClass(GetRelateradeArendenByPersOrgNrAndRole.class);
		verify(arendeExportClientMock, times(1)).getRelateradeArendenByPersOrgNrAndRole(getRelateradeArendenByPersOrgNrAndRoleRequestCaptor.capture());
		assertThat(getRelateradeArendenByPersOrgNrAndRoleRequestCaptor.getValue().getPersOrgNr()).isEqualTo(orgnr);
		assertThat(getRelateradeArendenByPersOrgNrAndRoleRequestCaptor.getValue().getArendeIntressentRoller().getString()).contains(StakeholderRole.APPLICANT.getText());
		assertThat(getRelateradeArendenByPersOrgNrAndRoleRequestCaptor.getValue().getHandelseIntressentRoller().getString()).contains(StakeholderRole.APPLICANT.getText());
	}

}
