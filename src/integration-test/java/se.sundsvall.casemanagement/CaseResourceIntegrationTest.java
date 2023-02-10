package se.sundsvall.casemanagement;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.api.model.CaseDTO;
import se.sundsvall.casemanagement.api.model.CaseResourceResponseDTO;
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
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.service.util.Constants;
import se.sundsvall.casemanagement.testutils.CustomAbstractAppTest;
import se.sundsvall.casemanagement.testutils.TestConstants;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.sundsvall.casemanagement.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casemanagement.TestUtil.getHeatPumpExtraParams;
import static se.sundsvall.casemanagement.TestUtil.getSandfilterExtraParams;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Constants.LOST_PARKING_PERMIT_VALUE;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Constants.PARKING_PERMIT_RENEWAL_VALUE;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Constants.PARKING_PERMIT_VALUE;
import static se.sundsvall.casemanagement.testutils.TestConstants.BYGG_CASE_ID;
import static se.sundsvall.casemanagement.testutils.TestConstants.CASE_DATA_CASE_ID;
import static se.sundsvall.casemanagement.testutils.TestConstants.CASE_DATA_ERRAND_NUMBER;
import static se.sundsvall.casemanagement.testutils.TestConstants.ECOS_CASE_ID;
import static se.sundsvall.casemanagement.testutils.TestConstants.ECOS_CASE_NUMBER;
import static se.sundsvall.casemanagement.testutils.TestConstants.PROPERTY_DESIGNATION_BALDER;

@WireMockAppTestSuite(files = "classpath:/IntegrationTest", classes = Application.class)
class CaseResourceIntegrationTest extends CustomAbstractAppTest {
    private static final String PERSON_ID = "e19981ad-34b2-4e14-88f5-133f61ca85aa";
    private static final String ORG_NUMBER = "123456-1234";

    @Autowired
    private CaseMappingRepository caseMappingRepository;

    @Test
    void testMinutMiljoAnmalanVarmepump() throws JsonProcessingException, ClassNotFoundException {
        EnvironmentalCaseDTO environmentalCase = TestUtil.createEnvironmentalCase(CaseType.ANMALAN_INSTALLATION_VARMEPUMP, AttachmentCategory.ANMALAN_VARMEPUMP);
        environmentalCase.getFacilities().get(0).setExtraParameters(getHeatPumpExtraParams());
        postCaseAndVerifyResponse(environmentalCase, ECOS_CASE_NUMBER);
    }

