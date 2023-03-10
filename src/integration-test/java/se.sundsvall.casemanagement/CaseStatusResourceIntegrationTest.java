package se.sundsvall.casemanagement;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.api.model.PlanningPermissionCaseDTO;
import se.sundsvall.casemanagement.api.model.enums.AddressCategory;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.casemanagement.api.model.CaseResourceResponseDTO;
import se.sundsvall.casemanagement.api.model.CaseStatusDTO;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.EnvironmentalCaseDTO;
import se.sundsvall.casemanagement.api.model.EnvironmentalFacilityDTO;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.service.util.Constants;
import se.sundsvall.casemanagement.testutils.CustomAbstractAppTest;
import se.sundsvall.casemanagement.testutils.TestConstants;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.sundsvall.casemanagement.TestUtil.OBJECT_MAPPER;
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
        // S??tter denna till "200_response" f??r att kunna styra vilket svar jag f??r i request mapping
        facility.setFacilityCollectionName("200_response");
        AddressDTO facilityAddressDTO = new AddressDTO();
        facilityAddressDTO.setAddressCategories(List.of(AddressCategory.VISITING_ADDRESS));
        facilityAddressDTO.setPropertyDesignation(PROPERTY_DESIGNATION_BALDER);
        facility.setAddress(facilityAddressDTO);
        eCase.setFacilities(List.of(facility));
        eCase.setExtraParameters(Map.of(Constants.SERVICE_NAME, "Ans??kan om livsmedel"));

        var postCaseResponse = setupCall()
                .withHttpMethod(HttpMethod.POST)
                .withServicePath("/cases")
                .withRequest(OBJECT_MAPPER.writeValueAsString(eCase))
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(CaseResourceResponseDTO.class);

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
        assertEquals(postCaseResponse.getCaseId(), getStatusResponse.getCaseId());
        assertEquals("Beg??ran om anst??nd", getStatusResponse.getStatus());
        assertEquals("Ans??kan om livsmedel", getStatusResponse.getServiceName());
    }

    @Test
    void testGetByggrStatusByExternalCaseId() throws JsonProcessingException, ClassNotFoundException {

        PlanningPermissionCaseDTO pCase = TestUtil.createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.ANS);
        String serviceNameValue = "Service name test";
        pCase.setExtraParameters(Map.of(Constants.SERVICE_NAME, serviceNameValue));

        var postCaseResponse = setupCall()
                .withHttpMethod(HttpMethod.POST)
                .withServicePath("/cases")
                .withRequest(OBJECT_MAPPER.writeValueAsString(pCase))
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(CaseResourceResponseDTO.class);

        assertEquals(TestConstants.BYGG_CASE_ID, postCaseResponse.getCaseId());

        var getStatusResponse = setupCall()
                .withHttpMethod(HttpMethod.GET)
                .withServicePath("/cases/" + pCase.getExternalCaseId() + "/status")
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(CaseStatusDTO.class);

        assertEquals(SystemType.BYGGR, getStatusResponse.getSystem());
        assertEquals(pCase.getCaseType(), getStatusResponse.getCaseType());
        assertEquals(pCase.getExternalCaseId(), getStatusResponse.getExternalCaseId());
        assertEquals(postCaseResponse.getCaseId(), getStatusResponse.getCaseId());
        assertEquals(Constants.BYGGR_HANDELSETYP_ANSOKAN, getStatusResponse.getStatus());
        assertEquals(serviceNameValue, getStatusResponse.getServiceName());
    }

    @Test
    void testGetCaseDataStatusByExternalCaseId() throws JsonProcessingException, ClassNotFoundException {

        OtherCaseDTO oCase = TestUtil.createOtherCase(CaseType.PARKING_PERMIT, AttachmentCategory.SIGNATURE);

        var postCaseResponse = setupCall()
                .withHttpMethod(HttpMethod.POST)
                .withServicePath("/cases")
                .withRequest(OBJECT_MAPPER.writeValueAsString(oCase))
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(CaseResourceResponseDTO.class);

        assertEquals(CASE_DATA_ERRAND_NUMBER, postCaseResponse.getCaseId());

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
        assertEquals("??rende inkommit", getStatusResponse.getStatus());
        assertNull(getStatusResponse.getServiceName());
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

        assertTrue(getStatusResponse.stream().anyMatch(caseStatus -> caseStatus.getStatus().equals("Beg??ran om anst??nd")));
        assertTrue(getStatusResponse.stream().anyMatch(caseStatus -> caseStatus.getStatus().equals("ANS??KAN")));
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
