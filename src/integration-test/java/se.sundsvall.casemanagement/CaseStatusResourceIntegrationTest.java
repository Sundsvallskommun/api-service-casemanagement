package se.sundsvall.casemanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.sundsvall.casemanagement.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casemanagement.testutils.TestConstants.BYGG_CASE_ID;
import static se.sundsvall.casemanagement.testutils.TestConstants.BYGG_CASE_NUMBER;
import static se.sundsvall.casemanagement.testutils.TestConstants.CASE_DATA_CASE_ID;
import static se.sundsvall.casemanagement.testutils.TestConstants.ECOS_CASE_NUMBER;
import static se.sundsvall.casemanagement.testutils.TestConstants.PROPERTY_DESIGNATION_BALDER;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.CaseResourceResponseDTO;
import se.sundsvall.casemanagement.api.model.CaseStatusDTO;
import se.sundsvall.casemanagement.api.model.EnvironmentalCaseDTO;
import se.sundsvall.casemanagement.api.model.EnvironmentalFacilityDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.api.model.PlanningPermissionCaseDTO;
import se.sundsvall.casemanagement.api.model.enums.AddressCategory;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.testutils.CustomAbstractAppTest;
import se.sundsvall.casemanagement.testutils.TestConstants;
import se.sundsvall.casemanagement.util.Constants;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.sundsvall.casemanagement.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.HandelseTyp.HANDELSETYP_ANSOKAN;
import static se.sundsvall.casemanagement.testutils.TestConstants.CASE_DATA_CASE_ID;
import static se.sundsvall.casemanagement.testutils.TestConstants.CASE_DATA_ERRAND_NUMBER;
import static se.sundsvall.casemanagement.testutils.TestConstants.ECOS_CASE_NUMBER;
import static se.sundsvall.casemanagement.testutils.TestConstants.PROPERTY_DESIGNATION_BALDER;

@WireMockAppTestSuite(files = "classpath:/IntegrationTest", classes = Application.class)
class CaseStatusResourceIntegrationTest extends CustomAbstractAppTest {
    
    @Autowired
    CaseMappingRepository caseMappingRepository;
    
    @BeforeEach
    void setup() {
        caseMappingRepository.deleteAll();
    }

    @Test
    void testGetEcosStatusByExternalCaseId() throws JsonProcessingException, ClassNotFoundException {

        EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.REGISTRERING_AV_LIVSMEDEL, AttachmentCategory.ANMALAN_LIVSMEDELSANLAGGNING);

        EnvironmentalFacilityDTO facility = new EnvironmentalFacilityDTO();
        // Sätter denna till "200_response" för att kunna styra vilket svar jag får i request mapping
        facility.setFacilityCollectionName("200_response");
        AddressDTO facilityAddressDTO = new AddressDTO();
        facilityAddressDTO.setAddressCategories(List.of(AddressCategory.VISITING_ADDRESS));
        facilityAddressDTO.setPropertyDesignation(PROPERTY_DESIGNATION_BALDER);
        facility.setAddress(facilityAddressDTO);
        eCase.setFacilities(List.of(facility));
        eCase.setExtraParameters(Map.of(Constants.SERVICE_NAME, "Ansökan om livsmedel"));

        var postCaseResponse = caseMappingRepository.save(CaseMapping.builder()
            .withExternalCaseId(eCase.getExternalCaseId())
            .withCaseType(CaseType.REGISTRERING_AV_LIVSMEDEL)
            .withCaseId(ECOS_CASE_NUMBER)
            .withSystem(SystemType.ECOS)
            .withServiceName("Ansökan om livsmedel")
            .withTimestamp(LocalDateTime.now())
            .build());

        assertEquals(ECOS_CASE_NUMBER, postCaseResponse.getCaseId());

        var getStatusResponse = setupCall()
                .withHttpMethod(HttpMethod.GET)
                .withServicePath("/cases/" + eCase.getExternalCaseId() + "/status")
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(CaseStatusDTO.class);

