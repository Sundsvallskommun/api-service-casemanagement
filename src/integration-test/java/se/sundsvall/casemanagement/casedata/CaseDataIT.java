package se.sundsvall.casemanagement.casedata;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;

import se.sundsvall.casemanagement.Application;
import se.sundsvall.casemanagement.api.model.CaseResourceResponseDTO;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.CaseRepository;
import se.sundsvall.casemanagement.testutils.CustomAbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@Testcontainers
@WireMockAppTestSuite(files = "classpath:/CasedataIT/", classes = Application.class)
public class CaseDataIT extends CustomAbstractAppTest {

    public static final String CASE_DATA_ID = "24";

    @Autowired
    private CaseMappingRepository caseMappingRepository;

    @Autowired
    private CaseRepository caseRepository;

    @Test
    void test1_PostParkingPermitCase() throws JsonProcessingException, ClassNotFoundException {
        var EXTERNAL_CASE_ID = "40621444";

        var result = setupCall()
            .withHttpMethod(HttpMethod.POST)
            .withServicePath("/cases")
            .withRequest("request.json")
            .withExpectedResponseStatus(HttpStatus.OK)
            .withExpectedResponse("expected-response.json")
            .sendRequestAndVerifyResponse()
            .andReturnBody(CaseResourceResponseDTO.class);

        assertThat(result).isNotNull();
        assertThat(result.getCaseId()).isEqualTo("Inskickat");

        // Make sure that there doesn't exist a case entity
        assertThat(caseRepository.findById(EXTERNAL_CASE_ID).isPresent()).isFalse();
        // Make sure that there exists a case mapping
        assertThat(caseMappingRepository.findAllByExternalCaseId(EXTERNAL_CASE_ID))
            .isNotNull()
            .hasSize(1)
            .allSatisfy(caseMapping -> {
                assertThat(caseMapping.getExternalCaseId()).isEqualTo(EXTERNAL_CASE_ID);
                assertThat(caseMapping.getCaseId()).isEqualTo(CASE_DATA_ID);
                assertThat(caseMapping.getCaseType()).isEqualTo(CaseType.PARKING_PERMIT);
                assertThat(caseMapping.getSystem()).isEqualTo(SystemType.CASE_DATA);
            });
    }

    @Test
    void test2_postCaseInternalError() {

        var EXTERNAL_CASE_ID = "40621445";

        setupCall()
            .withHttpMethod(HttpMethod.POST)
            .withServicePath("/cases")
            .withRequest("request.json")
            .withExpectedResponseStatus(HttpStatus.OK)
            .withExpectedResponse("expected-response.json")
            .sendRequestAndVerifyResponse();

        //  Make sure that there exists a case entity
        assertThat(caseRepository.findById(EXTERNAL_CASE_ID).isPresent()).isTrue();
        // Make sure that there doesn't exist a case mapping
        assertThat(caseMappingRepository.findAllByExternalCaseId(EXTERNAL_CASE_ID))
            .isNotNull()
            .hasSize(0);
    }

    @Test
    void test3_updateCase() {

        setupCall()
            .withHttpMethod(HttpMethod.PUT)
            .withServicePath("/cases/231")
            .withRequest("request.json")
            .withExpectedResponseStatus(HttpStatus.NO_CONTENT)
            .sendRequestAndVerifyResponse();
    }
}