    @Test
    void testMinutMiljoTillstandVarmepump() throws JsonProcessingException, ClassNotFoundException {
        EnvironmentalCaseDTO environmentalCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP, AttachmentCategory.ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW);
        environmentalCase.getFacilities().get(0).setExtraParameters(getHeatPumpExtraParams());
        postCaseAndVerifyResponse(environmentalCase, ECOS_CASE_NUMBER);
    }

    @Test
    void testMinutMiljoAnsokanAvlopp() throws JsonProcessingException, ClassNotFoundException {
        EnvironmentalCaseDTO environmentalCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
        environmentalCase.getFacilities().get(0).setExtraParameters(getSandfilterExtraParams());
        postCaseAndVerifyResponse(environmentalCase, ECOS_CASE_NUMBER);
    }

    @Test
    void testMinutMiljoAnmalanAvlopp() throws JsonProcessingException, ClassNotFoundException {
        EnvironmentalCaseDTO environmentalCase = TestUtil.createEnvironmentalCase(CaseType.ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC, AttachmentCategory.ANMALAN_ENSKILT_AVLOPP);
        environmentalCase.getFacilities().get(0).setExtraParameters(getSandfilterExtraParams());
        postCaseAndVerifyResponse(environmentalCase, ECOS_CASE_NUMBER);
    }

    @Test
    void testMinutMiljoAvloppsanlaggning() throws JsonProcessingException, ClassNotFoundException {
        EnvironmentalCaseDTO environmentalCase = TestUtil.createEnvironmentalCase(CaseType.ANMALAN_ANDRING_AVLOPPSANLAGGNING, AttachmentCategory.ANMALAN_ANDRING_AVLOPPSANLAGGNING);
        environmentalCase.getFacilities().get(0).setExtraParameters(getSandfilterExtraParams());
        postCaseAndVerifyResponse(environmentalCase, ECOS_CASE_NUMBER);
    }

    @Test
    void testMinutMiljoAvloppsanordning() throws JsonProcessingException, ClassNotFoundException {
        EnvironmentalCaseDTO environmentalCase = TestUtil.createEnvironmentalCase(CaseType.ANMALAN_ANDRING_AVLOPPSANORDNING, AttachmentCategory.ANMALAN_ANDRING_AVLOPPSANORDNING);
        environmentalCase.getFacilities().get(0).setExtraParameters(getSandfilterExtraParams());
        postCaseAndVerifyResponse(environmentalCase, ECOS_CASE_NUMBER);
    }

    @Test
    void testMinutMiljoHalsoskyddsverksamhet() throws JsonProcessingException, ClassNotFoundException {
        EnvironmentalCaseDTO environmentalCase = TestUtil.createEnvironmentalCase(CaseType.ANMALAN_HALSOSKYDDSVERKSAMHET, AttachmentCategory.ANMALAN_HALSOSKYDDSVERKSAMHET);
        postCaseAndVerifyResponse(environmentalCase, ECOS_CASE_NUMBER);
    }

    @Test
    void testMinutMiljoLivsmedel() throws JsonProcessingException, ClassNotFoundException {
        EnvironmentalCaseDTO environmentalCase = TestUtil.createEnvironmentalCase(CaseType.REGISTRERING_AV_LIVSMEDEL, AttachmentCategory.ANMALAN_LIVSMEDELSANLAGGNING);
        environmentalCase.setEndDate(null);
        postCaseAndVerifyResponse(environmentalCase, ECOS_CASE_NUMBER);
    }

    @Test
    void testPostEcosCaseAndVerifyPersistance() throws JsonProcessingException, ClassNotFoundException {
        EnvironmentalCaseDTO environmentalCase = TestUtil.createEnvironmentalCase(CaseType.REGISTRERING_AV_LIVSMEDEL, AttachmentCategory.ANMALAN_LIVSMEDELSANLAGGNING);
        postCaseAndVerifyResponse(environmentalCase, ECOS_CASE_NUMBER);

        List<CaseMapping> caseMappingList = caseMappingRepository.findAllByExternalCaseId(environmentalCase.getExternalCaseId());
        assertTrue(caseMappingList.stream().anyMatch(c -> c.getExternalCaseId().equals(environmentalCase.getExternalCaseId()) && c.getCaseId().equals(ECOS_CASE_ID) && c.getSystem().equals(SystemType.ECOS)));
    }

    @Test
    void testPostByggrCaseAndVerifyPersistance() throws JsonProcessingException, ClassNotFoundException {
        PlanningPermissionCaseDTO pCase = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.ANS);
        postCaseAndVerifyResponse(pCase, BYGG_CASE_ID);

        List<CaseMapping> caseMappingList = caseMappingRepository.findAllByExternalCaseId(pCase.getExternalCaseId());
        assertTrue(caseMappingList.stream().anyMatch(c -> c.getExternalCaseId().equals(pCase.getExternalCaseId()) && c.getCaseId().equals(BYGG_CASE_ID) && c.getSystem().equals(SystemType.BYGGR)));
    }

    @Test
    void testPostCaseDataCaseAndVerifyPersistance() throws JsonProcessingException, ClassNotFoundException {
        OtherCaseDTO oCase = TestUtil.createOtherCase(CaseType.PARKING_PERMIT, AttachmentCategory.SIGNATURE);
        postCaseAndVerifyResponse(oCase, CASE_DATA_ERRAND_NUMBER);

        List<CaseMapping> caseMappingList = caseMappingRepository.findAllByExternalCaseId(oCase.getExternalCaseId());
        assertTrue(caseMappingList.stream().anyMatch(c -> c.getExternalCaseId().equals(oCase.getExternalCaseId()) && c.getCaseId().equals(CASE_DATA_CASE_ID) && c.getSystem().equals(SystemType.CASE_DATA)));
    }

    @Test
    void testMinutMiljoLivsmedelMovingFacility() throws JsonProcessingException, ClassNotFoundException {

        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.REGISTRERING_AV_LIVSMEDEL, AttachmentCategory.ANMALAN_LIVSMEDELSANLAGGNING);

        EnvironmentalFacilityDTO facility = new EnvironmentalFacilityDTO();
        facility.setFacilityCollectionName("TestFacility");
        eCase.setFacilities(List.of(facility));

        postCaseAndVerifyResponse(eCase, ECOS_CASE_NUMBER);
    }

    @Test
    void testMinutMiljoLivsmedelContactPersonWithoutPersonId() throws JsonProcessingException, ClassNotFoundException {

        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.REGISTRERING_AV_LIVSMEDEL, AttachmentCategory.ANMALAN_LIVSMEDELSANLAGGNING);
        PersonDTO contactPerson = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.CONTACT_PERSON));
        contactPerson.setPersonId(null);
        eCase.getStakeholders().add(contactPerson);

        postCaseAndVerifyResponse(eCase, ECOS_CASE_NUMBER);
    }

    @Test
    void testByggrCaseControllOfficialWithoutPersonId() throws JsonProcessingException, ClassNotFoundException {

        PlanningPermissionCaseDTO pCase = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.ANS);
        PersonDTO controlOfficial = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.CONTROL_OFFICIAL));
        controlOfficial.setPersonId(null);
        pCase.getStakeholders().add(controlOfficial);

        postCaseAndVerifyResponse(pCase, BYGG_CASE_ID);
    }

    @Test
    void testMinutMiljoLivsmedel500Response() throws JsonProcessingException {

        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.REGISTRERING_AV_LIVSMEDEL, AttachmentCategory.ANMALAN_LIVSMEDELSANLAGGNING);

        EnvironmentalFacilityDTO facility = new EnvironmentalFacilityDTO();
        // Sätter denna till "INTERNAL_SERVER_ERROR" för att kunna styra vilket svar jag får i request mapping
        facility.setFacilityCollectionName("INTERNAL_SERVER_ERROR");
        AddressDTO facilityAddressDTO = new AddressDTO();
        facilityAddressDTO.setAddressCategories(List.of(AddressCategory.POSTAL_ADDRESS));
        facilityAddressDTO.setPropertyDesignation(PROPERTY_DESIGNATION_BALDER);
        facility.setAddress(facilityAddressDTO);
        eCase.setFacilities(List.of(facility));

        setupCall()
                .withHttpMethod(HttpMethod.POST)
                .withServicePath("/cases")
                .withRequest(OBJECT_MAPPER.writeValueAsString(eCase))
                .withExpectedResponseStatus(HttpStatus.BAD_REQUEST)
                .sendRequestAndVerifyResponse();
    }