        assertEquals(SystemType.ECOS, getStatusResponse.getSystem());
        assertEquals(eCase.getCaseType(), getStatusResponse.getCaseType());
        assertEquals(eCase.getExternalCaseId(), getStatusResponse.getExternalCaseId());
        assertEquals("MK-2021-837", getStatusResponse.getCaseId());
        assertEquals("Begäran om anstånd", getStatusResponse.getStatus());
        assertEquals("Ansökan om livsmedel", getStatusResponse.getServiceName());
    }

    @Test
    void testGetByggrStatusByExternalCaseId() throws JsonProcessingException, ClassNotFoundException {
        
        var postCaseResponse = caseMappingRepository.save(CaseMapping.builder()
            .withExternalCaseId(String.valueOf(new Random().nextLong()))
            .withCaseType(CaseType.PARKING_PERMIT)
            .withCaseId(BYGG_CASE_ID)
            .withSystem(SystemType.BYGGR)
            .withServiceName("serviceName")
            .withTimestamp(LocalDateTime.now())
            .build());

        assertEquals(BYGG_CASE_ID, postCaseResponse.getCaseId());

        var getStatusResponse = setupCall()
                .withHttpMethod(HttpMethod.GET)
                .withServicePath("/cases/" + postCaseResponse.getExternalCaseId() + "/status")
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(CaseStatusDTO.class);

        assertEquals(SystemType.BYGGR, getStatusResponse.getSystem());
        assertEquals(postCaseResponse.getCaseType(), getStatusResponse.getCaseType());
        assertEquals(postCaseResponse.getExternalCaseId(), getStatusResponse.getExternalCaseId());
        assertEquals(BYGG_CASE_NUMBER, getStatusResponse.getCaseId());
        assertEquals(HANDELSETYP_ANSOKAN, getStatusResponse.getStatus());
        assertEquals(postCaseResponse.getServiceName(), getStatusResponse.getServiceName());
    }
    
    @Test
    void testGetCaseDataStatusByExternalCaseId() throws JsonProcessingException, ClassNotFoundException {
        
        OtherCaseDTO oCase = TestUtil.createOtherCase(CaseType.PARKING_PERMIT,
            AttachmentCategory.SIGNATURE);
        
        var postCaseResponse = caseMappingRepository.save(CaseMapping.builder()
            .withExternalCaseId(oCase.getExternalCaseId())
            .withCaseType(CaseType.PARKING_PERMIT)
            .withCaseId(CASE_DATA_CASE_ID)
            .withSystem(SystemType.CASE_DATA)
            .withServiceName("serviceName")
            .withTimestamp(LocalDateTime.now())
            .build());
        
        assertEquals(CASE_DATA_CASE_ID, postCaseResponse.getCaseId());
        
        var getStatusResponse = setupCall()
            .withHttpMethod(HttpMethod.GET)
            .withServicePath("/cases/" + oCase.getExternalCaseId() + "/status")
            .withExpectedResponseStatus(HttpStatus.OK)
            .sendRequestAndVerifyResponse()
            .andReturnBody(CaseStatusDTO.class);
        
        assertEquals(SystemType.CASE_DATA, getStatusResponse.getSystem());
        assertEquals(oCase.getCaseType(), getStatusResponse.getCaseType());
        assertEquals(oCase.getExternalCaseId(), getStatusResponse.getExternalCaseId());
        assertEquals(CASE_DATA_CASE_ID, getStatusResponse.getCaseId());
        assertEquals("Ärende inkommit", getStatusResponse.getStatus());
        assertEquals("serviceName", getStatusResponse.getServiceName());
    }
    
    @Test
    void testGetStatusByOrgNr() throws JsonProcessingException, ClassNotFoundException {
        String organizationNumber = "123456-4321";
        
        var getStatusResponse = Arrays.asList(setupCall()
            .withHttpMethod(HttpMethod.GET)
            .withServicePath("/organization/" + organizationNumber + "/cases/status")
            .withExpectedResponseStatus(HttpStatus.OK)
            .sendRequestAndVerifyResponse()
            .andReturnBody(CaseStatusDTO[].class));
        
        assertTrue(getStatusResponse.stream().anyMatch(caseStatus -> caseStatus.getStatus().equals("Begäran om anstånd")));
        assertTrue(getStatusResponse.stream().anyMatch(caseStatus -> caseStatus.getStatus().equals("ANSÖKAN")));
    }
    
    @Test
    void testGetStatusByOrgNrNotFound() {
        String organizationNumber = "000000-0404";
        
        setupCall()
            .withHttpMethod(HttpMethod.GET)
            .withServicePath("/organization/" + organizationNumber + "/cases/status")
            .withExpectedResponseStatus(HttpStatus.NOT_FOUND)
            .sendRequestAndVerifyResponse();
    }
}
