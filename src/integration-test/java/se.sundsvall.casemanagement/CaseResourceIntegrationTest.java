package se.sundsvall.casemanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.sundsvall.casemanagement.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casemanagement.TestUtil.getHeatPumpExtraParams;
import static se.sundsvall.casemanagement.TestUtil.getSandfilterExtraParams;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.LOST_PARKING_PERMIT;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.PARKING_PERMIT;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.PARKING_PERMIT_RENEWAL;
import static se.sundsvall.casemanagement.testutils.TestConstants.BYGG_CASE_ID;
import static se.sundsvall.casemanagement.testutils.TestConstants.BYGG_CASE_NUMBER;
import static se.sundsvall.casemanagement.testutils.TestConstants.CASE_DATA_CASE_ID;
import static se.sundsvall.casemanagement.testutils.TestConstants.CASE_DATA_ERRAND_NUMBER;
import static se.sundsvall.casemanagement.testutils.TestConstants.ECOS_CASE_ID;
import static se.sundsvall.casemanagement.testutils.TestConstants.ECOS_CASE_NUMBER;
import static se.sundsvall.casemanagement.testutils.TestConstants.PROPERTY_DESIGNATION_BALDER;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
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
import se.sundsvall.casemanagement.integration.db.CaseRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.integration.db.model.DeliveryStatus;
import se.sundsvall.casemanagement.testutils.CustomAbstractAppTest;
import se.sundsvall.casemanagement.testutils.TestConstants;
import se.sundsvall.casemanagement.util.Constants;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/IntegrationTest", classes = Application.class)
@Sql(scripts = "classpath:/sql/caseType.sql")
class CaseResourceIntegrationTest extends CustomAbstractAppTest {
    private static final String PERSON_ID = "e19981ad-34b2-4e14-88f5-133f61ca85aa";
    private static final String ORG_NUMBER = "123456-1234";
    
    @Autowired
    private CaseMappingRepository caseMappingRepository;
    
    @Autowired
    private CaseRepository caseRepository;
    
    @Override
    protected Optional<Duration> getVerificationDelay() {
        return Optional.of(Duration.ofSeconds(3));
    }
    
    @Test
    void testMinutMiljoAnmalanVarmepump() throws JsonProcessingException, ClassNotFoundException {
        EnvironmentalCaseDTO environmentalCase = TestUtil.createEnvironmentalCase(CaseType.ANMALAN_INSTALLATION_VARMEPUMP, AttachmentCategory.ANMALAN_VARMEPUMP);
        environmentalCase.getFacilities().get(0).setExtraParameters(getHeatPumpExtraParams());
        postCaseAndVerifyResponse(environmentalCase, ECOS_CASE_NUMBER);
        // Make sure that there doesn't exist a case entity
        assertThat(caseRepository.findById(environmentalCase.getExternalCaseId()).isPresent()).isFalse();
        // Make sure that there exists a case mapping
        assertThat(caseMappingRepository.findAllByExternalCaseId(environmentalCase.getExternalCaseId()))
            .isNotNull()
            .allSatisfy(caseMapping -> {
                assertThat(caseMapping.getExternalCaseId()).isEqualTo(environmentalCase.getExternalCaseId());
                assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
                assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
            });
    }
    
