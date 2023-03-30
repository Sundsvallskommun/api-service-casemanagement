package se.sundsvall.casemanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.casemanagement.TestUtil;
import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.api.model.EnvironmentalCaseDTO;
import se.sundsvall.casemanagement.api.model.EnvironmentalFacilityDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.StakeholderDTO;
import se.sundsvall.casemanagement.api.model.enums.AddressCategory;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.api.model.enums.StakeholderType;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.integration.ecos.EcosService;
import se.sundsvall.casemanagement.integration.ecos.MinutMiljoClient;
import se.sundsvall.casemanagement.integration.ecos.MinutMiljoClientV2;
import se.sundsvall.casemanagement.service.exceptions.ApplicationException;
import se.sundsvall.casemanagement.testutils.TestConstants;
import se.sundsvall.casemanagement.util.CaseUtil;
import se.sundsvall.casemanagement.util.Constants;

import minutmiljo.AddDocumentsToCase;
import minutmiljo.AddDocumentsToCaseSvcDto;
import minutmiljo.AddPartyToCase;
import minutmiljo.AddPartyToFacility;
import minutmiljo.ArrayOfOccurrenceListItemSvcDto;
import minutmiljo.ArrayOfPartyAddressSvcDto;
import minutmiljo.ArrayOfPartySvcDto;
import minutmiljo.ArrayOfSearchCaseResultSvcDto;
import minutmiljo.BiologicalStepSvcDto;
import minutmiljo.CaseSvcDto;
import minutmiljo.ClosedTankSvcDto;
import minutmiljo.ContactInfoSvcDto;
import minutmiljo.CreateFoodFacility;
import minutmiljo.CreateHealthProtectionFacility;
import minutmiljo.CreateHealthProtectionFacilitySvcDto;
import minutmiljo.CreateHeatPumpFacility;
import minutmiljo.CreateHeatPumpFacilitySvcDto;
import minutmiljo.CreateHeatPumpFacilityWithHeatTransferFluidSvcDto;
import minutmiljo.CreateIndividualSewageFacility;
import minutmiljo.CreateIndividualSewageFacilitySvcDto;
import minutmiljo.CreateOccurrenceOnCase;
import minutmiljo.CreateOrganizationParty;
import minutmiljo.CreatePersonParty;
import minutmiljo.CreateSoilHeatingFacilitySvcDto;
import minutmiljo.DocumentSvcDto;
import minutmiljo.DrySolutionSvcDto;
import minutmiljo.FilterBedSvcDto;
import minutmiljo.GetCase;
import minutmiljo.GetCaseResponse;
import minutmiljo.InfiltrationPlantSvcDto;
import minutmiljo.MiniSewageSvcDto;
import minutmiljo.OccurrenceListItemSvcDto;
import minutmiljo.PartyAddressSvcDto;
import minutmiljo.PartySvcDto;
import minutmiljo.PurificationStepSvcDto;
import minutmiljo.SandFilterSvcDto;
import minutmiljo.SearchCaseResponse;
import minutmiljo.SearchCaseResultSvcDto;
import minutmiljo.SearchParty;
import minutmiljo.SearchPartyResponse;
import minutmiljo.SepticTankSvcDto;
import minutmiljoV2.RegisterDocument;
import minutmiljoV2.RegisterDocumentCaseSvcDtoV2;

@ExtendWith(MockitoExtension.class)
class EcosServiceTest {
    
    private static final String CYTONOL = "e19981ad-34b2-4e14-88f5-133f61ca85aa";
    @InjectMocks
    private EcosService ecosService;
    @Mock
    private CitizenMappingService citizenMappingServiceMock;
    @Mock
    private CaseMappingService caseMappingServiceMock;
    @Mock
    private MinutMiljoClient minutMiljoClientMock;
    @Mock
    private MinutMiljoClientV2 minutMiljoClientV2Mock;
    @Mock
    private FbService fbServiceMock;
    
    @BeforeEach
    void beforeEach() {
        TestUtil.standardMockFb(fbServiceMock);
        TestUtil.standardMockCitizenMapping(citizenMappingServiceMock);
        TestUtil.standardMockMinutMiljo(minutMiljoClientMock, minutMiljoClientV2Mock);
        TestUtil.standardMockFb(fbServiceMock);
        TestUtil.standardMockCitizenMapping(citizenMappingServiceMock);
    }
    
