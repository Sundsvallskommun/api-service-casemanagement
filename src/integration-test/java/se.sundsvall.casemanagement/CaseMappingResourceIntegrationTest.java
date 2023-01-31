package se.sundsvall.casemanagement;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import se.sundsvall.casemanagement.api.model.CaseResourceResponseDTO;
import se.sundsvall.casemanagement.api.model.EnvironmentalCaseDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.api.model.PlanningPermissionCaseDTO;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.service.util.Constants;
import se.sundsvall.casemanagement.testutils.CustomAbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

import java.util.Arrays;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static se.sundsvall.casemanagement.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casemanagement.TestUtil.createEnvironmentalCase;
import static se.sundsvall.casemanagement.TestUtil.createOtherCase;
import static se.sundsvall.casemanagement.TestUtil.createPlanningPermissionCaseDTO;
import static se.sundsvall.casemanagement.testutils.TestConstants.CASE_DATA_CASE_ID;

@WireMockAppTestSuite(files = "classpath:/IntegrationTest", classes = Application.class)
class CaseMappingResourceIntegrationTest extends CustomAbstractAppTest {

    @Autowired
    CaseMappingRepository caseMappingRepository;

    @BeforeEach
    void setup() {
        caseMappingRepository.deleteAll();
    }

    @Test
    void testGetCaseMappingWithExternalCaseId() throws JsonProcessingException, ClassNotFoundException {
        OtherCaseDTO oCase = createOtherCase(CaseType.PARKING_PERMIT, AttachmentCategory.PASSPORT_PHOTO);
        var postResult = setupCall()
                .withHttpMethod(HttpMethod.POST)
                .withServicePath("/cases")
                .withRequest(OBJECT_MAPPER.writeValueAsString(oCase))
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(CaseResourceResponseDTO.class);

        // Create another case so we can verify the correct CaseMapping is returned
        setupCall()
                .withHttpMethod(HttpMethod.POST)
                .withServicePath("/cases")
                .withRequest(OBJECT_MAPPER.writeValueAsString(TestUtil.createOtherCase(CaseType.PARKING_PERMIT, AttachmentCategory.PASSPORT_PHOTO)))
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse();

        var result = Arrays.asList(setupCall()
                .withHttpMethod(HttpMethod.GET)
                .withServicePath("/cases/case-mappings?external-case-id=" + oCase.getExternalCaseId())
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(CaseMapping[].class));

        assertEquals(1, result.size());
        assertEquals(CASE_DATA_CASE_ID, result.get(0).getCaseId());
        assertEquals(oCase.getExternalCaseId(), result.get(0).getExternalCaseId());
        assertEquals(oCase.getCaseType(), result.get(0).getCaseType());
        assertEquals(oCase.getExtraParameters().get(Constants.SERVICE_NAME), result.get(0).getServiceName());
        assertEquals(SystemType.CASE_DATA, result.get(0).getSystem());
        assertNotNull(result.get(0).getTimestamp());
    }

    @Test
    void testGetAllCaseMappings() throws JsonProcessingException, ClassNotFoundException {
        OtherCaseDTO case_1 = createOtherCase(CaseType.PARKING_PERMIT, AttachmentCategory.PASSPORT_PHOTO);
        var postResult_1 = setupCall()
                .withHttpMethod(HttpMethod.POST)
                .withServicePath("/cases")
                .withRequest(OBJECT_MAPPER.writeValueAsString(case_1))
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(CaseResourceResponseDTO.class);

        EnvironmentalCaseDTO case_2 = createEnvironmentalCase(CaseType.REGISTRERING_AV_LIVSMEDEL, AttachmentCategory.ANMALAN_LIVSMEDELSANLAGGNING);
        var postResult_2 = setupCall()
                .withHttpMethod(HttpMethod.POST)
                .withServicePath("/cases")
                .withRequest(OBJECT_MAPPER.writeValueAsString(case_2))
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(CaseResourceResponseDTO.class);

        PlanningPermissionCaseDTO case_3 = createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.ANS);
        var postResult_3 = setupCall()
                .withHttpMethod(HttpMethod.POST)
                .withServicePath("/cases")
                .withRequest(OBJECT_MAPPER.writeValueAsString(case_3))
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(CaseResourceResponseDTO.class);

        var result = Arrays.asList(setupCall()
                .withHttpMethod(HttpMethod.GET)
                .withServicePath("/cases/case-mappings")
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse()
                .andReturnBody(CaseMapping[].class));

        assertEquals(3, result.size());
        result.sort(Comparator.comparing(CaseMapping::getTimestamp));

        // Assert case_1
        assertEquals(CASE_DATA_CASE_ID, result.get(0).getCaseId());
        assertEquals(case_1.getExternalCaseId(), result.get(0).getExternalCaseId());
        assertEquals(case_1.getCaseType(), result.get(0).getCaseType());
        assertEquals(case_1.getExtraParameters().get(Constants.SERVICE_NAME), result.get(0).getServiceName());
        assertEquals(SystemType.CASE_DATA, result.get(0).getSystem());
        assertNotNull(result.get(0).getTimestamp());

        // Assert case_2
        // For Ecos the caseId in the response and the caseId in the DB is not the same.
        assertNotNull(postResult_2.getCaseId());
        assertNotNull(result.get(1).getCaseId());
        assertEquals(case_2.getExternalCaseId(), result.get(1).getExternalCaseId());
        assertEquals(case_2.getCaseType(), result.get(1).getCaseType());
        assertEquals(case_2.getExtraParameters().get(Constants.SERVICE_NAME), result.get(1).getServiceName());
        assertEquals(SystemType.ECOS, result.get(1).getSystem());
        assertNotNull(result.get(1).getTimestamp());

        // Assert case_3
        assertEquals(postResult_3.getCaseId(), result.get(2).getCaseId());
        assertEquals(case_3.getExternalCaseId(), result.get(2).getExternalCaseId());
        assertEquals(case_3.getCaseType(), result.get(2).getCaseType());
        assertEquals(case_3.getExtraParameters().get(Constants.SERVICE_NAME), result.get(2).getServiceName());
        assertEquals(SystemType.BYGGR, result.get(2).getSystem());
        assertNotNull(result.get(2).getTimestamp());
    }
}