    @Test
    void testMinutMiljoTillstandVarmepump() throws JsonProcessingException, ClassNotFoundException {
        EnvironmentalCaseDTO environmentalCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP, AttachmentCategory.ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW);
        environmentalCase.getFacilities().get(0).setExtraParameters(getHeatPumpExtraParams());
        postCaseAndVerifyResponse(environmentalCase, ECOS_CASE_NUMBER);
        // Make sure that there doesn't exist a case entity
        assertThat(caseRepository.findById(environmentalCase.getExternalCaseId()).isPresent()).isFalse();
        // Make sure that there exists a case mapping
        assertThat(caseMappingRepository.findAllByExternalCaseId(environmentalCase.getExternalCaseId()))
            .isNotNull()
            .allSatisfy(caseMapping -> {
                assertThat(caseMapping.getExternalCaseId()).isEqualTo(environmentalCase.getExternalCaseId());
                assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
                assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
            });
    }
    
    @Test
    void testMinutMiljoAnsokanAvlopp() throws JsonProcessingException, ClassNotFoundException {
        EnvironmentalCaseDTO environmentalCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
        environmentalCase.getFacilities().get(0).setExtraParameters(getSandfilterExtraParams());
        postCaseAndVerifyResponse(environmentalCase, ECOS_CASE_NUMBER);
        // Make sure that there doesn't exist a case entity
        assertThat(caseRepository.findById(environmentalCase.getExternalCaseId()).isPresent()).isFalse();
        // Make sure that there exists a case mapping
        assertThat(caseMappingRepository.findAllByExternalCaseId(environmentalCase.getExternalCaseId()))
            .isNotNull()
            .allSatisfy(caseMapping -> {
                assertThat(caseMapping.getExternalCaseId()).isEqualTo(environmentalCase.getExternalCaseId());
                assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
                assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
            });
    }
    
    @Test
    void testMinutMiljoAnmalanAvlopp() throws JsonProcessingException, ClassNotFoundException {
        EnvironmentalCaseDTO environmentalCase = TestUtil.createEnvironmentalCase(CaseType.ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC, AttachmentCategory.ANMALAN_ENSKILT_AVLOPP);
        environmentalCase.getFacilities().get(0).setExtraParameters(getSandfilterExtraParams());
        postCaseAndVerifyResponse(environmentalCase, ECOS_CASE_NUMBER);
        // Make sure that there doesn't exist a case entity
        assertThat(caseRepository.findById(environmentalCase.getExternalCaseId()).isPresent()).isFalse();
        // Make sure that there exists a case mapping
        assertThat(caseMappingRepository.findAllByExternalCaseId(environmentalCase.getExternalCaseId()))
            .isNotNull()
            .allSatisfy(caseMapping -> {
                assertThat(caseMapping.getExternalCaseId()).isEqualTo(environmentalCase.getExternalCaseId());
                assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
                assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
            });
    }
    
    @Test
    void testMinutMiljoAvloppsanlaggning() throws JsonProcessingException, ClassNotFoundException {
        EnvironmentalCaseDTO environmentalCase = TestUtil.createEnvironmentalCase(CaseType.ANMALAN_ANDRING_AVLOPPSANLAGGNING, AttachmentCategory.ANMALAN_ANDRING_AVLOPPSANLAGGNING);
        environmentalCase.getFacilities().get(0).setExtraParameters(getSandfilterExtraParams());
        postCaseAndVerifyResponse(environmentalCase, ECOS_CASE_NUMBER);
        // Make sure that there doesn't exist a case entity
        assertThat(caseRepository.findById(environmentalCase.getExternalCaseId()).isPresent()).isFalse();
        // Make sure that there exists a case mapping
        assertThat(caseMappingRepository.findAllByExternalCaseId(environmentalCase.getExternalCaseId()))
            .isNotNull()
            .allSatisfy(caseMapping -> {
                assertThat(caseMapping.getExternalCaseId()).isEqualTo(environmentalCase.getExternalCaseId());
                assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
                assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
            });
    }
    
    @Test
    void testMinutMiljoAvloppsanordning() throws JsonProcessingException, ClassNotFoundException {
        EnvironmentalCaseDTO environmentalCase = TestUtil.createEnvironmentalCase(CaseType.ANMALAN_ANDRING_AVLOPPSANORDNING, AttachmentCategory.ANMALAN_ANDRING_AVLOPPSANORDNING);
        environmentalCase.getFacilities().get(0).setExtraParameters(getSandfilterExtraParams());
        postCaseAndVerifyResponse(environmentalCase, ECOS_CASE_NUMBER);
        // Make sure that there doesn't exist a case entity
        assertThat(caseRepository.findById(environmentalCase.getExternalCaseId()).isPresent()).isFalse();
        // Make sure that there exists a case mapping
        assertThat(caseMappingRepository.findAllByExternalCaseId(environmentalCase.getExternalCaseId()))
            .isNotNull()
            .allSatisfy(caseMapping -> {
                assertThat(caseMapping.getExternalCaseId()).isEqualTo(environmentalCase.getExternalCaseId());
                assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
                assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
            });
    }
    
    @Test
    void testMinutMiljoHalsoskyddsverksamhet() throws JsonProcessingException, ClassNotFoundException {
        EnvironmentalCaseDTO environmentalCase = TestUtil.createEnvironmentalCase(CaseType.ANMALAN_HALSOSKYDDSVERKSAMHET, AttachmentCategory.ANMALAN_HALSOSKYDDSVERKSAMHET);
        postCaseAndVerifyResponse(environmentalCase, ECOS_CASE_NUMBER);
        // Make sure that there doesn't exist a case entity
        assertThat(caseRepository.findById(environmentalCase.getExternalCaseId()).isPresent()).isFalse();
        // Make sure that there exists a case mapping
        assertThat(caseMappingRepository.findAllByExternalCaseId(environmentalCase.getExternalCaseId()))
            .isNotNull()
            .allSatisfy(caseMapping -> {
                assertThat(caseMapping.getExternalCaseId()).isEqualTo(environmentalCase.getExternalCaseId());
                assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
                assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
            });
    }
    
    @Test
    void testMinutMiljoLivsmedel() throws JsonProcessingException, ClassNotFoundException {
        EnvironmentalCaseDTO environmentalCase = TestUtil.createEnvironmentalCase(CaseType.REGISTRERING_AV_LIVSMEDEL, AttachmentCategory.ANMALAN_LIVSMEDELSANLAGGNING);
        environmentalCase.setEndDate(null);
        postCaseAndVerifyResponse(environmentalCase, ECOS_CASE_NUMBER);
        // Make sure that there doesn't exist a case entity
        assertThat(caseRepository.findById(environmentalCase.getExternalCaseId()).isPresent()).isFalse();
        // Make sure that there exists a case mapping
        assertThat(caseMappingRepository.findAllByExternalCaseId(environmentalCase.getExternalCaseId()))
            .isNotNull()
            .allSatisfy(caseMapping -> {
                assertThat(caseMapping.getExternalCaseId()).isEqualTo(environmentalCase.getExternalCaseId());
                assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
                assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
            });
    }
    
    @Test
    void testPostEcosCaseAndVerifyPersistance() throws JsonProcessingException, ClassNotFoundException {
        EnvironmentalCaseDTO environmentalCase = TestUtil.createEnvironmentalCase(CaseType.REGISTRERING_AV_LIVSMEDEL, AttachmentCategory.ANMALAN_LIVSMEDELSANLAGGNING);
        postCaseAndVerifyResponse(environmentalCase, ECOS_CASE_NUMBER);
        
        
        // Make sure that there doesn't exist a case entity
        assertThat(caseRepository.findById(environmentalCase.getExternalCaseId()).isPresent()).isFalse();
        // Make sure that there exists a case mapping
        assertThat(caseMappingRepository.findAllByExternalCaseId(environmentalCase.getExternalCaseId()))
            .isNotNull()
            .allSatisfy(caseMapping -> {
                assertThat(caseMapping.getExternalCaseId()).isEqualTo(environmentalCase.getExternalCaseId());
                assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
                assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
            });
    }
    
    @Test
    void testPostByggrCaseAndVerifyPersistance() throws JsonProcessingException, ClassNotFoundException {
        PlanningPermissionCaseDTO pCase = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.ANS);
        postCaseAndVerifyResponse(pCase, BYGG_CASE_ID);
        
        // Make sure that there doesn't exist a case entity
        assertThat(caseRepository.findById(pCase.getExternalCaseId()).isPresent()).isFalse();
        // Make sure that there exists a case mapping
        assertThat(caseMappingRepository.findAllByExternalCaseId(pCase.getExternalCaseId()))
            .isNotNull()
            .allSatisfy(caseMapping -> {
                assertThat(caseMapping.getExternalCaseId()).isEqualTo(pCase.getExternalCaseId());
                assertThat(caseMapping.getCaseId()).isEqualTo(BYGG_CASE_NUMBER);
                assertThat(caseMapping.getSystem()).isEqualTo(SystemType.BYGGR);
            });
    }
    
    @Test
    void testPostCaseDataCaseAndVerifyPersistance() throws JsonProcessingException, ClassNotFoundException {
        OtherCaseDTO oCase = TestUtil.createOtherCase(CaseType.PARKING_PERMIT, AttachmentCategory.SIGNATURE);
        oCase.setCaseTitleAddition("Some case title addition");
        postCaseAndVerifyResponse(oCase, CASE_DATA_ERRAND_NUMBER);
        // Make sure that there doesn't exist a case entity
        assertThat(caseRepository.findById(oCase.getExternalCaseId()).isPresent()).isFalse();
        // Make sure that there exists a case mapping
        assertThat(caseMappingRepository.findAllByExternalCaseId(oCase.getExternalCaseId()))
            .isNotNull()
            .allSatisfy(caseMapping -> {
                assertThat(caseMapping.getExternalCaseId()).isEqualTo(oCase.getExternalCaseId());
                assertThat(caseMapping.getCaseId()).isEqualTo(CASE_DATA_CASE_ID);
                assertThat(caseMapping.getSystem()).isEqualTo(SystemType.CASE_DATA);
            });
    }
    
    
    @Test
    void testMinutMiljoLivsmedelMovingFacility() throws JsonProcessingException, ClassNotFoundException {
        
        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.REGISTRERING_AV_LIVSMEDEL, AttachmentCategory.ANMALAN_LIVSMEDELSANLAGGNING);
        
        EnvironmentalFacilityDTO facility = new EnvironmentalFacilityDTO();
        facility.setFacilityCollectionName("TestFacility");
        eCase.setFacilities(List.of(facility));
        
        postCaseAndVerifyResponse(eCase, ECOS_CASE_NUMBER);
        // Make sure that there doesn't exist a case entity
        assertThat(caseRepository.findById(eCase.getExternalCaseId()).isPresent()).isFalse();
        // Make sure that there exists a case mapping
        assertThat(caseMappingRepository.findAllByExternalCaseId(eCase.getExternalCaseId()))
            .isNotNull()
            .allSatisfy(caseMapping -> {
                assertThat(caseMapping.getExternalCaseId()).isEqualTo(eCase.getExternalCaseId());
                assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
                assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
            });
    }
    
    @Test
    void testMinutMiljoLivsmedelContactPersonWithoutPersonId() throws JsonProcessingException, ClassNotFoundException {
        
        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.REGISTRERING_AV_LIVSMEDEL, AttachmentCategory.ANMALAN_LIVSMEDELSANLAGGNING);
        PersonDTO contactPerson = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.CONTACT_PERSON));
        contactPerson.setPersonId(null);
        eCase.getStakeholders().add(contactPerson);
        
        postCaseAndVerifyResponse(eCase, ECOS_CASE_NUMBER);
        // Make sure that there doesn't exist a case entity
        assertThat(caseRepository.findById(eCase.getExternalCaseId()).isPresent()).isFalse();
        // Make sure that there exists a case mapping
        assertThat(caseMappingRepository.findAllByExternalCaseId(eCase.getExternalCaseId()))
            .isNotNull()
            .allSatisfy(caseMapping -> {
                assertThat(caseMapping.getExternalCaseId()).isEqualTo(eCase.getExternalCaseId());
                assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
                assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
            });
        
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
    void testMinutMiljoLivsmedel500Response() throws JsonProcessingException, InterruptedException {
        
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
            .withExpectedResponseStatus(HttpStatus.OK)
            .sendRequestAndVerifyResponse();
        
        // Make sure that there doesn't exist a case mapping
        assertThat(caseMappingRepository.findAllByExternalCaseId(eCase.getExternalCaseId())).isEmpty();
        TimeUnit.SECONDS.sleep(10);
        // Make sure that there exists a case entity with failed status
        var result = caseRepository.findById(eCase.getExternalCaseId()).orElseThrow(() -> new RuntimeException("Case not found"));
        
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(eCase.getExternalCaseId());
        assertThat(result.getDeliveryStatus()).isEqualTo(DeliveryStatus.FAILED);
    }
    
    
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
    
    @Test
    void testPostByggrCaseAttefallFacility() throws JsonProcessingException {
        PlanningPermissionCaseDTO pCase = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.ANS);
        pCase.setFacilities(null);
        pCase.setFacilities(List.of(TestUtil.createAttefallFacilityDTO(true)));
        
        setupCall()
            .withHttpMethod(HttpMethod.POST)
            .withServicePath("/cases")
            .withRequest(OBJECT_MAPPER.writeValueAsString(pCase))
            .withExpectedResponseStatus(HttpStatus.BAD_REQUEST)
            .sendRequestAndVerifyResponse();
    }
    
    @ParameterizedTest
    @EnumSource(value = CaseType.class, mode = EnumSource.Mode.INCLUDE, names = {PARKING_PERMIT, LOST_PARKING_PERMIT, PARKING_PERMIT_RENEWAL})
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
        
        assertThat(caseRepository.findById(otherCase.getExternalCaseId()).isPresent()).isFalse();
        // Make sure that there exists a case mapping
        assertThat(caseMappingRepository.findAllByExternalCaseId(otherCase.getExternalCaseId()))
            .isNotNull()
            .allSatisfy(caseMapping -> {
                assertThat(caseMapping.getExternalCaseId()).isEqualTo(otherCase.getExternalCaseId());
                assertThat(caseMapping.getCaseId()).isEqualTo(CASE_DATA_CASE_ID);
                assertThat(caseMapping.getSystem()).isEqualTo(SystemType.CASE_DATA);
            });
    }
    
    @ParameterizedTest
    @EnumSource(value = CaseType.class, mode = EnumSource.Mode.INCLUDE, names = {PARKING_PERMIT, LOST_PARKING_PERMIT, PARKING_PERMIT_RENEWAL})
    void testPostParkingPermitcaseError(CaseType caseType) throws JsonProcessingException, InterruptedException {
        
        OtherCaseDTO otherCase = new OtherCaseDTO();
        otherCase.setCaseType(caseType);
        otherCase.setExternalCaseId("INTERNAL_SERVER_ERROR");
        otherCase.setCaseTitleAddition("INTERNAL_SERVER_ERROR");
        otherCase.setDescription("INTERNAL_SERVER_ERROR");
        StakeholderDTO stakeholderDTO1 = TestUtil.createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT, StakeholderRole.CONTACT_PERSON));
        StakeholderDTO stakeholderDTO2 = TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.PAYMENT_PERSON, StakeholderRole.INVOICE_RECIPENT));
        otherCase.setStakeholders(List.of(stakeholderDTO1, stakeholderDTO2));
        AttachmentDTO attachmentDTO1 = TestUtil.createAttachment(AttachmentCategory.ANS);
        AttachmentDTO attachmentDTO2 = TestUtil.createAttachment(AttachmentCategory.ANMALAN_VARMEPUMP);
        otherCase.setAttachments(List.of(attachmentDTO1, attachmentDTO2));
        otherCase.setExtraParameters(TestUtil.createExtraParameters());
        
        setupCall()
            .withHttpMethod(HttpMethod.POST)
            .withServicePath("/cases")
            .withRequest(OBJECT_MAPPER.writeValueAsString(otherCase))
            .withExpectedResponseStatus(HttpStatus.OK)
            .sendRequestAndVerifyResponse();
        
        // Make sure that there doesn't exist a case mapping
        assertThat(caseMappingRepository.findAllByExternalCaseId(otherCase.getExternalCaseId())).isEmpty();
        TimeUnit.SECONDS.sleep(10);
        // Make sure that there exists a case entity with failed status
        var result = caseRepository.findById(otherCase.getExternalCaseId()).orElseThrow(() -> new RuntimeException("Case not found"));
        
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(otherCase.getExternalCaseId());
        assertThat(result.getDeliveryStatus()).isEqualTo(DeliveryStatus.FAILED);
    }
    
    @Test
    void testTwoCasesWithTheSameExternalCaseId() throws JsonProcessingException, ClassNotFoundException {
        String externalCaseId = UUID.randomUUID().toString();
        PlanningPermissionCaseDTO pCase_1 = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.ANS);
        pCase_1.setExternalCaseId(externalCaseId);
        
        PlanningPermissionCaseDTO pCase_2 = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.ANS);
        pCase_2.setExternalCaseId(externalCaseId);
        
        caseMappingRepository.save(CaseMapping.builder()
            .withCaseType(CaseType.PARKING_PERMIT)
            .withCaseId(BYGG_CASE_ID)
            .withSystem(SystemType.BYGGR)
            .withServiceName("serviceName")
            .withTimestamp(LocalDateTime.now())
            .withExternalCaseId(externalCaseId)
            .build());
        
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
    void testEcosPropertyDesignationNotFound() throws JsonProcessingException, ClassNotFoundException, InterruptedException {
        
        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.REGISTRERING_AV_LIVSMEDEL, AttachmentCategory.ANMALAN_LIVSMEDELSANLAGGNING);
        eCase.getFacilities().get(0).getAddress().setPropertyDesignation("RANDOM_PROPERTY_DESIGNATION");
        
        setupCall()
            .withHttpMethod(HttpMethod.POST)
            .withServicePath("/cases")
            .withRequest(OBJECT_MAPPER.writeValueAsString(eCase))
            .withExpectedResponseStatus(HttpStatus.OK)
            .sendRequestAndVerifyResponse()
            .andReturnBody(Problem.class);
        
        // Make sure that there doesn't exist a case mapping
        assertThat(caseMappingRepository.findAllByExternalCaseId(eCase.getExternalCaseId())).isEmpty();
        TimeUnit.SECONDS.sleep(10);
        // Make sure that there exists a case entity with failed status
        var result = caseRepository.findById(eCase.getExternalCaseId()).orElseThrow(() -> new RuntimeException("Case not found"));
        
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(eCase.getExternalCaseId());
        assertThat(result.getDeliveryStatus()).isEqualTo(DeliveryStatus.FAILED);
        
    }
    
    @Test
    void testPutCase() throws JsonProcessingException {
        var externalCaseId = UUID.randomUUID().toString();
        
        caseMappingRepository.save(CaseMapping.builder()
            .withCaseType(CaseType.PARKING_PERMIT_RENEWAL)
            .withCaseId(CASE_DATA_CASE_ID)
            .withSystem(SystemType.CASE_DATA)
            .withServiceName("serviceName")
            .withTimestamp(LocalDateTime.now())
            .withExternalCaseId(externalCaseId)
            .build());
        
        
        OtherCaseDTO oCasePut = TestUtil.createOtherCase(CaseType.PARKING_PERMIT_RENEWAL, AttachmentCategory.PASSPORT_PHOTO);
        setupCall()
            .withHttpMethod(HttpMethod.PUT)
            .withServicePath("/cases/" + externalCaseId)
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