    @Test
    void testFoodFacilityCase() throws ApplicationException {
        
        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.REGISTRERING_AV_LIVSMEDEL, AttachmentCategory.ANMALAN_LIVSMEDELSANLAGGNING);
        OrganizationDTO organization = (OrganizationDTO) TestUtil.createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT, StakeholderRole.OPERATOR));
        PersonDTO person = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.CONTACT_PERSON));
        eCase.setStakeholders(List.of(organization, person));
        
        // Mock citizenMappingService.getPersonalNumber
        doReturn(person.getPersonalNumber()).when(citizenMappingServiceMock).getPersonalNumber(person.getPersonId());
        
        var result = ecosService.postCase(eCase);
        assertEquals(TestConstants.ECOS_CASE_NUMBER, result.getCaseNumber());
        
        ArgumentCaptor<SearchParty> searchPartyArgumentCaptor = ArgumentCaptor.forClass(SearchParty.class);
        // Two times for organization (with and without Sokigo-prefix) and two times for person (with and without hyphen)
        verify(minutMiljoClientMock, times(4)).searchParty(searchPartyArgumentCaptor.capture());
        List<SearchParty> searchPartyList = searchPartyArgumentCaptor.getAllValues();
        List<SearchParty> organizationSearchParties = searchPartyList.stream().filter(searchParty -> searchParty.getModel().getOrganizationIdentificationNumber() != null).toList();
        List<SearchParty> personSearchParties = searchPartyList.stream().filter(searchParty -> searchParty.getModel().getPersonalIdentificationNumber() != null).toList();
        assertEquals(2, organizationSearchParties.size());
        assertEquals(2, personSearchParties.size());
        
        ArgumentCaptor<CreateOrganizationParty> createOrganizationPartyArgumentCaptor = ArgumentCaptor.forClass(CreateOrganizationParty.class);
        verify(minutMiljoClientMock, times(1)).createOrganizationParty(createOrganizationPartyArgumentCaptor.capture());
        assertEquals("16" + organization.getOrganizationNumber(), createOrganizationPartyArgumentCaptor.getValue().getOrganizationParty().getNationalIdentificationNumber());
        assertEquals(organization.getOrganizationName(), createOrganizationPartyArgumentCaptor.getValue().getOrganizationParty().getOrganizationName());
        assertAddress(organization.getAddresses(), createOrganizationPartyArgumentCaptor.getValue().getOrganizationParty().getAddresses());
        createOrganizationPartyArgumentCaptor.getValue().getOrganizationParty().getContactInfo().getContactInfoSvcDto().forEach(this::assertContactInfo);
        
        ArgumentCaptor<CreatePersonParty> createPersonPartyArgumentCaptor = ArgumentCaptor.forClass(CreatePersonParty.class);
        verify(minutMiljoClientMock, times(1)).createPersonParty(createPersonPartyArgumentCaptor.capture());
        assertEquals(new StringBuilder(person.getPersonalNumber()).insert(8, "-").toString(), createPersonPartyArgumentCaptor.getValue().getPersonParty().getNationalIdentificationNumber());
        assertEquals(person.getFirstName(), createPersonPartyArgumentCaptor.getValue().getPersonParty().getFirstName());
        assertEquals(person.getLastName(), createPersonPartyArgumentCaptor.getValue().getPersonParty().getLastName());
        assertAddress(person.getAddresses(), createPersonPartyArgumentCaptor.getValue().getPersonParty().getAddresses());
        assertContactInfo(createPersonPartyArgumentCaptor.getValue().getPersonParty().getContactInfo());
        
        ArgumentCaptor<CreateFoodFacility> createFoodFacilityArgumentCaptor = ArgumentCaptor.forClass(CreateFoodFacility.class);
        verify(minutMiljoClientMock, times(1)).createFoodFacility(createFoodFacilityArgumentCaptor.capture());
        assertEquals(TestConstants.ADRESSPLATS_ID, createFoodFacilityArgumentCaptor.getValue().getCreateFoodFacilitySvcDto().getAddress().getAdressPlatsId());
        assertEquals(TestConstants.FNR, createFoodFacilityArgumentCaptor.getValue().getCreateFoodFacilitySvcDto().getEstateDesignation().getFnr());
        assertEquals(result.getCaseId(), createFoodFacilityArgumentCaptor.getValue().getCreateFoodFacilitySvcDto().getCase());
        assertEquals(eCase.getFacilities().get(0).getDescription(), createFoodFacilityArgumentCaptor.getValue().getCreateFoodFacilitySvcDto().getNote());
        assertTrue(createFoodFacilityArgumentCaptor.getValue().getCreateFoodFacilitySvcDto().getFacilityCollectionName().contains(eCase.getFacilities().get(0).getFacilityCollectionName()));
        
        ArgumentCaptor<AddPartyToFacility> addPartyToFacilityArgumentCaptor = ArgumentCaptor.forClass(AddPartyToFacility.class);
        verify(minutMiljoClientMock, times(2)).addPartyToFacility(addPartyToFacilityArgumentCaptor.capture());
        List<AddPartyToFacility> addPartyToFacilityList = addPartyToFacilityArgumentCaptor.getAllValues();
        addPartyToFacilityList.forEach(addPartyToFacility -> {
            assertNotNull(addPartyToFacility.getModel().getFacilityId());
            assertNotNull(addPartyToFacility.getModel().getPartyId());
            assertNotNull(addPartyToFacility.getModel().getRoles().getGuid());
        });
        
        ArgumentCaptor<AddPartyToCase> addPartyToCaseArgumentCaptor = ArgumentCaptor.forClass(AddPartyToCase.class);
        verify(minutMiljoClientMock, times(2)).addPartyToCase(addPartyToCaseArgumentCaptor.capture());
        List<AddPartyToCase> addPartyToCaseList = addPartyToCaseArgumentCaptor.getAllValues();
        addPartyToCaseList.forEach(addPartyToCase -> {
            assertNotNull(addPartyToCase.getModel().getCaseId());
            assertNotNull(addPartyToCase.getModel().getPartyId());
            assertNotNull(addPartyToCase.getModel().getRoles().getGuid());
        });
        
        ArgumentCaptor<RegisterDocument> registerDocumentArgumentCaptor = ArgumentCaptor.forClass(RegisterDocument.class);
        verify(minutMiljoClientV2Mock, times(1)).registerDocumentV2(registerDocumentArgumentCaptor.capture());
        RegisterDocumentCaseSvcDtoV2 registerDocumentCaseSvcDtoV2 = registerDocumentArgumentCaptor.getValue().getRegisterDocumentCaseSvcDto();
        assertEquals(eCase.getFacilities().get(0).getFacilityCollectionName() + ", " + eCase.getFacilities().get(0).getAddress().getPropertyDesignation().toUpperCase(), registerDocumentCaseSvcDtoV2.getCaseSubtitleFree());
        assertEquals(Constants.ECOS_OCCURENCE_TYPE_ID_ANMALAN, registerDocumentCaseSvcDtoV2.getOccurrenceTypeId());
        assertEquals(Constants.ECOS_HANDLING_OFFICER_GROUP_ID_EXPEDITIONEN, registerDocumentCaseSvcDtoV2.getHandlingOfficerGroupId());
        assertEquals(Constants.ECOS_DIARY_PLAN_LIVSMEDEL, registerDocumentCaseSvcDtoV2.getDiaryPlanId());
        assertEquals(Constants.ECOS_PROCESS_TYPE_ID_REGISTRERING_AV_LIVSMEDEL, registerDocumentCaseSvcDtoV2.getProcessTypeId());
        
        verify(caseMappingServiceMock, times(1)).postCaseMapping(any(CaseMapping.class));
    }
    
    @Test
    void testMinimalFoodFacilityCase() throws ApplicationException {
        
        EnvironmentalCaseDTO eCase = new EnvironmentalCaseDTO();
        List<AttachmentDTO> aList = new ArrayList<>();
        AttachmentDTO attachment = new AttachmentDTO();
        attachment.setCategory(AttachmentCategory.ANMALAN_LIVSMEDELSANLAGGNING);
        attachment.setExtension(TestConstants.PDF_EXTENSION);
        attachment.setName("Document Name");
        attachment.setFile(TestConstants.BASE64_STRING);
        aList.add(attachment);
        eCase.setAttachments(aList);
        
        List<StakeholderDTO> sList = new ArrayList<>();
        OrganizationDTO organization = new OrganizationDTO();
        organization.setType(StakeholderType.ORGANIZATION);
        List<StakeholderRole> srList = new ArrayList<>();
        srList.add(StakeholderRole.OPERATOR);
        organization.setRoles(srList);
        organization.setOrganizationName("organizationName");
        organization.setOrganizationNumber(TestConstants.ORG_NUMBER);
        
        sList.add(organization);
        
        PersonDTO person = new PersonDTO();
        person.setType(StakeholderType.PERSON);
        List<StakeholderRole> srList2 = new ArrayList<>();
        srList2.add(StakeholderRole.INVOICE_RECIPENT);
        person.setRoles(srList2);
        person.setFirstName("Förnamn");
        person.setLastName("Efternamn");
        
        sList.add(person);
        
        eCase.setStakeholders(sList);
        
        EnvironmentalFacilityDTO facility = new EnvironmentalFacilityDTO();
        facility.setFacilityCollectionName("facilityCollectionName");
        
        AddressDTO facilityAddress = new AddressDTO();
        facilityAddress.setAddressCategories(List.of(AddressCategory.VISITING_ADDRESS));
        facilityAddress.setPropertyDesignation(TestConstants.PROPERTY_DESIGNATION_BALDER);
        facility.setAddress(facilityAddress);
        eCase.setFacilities(List.of(facility));
        
        eCase.setCaseType(CaseType.REGISTRERING_AV_LIVSMEDEL);
        eCase.setExternalCaseId(String.valueOf(new Random().nextLong()));
        
        ecosService.postCase(eCase);
        
        verify(minutMiljoClientMock, times(2)).searchParty(any());
        verify(minutMiljoClientMock, times(1)).createOrganizationParty(any());
        verify(minutMiljoClientMock, times(1)).createPersonParty(any());
        verify(minutMiljoClientMock, times(1)).createFoodFacility(any());
        verify(minutMiljoClientMock, times(2)).addPartyToFacility(any());
        verify(minutMiljoClientMock, times(2)).addPartyToCase(any());
        verify(minutMiljoClientV2Mock, times(1)).registerDocumentV2(any());
        verify(caseMappingServiceMock, times(1)).postCaseMapping(any(CaseMapping.class));
    }
    
    // createHeatPumpFacility
    @Test
    void testCreateHeatPumpFacilityCase() throws ApplicationException {
        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP, AttachmentCategory.ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW);
        OrganizationDTO organization = (OrganizationDTO) TestUtil.createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT, StakeholderRole.OPERATOR));
        PersonDTO person = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.CONTACT_PERSON));
        eCase.setStakeholders(List.of(organization, person));
        
        // Mock citizenMappingService.getPersonalNumber
        doReturn(person.getPersonalNumber()).when(citizenMappingServiceMock).getPersonalNumber(person.getPersonId());
        
        var result = ecosService.postCase(eCase);
        assertEquals(TestConstants.ECOS_CASE_NUMBER, result.getCaseNumber());
        
        ArgumentCaptor<CreateHeatPumpFacility> createHeatPumpFacilityArgumentCaptor = ArgumentCaptor.forClass(CreateHeatPumpFacility.class);
        verify(minutMiljoClientMock, times(1)).createHeatPumpFacility(createHeatPumpFacilityArgumentCaptor.capture());
        CreateSoilHeatingFacilitySvcDto createSoilHeatingFacilitySvcDto = (CreateSoilHeatingFacilitySvcDto) createHeatPumpFacilityArgumentCaptor.getValue().getCreateIndividualSewageSvcDto();
        
        assertEquals(TestConstants.ADRESSPLATS_ID, createSoilHeatingFacilitySvcDto.getAddress().getAdressPlatsId());
        assertEquals(Constants.ECOS_FACILITY_STATUS_ID_ANMALD_ANSOKT, createSoilHeatingFacilitySvcDto.getFacilityStatusId());
        assertEquals(result.getCaseId(), createSoilHeatingFacilitySvcDto.getCreatedFromCaseId());
        assertEquals(TestConstants.FNR, createSoilHeatingFacilitySvcDto.getEstate().getFnr());
        assertNotNull(createSoilHeatingFacilitySvcDto.getManufacturer());
        assertNotNull(createSoilHeatingFacilitySvcDto.getModel());
        assertNotNull(createSoilHeatingFacilitySvcDto.getPowerConsumption());
        assertNotNull(createSoilHeatingFacilitySvcDto.getPowerOutput());
    }
    
    @Test
    void testAirHeating() throws ApplicationException {
        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP, AttachmentCategory.ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW);
        Map<String, String> extraParameters = new HashMap<>();
        extraParameters.put("CreateAirHeatingFacilitySvcDto_Manufacturer", "Mitsubishi");
        extraParameters.put("CreateAirHeatingFacilitySvcDto_Model", "SuperHeater 2000");
        extraParameters.put("CreateAirHeatingFacilitySvcDto_PowerConsumption", "1.7");
        extraParameters.put("CreateAirHeatingFacilitySvcDto_PowerOutput", "4.8");
        eCase.getFacilities().get(0).setExtraParameters(extraParameters);
        
        ecosService.postCase(eCase);
        
        ArgumentCaptor<CreateHeatPumpFacility> createHeatPumpFacilityArgumentCaptor = ArgumentCaptor.forClass(CreateHeatPumpFacility.class);
        verifyMinutMiljoCallsForHeatPumpCase(createHeatPumpFacilityArgumentCaptor);
        
        verifyStandardParams(extraParameters, createHeatPumpFacilityArgumentCaptor.getValue().getCreateIndividualSewageSvcDto());
    }
    
    @Test
    void testGeothermalHeating() throws ApplicationException {
        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP, AttachmentCategory.ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW);
        Map<String, String> extraParameters = new HashMap<>();
        extraParameters.put("CreateGeothermalHeatingFacilitySvcDto_Manufacturer", "Mitsubishi");
        extraParameters.put("CreateGeothermalHeatingFacilitySvcDto_Model", "SuperHeater 2000");
        extraParameters.put("CreateGeothermalHeatingFacilitySvcDto_PowerConsumption", "1.7");
        extraParameters.put("CreateGeothermalHeatingFacilitySvcDto_PowerOutput", "4.8");
        extraParameters.put("CreateGeothermalHeatingFacilitySvcDto_Capacity", "90.8");
        extraParameters.put("CreateGeothermalHeatingFacilitySvcDto_HeatTransferFluidId", CYTONOL);
        eCase.getFacilities().get(0).setExtraParameters(extraParameters);
        
        ecosService.postCase(eCase);
        
        ArgumentCaptor<CreateHeatPumpFacility> createHeatPumpFacilityArgumentCaptor = ArgumentCaptor.forClass(CreateHeatPumpFacility.class);
        verifyMinutMiljoCallsForHeatPumpCase(createHeatPumpFacilityArgumentCaptor);
        
        verifyStandardParams(extraParameters, createHeatPumpFacilityArgumentCaptor.getValue().getCreateIndividualSewageSvcDto());
        verifyFluidParams(extraParameters, (CreateHeatPumpFacilityWithHeatTransferFluidSvcDto) createHeatPumpFacilityArgumentCaptor.getValue().getCreateIndividualSewageSvcDto());
    }
    
    @Test
    void testSoilHeating() throws ApplicationException {
        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP, AttachmentCategory.ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW);
        Map<String, String> extraParameters = new HashMap<>();
        extraParameters.put("CreateSoilHeatingFacilitySvcDto_Manufacturer", "Mitsubishi");
        extraParameters.put("CreateSoilHeatingFacilitySvcDto_Model", "SuperHeater 2000");
        extraParameters.put("CreateSoilHeatingFacilitySvcDto_PowerConsumption", "1.7");
        extraParameters.put("CreateSoilHeatingFacilitySvcDto_PowerOutput", "4.8");
        extraParameters.put("CreateSoilHeatingFacilitySvcDto_Capacity", "90.8");
        extraParameters.put("CreateSoilHeatingFacilitySvcDto_HeatTransferFluidId", CYTONOL);
        eCase.getFacilities().get(0).setExtraParameters(extraParameters);
        
        ecosService.postCase(eCase);
        
        ArgumentCaptor<CreateHeatPumpFacility> createHeatPumpFacilityArgumentCaptor = ArgumentCaptor.forClass(CreateHeatPumpFacility.class);
        verifyMinutMiljoCallsForHeatPumpCase(createHeatPumpFacilityArgumentCaptor);
        
        verifyStandardParams(extraParameters, createHeatPumpFacilityArgumentCaptor.getValue().getCreateIndividualSewageSvcDto());
        verifyFluidParams(extraParameters, (CreateHeatPumpFacilityWithHeatTransferFluidSvcDto) createHeatPumpFacilityArgumentCaptor.getValue().getCreateIndividualSewageSvcDto());
    }
    
    @Test
    void testExtraParamsNull() throws ApplicationException {
        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP, AttachmentCategory.ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW);
        eCase.getFacilities().get(0).setExtraParameters(null);
        
        ecosService.postCase(eCase);
        
        verify(minutMiljoClientMock, times(6)).searchParty(any());
        verify(minutMiljoClientMock, times(1)).createOrganizationParty(any());
        verify(minutMiljoClientMock, times(2)).createPersonParty(any());
        verify(minutMiljoClientMock, times(0)).createHeatPumpFacility(any());
        verify(minutMiljoClientMock, times(0)).addPartyToFacility(any());
        verify(minutMiljoClientMock, times(3)).addPartyToCase(any());
        verify(minutMiljoClientV2Mock, times(1)).registerDocumentV2(any());
    }
    
    @Test
    void testExtraParamsEmpty() throws ApplicationException {
        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP, AttachmentCategory.ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW);
        Map<String, String> extraParameters = new HashMap<>();
        eCase.getFacilities().get(0).setExtraParameters(extraParameters);
        
        ecosService.postCase(eCase);
        
        verify(minutMiljoClientMock, times(6)).searchParty(any());
        verify(minutMiljoClientMock, times(1)).createOrganizationParty(any());
        verify(minutMiljoClientMock, times(2)).createPersonParty(any());
        verify(minutMiljoClientMock, times(0)).createHeatPumpFacility(any());
        verify(minutMiljoClientMock, times(0)).addPartyToFacility(any());
        verify(minutMiljoClientMock, times(3)).addPartyToCase(any());
        verify(minutMiljoClientV2Mock, times(1)).registerDocumentV2(any());
    }
    
    @Test
    void testMarineHeating() throws ApplicationException {
        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP, AttachmentCategory.ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW);
        Map<String, String> extraParameters = new HashMap<>();
        extraParameters.put("CreateMarineHeatingFacilitySvcDto_Manufacturer", "Mitsubishi");
        extraParameters.put("CreateMarineHeatingFacilitySvcDto_Model", "SuperHeater 2000");
        extraParameters.put("CreateMarineHeatingFacilitySvcDto_PowerConsumption", "1.7");
        extraParameters.put("CreateMarineHeatingFacilitySvcDto_PowerOutput", "4.8");
        extraParameters.put("CreateMarineHeatingFacilitySvcDto_Capacity", "90.8");
        extraParameters.put("CreateMarineHeatingFacilitySvcDto_HeatTransferFluidId", CYTONOL);
        eCase.getFacilities().get(0).setExtraParameters(extraParameters);
        
        ecosService.postCase(eCase);
        
        ArgumentCaptor<CreateHeatPumpFacility> createHeatPumpFacilityArgumentCaptor = ArgumentCaptor.forClass(CreateHeatPumpFacility.class);
        verifyMinutMiljoCallsForHeatPumpCase(createHeatPumpFacilityArgumentCaptor);
        
        verifyStandardParams(extraParameters, createHeatPumpFacilityArgumentCaptor.getValue().getCreateIndividualSewageSvcDto());
        verifyFluidParams(extraParameters, (CreateHeatPumpFacilityWithHeatTransferFluidSvcDto) createHeatPumpFacilityArgumentCaptor.getValue().getCreateIndividualSewageSvcDto());
    }
    
    private void verifyStandardParams(Map<String, String> extraParameters, CreateHeatPumpFacilitySvcDto svcDto) {
        Assertions.assertEquals(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("Manufacturer")).findFirst().orElseThrow().getValue(), svcDto.getManufacturer());
        Assertions.assertEquals(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("Model")).findFirst().orElseThrow().getValue(), svcDto.getModel());
        Assertions.assertEquals(CaseUtil.parseDouble(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("PowerConsumption")).findFirst().orElseThrow().getValue()), svcDto.getPowerConsumption());
        Assertions.assertEquals(CaseUtil.parseDouble(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("PowerOutput")).findFirst().orElseThrow().getValue()), svcDto.getPowerOutput());
    }
    
    private void verifyFluidParams(Map<String, String> extraParameters, CreateHeatPumpFacilityWithHeatTransferFluidSvcDto svcDto) {
        Assertions.assertEquals(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("HeatTransferFluidId")).findFirst().orElseThrow().getValue(), svcDto.getHeatTransferFluidId());
        Assertions.assertEquals(CaseUtil.parseDouble(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("Capacity")).findFirst().orElseThrow().getValue()), svcDto.getCapacity());
    }
    
    private void verifyMinutMiljoCallsForHeatPumpCase(ArgumentCaptor<CreateHeatPumpFacility> createHeatPumpFacilityArgumentCaptor) {
        verify(minutMiljoClientMock, times(6)).searchParty(any());
        verify(minutMiljoClientMock, times(1)).createOrganizationParty(any());
        verify(minutMiljoClientMock, times(2)).createPersonParty(any());
        verify(minutMiljoClientMock, times(1)).createHeatPumpFacility(createHeatPumpFacilityArgumentCaptor.capture());
        verify(minutMiljoClientMock, times(3)).addPartyToFacility(any());
        verify(minutMiljoClientMock, times(3)).addPartyToCase(any());
        verify(minutMiljoClientV2Mock, times(1)).registerDocumentV2(any());
    }
    
    @Test
    void healthProtectionTest() throws ApplicationException {
        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANMALAN_HALSOSKYDDSVERKSAMHET, AttachmentCategory.ANMALAN_HALSOSKYDDSVERKSAMHET);
        
        ecosService.postCase(eCase);
        
        ArgumentCaptor<CreateHealthProtectionFacility> createHealthProtectionFacilityArgumentCaptor = ArgumentCaptor.forClass(CreateHealthProtectionFacility.class);
        verify(minutMiljoClientMock, times(6)).searchParty(any());
        verify(minutMiljoClientMock, times(1)).createOrganizationParty(any());
        verify(minutMiljoClientMock, times(2)).createPersonParty(any());
        verify(minutMiljoClientMock, times(1)).createHealthProtectionFacility(createHealthProtectionFacilityArgumentCaptor.capture());
        verify(minutMiljoClientMock, times(3)).addPartyToFacility(any());
        verify(minutMiljoClientMock, times(3)).addPartyToCase(any());
        verify(minutMiljoClientV2Mock, times(1)).registerDocumentV2(any());
        
        CreateHealthProtectionFacilitySvcDto svcDto = createHealthProtectionFacilityArgumentCaptor.getValue().getCreateHealthProtectionFacilitySvcDto();
        Assertions.assertEquals(TestConstants.ADRESSPLATS_ID, svcDto.getAddress().getAdressPlatsId());
        Assertions.assertEquals(TestConstants.FNR, svcDto.getEstateDesignation().getFnr());
        Assertions.assertEquals(TestConstants.ECOS_CASE_ID, svcDto.getCase());
        Assertions.assertEquals(eCase.getFacilities().get(0).getDescription(), svcDto.getNote());
        Assertions.assertEquals(eCase.getFacilities().get(0).getFacilityCollectionName(), svcDto.getFacilityCollectionName());
    }
    
    @Test
    void individualSewageSepticTank() throws ApplicationException {
        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
        Map<String, String> extraParameters = new HashMap<>();
        String prefix = "SepticTankSvcDto_";
        TestUtil.setSewageStandardExtraParams(extraParameters, prefix);
        extraParameters.put(prefix + "EmptyingInterval", String.valueOf(new Random().nextInt()));
        extraParameters.put(prefix + "HasCeMarking", String.valueOf(new Random().nextBoolean()));
        extraParameters.put(prefix + "HasTPipe", String.valueOf(new Random().nextBoolean()));
        extraParameters.put(prefix + "Volume", String.valueOf(new Random().nextDouble()));
        eCase.getFacilities().get(0).setExtraParameters(extraParameters);
        
        ecosService.postCase(eCase);
        
        ArgumentCaptor<CreateIndividualSewageFacility> argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
        verifyMinutMiljoCallsForSewageCase(argumentCaptor);
        verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);
        
        SepticTankSvcDto septicTankSvcDto = (SepticTankSvcDto) argumentCaptor.getValue().getCreateIndividualSewageSvcDto().getPurificationSteps().getPurificationStepSvcDto().get(0);
        
        Assertions.assertEquals(CaseUtil.parseInteger(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "EmptyingInterval")).findFirst().orElseThrow().getValue()), septicTankSvcDto.getEmptyingInterval());
        Assertions.assertEquals(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "HasCeMarking")).findFirst().orElseThrow().getValue()), septicTankSvcDto.isHasCeMarking());
        Assertions.assertEquals(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "HasTPipe")).findFirst().orElseThrow().getValue()), septicTankSvcDto.isHasTPipe());
        Assertions.assertEquals(CaseUtil.parseDouble(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Volume")).findFirst().orElseThrow().getValue()), septicTankSvcDto.getVolume());
    }
    
    @Test
    void individualSewageInfiltration() throws ApplicationException {
        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
        Map<String, String> extraParameters = new HashMap<>();
        String prefix = "InfiltrationPlantSvcDto_";
        TestUtil.setSewageStandardExtraParams(extraParameters, prefix);
        
        extraParameters.put(prefix + "Elevated", String.valueOf(new Random().nextBoolean()));
        extraParameters.put(prefix + "Reinforced", String.valueOf(new Random().nextBoolean()));
        extraParameters.put(prefix + "IsModuleSystem", String.valueOf(new Random().nextBoolean()));
        extraParameters.put(prefix + "SpreadLinesCount", String.valueOf(new Random().nextInt()));
        extraParameters.put(prefix + "SpreadLinesLength", String.valueOf(new Random().nextInt()));
        eCase.getFacilities().get(0).setExtraParameters(extraParameters);
        
        ecosService.postCase(eCase);
        
        ArgumentCaptor<CreateIndividualSewageFacility> argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
        verifyMinutMiljoCallsForSewageCase(argumentCaptor);
        verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);
        
        InfiltrationPlantSvcDto infiltrationPlantSvcDto = (InfiltrationPlantSvcDto) argumentCaptor.getValue().getCreateIndividualSewageSvcDto().getPurificationSteps().getPurificationStepSvcDto().get(0);
        
        Assertions.assertEquals(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Elevated")).findFirst().orElseThrow().getValue()), infiltrationPlantSvcDto.isElevated());
        Assertions.assertEquals(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Reinforced")).findFirst().orElseThrow().getValue()), infiltrationPlantSvcDto.isReinforced());
        Assertions.assertEquals(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "IsModuleSystem")).findFirst().orElseThrow().getValue()), infiltrationPlantSvcDto.isIsModuleSystem());
        Assertions.assertEquals(CaseUtil.parseInteger(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "SpreadLinesCount")).findFirst().orElseThrow().getValue()), infiltrationPlantSvcDto.getSpreadLinesCount());
        Assertions.assertEquals(CaseUtil.parseInteger(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "SpreadLinesLength")).findFirst().orElseThrow().getValue()), infiltrationPlantSvcDto.getSpreadLinesLength());
    }
    
    @Test
    void individualSewageClosedTank() throws ApplicationException {
        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
        Map<String, String> extraParameters = new HashMap<>();
        String prefix = "ClosedTankSvcDto_";
        TestUtil.setSewageStandardExtraParams(extraParameters, prefix);
        
        extraParameters.put(prefix + "EmptyingInterval", String.valueOf(new Random().nextInt()));
        extraParameters.put(prefix + "HasCeMarking", String.valueOf(new Random().nextBoolean()));
        extraParameters.put(prefix + "Volume", String.valueOf(new Random().nextDouble()));
        eCase.getFacilities().get(0).setExtraParameters(extraParameters);
        
        ecosService.postCase(eCase);
        
        ArgumentCaptor<CreateIndividualSewageFacility> argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
        verifyMinutMiljoCallsForSewageCase(argumentCaptor);
        verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);
        
        ClosedTankSvcDto closedTankSvcDto = (ClosedTankSvcDto) argumentCaptor.getValue().getCreateIndividualSewageSvcDto().getPurificationSteps().getPurificationStepSvcDto().get(0);
        
        Assertions.assertEquals(CaseUtil.parseInteger(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "EmptyingInterval")).findFirst().orElseThrow().getValue()), closedTankSvcDto.getEmptyingInterval());
        Assertions.assertEquals(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "HasCeMarking")).findFirst().orElseThrow().getValue()), closedTankSvcDto.isHasCeMarking());
        Assertions.assertEquals(CaseUtil.parseDouble(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Volume")).findFirst().orElseThrow().getValue()), closedTankSvcDto.getVolume());
    }
    
    @Test
    void individualSewageDrySolution() throws ApplicationException {
        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
        Map<String, String> extraParameters = new HashMap<>();
        String prefix = "DrySolutionSvcDto_";
        TestUtil.setSewageStandardExtraParams(extraParameters, prefix);
        
        extraParameters.put(prefix + "CompostProductName", UUID.randomUUID().toString());
        extraParameters.put(prefix + "DrySolutionCompostTypeId", UUID.randomUUID().toString());
        extraParameters.put(prefix + "DrySolutionTypeId", UUID.randomUUID().toString());
        extraParameters.put(prefix + "NoContOrCompt", UUID.randomUUID().toString());
        extraParameters.put(prefix + "NoLPerContOrCompt", UUID.randomUUID().toString());
        extraParameters.put(prefix + "ToiletProductName", UUID.randomUUID().toString());
        extraParameters.put(prefix + "Volume", String.valueOf(new Random().nextDouble()));
        eCase.getFacilities().get(0).setExtraParameters(extraParameters);
        
        ecosService.postCase(eCase);
        
        ArgumentCaptor<CreateIndividualSewageFacility> argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
        verifyMinutMiljoCallsForSewageCase(argumentCaptor);
        verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);
        
        DrySolutionSvcDto drySolutionSvcDto = (DrySolutionSvcDto) argumentCaptor.getValue().getCreateIndividualSewageSvcDto().getPurificationSteps().getPurificationStepSvcDto().get(0);
        
        Assertions.assertEquals(CaseUtil.parseString(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "CompostProductName")).findFirst().orElseThrow().getValue()), drySolutionSvcDto.getCompostProductName());
        Assertions.assertEquals(CaseUtil.parseString(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "DrySolutionCompostTypeId")).findFirst().orElseThrow().getValue()), drySolutionSvcDto.getDrySolutionCompostTypeId());
        Assertions.assertEquals(CaseUtil.parseString(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "DrySolutionTypeId")).findFirst().orElseThrow().getValue()), drySolutionSvcDto.getDrySolutionTypeId());
        Assertions.assertEquals(CaseUtil.parseString(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "NoContOrCompt")).findFirst().orElseThrow().getValue()), drySolutionSvcDto.getNoContOrCompt());
        Assertions.assertEquals(CaseUtil.parseString(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "NoLPerContOrCompt")).findFirst().orElseThrow().getValue()), drySolutionSvcDto.getNoLPerContOrCompt());
        Assertions.assertEquals(CaseUtil.parseString(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "ToiletProductName")).findFirst().orElseThrow().getValue()), drySolutionSvcDto.getToiletProductName());
        Assertions.assertEquals(CaseUtil.parseDouble(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Volume")).findFirst().orElseThrow().getValue()), drySolutionSvcDto.getVolume());
    }
    
    @Test
    void individualSewageMiniSewage() throws ApplicationException {
        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
        Map<String, String> extraParameters = new HashMap<>();
        String prefix = "MiniSewageSvcDto_";
        TestUtil.setSewageStandardExtraParams(extraParameters, prefix);
        
        extraParameters.put(prefix + "CeMarking", String.valueOf(new Random().nextBoolean()));
        extraParameters.put(prefix + "Manufacturer", UUID.randomUUID().toString());
        extraParameters.put(prefix + "Model", UUID.randomUUID().toString());
        eCase.getFacilities().get(0).setExtraParameters(extraParameters);
        
        ecosService.postCase(eCase);
        
        ArgumentCaptor<CreateIndividualSewageFacility> argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
        verifyMinutMiljoCallsForSewageCase(argumentCaptor);
        verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);
        
        MiniSewageSvcDto svcDto = (MiniSewageSvcDto) argumentCaptor.getValue().getCreateIndividualSewageSvcDto().getPurificationSteps().getPurificationStepSvcDto().get(0);
        
        Assertions.assertEquals(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "CeMarking")).findFirst().orElseThrow().getValue()), svcDto.isCeMarking());
        Assertions.assertEquals(CaseUtil.parseString(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Manufacturer")).findFirst().orElseThrow().getValue()), svcDto.getManufacturer());
        Assertions.assertEquals(CaseUtil.parseString(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Model")).findFirst().orElseThrow().getValue()), svcDto.getModel());
    }
    
    @Test
    void individualSewageFilterBed() throws ApplicationException {
        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
        Map<String, String> extraParameters = new HashMap<>();
        String prefix = "FilterBedSvcDto_";
        TestUtil.setSewageStandardExtraParams(extraParameters, prefix);
        
        extraParameters.put(prefix + "Volume", String.valueOf(new Random().nextDouble()));
        eCase.getFacilities().get(0).setExtraParameters(extraParameters);
        
        ecosService.postCase(eCase);
        
        ArgumentCaptor<CreateIndividualSewageFacility> argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
        verifyMinutMiljoCallsForSewageCase(argumentCaptor);
        verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);
        
        FilterBedSvcDto svcDto = (FilterBedSvcDto) argumentCaptor.getValue().getCreateIndividualSewageSvcDto().getPurificationSteps().getPurificationStepSvcDto().get(0);
        Assertions.assertEquals(CaseUtil.parseDouble(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Volume")).findFirst().orElseThrow().getValue()), svcDto.getVolume());
    }
    
    @Test
    void individualSewageSandFilter() throws ApplicationException {
        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
        Map<String, String> extraParameters = new HashMap<>();
        String prefix = "SandFilterSvcDto_";
        TestUtil.setSewageStandardExtraParams(extraParameters, prefix);
        
        extraParameters.put(prefix + "Area", String.valueOf(new Random().nextInt()));
        extraParameters.put(prefix + "Elevated", String.valueOf(new Random().nextBoolean()));
        extraParameters.put(prefix + "WaterTight", String.valueOf(new Random().nextBoolean()));
        eCase.getFacilities().get(0).setExtraParameters(extraParameters);
        
        ecosService.postCase(eCase);
        
        ArgumentCaptor<CreateIndividualSewageFacility> argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
        verifyMinutMiljoCallsForSewageCase(argumentCaptor);
        verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);
        
        SandFilterSvcDto svcDto = (SandFilterSvcDto) argumentCaptor.getValue().getCreateIndividualSewageSvcDto().getPurificationSteps().getPurificationStepSvcDto().get(0);
        Assertions.assertEquals(CaseUtil.parseInteger(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Area")).findFirst().orElseThrow().getValue()), svcDto.getArea());
        Assertions.assertEquals(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Elevated")).findFirst().orElseThrow().getValue()), svcDto.isElevated());
        Assertions.assertEquals(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "WaterTight")).findFirst().orElseThrow().getValue()), svcDto.isWaterTight());
    }
    
    @Test
    void individualSewageBiologicalStep() throws ApplicationException {
        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
        Map<String, String> extraParameters = new HashMap<>();
        String prefix = "BiologicalStepSvcDto_";
        TestUtil.setSewageStandardExtraParams(extraParameters, prefix);
        
        extraParameters.put(prefix + "Area", String.valueOf(new Random().nextInt()));
        extraParameters.put(prefix + "BiologicalStepTypeId", UUID.randomUUID().toString());
        eCase.getFacilities().get(0).setExtraParameters(extraParameters);
        
        ecosService.postCase(eCase);
        
        ArgumentCaptor<CreateIndividualSewageFacility> argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
        verifyMinutMiljoCallsForSewageCase(argumentCaptor);
        verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);
        
        BiologicalStepSvcDto svcDto = (BiologicalStepSvcDto) argumentCaptor.getValue().getCreateIndividualSewageSvcDto().getPurificationSteps().getPurificationStepSvcDto().get(0);
        Assertions.assertEquals(CaseUtil.parseInteger(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Area")).findFirst().orElseThrow().getValue()), svcDto.getArea());
        Assertions.assertEquals(CaseUtil.parseString(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "BiologicalStepTypeId")).findFirst().orElseThrow().getValue()), svcDto.getBiologicalStepTypeId());
    }
    
    @Test
    void individualSewagePhosphorusTrap() throws ApplicationException {
        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
        Map<String, String> extraParameters = new HashMap<>();
        String prefix = "PhosphorusTrapSvcDto_";
        TestUtil.setSewageStandardExtraParams(extraParameters, prefix);
        eCase.getFacilities().get(0).setExtraParameters(extraParameters);
        
        ecosService.postCase(eCase);
        
        ArgumentCaptor<CreateIndividualSewageFacility> argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
        verifyMinutMiljoCallsForSewageCase(argumentCaptor);
        verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);
    }
    
    @Test
    void individualSewageChemicalPretreatment() throws ApplicationException {
        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
        Map<String, String> extraParameters = new HashMap<>();
        String prefix = "ChemicalPretreatmentSvcDto_";
        TestUtil.setSewageStandardExtraParams(extraParameters, prefix);
        eCase.getFacilities().get(0).setExtraParameters(extraParameters);
        
        ecosService.postCase(eCase);
        
        ArgumentCaptor<CreateIndividualSewageFacility> argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
        verifyMinutMiljoCallsForSewageCase(argumentCaptor);
        verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);
    }
    
    private void verifyMinutMiljoCallsForSewageCase(ArgumentCaptor<CreateIndividualSewageFacility> argumentCaptor) {
        verify(minutMiljoClientMock, times(6)).searchParty(any());
        verify(minutMiljoClientMock, times(1)).createOrganizationParty(any());
        verify(minutMiljoClientMock, times(2)).createPersonParty(any());
        verify(minutMiljoClientMock, times(1)).createIndividualSewageFacility(argumentCaptor.capture());
        verify(minutMiljoClientMock, times(3)).addPartyToFacility(any());
        verify(minutMiljoClientMock, times(3)).addPartyToCase(any());
        verify(minutMiljoClientV2Mock, times(1)).registerDocumentV2(any());
    }
    
    
    private void verifyStandardParams(Map<String, String> extraParameters, CreateIndividualSewageFacilitySvcDto svcDto, String prefix) {
        Assertions.assertEquals(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("OnGrantLand")).findFirst().orElseThrow().getValue()), svcDto.isOnGrantLand());
        Assertions.assertEquals(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("ProtectionLevelApprovedEnvironmentId")).findFirst().orElseThrow().getValue(), svcDto.getProtectionLevelApprovedEnvironmentId());
        Assertions.assertEquals(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("ProtectionLevelApprovedHealthId")).findFirst().orElseThrow().getValue(), svcDto.getProtectionLevelApprovedHealthId());
        Assertions.assertEquals(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("WastewaterApprovedForId")).findFirst().orElseThrow().getValue(), svcDto.getWastewaterApprovedForId());
        Assertions.assertEquals(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("WasteWaterInboundId")).findFirst().orElseThrow().getValue(), svcDto.getWasteWaterInboundId());
        
        PurificationStepSvcDto purificationStepSvcDto = svcDto.getPurificationSteps().getPurificationStepSvcDto().get(0);
        Assertions.assertEquals(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "HasOverflowAlarm")).findFirst().orElseThrow().getValue()), purificationStepSvcDto.isHasOverflowAlarm());
        Assertions.assertEquals(CaseUtil.parseLocalDateTime(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "InstallationDate")).findFirst().orElseThrow().getValue()), purificationStepSvcDto.getInstallationDate());
        Assertions.assertEquals(CaseUtil.parseInteger(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "LifeTime")).findFirst().orElseThrow().getValue()), purificationStepSvcDto.getLifeTime());
        Assertions.assertEquals(CaseUtil.parseInteger(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "PersonCapacity")).findFirst().orElseThrow().getValue()), purificationStepSvcDto.getPersonCapacity());
        Assertions.assertEquals(Constants.ECOS_FACILITY_STATUS_ID_ANMALD_ANSOKT, purificationStepSvcDto.getPurificationStepFacilityStatusId());
        Assertions.assertEquals(CaseUtil.parseInteger(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "StepNumber")).findFirst().orElseThrow().getValue()), purificationStepSvcDto.getStepNumber());
    }
    
    @Test
    void testMissingFacilityAddress() throws ApplicationException {
        
        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.REGISTRERING_AV_LIVSMEDEL, AttachmentCategory.ANMALAN_LIVSMEDELSANLAGGNING);
        eCase.getFacilities().get(0).setAddress(null);
        
        var result = ecosService.postCase(eCase);
        
        verify(minutMiljoClientMock, times(6)).searchParty(any());
        verify(minutMiljoClientMock, times(1)).createOrganizationParty(any());
        verify(minutMiljoClientMock, times(2)).createPersonParty(any());
        verify(minutMiljoClientMock, times(3)).addPartyToCase(any());
        verify(minutMiljoClientV2Mock, times(1)).registerDocumentV2(any());
        ArgumentCaptor<CreateOccurrenceOnCase> createOccurrenceOnCaseArgumentCaptor = ArgumentCaptor.forClass(CreateOccurrenceOnCase.class);
        verify(minutMiljoClientMock, times(1)).createOccurrenceOnCase(createOccurrenceOnCaseArgumentCaptor.capture());
        assertEquals(result.getCaseId(), createOccurrenceOnCaseArgumentCaptor.getValue().getCreateOccurrenceOnCaseSvcDto().getCaseId());
        assertNotNull(createOccurrenceOnCaseArgumentCaptor.getValue().getCreateOccurrenceOnCaseSvcDto().getOccurrenceDate());
        assertEquals(Constants.ECOS_OCCURRENCE_TYPE_ID_INFO_FRAN_ETJANST, createOccurrenceOnCaseArgumentCaptor.getValue().getCreateOccurrenceOnCaseSvcDto().getOccurrenceTypeId());
        assertEquals(Constants.ECOS_OCCURENCE_TEXT_MOBIL_ANLAGGNING, createOccurrenceOnCaseArgumentCaptor.getValue().getCreateOccurrenceOnCaseSvcDto().getNote());
        
        // If facility doesn't have any address, we should not register any facility and therefore not add any stakeholder to facility.
        // This results in manual handling for admin.
        verify(minutMiljoClientMock, times(0)).createFoodFacility(any());
        verify(minutMiljoClientMock, times(0)).addPartyToFacility(any());
        
        verify(caseMappingServiceMock, times(1)).postCaseMapping(any(CaseMapping.class));
    }
    
    @Test
    void testGetStatus() {
        String caseId = MessageFormat.format("MK-2022-{0}", new Random().nextInt(100000));
        String externalCaseID = UUID.randomUUID().toString();
        
        CaseMapping caseMapping = new CaseMapping();
        caseMapping.setExternalCaseId(externalCaseID);
        caseMapping.setCaseId(caseId);
        caseMapping.setCaseType(CaseType.REGISTRERING_AV_LIVSMEDEL);
        caseMapping.setSystem(SystemType.ECOS);
        caseMapping.setServiceName(RandomStringUtils.random(10, true, false));
        caseMapping.setTimestamp(LocalDateTime.now());
        doReturn(List.of(caseMapping)).when(caseMappingServiceMock).getCaseMapping(externalCaseID, caseId);
        
        GetCaseResponse getCaseResponse = new GetCaseResponse();
        CaseSvcDto caseSvcDto = new CaseSvcDto();
        caseSvcDto.setCaseNumber(caseId);
        caseSvcDto.setCaseId(UUID.randomUUID().toString());
        ArrayOfOccurrenceListItemSvcDto arrayOfOccurrenceListItemSvcDto = new ArrayOfOccurrenceListItemSvcDto();
        OccurrenceListItemSvcDto occurrenceListItemSvcDto_1 = new OccurrenceListItemSvcDto();
        occurrenceListItemSvcDto_1.setOccurrenceDate(LocalDateTime.now().minusDays(5));
        occurrenceListItemSvcDto_1.setOccurrenceDescription(RandomStringUtils.random(5, true, false));
        OccurrenceListItemSvcDto occurrenceListItemSvcDto_2 = new OccurrenceListItemSvcDto();
        occurrenceListItemSvcDto_2.setOccurrenceDate(LocalDateTime.now().minusDays(1));
        occurrenceListItemSvcDto_2.setOccurrenceDescription(RandomStringUtils.random(5, true, false));
        OccurrenceListItemSvcDto occurrenceListItemSvcDto_3 = new OccurrenceListItemSvcDto();
        occurrenceListItemSvcDto_3.setOccurrenceDate(LocalDateTime.now().minusDays(3));
        occurrenceListItemSvcDto_3.setOccurrenceDescription(RandomStringUtils.random(5, true, false));
        arrayOfOccurrenceListItemSvcDto.getOccurrenceListItemSvcDto().addAll(List.of(occurrenceListItemSvcDto_1, occurrenceListItemSvcDto_2, occurrenceListItemSvcDto_3));
        caseSvcDto.setOccurrences(arrayOfOccurrenceListItemSvcDto);
        getCaseResponse.setGetCaseResult(caseSvcDto);
        doReturn(getCaseResponse).when(minutMiljoClientMock).getCase(any());
        
        var result = ecosService.getStatus(caseId, externalCaseID);
        
        assertEquals(caseId, result.getCaseId());
        assertEquals(externalCaseID, result.getExternalCaseId());
        assertEquals(caseMapping.getCaseType(), result.getCaseType());
        assertEquals(caseMapping.getSystem(), result.getSystem());
        assertEquals(caseMapping.getServiceName(), result.getServiceName());
        assertEquals(occurrenceListItemSvcDto_2.getOccurrenceDescription(), result.getStatus());
        assertEquals(occurrenceListItemSvcDto_2.getOccurrenceDate(), result.getTimestamp());
        
        ArgumentCaptor<GetCase> getCaseArgumentCaptor = ArgumentCaptor.forClass(GetCase.class);
        verify(minutMiljoClientMock, times(1)).getCase(getCaseArgumentCaptor.capture());
        assertEquals(caseId, getCaseArgumentCaptor.getValue().getCaseId());
    }
    
    @Test
    void testGetStatusNotFound() {
        String caseId = MessageFormat.format("MK-2022-{0}", new Random().nextInt(100000));
        String externalCaseID = UUID.randomUUID().toString();
        
        GetCaseResponse getCaseResponse = new GetCaseResponse();
        CaseSvcDto caseSvcDto = new CaseSvcDto();
        caseSvcDto.setCaseNumber(caseId);
        caseSvcDto.setCaseId(UUID.randomUUID().toString());
        getCaseResponse.setGetCaseResult(caseSvcDto);
        doReturn(getCaseResponse).when(minutMiljoClientMock).getCase(any());
        
        var problem = assertThrows(ThrowableProblem.class, () -> ecosService.getStatus(caseId, externalCaseID));
        assertEquals(Status.NOT_FOUND, problem.getStatus());
        assertEquals(Constants.ERR_MSG_STATUS_NOT_FOUND, problem.getDetail());
    }
    
    @Test
    void testGetStatusByOrgnr() {
        String caseNumber = MessageFormat.format("MK-2022-{0}", new Random().nextInt(100000));
        String caseId = UUID.randomUUID().toString();
        String externalCaseID = UUID.randomUUID().toString();
        String orgnr = TestUtil.generateRandomOrganizationNumber();
        
        // Mock minutMiljoClient.searchParty
        SearchPartyResponse searchPartyResponse = new SearchPartyResponse();
        ArrayOfPartySvcDto arrayOfPartySvcDto = new ArrayOfPartySvcDto();
        PartySvcDto partySvcDto_1 = new PartySvcDto();
        partySvcDto_1.setId(UUID.randomUUID().toString());
        PartySvcDto partySvcDto_2 = new PartySvcDto();
        partySvcDto_2.setId(UUID.randomUUID().toString());
        arrayOfPartySvcDto.getPartySvcDto().addAll(List.of(partySvcDto_1, partySvcDto_2));
        searchPartyResponse.setSearchPartyResult(arrayOfPartySvcDto);
        doReturn(searchPartyResponse).when(minutMiljoClientMock).searchParty(any());
        
        // Mock minutMiljoClient.searchCase
        SearchCaseResponse searchCaseResponse = new SearchCaseResponse();
        ArrayOfSearchCaseResultSvcDto arrayOfSearchCaseResultSvcDto = new ArrayOfSearchCaseResultSvcDto();
        SearchCaseResultSvcDto searchCaseResultSvcDto_1 = new SearchCaseResultSvcDto();
        searchCaseResultSvcDto_1.setCaseId(caseId);
        searchCaseResultSvcDto_1.setCaseNumber(caseNumber);
        SearchCaseResultSvcDto searchCaseResultSvcDto_2 = new SearchCaseResultSvcDto();
        searchCaseResultSvcDto_2.setCaseId(caseId);
        searchCaseResultSvcDto_2.setCaseNumber(caseNumber);
        arrayOfSearchCaseResultSvcDto.getSearchCaseResultSvcDto().addAll(List.of(searchCaseResultSvcDto_1, searchCaseResultSvcDto_2));
        searchCaseResponse.setSearchCaseResult(arrayOfSearchCaseResultSvcDto);
        doReturn(searchCaseResponse).when(minutMiljoClientMock).searchCase(any());
        
        // Mock caseMappingService.getCaseMapping
        CaseMapping caseMapping = new CaseMapping();
        caseMapping.setExternalCaseId(externalCaseID);
        caseMapping.setCaseId(caseId);
        caseMapping.setCaseType(CaseType.REGISTRERING_AV_LIVSMEDEL);
        caseMapping.setSystem(SystemType.ECOS);
        caseMapping.setServiceName(RandomStringUtils.random(10, true, false));
        caseMapping.setTimestamp(LocalDateTime.now());
        doReturn(List.of(caseMapping)).when(caseMappingServiceMock).getCaseMapping(any(), any());
        
        // Mock minutMiljoClient.getCase
        GetCaseResponse getCaseResponse = new GetCaseResponse();
        CaseSvcDto caseSvcDto = new CaseSvcDto();
        caseSvcDto.setCaseNumber(caseNumber);
        caseSvcDto.setCaseId(caseId);
        ArrayOfOccurrenceListItemSvcDto arrayOfOccurrenceListItemSvcDto = new ArrayOfOccurrenceListItemSvcDto();
        OccurrenceListItemSvcDto occurrenceListItemSvcDto_1 = new OccurrenceListItemSvcDto();
        occurrenceListItemSvcDto_1.setOccurrenceDate(LocalDateTime.now().minusDays(5));
        occurrenceListItemSvcDto_1.setOccurrenceDescription(RandomStringUtils.random(5, true, false));
        OccurrenceListItemSvcDto occurrenceListItemSvcDto_2 = new OccurrenceListItemSvcDto();
        occurrenceListItemSvcDto_2.setOccurrenceDate(LocalDateTime.now().minusDays(1));
        occurrenceListItemSvcDto_2.setOccurrenceDescription(RandomStringUtils.random(5, true, false));
        OccurrenceListItemSvcDto occurrenceListItemSvcDto_3 = new OccurrenceListItemSvcDto();
        occurrenceListItemSvcDto_3.setOccurrenceDate(LocalDateTime.now().minusDays(3));
        occurrenceListItemSvcDto_3.setOccurrenceDescription(RandomStringUtils.random(5, true, false));
        arrayOfOccurrenceListItemSvcDto.getOccurrenceListItemSvcDto().addAll(List.of(occurrenceListItemSvcDto_1, occurrenceListItemSvcDto_2, occurrenceListItemSvcDto_3));
        caseSvcDto.setOccurrences(arrayOfOccurrenceListItemSvcDto);
        getCaseResponse.setGetCaseResult(caseSvcDto);
        doReturn(getCaseResponse).when(minutMiljoClientMock).getCase(any());
        
        var result = ecosService.getEcosStatusByOrgNr(orgnr);
        
        assertEquals(2, result.size());
        result.forEach(status -> {
            assertEquals(caseNumber, status.getCaseId());
            assertEquals(externalCaseID, status.getExternalCaseId());
            assertEquals(caseMapping.getCaseType(), status.getCaseType());
            assertEquals(caseMapping.getSystem(), status.getSystem());
            assertEquals(caseMapping.getServiceName(), status.getServiceName());
            assertEquals(occurrenceListItemSvcDto_2.getOccurrenceDescription(), status.getStatus());
            assertEquals(occurrenceListItemSvcDto_2.getOccurrenceDate(), status.getTimestamp());
        });
        
        ArgumentCaptor<GetCase> getCaseArgumentCaptor = ArgumentCaptor.forClass(GetCase.class);
        verify(minutMiljoClientMock, times(2)).getCase(getCaseArgumentCaptor.capture());
    }
    
    @Test
    void testAddDocumentsToCase() {
        String caseId = UUID.randomUUID().toString();
        AttachmentDTO attachmentDTO = TestUtil.createAttachmentDTO(AttachmentCategory.ANMALAN_LIVSMEDELSANLAGGNING);
        
        ecosService.addDocumentsToCase(caseId, List.of(attachmentDTO));
        
        ArgumentCaptor<AddDocumentsToCase> addDocumentsToCaseArgumentCaptor = ArgumentCaptor.forClass(AddDocumentsToCase.class);
        verify(minutMiljoClientMock, times(1)).addDocumentsToCase(addDocumentsToCaseArgumentCaptor.capture());
        AddDocumentsToCaseSvcDto addDocumentsToCaseSvcDto = addDocumentsToCaseArgumentCaptor.getValue().getAddDocumentToCaseSvcDto();
        assertEquals(caseId, addDocumentsToCaseSvcDto.getCaseId());
        assertEquals(Constants.ECOS_OCCURRENCE_TYPE_ID_KOMPLETTERING, addDocumentsToCaseSvcDto.getOccurrenceTypeId());
        assertEquals(Constants.ECOS_DOCUMENT_STATUS_INKOMMEN, addDocumentsToCaseSvcDto.getDocumentStatusId());
        DocumentSvcDto documentSvcDto = addDocumentsToCaseSvcDto.getDocuments().getDocumentSvcDto().get(0);
        assertEquals(attachmentDTO.getName() + attachmentDTO.getExtension().toLowerCase(), documentSvcDto.getFilename());
        assertEquals(attachmentDTO.getMimeType().toLowerCase(), documentSvcDto.getContentType());
        assertNotNull(documentSvcDto.getData());
        assertEquals(attachmentDTO.getCategory().getDescription(), documentSvcDto.getDocumentTypeId());
        assertEquals(attachmentDTO.getNote(), documentSvcDto.getNote());
    }
    
    private void assertContactInfo(ContactInfoSvcDto contactInfoSvcDto) {
        assertNotNull(contactInfoSvcDto.getTitle());
        contactInfoSvcDto.getContactDetails().getContactInfoItemSvcDto().forEach(contactInfoItemSvcDto -> {
            assertNotNull(contactInfoItemSvcDto.getContactDetailTypeId());
            assertNotNull(contactInfoItemSvcDto.getContactPathId());
            assertNotNull(contactInfoItemSvcDto.getValue());
        });
    }
    
    private void assertAddress(List<AddressDTO> addresses, ArrayOfPartyAddressSvcDto minutAddresses) {
        assertEquals(addresses.size(), minutAddresses.getPartyAddressSvcDto().size());
        for (int i = 0; i < addresses.size(); i++) {
            AddressDTO address = addresses.get(i);
            PartyAddressSvcDto minutAddress = minutAddresses.getPartyAddressSvcDto().get(i);
            assertEquals(address.getCareOf(), minutAddress.getCareOfName());
            assertEquals(address.getStreet(), minutAddress.getStreetName());
            assertEquals(address.getHouseNumber(), minutAddress.getStreetNumber());
            assertEquals(address.getPostalCode(), minutAddress.getPostCode());
            assertEquals(address.getCity(), minutAddress.getPostalArea());
            assertEquals(address.getCountry(), minutAddress.getCountry());
            assertEquals(address.getAddressCategories().size(), minutAddress.getAddressTypes().getAddressTypeSvcDto().size());
        }
    }
}
