package se.sundsvall.casemanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.ANDRING_ANSOKAN_OM_BYGGLOV;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.STRANDSKYDD_ANDRAD_ANVANDNING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.STRANDSKYDD_ANLAGGANDE;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.STRANDSKYDD_ANORDNANDE;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.STRANDSKYDD_NYBYGGNAD;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.TILLBYGGNAD_ANSOKAN_OM_BYGGLOV;
import static se.sundsvall.casemanagement.service.util.Constants.BYGGR_HANDELSESLAG_KOMPLETTERANDE_HANDLINGAR;
import static se.sundsvall.casemanagement.service.util.Constants.BYGGR_HANDELSESLAG_SLUTBESKED;
import static se.sundsvall.casemanagement.service.util.Constants.BYGGR_HANDELSETYP_BESLUT;
import static se.sundsvall.casemanagement.service.util.Constants.BYGGR_HANDELSETYP_HANDLING;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.integration.soap.arendeexport.ArendeExportClient;
import se.sundsvall.casemanagement.service.exceptions.ApplicationException;
import se.sundsvall.casemanagement.service.util.Constants;
import se.sundsvall.casemanagement.testutils.TestConstants;

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
    
    @InjectMocks
    private ByggrService byggrService;
    
    @Mock
    private FbService fbServiceMock;
    @Mock
    private CitizenMappingService citizenMappingServiceMock;
    @Mock
    private CaseMappingService caseMappingServiceMock;
    @Mock
    private ArendeExportClient arendeExportClientMock;
    
    private static void assertCaseStatus(String caseId, String externalCaseID, CaseType caseType, String serviceName, String status, LocalDateTime dateTime, CaseStatusDTO getStatusResult) {
        assertEquals(caseId, getStatusResult.getCaseId());
        assertEquals(externalCaseID, getStatusResult.getExternalCaseId());
        assertEquals(SystemType.BYGGR, getStatusResult.getSystem());
        assertEquals(caseType, getStatusResult.getCaseType());
        assertEquals(serviceName, getStatusResult.getServiceName());
        assertEquals(status, getStatusResult.getStatus());
        assertEquals(dateTime, getStatusResult.getTimestamp());
    }
    
    private static void assertHandelse(String dnr, SaveNewHandelseMessage saveNewHandelseMessage, List<String> notesToContain, String handelseRubrik, String handelsetyp, String handelseslag, List<AttachmentDTO> attachments) {
        assertEquals(dnr, saveNewHandelseMessage.getDnr());
        assertEquals(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN, saveNewHandelseMessage.getHandlaggarSign());
        assertEquals(handelseRubrik, saveNewHandelseMessage.getHandelse().getRubrik());
        assertEquals(Constants.BYGGR_HANDELSE_RIKTNING_IN, saveNewHandelseMessage.getHandelse().getRiktning());
        assertEquals(handelsetyp, saveNewHandelseMessage.getHandelse().getHandelsetyp());
        assertEquals(handelseslag, saveNewHandelseMessage.getHandelse().getHandelseslag());
        assertNotNull(saveNewHandelseMessage.getHandelse().getStartDatum());
        notesToContain.forEach(text -> assertTrue(saveNewHandelseMessage.getHandelse().getAnteckning().contains(text)));
        
        if (attachments != null) {
            assertHandlingar(attachments, saveNewHandelseMessage.getHandlingar().getHandling());
        }
        
    }
    
    private static void assertHandlingar(List<AttachmentDTO> attachments, List<HandelseHandling> handlingList) {
        assertEquals(attachments.size(), handlingList.size());
        attachments.forEach(attachmentDTO -> {
            assertEquals(1, handlingList.stream().filter(handelseHandling -> handelseHandling.getTyp().equals(attachmentDTO.getCategory().name())).count());
            assertEquals(1, handlingList.stream().filter(handelseHandling -> handelseHandling.getAnteckning().equals(attachmentDTO.getName())).count());
            assertEquals(1, handlingList.stream().filter(handelseHandling -> handelseHandling.getDokument().getNamn().equals(attachmentDTO.getName())).count());
            assertEquals(1, handlingList.stream().filter(handelseHandling -> handelseHandling.getDokument().getBeskrivning().equals(attachmentDTO.getNote())).count());
            assertEquals(1, handlingList.stream().filter(handelseHandling -> handelseHandling.getDokument().getFil().getFilAndelse().equals(attachmentDTO.getExtension().toLowerCase())).count());
        });
        
        handlingList.forEach(handling -> {
            assertNotNull(handling.getDokument().getFil().getFilBuffer());
            assertEquals(Constants.BYGGR_HANDLING_STATUS_INKOMMEN, handling.getStatus());
        });
    }
    
    private static void assertOrganizationDTO(OrganizationDTO organizationDTO, ArendeIntressent arendeIntressent) {
        assertTrue(arendeIntressent.isArForetag());
        assertEquals(organizationDTO.getOrganizationName(), arendeIntressent.getNamn());
        assertEquals(organizationDTO.getOrganizationNumber(), arendeIntressent.getPersOrgNr());
        assertTrue(arendeIntressent.getRollLista().getRoll().containsAll(organizationDTO.getRoles().stream().map(StakeholderRole::getText).toList()));
        assertCommunication(organizationDTO, arendeIntressent);
        assertAddress(organizationDTO, arendeIntressent);
    }
    
    private static void assertPersonDTO(PersonDTO personDTO, ArendeIntressent arendeIntressent) {
        assertFalse(arendeIntressent.isArForetag());
        assertEquals(personDTO.getFirstName(), arendeIntressent.getFornamn());
        assertEquals(personDTO.getLastName(), arendeIntressent.getEfternamn());
        assertEquals(personDTO.getPersonalNumber(), arendeIntressent.getPersOrgNr());
        assertTrue(arendeIntressent.getRollLista().getRoll().containsAll(personDTO.getRoles().stream().map(StakeholderRole::getText).toList()));
        assertCommunication(personDTO, arendeIntressent);
        assertAddress(personDTO, arendeIntressent);
    }
    
    private static void assertCommunication(StakeholderDTO stakeholderDTO, ArendeIntressent arendeIntressent) {
        assertTrue(arendeIntressent.getIntressentKommunikationLista().getIntressentKommunikation().stream().anyMatch(kom -> kom.getBeskrivning().equals(stakeholderDTO.getEmailAddress())));
        assertTrue(arendeIntressent.getIntressentKommunikationLista().getIntressentKommunikation().stream().anyMatch(kom -> kom.getBeskrivning().equals(stakeholderDTO.getCellphoneNumber())));
        assertTrue(arendeIntressent.getIntressentKommunikationLista().getIntressentKommunikation().stream().anyMatch(kom -> kom.getBeskrivning().equals(stakeholderDTO.getPhoneNumber())));
    }
    
    private static void assertAddress(StakeholderDTO stakeholderDTO, ArendeIntressent arendeIntressent) {
        var postalAddress = stakeholderDTO.getAddresses().stream().filter(addressDTO -> addressDTO.getAddressCategories().contains(AddressCategory.POSTAL_ADDRESS)).findFirst().orElseThrow();
        
        assertEquals(postalAddress.getStreet() + " " + postalAddress.getHouseNumber(), arendeIntressent.getAdress());
        assertEquals(postalAddress.getPostalCode(), arendeIntressent.getPostNr());
        assertEquals(postalAddress.getCity(), arendeIntressent.getOrt());
        assertEquals(postalAddress.getCountry(), arendeIntressent.getLand());
        
        var invoiceAddress = stakeholderDTO.getAddresses().stream().filter(addressDTO -> addressDTO.getAddressCategories().contains(AddressCategory.INVOICE_ADDRESS)).findFirst();
        if (invoiceAddress.isPresent()) {
            assertEquals(invoiceAddress.get().getStreet() + " " + invoiceAddress.get().getHouseNumber(), arendeIntressent.getFakturaAdress().getAdress());
            assertEquals(invoiceAddress.get().getPostalCode(), arendeIntressent.getFakturaAdress().getPostNr());
            assertEquals(invoiceAddress.get().getCity(), arendeIntressent.getFakturaAdress().getOrt());
            assertEquals(invoiceAddress.get().getCountry(), arendeIntressent.getFakturaAdress().getLand());
            assertEquals(invoiceAddress.get().getAttention(), arendeIntressent.getFakturaAdress().getAttention());
        }
    }
    
    @BeforeEach
    public void setup() {
        TestUtil.standardMockFb(fbServiceMock);
        TestUtil.standardMockArendeExport(arendeExportClientMock);
        TestUtil.standardMockCitizenMapping(citizenMappingServiceMock);
    }
    
    //ANSOKAN_OM_BYGGLOV
    @ParameterizedTest
    @EnumSource(value = CaseType.class, names = {"STRANDSKYDD_NYBYGGNAD",
        "STRANDSKYDD_ANLAGGANDE", "STRANDSKYDD_ANORDNANDE",
        "STRANDSKYDD_ANDRAD_ANVANDNING"})
    void testStrandskyddCaseType(CaseType caseType) throws ApplicationException {
        var caseTypes = Map.of(
            STRANDSKYDD_NYBYGGNAD, Constants.BYGGR_ARENDEMENING_STRANDSKYDD_FOR_NYBYGGNAD,
            STRANDSKYDD_ANLAGGANDE, Constants.BYGGR_ARENDEMENING_STRANDSKYDD_FOR_ANLAGGANDE,
            STRANDSKYDD_ANORDNANDE, Constants.BYGGR_ARENDEMENING_STRANDSKYDD_FOR_ANORDNANDE,
            STRANDSKYDD_ANDRAD_ANVANDNING, Constants.BYGGR_ARENDEMENING_STRANDSKYDD_FOR_ANDRAD_ANVANDNING
        );
        
        
        var input = TestUtil.createPlanningPermissionCaseDTO(caseType, AttachmentCategory.ANS);
        var inputFacility = input.getFacilities().get(0);
        var inputAttachment = input.getAttachments().get(0);
        var response = byggrService.postCase(input);
        
        assertEquals(TestConstants.BYGG_CASE_ID, response.getDnr());
        
        var saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
        verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
        
        var saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
        var arende = saveNewArendeMessage.getArende();
        var handelse = saveNewArendeMessage.getHandelse();
        var handlingar = saveNewArendeMessage.getHandlingar();
        
        // SaveNewArendeMessage
        assertEquals(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN, saveNewArendeMessage.getHandlaggarSign());
        
        // Arende
        assertEquals(Constants.BYGGR_ARENDETYP_STRANDSKYDD, arende.getArendetyp());
        assertEquals(caseType.getArendeslag(), arende.getArendeslag());
        assertEquals(inputFacility.getFacilityType().getValue(), arende.getArendeklass());
        assertEquals(Constants.BYGGR_ARENDEGRUPP_STRANDSKYDD, arende.getArendegrupp());
        assertEquals(Constants.BYGGR_NAMNDKOD_STADSBYGGNADSNAMNDEN, arende.getNamndkod());
        assertEquals(Constants.BYGGR_ENHETKOD_STADSBYGGNADSKONTORET, arende.getEnhetkod());
        assertEquals(Constants.BYGGR_KOMMUNKOD_SUNDSVALL_KOMMUN, arende.getKommun());
        assertEquals(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN, arende.getHandlaggare().getSignatur());
        assertEquals(inputFacility.getAddress().getIsZoningPlanArea(), arende.isArInomplan());
        
        assertEquals("%s %s samt %s".formatted(caseTypes.get(caseType), inputFacility.getFacilityType().getDescription(), input.getCaseTitleAddition().trim().toLowerCase()), arende.getBeskrivning());
        assertEquals(input.getStakeholders().get(0).getAddresses().get(0).getInvoiceMarking(), arende.getProjektnr());
        assertNotNull(arende.getAnkomstDatum());
        
        // Intressenter
        assertEquals(3, arende.getIntressentLista().getIntressent().size());
        
        // ArendeObjekt
        assertEquals(1, arende.getObjektLista().getAbstractArendeObjekt().size());
        var arendeFastighet = (ArendeFastighet) arende.getObjektLista().getAbstractArendeObjekt().get(0);
        assertEquals(inputFacility.isMainFacility(), arendeFastighet.isArHuvudObjekt());
        assertEquals(TestConstants.FNR, arendeFastighet.getFastighet().getFnr());
        
        // Handlingar
        assertEquals(1, handlingar.getHandling().size());
        var handling = handlingar.getHandling().get(0);
        assertEquals(handling.getAnteckning(), inputAttachment.getName());
        assertNotNull(handling.getDokument().getFil().getFilBuffer());
        assertEquals(inputAttachment.getExtension().toLowerCase(), handling.getDokument().getFil().getFilAndelse());
        assertEquals(inputAttachment.getName(), handling.getDokument().getNamn());
        assertEquals(inputAttachment.getNote(), handling.getDokument().getBeskrivning());
        assertEquals(Constants.BYGGR_HANDLING_STATUS_INKOMMEN, handling.getStatus());
        assertEquals(inputAttachment.getCategory().name(), handling.getTyp());
        
        // Handelser
        assertNotNull(handelse.getStartDatum());
        assertEquals(Constants.BYGGR_HANDELSE_RIKTNING_IN, handelse.getRiktning());
        assertEquals(Constants.BYGGR_HANDELSE_RUBRIK_STRANDSKYDD, handelse.getRubrik());
        assertEquals(Constants.BYGGR_HANDELSETYP_ANSOKAN, handelse.getHandelsetyp());
        assertEquals(Constants.BYGGR_HANDELSESLAG_STRANDSKYDD, handelse.getHandelseslag());
    }
    
    //ANSOKAN_OM_BYGGLOV
    @ParameterizedTest
    @EnumSource(value = CaseType.class, names = {"NYBYGGNAD_ANSOKAN_OM_BYGGLOV",
        "ANDRING_ANSOKAN_OM_BYGGLOV", "TILLBYGGNAD_ANSOKAN_OM_BYGGLOV"})
    void testPostNybyggnad(CaseType caseType) throws ApplicationException {
        var caseTypes = Map.of(
            NYBYGGNAD_ANSOKAN_OM_BYGGLOV, Constants.BYGGR_ARENDEMENING_BYGGLOV_FOR_NYBYGGNAD_AV,
            TILLBYGGNAD_ANSOKAN_OM_BYGGLOV, Constants.BYGGR_ARENDEMENING_BYGGLOV_FOR_TILLBYGGNAD,
            ANDRING_ANSOKAN_OM_BYGGLOV, Constants.BYGGR_ARENDEMENING_BYGGLOV_ANDRING_ANSOKAN_OM_
        );
        
        
        var input = TestUtil.createPlanningPermissionCaseDTO(caseType, AttachmentCategory.ANS);
        var inputFacility = input.getFacilities().get(0);
        var inputAttachment = input.getAttachments().get(0);
        var response = byggrService.postCase(input);
        
        assertEquals(TestConstants.BYGG_CASE_ID, response.getDnr());
        
        var saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
        verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
        
        var saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
        var arende = saveNewArendeMessage.getArende();
        var handelse = saveNewArendeMessage.getHandelse();
        var handlingar = saveNewArendeMessage.getHandlingar();
        
        // SaveNewArendeMessage
        assertEquals(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN, saveNewArendeMessage.getHandlaggarSign());
        
        // Arende
        assertEquals(Constants.BYGGR_ARENDETYP_BYGGLOV_FOR, arende.getArendetyp());
        if (!caseType.equals(ANDRING_ANSOKAN_OM_BYGGLOV)) {
            assertEquals(caseType.getArendeslag(), arende.getArendeslag());
            assertEquals(inputFacility.getFacilityType().getValue(), arende.getArendeklass());
        } else {
            assertEquals(inputFacility.getFacilityType().getValue(), arende.getArendeslag());
        }
        assertEquals(Constants.BYGGR_ARENDEGRUPP_LOV_ANMALNINGSARENDE, arende.getArendegrupp());
        assertEquals(Constants.BYGGR_NAMNDKOD_STADSBYGGNADSNAMNDEN, arende.getNamndkod());
        assertEquals(Constants.BYGGR_ENHETKOD_STADSBYGGNADSKONTORET, arende.getEnhetkod());
        assertEquals(Constants.BYGGR_KOMMUNKOD_SUNDSVALL_KOMMUN, arende.getKommun());
        assertEquals(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN, arende.getHandlaggare().getSignatur());
        assertEquals(inputFacility.getAddress().getIsZoningPlanArea(), arende.isArInomplan());
        
        assertEquals("%s %s samt %s".formatted(caseTypes.get(caseType), inputFacility.getFacilityType().getDescription(), input.getCaseTitleAddition().trim().toLowerCase()), arende.getBeskrivning());
        assertEquals(input.getStakeholders().get(0).getAddresses().get(0).getInvoiceMarking(), arende.getProjektnr());
        assertNotNull(arende.getAnkomstDatum());
        
        // Intressenter
        assertEquals(3, arende.getIntressentLista().getIntressent().size());
        
        // ArendeObjekt
        assertEquals(1, arende.getObjektLista().getAbstractArendeObjekt().size());
        var arendeFastighet = (ArendeFastighet) arende.getObjektLista().getAbstractArendeObjekt().get(0);
        assertEquals(inputFacility.isMainFacility(), arendeFastighet.isArHuvudObjekt());
        assertEquals(TestConstants.FNR, arendeFastighet.getFastighet().getFnr());
        
        // Handlingar
        assertEquals(1, handlingar.getHandling().size());
        var handling = handlingar.getHandling().get(0);
        assertEquals(handling.getAnteckning(), inputAttachment.getName());
        assertNotNull(handling.getDokument().getFil().getFilBuffer());
        assertEquals(inputAttachment.getExtension().toLowerCase(), handling.getDokument().getFil().getFilAndelse());
        assertEquals(inputAttachment.getName(), handling.getDokument().getNamn());
        assertEquals(inputAttachment.getNote(), handling.getDokument().getBeskrivning());
        assertEquals(Constants.BYGGR_HANDLING_STATUS_INKOMMEN, handling.getStatus());
        assertEquals(inputAttachment.getCategory().name(), handling.getTyp());
        
        // Handelser
        assertNotNull(handelse.getStartDatum());
        assertEquals(Constants.BYGGR_HANDELSE_RIKTNING_IN, handelse.getRiktning());
        assertEquals(Constants.BYGGR_HANDELSE_RUBRIK_BYGGLOV, handelse.getRubrik());
        assertEquals(Constants.BYGGR_HANDELSETYP_ANSOKAN, handelse.getHandelsetyp());
        assertEquals(Constants.BYGGR_HANDELSESLAG_BYGGLOV, handelse.getHandelseslag());
    }
    
    // ANMALAN_ATTEFALL
    @Test
    void testPostAttefall() throws ApplicationException {
        
        PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.ANMALAN_ATTEFALL, AttachmentCategory.ANS);
        // Set facilityType to a compatible value
        input.getFacilities().get(0).setFacilityType(FacilityType.EXTENSION);
        // Set addressCategory to not be INVOICE_ADDRESS, so we can test projektnr to be propertyDesignation
        input.getStakeholders().get(0).getAddresses().get(0).setAddressCategories(List.of(AddressCategory.POSTAL_ADDRESS));
        PlanningPermissionFacilityDTO inputFacility = input.getFacilities().get(0);
        AttachmentDTO inputAttachment = input.getAttachments().get(0);
        SaveNewArendeResponse2 response = byggrService.postCase(input);
        
        assertEquals(TestConstants.BYGG_CASE_ID, response.getDnr());
        
        ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
        verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
        SaveNewArendeMessage saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
        Arende2 arende = saveNewArendeMessage.getArende();
        Handelse handelse = saveNewArendeMessage.getHandelse();
        ArrayOfHandling handlingar = saveNewArendeMessage.getHandlingar();
        
        // SaveNewArendeMessage
        assertEquals(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN, saveNewArendeMessage.getHandlaggarSign());
        
        // Arende
        assertEquals(Constants.BYGGR_ARENDETYP_ANMALAN_ATTEFALL, arende.getArendetyp());
        assertEquals(inputFacility.getFacilityType().getValue(), arende.getArendeslag());
        assertNull(arende.getArendeklass());
        assertEquals(Constants.BYGGR_ARENDEGRUPP_LOV_ANMALNINGSARENDE, arende.getArendegrupp());
        assertEquals(Constants.BYGGR_NAMNDKOD_STADSBYGGNADSNAMNDEN, arende.getNamndkod());
        assertEquals(Constants.BYGGR_ENHETKOD_STADSBYGGNADSKONTORET, arende.getEnhetkod());
        assertEquals(Constants.BYGGR_KOMMUNKOD_SUNDSVALL_KOMMUN, arende.getKommun());
        assertEquals(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN, arende.getHandlaggare().getSignatur());
        assertEquals(inputFacility.getAddress().getIsZoningPlanArea(), arende.isArInomplan());
        assertNull(arende.getBeskrivning());
        // Remove SUNDSVALL from propertyDesignation
        String propertyDesignation = inputFacility.getAddress().getPropertyDesignation().substring(inputFacility.getAddress().getPropertyDesignation().indexOf(" ") + 1);
        assertEquals(propertyDesignation, arende.getProjektnr());
        assertNotNull(arende.getAnkomstDatum());
        
        // Intressenter
        assertEquals(3, arende.getIntressentLista().getIntressent().size());
        
        // ArendeObjekt
        assertEquals(1, arende.getObjektLista().getAbstractArendeObjekt().size());
        ArendeFastighet arendeFastighet = (ArendeFastighet) arende.getObjektLista().getAbstractArendeObjekt().get(0);
        assertEquals(inputFacility.isMainFacility(), arendeFastighet.isArHuvudObjekt());
        assertEquals(TestConstants.FNR, arendeFastighet.getFastighet().getFnr());
        
        // Handlingar
        assertEquals(1, handlingar.getHandling().size());
        HandelseHandling handling = handlingar.getHandling().get(0);
        assertEquals(handling.getAnteckning(), inputAttachment.getName());
        assertNotNull(handling.getDokument().getFil().getFilBuffer());
        assertEquals(inputAttachment.getExtension().toLowerCase(), handling.getDokument().getFil().getFilAndelse());
        assertEquals(inputAttachment.getName(), handling.getDokument().getNamn());
        assertEquals(inputAttachment.getNote(), handling.getDokument().getBeskrivning());
        assertEquals(Constants.BYGGR_HANDLING_STATUS_INKOMMEN, handling.getStatus());
        assertEquals(inputAttachment.getCategory().name(), handling.getTyp());
        
        // Handelser
        assertNotNull(handelse.getStartDatum());
        assertEquals(Constants.BYGGR_HANDELSE_RIKTNING_IN, handelse.getRiktning());
        assertEquals(Constants.BYGGR_HANDELSE_RUBRIK_ANMALAN_ATTEFALL, handelse.getRubrik());
        assertEquals(Constants.BYGGR_HANDELSETYP_ANMALAN, handelse.getHandelsetyp());
        assertEquals(Constants.BYGGR_HANDELSESLAG_ANMALAN_ATTEFALL, handelse.getHandelseslag());
    }
    
    // ANMALAN_ELDSTAD
    @Test
    void testPostEldstad() throws ApplicationException {
        
        PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.ANMALAN_ELDSTAD,
            AttachmentCategory.ANS);
        // Set facilityType to a compatible value
        input.getFacilities().get(0).setFacilityType(FacilityType.FIREPLACE);
        // Set addressCategory to not be INVOICE_ADDRESS, so we can test projektnr to be propertyDesignation
        input.getStakeholders().get(0).getAddresses().get(0).setAddressCategories(List.of(AddressCategory.POSTAL_ADDRESS));
        PlanningPermissionFacilityDTO inputFacility = input.getFacilities().get(0);
        AttachmentDTO inputAttachment = input.getAttachments().get(0);
        SaveNewArendeResponse2 response = byggrService.postCase(input);
        
        assertEquals(TestConstants.BYGG_CASE_ID, response.getDnr());
        
        ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
        verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
        SaveNewArendeMessage saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
        Arende2 arende = saveNewArendeMessage.getArende();
        Handelse handelse = saveNewArendeMessage.getHandelse();
        ArrayOfHandling handlingar = saveNewArendeMessage.getHandlingar();
        
        // SaveNewArendeMessage
        assertEquals(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN, saveNewArendeMessage.getHandlaggarSign());
        
        // Arende
        assertEquals(Constants.BYGGR_HANDELSETYP_ANMALAN, arende.getArendetyp());
        assertEquals(inputFacility.getFacilityType().getValue(), arende.getArendeslag());
        assertNull(arende.getArendeklass());
        assertEquals(Constants.BYGGR_ARENDEGRUPP_LOV_ANMALNINGSARENDE, arende.getArendegrupp());
        assertEquals(Constants.BYGGR_NAMNDKOD_STADSBYGGNADSNAMNDEN, arende.getNamndkod());
        assertEquals(Constants.BYGGR_ENHETKOD_STADSBYGGNADSKONTORET, arende.getEnhetkod());
        assertEquals(Constants.BYGGR_KOMMUNKOD_SUNDSVALL_KOMMUN, arende.getKommun());
        assertEquals(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN, arende.getHandlaggare().getSignatur());
        assertEquals(inputFacility.getAddress().getIsZoningPlanArea(), arende.isArInomplan());
        assertNull(arende.getBeskrivning());
        // Remove SUNDSVALL from propertyDesignation
        String propertyDesignation = inputFacility.getAddress().getPropertyDesignation().substring(inputFacility.getAddress().getPropertyDesignation().indexOf(" ") + 1);
        assertEquals(propertyDesignation, arende.getProjektnr());
        assertNotNull(arende.getAnkomstDatum());
        
        // Intressenter
        assertEquals(3, arende.getIntressentLista().getIntressent().size());
        
        // ArendeObjekt
        assertEquals(1, arende.getObjektLista().getAbstractArendeObjekt().size());
        ArendeFastighet arendeFastighet = (ArendeFastighet) arende.getObjektLista().getAbstractArendeObjekt().get(0);
        assertEquals(inputFacility.isMainFacility(), arendeFastighet.isArHuvudObjekt());
        assertEquals(TestConstants.FNR, arendeFastighet.getFastighet().getFnr());
        
        // Handlingar
        assertEquals(1, handlingar.getHandling().size());
        HandelseHandling handling = handlingar.getHandling().get(0);
        assertEquals(handling.getAnteckning(), inputAttachment.getName());
        assertNotNull(handling.getDokument().getFil().getFilBuffer());
        assertEquals(inputAttachment.getExtension().toLowerCase(), handling.getDokument().getFil().getFilAndelse());
        assertEquals(inputAttachment.getName(), handling.getDokument().getNamn());
        assertEquals(inputAttachment.getNote(), handling.getDokument().getBeskrivning());
        assertEquals(Constants.BYGGR_HANDLING_STATUS_INKOMMEN, handling.getStatus());
        assertEquals(inputAttachment.getCategory().name(), handling.getTyp());
        
        // Handelser
        assertNotNull(handelse.getStartDatum());
        assertEquals(Constants.BYGGR_HANDELSE_RIKTNING_IN, handelse.getRiktning());
        assertEquals(Constants.BYGGR_HANDELSE_RUBRIK_ELDSTAD, handelse.getRubrik());
        assertEquals(Constants.BYGGR_HANDELSETYP_ANMALAN, handelse.getHandelsetyp());
        assertEquals(Constants.BYGGR_HANDELSESLAG_ELDSTAD, handelse.getHandelseslag());
    }
    
    // ANMALAN_ELDSTAD_SMOKE
    @Test
    void testPostEldstadRokkanal() throws ApplicationException {
        
        PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.ANMALAN_ELDSTAD,
            AttachmentCategory.ANS);
        // Set facilityType to a compatible value
        input.getFacilities().get(0).setFacilityType(FacilityType.FIREPLACE_SMOKECHANNEL);
        // Set addressCategory to not be INVOICE_ADDRESS, so we can test projektnr to be propertyDesignation
        input.getStakeholders().get(0).getAddresses().get(0).setAddressCategories(List.of(AddressCategory.POSTAL_ADDRESS));
        PlanningPermissionFacilityDTO inputFacility = input.getFacilities().get(0);
        AttachmentDTO inputAttachment = input.getAttachments().get(0);
        SaveNewArendeResponse2 response = byggrService.postCase(input);
        
        assertEquals(TestConstants.BYGG_CASE_ID, response.getDnr());
        
        ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
        verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
        SaveNewArendeMessage saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
        Arende2 arende = saveNewArendeMessage.getArende();
        Handelse handelse = saveNewArendeMessage.getHandelse();
        ArrayOfHandling handlingar = saveNewArendeMessage.getHandlingar();
        
        // SaveNewArendeMessage
        assertEquals(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN, saveNewArendeMessage.getHandlaggarSign());
        
        // Arende
        assertEquals(Constants.BYGGR_HANDELSETYP_ANMALAN, arende.getArendetyp());
        assertEquals(inputFacility.getFacilityType().getValue(), arende.getArendeslag());
        assertNull(arende.getArendeklass());
        assertEquals(Constants.BYGGR_ARENDEGRUPP_LOV_ANMALNINGSARENDE, arende.getArendegrupp());
        assertEquals(Constants.BYGGR_NAMNDKOD_STADSBYGGNADSNAMNDEN, arende.getNamndkod());
        assertEquals(Constants.BYGGR_ENHETKOD_STADSBYGGNADSKONTORET, arende.getEnhetkod());
        assertEquals(Constants.BYGGR_KOMMUNKOD_SUNDSVALL_KOMMUN, arende.getKommun());
        assertEquals(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN, arende.getHandlaggare().getSignatur());
        assertEquals(inputFacility.getAddress().getIsZoningPlanArea(), arende.isArInomplan());
        assertNull(arende.getBeskrivning());
        // Remove SUNDSVALL from propertyDesignation
        String propertyDesignation = inputFacility.getAddress().getPropertyDesignation().substring(inputFacility.getAddress().getPropertyDesignation().indexOf(" ") + 1);
        assertEquals(propertyDesignation, arende.getProjektnr());
        assertNotNull(arende.getAnkomstDatum());
        
        // Intressenter
        assertEquals(3, arende.getIntressentLista().getIntressent().size());
        
        // ArendeObjekt
        assertEquals(1, arende.getObjektLista().getAbstractArendeObjekt().size());
        ArendeFastighet arendeFastighet = (ArendeFastighet) arende.getObjektLista().getAbstractArendeObjekt().get(0);
        assertEquals(inputFacility.isMainFacility(), arendeFastighet.isArHuvudObjekt());
        assertEquals(TestConstants.FNR, arendeFastighet.getFastighet().getFnr());
        
        // Handlingar
        assertEquals(1, handlingar.getHandling().size());
        HandelseHandling handling = handlingar.getHandling().get(0);
        assertEquals(handling.getAnteckning(), inputAttachment.getName());
        assertNotNull(handling.getDokument().getFil().getFilBuffer());
        assertEquals(inputAttachment.getExtension().toLowerCase(), handling.getDokument().getFil().getFilAndelse());
        assertEquals(inputAttachment.getName(), handling.getDokument().getNamn());
        assertEquals(inputAttachment.getNote(), handling.getDokument().getBeskrivning());
        assertEquals(Constants.BYGGR_HANDLING_STATUS_INKOMMEN, handling.getStatus());
        assertEquals(inputAttachment.getCategory().name(), handling.getTyp());
        
        // Handelser
        assertNotNull(handelse.getStartDatum());
        assertEquals(Constants.BYGGR_HANDELSE_RIKTNING_IN, handelse.getRiktning());
        assertEquals(Constants.BYGGR_HANDELSE_RUBRIK_ELDSTAD_ROKKANAL, handelse.getRubrik());
        assertEquals(Constants.BYGGR_HANDELSETYP_ANMALAN, handelse.getHandelsetyp());
        assertEquals(Constants.BYGGR_HANDELSESLAG_ELDSTAD_ROKKANAL, handelse.getHandelseslag());
    }
    
    @Test
    void testCallToCaseMapping() throws ApplicationException {
        PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.ANS);
        input.getExtraParameters().put(Constants.SERVICE_NAME, "Test service name");
        PersonDTO applicant = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT));
        input.setStakeholders(List.of(applicant));
        
        var postResult = byggrService.postCase(input);
        
        CaseMapping caseMapping = new CaseMapping(input.getExternalCaseId(),
            postResult.getDnr(),
            SystemType.BYGGR,
            input.getCaseType(),
            input.getExtraParameters().get(Constants.SERVICE_NAME));
        
        verify(caseMappingServiceMock, times(1)).postCaseMapping(caseMapping);
    }
    
    // Test no duplicates of arendefastighet
    @Test
    void testNoDuplicateFacilities() throws ApplicationException {
        PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.ANS);
        
        String propertyDesignation = "Sundsvall test 123:123";
        var facility1 = TestUtil.createPlanningPermissionFacilityDTO(true);
        var facility2 = TestUtil.createPlanningPermissionFacilityDTO(false);
        var facility3 = TestUtil.createPlanningPermissionFacilityDTO(false);
        facility1.getAddress().setPropertyDesignation(propertyDesignation);
        facility2.getAddress().setPropertyDesignation(propertyDesignation);
        facility3.getAddress().setPropertyDesignation(propertyDesignation);
        // Add some facilities
        input.setFacilities(List.of(facility1, facility2, facility3));
        
        byggrService.postCase(input);
        
        ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
        verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
        SaveNewArendeMessage saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
        Arende2 arende = saveNewArendeMessage.getArende();
        
        // ArendeObjekt
        assertEquals(1, arende.getObjektLista().getAbstractArendeObjekt().size());
    }
    
    // Test getMainOrTheOnlyFacility
    @Test
    void testGetMainOrTheOnlyFacility() throws ApplicationException {
        PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.ANS);
        // Set addressCategory to not be INVOICE_ADDRESS, so we can test projektnr to be propertyDesignation
        input.getStakeholders().get(0).getAddresses().get(0).setAddressCategories(List.of(AddressCategory.POSTAL_ADDRESS));
        
        var mainFacility = TestUtil.createPlanningPermissionFacilityDTO(true);
        var randomFacility_1 = TestUtil.createPlanningPermissionFacilityDTO(false);
        randomFacility_1.getAddress().setPropertyDesignation("Sundsvall test 1:1");
        var randomFacility_2 = TestUtil.createPlanningPermissionFacilityDTO(false);
        randomFacility_2.getAddress().setPropertyDesignation("Sundsvall test 2:2");
        // Add some facilities
        input.setFacilities(List.of(randomFacility_1, mainFacility, randomFacility_2));
        
        byggrService.postCase(input);
        
        ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
        verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
        SaveNewArendeMessage saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
        Arende2 arende = saveNewArendeMessage.getArende();
        
        // Arende
        assertEquals(mainFacility.getFacilityType().getValue(), arende.getArendeklass());
        assertEquals(mainFacility.getAddress().getIsZoningPlanArea(), arende.isArInomplan());
        // Remove SUNDSVALL from propertyDesignation
        String propertyDesignation = mainFacility.getAddress().getPropertyDesignation().substring(mainFacility.getAddress().getPropertyDesignation().indexOf(" ") + 1);
        assertEquals(propertyDesignation, arende.getProjektnr());
        
        // ArendeObjekt
        assertEquals(3, arende.getObjektLista().getAbstractArendeObjekt().size());
        Long nrOfMainFacilities = arende.getObjektLista().getAbstractArendeObjekt().stream().map(ArendeFastighet.class::cast).filter(AbstractArendeObjekt::isArHuvudObjekt).count();
        assertEquals(1, nrOfMainFacilities);
    }
    
    @Test
    void testSetPersonInvoiceAddressError() {
        PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.ANS);
        PersonDTO applicant = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT));
        applicant.setAddresses(List.of(TestUtil.createAddressDTO(List.of(AddressCategory.INVOICE_ADDRESS))));
        input.setStakeholders(List.of(applicant));
        
        var problem = assertThrows(ThrowableProblem.class, () -> byggrService.postCase(input));
        assertEquals(Status.BAD_REQUEST, problem.getStatus());
        assertEquals(Constants.ERR_MSG_PERSON_INVOICE_ADDRESS, problem.getDetail());
    }
    
    @Test
    void testSetPersonFields() throws ApplicationException {
        PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.ANS);
        PersonDTO applicant = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT));
        input.setStakeholders(List.of(applicant));
        
        byggrService.postCase(input);
        
        ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
        verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
        SaveNewArendeMessage saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
        Arende2 arende = saveNewArendeMessage.getArende();
        var intressenter = arende.getIntressentLista().getIntressent();
        
        assertEquals(1, intressenter.size());
        var applicants = intressenter.stream().filter(intressent -> intressent.getRollLista().getRoll().contains(StakeholderRole.APPLICANT.getText())).toList();
        assertEquals(1, applicants.size());
        assertPersonDTO(applicant, applicants.get(0));
    }
    
    @Test
    void testSetOrganisationFields() throws ApplicationException {
        PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.ANS);
        OrganizationDTO applicant = (OrganizationDTO) TestUtil.createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT));
        input.setStakeholders(List.of(applicant));
        
        byggrService.postCase(input);
        
        ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
        verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
        SaveNewArendeMessage saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
        Arende2 arende = saveNewArendeMessage.getArende();
        var intressenter = arende.getIntressentLista().getIntressent();
        
        assertEquals(1, intressenter.size());
        var applicants = intressenter.stream().filter(intressent -> intressent.getRollLista().getRoll().contains(StakeholderRole.APPLICANT.getText())).toList();
        assertEquals(1, applicants.size());
        assertOrganizationDTO(applicant, applicants.get(0));
    }
    
    // 1 applicant and 1 propertyOwner
    @Test
    void testPopulateStakeholderListWithPropertyOwners_1() throws ApplicationException {
        PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.ANS);
        PersonDTO applicant = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT));
        PersonDTO propertyOwner = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.PROPERTY_OWNER));
        input.setStakeholders(List.of(applicant));
        
        List<StakeholderDTO> stakeholderDTOList = new ArrayList<>();
        stakeholderDTOList.add(propertyOwner);
        doReturn(stakeholderDTOList).when(fbServiceMock).getPropertyOwnerByPropertyDesignation(anyString());
        
        byggrService.postCase(input);
        
        ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
        verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
        SaveNewArendeMessage saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
        Arende2 arende = saveNewArendeMessage.getArende();
        var intressenter = arende.getIntressentLista().getIntressent();
        
        assertEquals(2, intressenter.size());
        
        var propertyOwners = intressenter.stream().filter(intressent -> intressent.getRollLista().getRoll().contains(StakeholderRole.PROPERTY_OWNER.getText())).toList();
        assertEquals(1, propertyOwners.size());
        assertPersonDTO(propertyOwner, propertyOwners.get(0));
        
        var applicants = intressenter.stream().filter(intressent -> intressent.getRollLista().getRoll().contains(StakeholderRole.APPLICANT.getText())).toList();
        assertEquals(1, applicants.size());
        assertPersonDTO(applicant, applicants.get(0));
    }
    
    // same as testPopulateStakeholderListWithPropertyOwners_1 but for organization
    @Test
    void testPopulateStakeholderListWithPropertyOwners_1_1() throws ApplicationException {
        PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.ANS);
        OrganizationDTO applicant = (OrganizationDTO) TestUtil.createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT));
        OrganizationDTO propertyOwner = (OrganizationDTO) TestUtil.createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.PROPERTY_OWNER));
        input.setStakeholders(List.of(applicant));
        
        List<StakeholderDTO> stakeholderDTOList = new ArrayList<>();
        stakeholderDTOList.add(propertyOwner);
        doReturn(stakeholderDTOList).when(fbServiceMock).getPropertyOwnerByPropertyDesignation(anyString());
        
        byggrService.postCase(input);
        
        ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
        verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
        SaveNewArendeMessage saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
        Arende2 arende = saveNewArendeMessage.getArende();
        var intressenter = arende.getIntressentLista().getIntressent();
        
        assertEquals(2, intressenter.size());
        
        var propertyOwners = intressenter.stream().filter(intressent -> intressent.getRollLista().getRoll().contains(StakeholderRole.PROPERTY_OWNER.getText())).toList();
        assertEquals(1, propertyOwners.size());
        assertOrganizationDTO(propertyOwner, propertyOwners.get(0));
        
        var applicants = intressenter.stream().filter(intressent -> intressent.getRollLista().getRoll().contains(StakeholderRole.APPLICANT.getText())).toList();
        assertEquals(1, applicants.size());
        assertOrganizationDTO(applicant, applicants.get(0));
    }
    
    // 1 applicant that is also propertyOwner + 1 more propertyOwner
    @Test
    void testPopulateStakeholderListWithPropertyOwners_2() throws ApplicationException {
        PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.ANS);
        PersonDTO applicant = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT));
        input.setStakeholders(List.of(applicant));
        
        PersonDTO propertyOwner = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.PROPERTY_OWNER));
        List<StakeholderDTO> stakeholderDTOList = new ArrayList<>(List.of(applicant, propertyOwner));
        doReturn(stakeholderDTOList).when(fbServiceMock).getPropertyOwnerByPropertyDesignation(anyString());
        
        byggrService.postCase(input);
        
        ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
        verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
        SaveNewArendeMessage saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
        Arende2 arende = saveNewArendeMessage.getArende();
        var intressenter = arende.getIntressentLista().getIntressent();
        
        assertEquals(2, intressenter.size());
        
        var propertyOwners = intressenter.stream().filter(intressent -> intressent.getRollLista().getRoll().contains(StakeholderRole.PROPERTY_OWNER.getText())).toList();
        assertEquals(2, propertyOwners.size());
        
        var applicants = intressenter.stream().filter(intressent -> intressent.getRollLista().getRoll().contains(StakeholderRole.APPLICANT.getText())).toList();
        assertEquals(1, applicants.size());
    }
    
    // Case does not contain PropertyOwner
    @Test
    void testContainsPropertyOwner() throws ApplicationException {
        PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.ANS);
        PersonDTO applicant = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT));
        input.setStakeholders(List.of(applicant));
        
        doReturn(new ArrayList<>()).when(fbServiceMock).getPropertyOwnerByPropertyDesignation(anyString());
        
        var postResult = byggrService.postCase(input);
        
        ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
        verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
        SaveNewArendeMessage saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
        Arende2 arende = saveNewArendeMessage.getArende();
        var intressenter = arende.getIntressentLista().getIntressent();
        
        assertEquals(1, intressenter.size());
        
        ArgumentCaptor<SaveNewHandelse> saveNewHandelseRequestCaptor = ArgumentCaptor.forClass(SaveNewHandelse.class);
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
    void testControlOfficial() throws ApplicationException {
        PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.ANS);
        PersonDTO controlOfficial = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.CONTROL_OFFICIAL));
        PersonDTO applicant = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT));
        input.setStakeholders(List.of(applicant, controlOfficial));
        
        var postResult = byggrService.postCase(input);
        
        ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
        verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
        SaveNewArendeMessage saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
        Arende2 arende = saveNewArendeMessage.getArende();
        var intressenter = arende.getIntressentLista().getIntressent();
        
        // The control official should be excluded
        assertEquals(1, intressenter.size());
        
        ArgumentCaptor<SaveNewHandelse> saveNewHandelseRequestCaptor = ArgumentCaptor.forClass(SaveNewHandelse.class);
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
    void testDoublePersonId() throws ApplicationException {
        String personId = UUID.randomUUID().toString();
        PlanningPermissionCaseDTO input = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.ANS);
        PersonDTO paymentPerson = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.PAYMENT_PERSON));
        paymentPerson.setPersonId(personId);
        PersonDTO applicant = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT));
        applicant.setPersonId(personId);
        input.setStakeholders(List.of(applicant, paymentPerson));
        
        var postResult = byggrService.postCase(input);
        
        ArgumentCaptor<SaveNewArende> saveNewArendeRequestCaptor = ArgumentCaptor.forClass(SaveNewArende.class);
        verify(arendeExportClientMock).saveNewArende(saveNewArendeRequestCaptor.capture());
        SaveNewArendeMessage saveNewArendeMessage = saveNewArendeRequestCaptor.getValue().getMessage();
        Arende2 arende = saveNewArendeMessage.getArende();
        var intressenter = arende.getIntressentLista().getIntressent();
        
        // Both should be excluded
        assertEquals(0, intressenter.size());
        
        ArgumentCaptor<SaveNewHandelse> saveNewHandelseRequestCaptor = ArgumentCaptor.forClass(SaveNewHandelse.class);
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
        String dnr = UUID.randomUUID().toString();
        List<AttachmentDTO> attachments = List.of(TestUtil.createAttachmentDTO((AttachmentCategory) TestUtil.getRandomOfEnum(AttachmentCategory.class)));
        byggrService.saveNewIncomingAttachmentHandelse(dnr, attachments);
        
        ArgumentCaptor<SaveNewHandelse> saveNewHandelseRequestCaptor = ArgumentCaptor.forClass(SaveNewHandelse.class);
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
    
    // Test getByggrStatus
    @Test
    void testGetByggrStatus() {
        String caseId = MessageFormat.format("BYGG-2022-{0}", new Random().nextInt(100000));
        String externalCaseID = UUID.randomUUID().toString();
        
        // Mock caseMappingServiceMock
        List<CaseMapping> caseMappingList = new ArrayList<>();
        caseMappingList.add(new CaseMapping(externalCaseID, caseId, SystemType.BYGGR, CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, "Test service"));
        doReturn(List.of(new CaseMapping(externalCaseID, caseId, SystemType.BYGGR, CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, "Test service")))
            .when(caseMappingServiceMock).getCaseMapping(externalCaseID, caseId);
        
        // Mock arendeExportClientMock
        GetArendeResponse getArendeResponse = new GetArendeResponse();
        Arende arende = new Arende();
        arende.setDnr(caseId);
        arende.setStatus("Pågående");
        ArrayOfHandelse arrayOfHandelse = new ArrayOfHandelse();
        Handelse handelse_1 = new Handelse();
        handelse_1.setStartDatum(LocalDateTime.now().minusDays(5));
        handelse_1.setHandelsetyp("Handelsetyp 1");
        handelse_1.setHandelseslag("Handelseslag 1");
        handelse_1.setHandelseutfall("Handelseutfall 1");
        Handelse handelse_2 = new Handelse();
        handelse_2.setStartDatum(LocalDateTime.now().minusDays(2));
        handelse_2.setHandelsetyp(BYGGR_HANDELSETYP_BESLUT);
        handelse_2.setHandelseslag(BYGGR_HANDELSESLAG_SLUTBESKED);
        handelse_2.setHandelseutfall("Handelseutfall 2");
        Handelse handelse_3 = new Handelse();
        handelse_3.setStartDatum(LocalDateTime.now().minusDays(10));
        handelse_3.setHandelsetyp("Handelsetyp 3");
        handelse_3.setHandelseslag("Handelseslag 3");
        handelse_3.setHandelseutfall("Handelseutfall 3");
        arrayOfHandelse.getHandelse().add(handelse_1);
        arrayOfHandelse.getHandelse().add(handelse_2);
        arrayOfHandelse.getHandelse().add(handelse_3);
        arende.setHandelseLista(arrayOfHandelse);
        getArendeResponse.setGetArendeResult(arende);
        doReturn(getArendeResponse).when(arendeExportClientMock).getArende(any());
        
        // Let's go
        var getStatusResult = byggrService.getByggrStatus(caseId, externalCaseID);
        
        assertCaseStatus(caseId, externalCaseID, caseMappingList.get(0).getCaseType(), caseMappingList.get(0).getServiceName(), handelse_2.getHandelseslag(), handelse_2.getStartDatum(), getStatusResult);
        
        ArgumentCaptor<GetArende> getArendeRequestCaptor = ArgumentCaptor.forClass(GetArende.class);
        verify(arendeExportClientMock, times(1)).getArende(getArendeRequestCaptor.capture());
        assertEquals(caseId, getArendeRequestCaptor.getValue().getDnr());
    }
    
    // Test getByggrStatusByOrgNr
    @Test
    void testGetByggrStatusByOrgNr() {
        
        String caseId_1 = MessageFormat.format("BYGG-2021-{0}", new Random().nextInt(100000));
        String caseId_2 = MessageFormat.format("BYGG-2022-{0}", new Random().nextInt(100000));
        String externalCaseID_1 = UUID.randomUUID().toString();
        String externalCaseID_2 = UUID.randomUUID().toString();
        
        // Mock caseMappingServiceMock
        List<CaseMapping> caseMappingList_1 = new ArrayList<>();
        caseMappingList_1.add(new CaseMapping(externalCaseID_1, caseId_1, SystemType.BYGGR, CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, "Test service"));
        doReturn(caseMappingList_1)
            .when(caseMappingServiceMock).getCaseMapping(null, caseId_1);
        doReturn(caseMappingList_1)
            .when(caseMappingServiceMock).getCaseMapping(externalCaseID_1, caseId_1);
        
        List<CaseMapping> caseMappingList_2 = new ArrayList<>();
        caseMappingList_2.add(new CaseMapping(externalCaseID_2, caseId_2, SystemType.BYGGR, CaseType.ANMALAN_ATTEFALL, "Test service 2"));
        doReturn(caseMappingList_2)
            .when(caseMappingServiceMock).getCaseMapping(null, caseId_2);
        doReturn(caseMappingList_2)
            .when(caseMappingServiceMock).getCaseMapping(externalCaseID_2, caseId_2);
        
        // Mock arenadeExportClientMock
        GetRelateradeArendenByPersOrgNrAndRoleResponse getRelateradeArendenByPersOrgNrAndRoleResponse = new GetRelateradeArendenByPersOrgNrAndRoleResponse();
        ArrayOfArende1 arrayOfArende = new ArrayOfArende1();
        Arende arende_1 = new Arende();
        arende_1.setDnr(caseId_1);
        arende_1.setStatus("Pågående");
        ArrayOfHandelse arrayOfHandelse_1 = new ArrayOfHandelse();
        Handelse handelse_1 = new Handelse();
        handelse_1.setStartDatum(LocalDateTime.now().minusDays(5));
        handelse_1.setHandelsetyp(BYGGR_HANDELSETYP_HANDLING);
        handelse_1.setHandelseslag(BYGGR_HANDELSESLAG_KOMPLETTERANDE_HANDLINGAR);
        Handelse handelse_2 = new Handelse();
        handelse_2.setStartDatum(LocalDateTime.now().minusDays(2));
        handelse_2.setHandelsetyp(BYGGR_HANDELSETYP_BESLUT);
        handelse_2.setHandelseslag(BYGGR_HANDELSESLAG_SLUTBESKED);
        arrayOfHandelse_1.getHandelse().add(handelse_1);
        arrayOfHandelse_1.getHandelse().add(handelse_2);
        arende_1.setHandelseLista(arrayOfHandelse_1);
        arrayOfArende.getArende().add(arende_1);
        
        Arende arende_2 = new Arende();
        arende_2.setDnr(caseId_2);
        arende_2.setStatus("Pågående");
        ArrayOfHandelse arrayOfHandelse_2 = new ArrayOfHandelse();
        Handelse handelse_2_1 = new Handelse();
        handelse_2_1.setStartDatum(LocalDateTime.now().minusDays(5));
        handelse_2_1.setHandelsetyp(BYGGR_HANDELSETYP_HANDLING);
        handelse_2_1.setHandelseslag(BYGGR_HANDELSESLAG_KOMPLETTERANDE_HANDLINGAR);
        Handelse handelse_2_2 = new Handelse();
        handelse_2_2.setStartDatum(LocalDateTime.now().minusDays(10));
        handelse_2_2.setHandelsetyp(BYGGR_HANDELSETYP_BESLUT);
        handelse_2_2.setHandelseslag(BYGGR_HANDELSESLAG_SLUTBESKED);
        arrayOfHandelse_2.getHandelse().add(handelse_2_1);
        arrayOfHandelse_2.getHandelse().add(handelse_2_2);
        arende_2.setHandelseLista(arrayOfHandelse_2);
        arrayOfArende.getArende().add(arende_2);
        getRelateradeArendenByPersOrgNrAndRoleResponse.setGetRelateradeArendenByPersOrgNrAndRoleResult(arrayOfArende);
        doReturn(getRelateradeArendenByPersOrgNrAndRoleResponse).when(arendeExportClientMock).getRelateradeArendenByPersOrgNrAndRole(any());
        
        String orgnr = TestUtil.generateRandomOrganizationNumber();
        var getStatusResult = byggrService.getByggrStatusByOrgNr(orgnr);
        
        assertEquals(2, getStatusResult.size());
        assertCaseStatus(caseId_1, externalCaseID_1, caseMappingList_1.get(0).getCaseType(), caseMappingList_1.get(0).getServiceName(), handelse_2.getHandelseslag(), handelse_2.getStartDatum(), getStatusResult.get(0));
        assertCaseStatus(caseId_2, externalCaseID_2, caseMappingList_2.get(0).getCaseType(), caseMappingList_2.get(0).getServiceName(), handelse_2_1.getHandelseslag(), handelse_2_1.getStartDatum(), getStatusResult.get(1));
        
        ArgumentCaptor<GetRelateradeArendenByPersOrgNrAndRole> getRelateradeArendenByPersOrgNrAndRoleRequestCaptor = ArgumentCaptor.forClass(GetRelateradeArendenByPersOrgNrAndRole.class);
        verify(arendeExportClientMock, times(1)).getRelateradeArendenByPersOrgNrAndRole(getRelateradeArendenByPersOrgNrAndRoleRequestCaptor.capture());
        assertEquals(orgnr, getRelateradeArendenByPersOrgNrAndRoleRequestCaptor.getValue().getPersOrgNr());
        assertTrue(getRelateradeArendenByPersOrgNrAndRoleRequestCaptor.getValue().getArendeIntressentRoller().getString().contains(StakeholderRole.APPLICANT.getText()));
        assertTrue(getRelateradeArendenByPersOrgNrAndRoleRequestCaptor.getValue().getHandelseIntressentRoller().getString().contains(StakeholderRole.APPLICANT.getText()));
    }
    
}
