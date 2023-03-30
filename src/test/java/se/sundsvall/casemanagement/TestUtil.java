package se.sundsvall.casemanagement;

import arendeexport.SaveNewArendeResponse;
import arendeexport.SaveNewArendeResponse2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import minutmiljo.CreateFoodFacilityResponse;
import minutmiljo.CreateHealthProtectionFacilityResponse;
import minutmiljo.CreateHeatPumpFacilityResponse;
import minutmiljo.CreateIndividualSewageFacilityResponse;
import minutmiljo.CreateOrganizationPartyResponse;
import minutmiljo.CreatePersonPartyResponse;
import minutmiljo.SearchPartyResponse;
import minutmiljoV2.RegisterDocumentCaseResultSvcDto;
import minutmiljoV2.RegisterDocumentResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.api.model.CoordinatesDTO;
import se.sundsvall.casemanagement.api.model.EnvironmentalCaseDTO;
import se.sundsvall.casemanagement.api.model.EnvironmentalFacilityDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
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
import se.sundsvall.casemanagement.integration.byggr.ArendeExportClient;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.rest.fb.FbClient;
import se.sundsvall.casemanagement.integration.rest.fb.model.DataItem;
import se.sundsvall.casemanagement.integration.rest.fb.model.FbPropertyInfo;
import se.sundsvall.casemanagement.integration.rest.fb.model.GruppItem;
import se.sundsvall.casemanagement.integration.rest.fb.model.ResponseDto;
import se.sundsvall.casemanagement.integration.ecos.MinutMiljoClient;
import se.sundsvall.casemanagement.integration.ecos.MinutMiljoClientV2;
import se.sundsvall.casemanagement.service.CitizenMappingService;
import se.sundsvall.casemanagement.service.FbService;
import se.sundsvall.casemanagement.util.Constants;
import se.sundsvall.casemanagement.testutils.TestConstants;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.lenient;

@Component
public class TestUtil {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
            .registerModule(new JavaTimeModule());
    
    public static void mockFbPropertyOwners(FbClient fbClientMock, List<StakeholderDTO> propertyOwners) {
        ResponseDto lagfarenAgareResponse = new ResponseDto();
        DataItem lagfarenAgareDataItem = new DataItem();
        List<GruppItem> lagfarenAgareGruppItemList = new ArrayList<>();

        ResponseDto agareInfoResponse = new ResponseDto();
        List<DataItem> agareInfoDataItemList = new ArrayList<>();

        propertyOwners.forEach(propertyOwner -> {

            GruppItem gruppItem = new GruppItem();
            DataItem dataItem = new DataItem();

            if (propertyOwner instanceof PersonDTO personDTO) {

                gruppItem.setIdentitetsnummer(personDTO.getPersonalNumber());
                gruppItem.setUuid(UUID.randomUUID().toString());

                dataItem.setGallandeFornamn(personDTO.getFirstName());
                dataItem.setGallandeEfternamn(personDTO.getLastName());
                dataItem.setIdentitetsnummer(personDTO.getPersonalNumber());
                dataItem.setJuridiskForm(Constants.FB_JURIDISK_FORM_PRIVATPERSON);

            } else if (propertyOwner instanceof OrganizationDTO organizationDTO) {

                gruppItem.setIdentitetsnummer(organizationDTO.getOrganizationNumber());
                gruppItem.setUuid(UUID.randomUUID().toString());

                dataItem.setGallandeOrganisationsnamn(organizationDTO.getOrganizationName());
                dataItem.setIdentitetsnummer(organizationDTO.getOrganizationNumber());
                dataItem.setJuridiskForm("16");
            }

            lagfarenAgareGruppItemList.add(gruppItem);
            agareInfoDataItemList.add(dataItem);
        });

        lagfarenAgareDataItem.setGrupp(lagfarenAgareGruppItemList);
        lagfarenAgareResponse.setData(List.of(lagfarenAgareDataItem));
        Mockito.doReturn(lagfarenAgareResponse).when(fbClientMock).getPropertyOwnerByFnr(any(), any(), any(), any());

        agareInfoResponse.setData(agareInfoDataItemList);
        Mockito.doReturn(agareInfoResponse).when(fbClientMock).getPropertyOwnerInfoByUuid(any(), any(), any(), any());
    }
    
    @Test
    void doshit() throws JsonProcessingException {
       
       var test = createEnvironmentalCase(CaseType.REGISTRERING_AV_LIVSMEDEL,
            AttachmentCategory.ANMALAN_LIVSMEDELSANLAGGNING);
        ObjectWriter ow =
            new ObjectMapper().registerModule(new JavaTimeModule()).writer().withDefaultPrettyPrinter();
        var result = ow.writeValueAsString(test);
        System.out.println(result);
    }

