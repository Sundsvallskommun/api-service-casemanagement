package se.sundsvall.casemanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static se.sundsvall.casemanagement.TestUtil.createOtherCase;
import static se.sundsvall.casemanagement.testutils.TestConstants.CASE_DATA_CASE_ID;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.testutils.CustomAbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/CaseMappingIT", classes = Application.class)
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
        
        caseMappingRepository.save(CaseMapping.builder()
            .withExternalCaseId(oCase.getExternalCaseId())
            .withCaseType(CaseType.PARKING_PERMIT)
            .withCaseId(CASE_DATA_CASE_ID)
            .withSystem(SystemType.CASE_DATA)
            .withServiceName("serviceName")
            .withTimestamp(LocalDateTime.now())
            .build());
        
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
        assertEquals("serviceName", result.get(0).getServiceName());
        assertEquals(SystemType.CASE_DATA, result.get(0).getSystem());
        assertNotNull(result.get(0).getTimestamp());
    }
    
    @Test
    void testGetAllCaseMappings() throws JsonProcessingException, ClassNotFoundException {
        var postResult_1 = caseMappingRepository.save(CaseMapping.builder()
            .withExternalCaseId(String.valueOf(new Random().nextLong()))
            .withCaseType(CaseType.PARKING_PERMIT)
            .withCaseId(CASE_DATA_CASE_ID)
            .withSystem(SystemType.CASE_DATA)
            .withServiceName("serviceName")
            .withTimestamp(LocalDateTime.now())
            .build());
        
        var postResult_2 = caseMappingRepository.save(CaseMapping.builder()
            .withExternalCaseId(String.valueOf(new Random().nextLong()))
            .withCaseType(CaseType.REGISTRERING_AV_LIVSMEDEL)
            .withCaseId(CASE_DATA_CASE_ID)
            .withSystem(SystemType.ECOS)
            .withServiceName("serviceName")
            .withTimestamp(LocalDateTime.now())
            .build());
        
        var postResult_3 = caseMappingRepository.save(CaseMapping.builder()
            .withExternalCaseId(String.valueOf(new Random().nextLong()))
            .withCaseType(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV)
            .withCaseId(CASE_DATA_CASE_ID)
            .withSystem(SystemType.BYGGR)
            .withServiceName("serviceName")
            .withTimestamp(LocalDateTime.now())
            .build());
        
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
        assertEquals(postResult_1.getExternalCaseId(), result.get(0).getExternalCaseId());
        assertEquals(postResult_1.getCaseType(), result.get(0).getCaseType());
        assertEquals(postResult_1.getServiceName(), result.get(0).getServiceName());
        assertEquals(SystemType.CASE_DATA, result.get(0).getSystem());
        assertNotNull(result.get(0).getTimestamp());
        
        // Assert case_2
        // For Ecos the caseId in the response and the caseId in the DB is not the same.
        assertNotNull(postResult_2.getCaseId());
        assertNotNull(result.get(1).getCaseId());
        assertEquals(postResult_2.getExternalCaseId(), result.get(1).getExternalCaseId());
        assertEquals(postResult_2.getCaseType(), result.get(1).getCaseType());
        assertEquals(postResult_2.getServiceName(), result.get(1).getServiceName());
        assertEquals(SystemType.ECOS, result.get(1).getSystem());
        assertNotNull(result.get(1).getTimestamp());
        
        // Assert case_3
        assertEquals(postResult_3.getCaseId(), result.get(2).getCaseId());
        assertEquals(postResult_3.getExternalCaseId(), result.get(2).getExternalCaseId());
        assertEquals(postResult_3.getCaseType(), result.get(2).getCaseType());
        assertEquals(postResult_3.getServiceName(), result.get(2).getServiceName());
        assertEquals(SystemType.BYGGR, result.get(2).getSystem());
        assertNotNull(result.get(2).getTimestamp());
    }
}