//    @Test
//    void testCaseDataBadRequest() throws JsonProcessingException {
//        OtherCaseDTO oCase = TestUtil.createOtherCase(CaseType.PARKING_PERMIT, AttachmentCategory.SIGNATURE);
//        oCase.setDescription("BAD_REQUEST");
//
//        setupCall()
//                .withHttpMethod(HttpMethod.POST)
//                .withServicePath("/cases")
//                .withRequest(OBJECT_MAPPER.writeValueAsString(oCase))
//                .withExpectedResponseStatus(HttpStatus.BAD_GATEWAY)
//                .sendRequestAndVerifyResponse();
//    }

    @Test
    void testMinutMiljoLivsmedelEndBeforeStart() throws JsonProcessingException, ClassNotFoundException {

        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.REGISTRERING_AV_LIVSMEDEL, AttachmentCategory.ANMALAN_LIVSMEDELSANLAGGNING);
        eCase.setStartDate(LocalDate.now().minusDays(1));
        eCase.setEndDate(LocalDate.now().minusDays(5));

        EnvironmentalFacilityDTO facility = new EnvironmentalFacilityDTO();
        // Sätter denna till "INTERNAL_SERVER_ERROR" för att kunna styra vilket svar jag får i request mapping
        facility.setFacilityCollectionName("INTERNAL_SERVER_ERROR");
        AddressDTO facilityAddressDTO = new AddressDTO();
        facilityAddressDTO.setAddressCategories(List.of(AddressCategory.POSTAL_ADDRESS));
        facilityAddressDTO.setPropertyDesignation(PROPERTY_DESIGNATION_BALDER);
        facility.setAddress(facilityAddressDTO);
        eCase.setFacilities(List.of(facility));

        var problem = setupCall()
                .withHttpMethod(HttpMethod.POST)
                .withServicePath("/cases")
                .withRequest(OBJECT_MAPPER.writeValueAsString(eCase))
                .withExpectedResponseStatus(HttpStatus.BAD_REQUEST)
                .sendRequestAndVerifyResponse()
                .andReturnBody(ConstraintViolationProblem.class);

        assertEquals(Constants.ERR_START_MUST_BE_BEFORE_END, problem.getViolations().get(0).getMessage());

    }

    @Test
    void testPostByggrCase() throws JsonProcessingException, ClassNotFoundException {
        PlanningPermissionCaseDTO pCase = new PlanningPermissionCaseDTO();
        pCase.setCaseType(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV);
        pCase.setExternalCaseId(String.valueOf(new Random().nextLong()));

        List<AttachmentDTO> listOfAttachmentDTO = new ArrayList<>();
        AttachmentDTO attachmentDTO = new AttachmentDTO();
        attachmentDTO.setCategory(AttachmentCategory.ANS);
        attachmentDTO.setExtension(TestConstants.PDF_EXTENSION);
        attachmentDTO.setFile(TestConstants.BASE64_STRING);
        attachmentDTO.setMimeType(TestConstants.MIMETYPE_PDF);
        attachmentDTO.setName("Ansökan om bygglov");
        listOfAttachmentDTO.add(attachmentDTO);
        pCase.setAttachments(listOfAttachmentDTO);

        PlanningPermissionFacilityDTO planningPermissionFacility = new PlanningPermissionFacilityDTO();
        planningPermissionFacility.setFacilityType(FacilityType.ONE_FAMILY_HOUSE);
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressCategories(List.of(AddressCategory.INVOICE_ADDRESS));
        addressDTO.setPropertyDesignation(PROPERTY_DESIGNATION_BALDER);
        planningPermissionFacility.setAddress(addressDTO);
        pCase.setFacilities(List.of(planningPermissionFacility));

        List<StakeholderDTO> stakeholderDTOS = new ArrayList<>();
        PersonDTO personDTO = new PersonDTO();
        personDTO.setType(StakeholderType.PERSON);
        personDTO.setFirstName("Test");
        personDTO.setLastName("Testorsson");
        personDTO.setPersonId(PERSON_ID);
        personDTO.setCellphoneNumber("060123456");
        personDTO.setPhoneNumber("0701234567");
        personDTO.setEmailAddress("test@sundsvall.se");
        List<StakeholderRole> roles = new ArrayList<>();
        roles.add(StakeholderRole.APPLICANT);
        personDTO.setRoles(roles);
        stakeholderDTOS.add(personDTO);

        OrganizationDTO organizationDTO = new OrganizationDTO();
        organizationDTO.setType(StakeholderType.ORGANIZATION);
        List<StakeholderRole> srList = new ArrayList<>();
        srList.add(StakeholderRole.PROPERTY_OWNER);
        organizationDTO.setRoles(srList);
        organizationDTO.setOrganizationName("organizationName");
        organizationDTO.setOrganizationNumber(ORG_NUMBER);

        List<AddressDTO> addressDTOS = new ArrayList<>();
        AddressDTO orgAddressDTO = new AddressDTO();
        orgAddressDTO.setStreet("Testargatan");
        orgAddressDTO.setHouseNumber("123");
        orgAddressDTO.setCity("Sundsvall");
        orgAddressDTO.setCountry("Sweden");
        orgAddressDTO.setAddressCategories(List.of(AddressCategory.POSTAL_ADDRESS,
                AddressCategory.VISITING_ADDRESS, AddressCategory.INVOICE_ADDRESS));
        addressDTOS.add(orgAddressDTO);
        organizationDTO.setAddresses(addressDTOS);

        stakeholderDTOS.add(organizationDTO);
        pCase.setStakeholders(stakeholderDTOS);

        postCaseAndVerifyResponse(pCase, BYGG_CASE_ID);
    }

    @Test
    void testPostByggrMultipleMainFacility() throws JsonProcessingException, ClassNotFoundException {
        PlanningPermissionCaseDTO pCase = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.ANS);
        pCase.setFacilities(List.of(TestUtil.createPlanningPermissionFacilityDTO(true), TestUtil.createPlanningPermissionFacilityDTO(true)));
        ConstraintViolationProblem problem = setupCall()
                .withHttpMethod(HttpMethod.POST)
                .withServicePath("/cases")
                .withRequest(OBJECT_MAPPER.writeValueAsString(pCase))
                .withExpectedResponseStatus(HttpStatus.BAD_REQUEST)
                .sendRequestAndVerifyResponse()
                .andReturnBody(ConstraintViolationProblem.class);

        assertEquals(Constants.ERR_MSG_ONLY_ONE_MAIN_FACILITY, problem.getViolations().get(0).getMessage());
    }

    @Test
    void testPostByggrCaseWithoutFacility() throws JsonProcessingException {
        PlanningPermissionCaseDTO pCase = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.ANS);
        pCase.setFacilities(null);
        setupCall()
                .withHttpMethod(HttpMethod.POST)
                .withServicePath("/cases")
                .withRequest(OBJECT_MAPPER.writeValueAsString(pCase))
                .withExpectedResponseStatus(HttpStatus.BAD_REQUEST)
                .sendRequestAndVerifyResponse();
    }

    @ParameterizedTest
    @EnumSource(value = CaseType.class, mode = EnumSource.Mode.INCLUDE, names = {PARKING_PERMIT_VALUE, LOST_PARKING_PERMIT_VALUE, PARKING_PERMIT_RENEWAL_VALUE})
    void testPostParkingPermitCase(CaseType caseType) throws JsonProcessingException, ClassNotFoundException {
        OtherCaseDTO otherCase = new OtherCaseDTO();
        otherCase.setCaseType(caseType);
        otherCase.setExternalCaseId(UUID.randomUUID().toString());
        otherCase.setCaseTitleAddition("Some case title addition");
        otherCase.setDescription("Some random description");
        StakeholderDTO stakeholderDTO1 = TestUtil.createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT, StakeholderRole.CONTACT_PERSON));
        StakeholderDTO stakeholderDTO2 = TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.PAYMENT_PERSON, StakeholderRole.INVOICE_RECIPENT));
        otherCase.setStakeholders(List.of(stakeholderDTO1, stakeholderDTO2));
        AttachmentDTO attachmentDTO1 = TestUtil.createAttachment(AttachmentCategory.ANS);
        AttachmentDTO attachmentDTO2 = TestUtil.createAttachment(AttachmentCategory.ANMALAN_VARMEPUMP);
        otherCase.setAttachments(List.of(attachmentDTO1, attachmentDTO2));
        otherCase.setExtraParameters(TestUtil.createExtraParameters());

        postCaseAndVerifyResponse(otherCase, CASE_DATA_ERRAND_NUMBER);
    }

    @Test
    void testTwoCasesWithTheSameExternalCaseId() throws JsonProcessingException, ClassNotFoundException {
        String externalCaseId = UUID.randomUUID().toString();
        PlanningPermissionCaseDTO pCase_1 = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.ANS);
        pCase_1.setExternalCaseId(externalCaseId);

        PlanningPermissionCaseDTO pCase_2 = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.ANS);
        pCase_2.setExternalCaseId(externalCaseId);

        // Try to post the first one - should succeed
        postCaseAndVerifyResponse(pCase_1, BYGG_CASE_ID);

        // Try to post the second one - should fail
        Problem response = setupCall()
                .withHttpMethod(HttpMethod.POST)
                .withServicePath("/cases")
                .withRequest(OBJECT_MAPPER.writeValueAsString(pCase_2))
                .withExpectedResponseStatus(HttpStatus.BAD_REQUEST)
                .sendRequestAndVerifyResponse()
                .andReturnBody(Problem.class);

        assertEquals("A resources already exists with the same externalCaseId: " + externalCaseId, response.getDetail());
    }


    @Test
    void testEcosPropertyDesignationNotFound() throws JsonProcessingException, ClassNotFoundException {

        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.REGISTRERING_AV_LIVSMEDEL, AttachmentCategory.ANMALAN_LIVSMEDELSANLAGGNING);
        eCase.getFacilities().get(0).getAddress().setPropertyDesignation("RANDOM_PROPERTY_DESIGNATION");

        Problem response = setupCall()
                .withHttpMethod(HttpMethod.POST)
                .withServicePath("/cases")
                .withRequest(OBJECT_MAPPER.writeValueAsString(eCase))
                .withExpectedResponseStatus(HttpStatus.BAD_REQUEST)
                .sendRequestAndVerifyResponse()
                .andReturnBody(Problem.class);

        assertEquals(MessageFormat.format("The specified propertyDesignation({0}) could not be found", eCase.getFacilities().get(0).getAddress().getPropertyDesignation()), response.getDetail());
    }

    @Test
    void testPutCase() throws JsonProcessingException, ClassNotFoundException {
        OtherCaseDTO oCase = TestUtil.createOtherCase(CaseType.PARKING_PERMIT, AttachmentCategory.SIGNATURE);
        postCaseAndVerifyResponse(oCase, CASE_DATA_ERRAND_NUMBER);

        OtherCaseDTO oCasePut = TestUtil.createOtherCase(CaseType.PARKING_PERMIT_RENEWAL, AttachmentCategory.PASSPORT_PHOTO);
        setupCall()
                .withHttpMethod(HttpMethod.PUT)
                .withServicePath("/cases/" + oCase.getExternalCaseId())
                .withRequest(OBJECT_MAPPER.writeValueAsString(oCasePut))
                .withExpectedResponseStatus(HttpStatus.NO_CONTENT)
                .sendRequestAndVerifyResponse();
    }

    private void postCaseAndVerifyResponse(CaseDTO inputCaseDTO, String caseId) throws JsonProcessingException, ClassNotFoundException {
        CaseResourceResponseDTO response = setupCall()
                .withHttpMethod(HttpMethod.POST)
                .withServicePath("/cases")
                .withRequest(OBJECT_MAPPER.writeValueAsString(inputCaseDTO))
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(CaseResourceResponseDTO.class);

        assertEquals(caseId, response.getCaseId());
    }
}