    public static EnvironmentalCaseDTO createEnvironmentalCase(CaseType caseType, AttachmentCategory attachmentCategory) {
        EnvironmentalCaseDTO eCase = new EnvironmentalCaseDTO();
        List<AttachmentDTO> aList = new ArrayList<>();
        aList.add(createAttachmentDTO(attachmentCategory));
        eCase.setAttachments(aList);
        eCase.setStartDate(LocalDate.now());

        List<StakeholderDTO> sList = new ArrayList<>();
        sList.add(createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT, StakeholderRole.OPERATOR)));
        sList.add(createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.CONTACT_PERSON)));
        sList.add(createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.CONTACT_PERSON)));

        eCase.setStakeholders(sList);

        eCase.setFacilities(List.of(createEnvironmentalFacilityDTO(caseType)));

        eCase.setCaseType(caseType);
        eCase.setCaseTitleAddition("Some case title addition");
        eCase.setDescription(RandomStringUtils.random(10, true, false));
        eCase.setStartDate(LocalDate.now().plusDays(10));
        eCase.setEndDate(LocalDate.now().plusDays(365));
        eCase.setExternalCaseId(String.valueOf(new Random().nextLong()));
        eCase.setExtraParameters(createExtraParameters());

        return eCase;
    }

    public static AttachmentDTO createAttachmentDTO(AttachmentCategory attachmentCategory) {
        AttachmentDTO attachmentDTO = new AttachmentDTO();
        attachmentDTO.setCategory(attachmentCategory);
        attachmentDTO.setExtension(".pdf");
        attachmentDTO.setMimeType(TestConstants.MIMETYPE_PDF);
        attachmentDTO.setName(RandomStringUtils.random(10, true, false));
        attachmentDTO.setNote(RandomStringUtils.random(10, true, false));
        attachmentDTO.setFile(TestConstants.BASE64_STRING);
        return attachmentDTO;
    }

    public static EnvironmentalFacilityDTO createEnvironmentalFacilityDTO(CaseType caseType) {
        EnvironmentalFacilityDTO facility = new EnvironmentalFacilityDTO();
        facility.setFacilityCollectionName(RandomStringUtils.random(10, true, false));
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressCategories(List.of(AddressCategory.VISITING_ADDRESS));
        addressDTO.setPropertyDesignation(TestConstants.PROPERTY_DESIGNATION_BALDER);
        facility.setAddress(addressDTO);
        facility.setDescription(RandomStringUtils.random(10, true, false));
        facility.setExtraParameters(switch (caseType) {
            case ANSOKAN_TILLSTAND_VARMEPUMP, ANMALAN_INSTALLATION_VARMEPUMP -> getHeatPumpExtraParams();
            default -> createExtraParameters();
        });
        return facility;
    }

    public static PlanningPermissionCaseDTO createPlanningPermissionCaseDTO(CaseType caseType, AttachmentCategory attachmentCategory) {
        PlanningPermissionCaseDTO pCase = new PlanningPermissionCaseDTO();
        List<AttachmentDTO> aList = new ArrayList<>();
        aList.add(createAttachmentDTO(attachmentCategory));
        pCase.setAttachments(aList);

        List<StakeholderDTO> sList = new ArrayList<>();
        sList.add(createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT, StakeholderRole.PAYMENT_PERSON)));
        sList.add(createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.CONTACT_PERSON)));
        sList.add(createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.CONTACT_PERSON)));

        pCase.setStakeholders(sList);

        pCase.setFacilities(List.of(createPlanningPermissionFacilityDTO(false)));

        pCase.setCaseType(caseType);
        pCase.setCaseTitleAddition(RandomStringUtils.random(10, true, false));
        pCase.setDescription(RandomStringUtils.random(10, true, false));
        pCase.setExternalCaseId(String.valueOf(new Random().nextLong()));
        pCase.setDiaryNumber(RandomStringUtils.random(5));
        pCase.setExtraParameters(createExtraParameters());

        return pCase;
    }

    public static PlanningPermissionFacilityDTO createPlanningPermissionFacilityDTO(boolean mainFacility) {
        PlanningPermissionFacilityDTO facility = new PlanningPermissionFacilityDTO();
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressCategories(List.of(AddressCategory.VISITING_ADDRESS));
        addressDTO.setPropertyDesignation(TestConstants.PROPERTY_DESIGNATION_BALDER);
        facility.setAddress(addressDTO);
        facility.setFacilityType(FacilityType.ONE_FAMILY_HOUSE);
        facility.setDescription(RandomStringUtils.random(10, true, false));
        facility.setExtraParameters(createExtraParameters());
        facility.setMainFacility(mainFacility);
        return facility;
    }
    
    public static PlanningPermissionFacilityDTO createAttefallFacilityDTO(boolean mainFacility) {
        PlanningPermissionFacilityDTO facility = new PlanningPermissionFacilityDTO();
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressCategories(List.of(AddressCategory.VISITING_ADDRESS));
        addressDTO.setPropertyDesignation(TestConstants.PROPERTY_DESIGNATION_BALDER);
        facility.setAddress(addressDTO);
        facility.setFacilityType(FacilityType.ANCILLARY_BUILDING);
        facility.setDescription(RandomStringUtils.random(10, true, false));
        facility.setExtraParameters(createExtraParameters());
        facility.setMainFacility(mainFacility);
        return facility;
    }

    public static OtherCaseDTO createOtherCase(CaseType caseType, AttachmentCategory attachmentCategory) {
        OtherCaseDTO oCase = new OtherCaseDTO();
        List<AttachmentDTO> aList = new ArrayList<>();
        aList.add(createAttachmentDTO(attachmentCategory));
        oCase.setAttachments(aList);

        List<StakeholderDTO> sList = new ArrayList<>();
        sList.add(createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT, StakeholderRole.OPERATOR)));
        sList.add(createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.CONTACT_PERSON)));
        sList.add(createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.CONTACT_PERSON)));

        oCase.setStakeholders(sList);

        oCase.setCaseType(caseType);
        oCase.setCaseTitleAddition(RandomStringUtils.random(10, true, false));
        oCase.setDescription(RandomStringUtils.random(10, true, false));
        oCase.setExternalCaseId(String.valueOf(new Random().nextLong()));
        oCase.setExtraParameters(createExtraParameters());

        return oCase;
    }

    public static StakeholderDTO createStakeholder(StakeholderType stakeholderType, List<StakeholderRole> stakeholderRoles) {
        if (stakeholderType.equals(StakeholderType.PERSON)) {
            PersonDTO personDTO = new PersonDTO();
            personDTO.setType(StakeholderType.PERSON);
            personDTO.setPersonId(UUID.randomUUID().toString());
            personDTO.setPersonalNumber(generateRandomPersonalNumber());
            personDTO.setFirstName(RandomStringUtils.random(10, true, false));
            personDTO.setLastName(RandomStringUtils.random(10, true, false));
            personDTO.setRoles(stakeholderRoles);
            personDTO.setEmailAddress(MessageFormat.format("{0}@{1}.com", RandomStringUtils.random(10, true, false), RandomStringUtils.random(5, true, false)));
            personDTO.setCellphoneNumber(RandomStringUtils.random(10, true, false));
            personDTO.setPhoneNumber(RandomStringUtils.random(10, true, false));
            personDTO.setAddresses(List.of(createAddressDTO(List.of(AddressCategory.VISITING_ADDRESS, AddressCategory.POSTAL_ADDRESS))));
            personDTO.setExtraParameters(createExtraParameters());

            return personDTO;
        } else {
            OrganizationDTO organizationDTO = new OrganizationDTO();
            organizationDTO.setType(StakeholderType.ORGANIZATION);
            organizationDTO.setOrganizationNumber(generateRandomOrganizationNumber());
            organizationDTO.setOrganizationName(RandomStringUtils.random(10, true, false));
            organizationDTO.setRoles(stakeholderRoles);
            organizationDTO.setEmailAddress(MessageFormat.format("{0}@{1}.com", RandomStringUtils.random(10, true, false), RandomStringUtils.random(5, true, false)));
            organizationDTO.setCellphoneNumber(RandomStringUtils.random(10, true, false));
            organizationDTO.setPhoneNumber(RandomStringUtils.random(10, true, false));
            organizationDTO.setAddresses(List.of(createAddressDTO(List.of(AddressCategory.VISITING_ADDRESS, AddressCategory.INVOICE_ADDRESS, AddressCategory.POSTAL_ADDRESS))));
            organizationDTO.setAuthorizedSignatory(RandomStringUtils.random(10, true, false));
            organizationDTO.setExtraParameters(createExtraParameters());

            return organizationDTO;
        }
    }

    public static AddressDTO createAddressDTO(List<AddressCategory> addressCategories) {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressCategories(addressCategories);
        addressDTO.setCity(RandomStringUtils.random(10, true, false));
        addressDTO.setCountry(Constants.SWEDEN);
        addressDTO.setPropertyDesignation(TestConstants.PROPERTY_DESIGNATION_FILLA);
        addressDTO.setStreet(RandomStringUtils.random(10, true, false));
        addressDTO.setHouseNumber(RandomStringUtils.random(10, true, true));
        addressDTO.setCareOf(RandomStringUtils.random(10, true, false));
        addressDTO.setPostalCode(RandomStringUtils.random(10, false, true));
        addressDTO.setAppartmentNumber(RandomStringUtils.random(10, true, true));
        addressDTO.setAttention(RandomStringUtils.random(10, true, false));
        addressDTO.setInvoiceMarking(RandomStringUtils.random(10, true, true));
        addressDTO.setIsZoningPlanArea(false);
        CoordinatesDTO coordinatesDTO = new CoordinatesDTO();
        coordinatesDTO.setLatitude(new Random().nextDouble());
        coordinatesDTO.setLongitude(new Random().nextDouble());
        addressDTO.setLocation(coordinatesDTO);
        addressDTO.setExtraParameters(createExtraParameters());

        return addressDTO;
    }

    public static String generateRandomOrganizationNumber() {
        return (new Random().nextInt(999999 - 111111) + 111111) + "-" + (new Random().nextInt(9999 - 1111) + 1111);
    }

    public static String generateRandomPersonalNumber() {
        return "199901" + new Random().nextInt(3) + (new Random().nextInt(9) + 1) + (new Random().nextInt(9999 - 1111) + 1111);
    }




    public static void standardMockMinutMiljo(MinutMiljoClient mock, MinutMiljoClientV2 mockV2) {
        lenient().doReturn(new SearchPartyResponse()).when(mock).searchParty(any());

        CreatePersonPartyResponse createPersonPartyResponse = new CreatePersonPartyResponse();
        createPersonPartyResponse.setCreatePersonPartyResult(UUID.randomUUID().toString());
        lenient().doReturn(createPersonPartyResponse).when(mock).createPersonParty(any());

        CreateOrganizationPartyResponse createOrganizationPartyResponse = new CreateOrganizationPartyResponse();
        createOrganizationPartyResponse.setCreateOrganizationPartyResult(UUID.randomUUID().toString());
        lenient().doReturn(createOrganizationPartyResponse).when(mock).createOrganizationParty(any());

        RegisterDocumentResponse registerDocumentResponse = new RegisterDocumentResponse();
        RegisterDocumentCaseResultSvcDto registerDocumentCaseResultSvcDto = new RegisterDocumentCaseResultSvcDto();
        registerDocumentCaseResultSvcDto.setCaseId(TestConstants.ECOS_CASE_ID);
        registerDocumentCaseResultSvcDto.setCaseNumber(TestConstants.ECOS_CASE_NUMBER);
        registerDocumentResponse.setRegisterDocumentResult(registerDocumentCaseResultSvcDto);
        lenient().doReturn(registerDocumentResponse).when(mockV2).registerDocumentV2(any());

        CreateFoodFacilityResponse createFoodFacilityResponse = new CreateFoodFacilityResponse();
        createFoodFacilityResponse.setCreateFoodFacilityResult(UUID.randomUUID().toString());
        lenient().doReturn(createFoodFacilityResponse).when(mock).createFoodFacility(any());

        CreateHeatPumpFacilityResponse createHeatPumpFacilityResponse = new CreateHeatPumpFacilityResponse();
        createHeatPumpFacilityResponse.setCreateHeatPumpFacilityResult(UUID.randomUUID().toString());
        lenient().doReturn(createHeatPumpFacilityResponse).when(mock).createHeatPumpFacility(any());

        CreateIndividualSewageFacilityResponse createIndividualSewageFacilityResponse = new CreateIndividualSewageFacilityResponse();
        createIndividualSewageFacilityResponse.setCreateIndividualSewageFacilityResult(UUID.randomUUID().toString());
        lenient().doReturn(createIndividualSewageFacilityResponse).when(mock).createIndividualSewageFacility(any());

        CreateHealthProtectionFacilityResponse createHealthProtectionFacilityResponse = new CreateHealthProtectionFacilityResponse();
        createHealthProtectionFacilityResponse.setCreateHealthProtectionFacilityResult(UUID.randomUUID().toString());
        lenient().doReturn(createHealthProtectionFacilityResponse).when(mock).createHealthProtectionFacility(any());
    }

    public static void standardMockArendeExport(ArendeExportClient mock) {
        SaveNewArendeResponse saveNewArendeResponse = new SaveNewArendeResponse();
        SaveNewArendeResponse2 saveNewArendeResult = new SaveNewArendeResponse2();
        saveNewArendeResult.setDnr(TestConstants.BYGG_CASE_ID);
        saveNewArendeResponse.setSaveNewArendeResult(saveNewArendeResult);

        lenient().doReturn(saveNewArendeResponse).when(mock).saveNewArende(any());
    }

    public static void standardMockFb(FbService fbMock) {
        FbPropertyInfo fbPropertyInfo = new FbPropertyInfo();
        fbPropertyInfo.setFnr(TestConstants.FNR);
        fbPropertyInfo.setAdressplatsId(TestConstants.ADRESSPLATS_ID);

        lenient().doReturn(fbPropertyInfo).when(fbMock).getPropertyInfoByPropertyDesignation(anyString());
    }

    public static void standardMockCitizenMapping(CitizenMappingService mock) {
        lenient().doReturn(generateRandomPersonalNumber()).when(mock).getPersonalNumber(anyString());
    }

    public static void setSewageStandardExtraParams(Map<String, String> extraParameters, String prefix) {
        extraParameters.put("OnGrantLand", String.valueOf(new Random().nextBoolean()));
        extraParameters.put("ProtectionLevelApprovedEnvironmentId", UUID.randomUUID().toString());
        extraParameters.put("ProtectionLevelApprovedHealthId", UUID.randomUUID().toString());
        extraParameters.put("WastewaterApprovedForId", UUID.randomUUID().toString());
        extraParameters.put("WasteWaterInboundId", UUID.randomUUID().toString());
        extraParameters.put(prefix + "StepNumber", String.valueOf(new Random().nextInt()));
        extraParameters.put(prefix + "HasOverflowAlarm", String.valueOf(new Random().nextBoolean()));
        extraParameters.put(prefix + "LifeTime", String.valueOf(new Random().nextInt()));
        extraParameters.put(prefix + "InstallationDate", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        extraParameters.put(prefix + "PersonCapacity", String.valueOf(new Random().nextInt()));
    }

    public static Map<String, String> getSandfilterExtraParams() {
        Map<String, String> extraParameters = new HashMap<>();
        String prefix = "SandFilterSvcDto_";
        TestUtil.setSewageStandardExtraParams(extraParameters, prefix);

        extraParameters.put(prefix + "Area", String.valueOf(new Random().nextInt()));
        extraParameters.put(prefix + "Elevated", String.valueOf(new Random().nextBoolean()));
        extraParameters.put(prefix + "WaterTight", String.valueOf(new Random().nextBoolean()));

        return extraParameters;
    }

    public static Map<String, String> getHeatPumpExtraParams() {
        Map<String, String> extraParameters = new HashMap<>();
        String prefix = "CreateSoilHeatingFacilitySvcDto_";
        TestUtil.setSewageStandardExtraParams(extraParameters, prefix);

        extraParameters.put(prefix + "Manufacturer", RandomStringUtils.random(10, true, false));
        extraParameters.put(prefix + "Model", RandomStringUtils.random(10, true, false));
        extraParameters.put(prefix + "PowerConsumption", String.valueOf(new Random().nextDouble()));
        extraParameters.put(prefix + "PowerOutput", String.valueOf(new Random().nextDouble()));
        extraParameters.put(prefix + "Capacity", String.valueOf(new Random().nextDouble()));
        extraParameters.put(prefix + "HeatTransferFluidId", UUID.randomUUID().toString());

        return extraParameters;
    }

    public static AttachmentDTO createAttachment(AttachmentCategory attachmentCategory) {
        AttachmentDTO attachmentDTO = new AttachmentDTO();
        attachmentDTO.setCategory(attachmentCategory);
        attachmentDTO.setName("Some attachment name.pdf");
        attachmentDTO.setNote("Some attachment note");
        attachmentDTO.setExtension(".pdf");
        attachmentDTO.setMimeType("application/pdf");
        attachmentDTO.setFile(TestConstants.BASE64_STRING);
        attachmentDTO.setExtraParameters(createExtraParameters());

        return attachmentDTO;
    }

    public static Map<String, String> createExtraParameters() {
        Map<String, String> extraParams = new HashMap<>();
        extraParams.put(RandomStringUtils.random(10, true, false), RandomStringUtils.random(20, true, false));
        extraParams.put(RandomStringUtils.random(10, true, false), RandomStringUtils.random(20, true, false));
        extraParams.put(RandomStringUtils.random(10, true, false), RandomStringUtils.random(20, true, false));

        return extraParams;
    }

    public static <E extends Enum<E>> Enum<?> getRandomOfEnum(Class<E> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants()).toList().get(new Random().nextInt(enumClass.getEnumConstants().length));
    }
}
