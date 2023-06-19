package se.sundsvall.casemanagement.byggr;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Optional;

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
@WireMockAppTestSuite(files = "classpath:/ByggrCreateCaseIT", classes = Application.class)
class ByggrCreateCaseIT extends CustomAbstractAppTest {

    @Autowired
    private CaseMappingRepository caseMappingRepository;

    @Autowired
    private CaseRepository caseRepository;

    @Override
    protected Optional<Duration> getVerificationDelay() {
        return Optional.of(Duration.ofSeconds(3));
    }

    @Test
    void test1_PostByggrCase() throws JsonProcessingException, ClassNotFoundException {

        var EXTERNAL_CASE_ID = "5123";

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
                assertThat(caseMapping.getCaseId()).isEqualTo("BYGG 2021-000200");
                assertThat(caseMapping.getCaseType()).isEqualTo(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV);
                assertThat(caseMapping.getSystem()).isEqualTo(SystemType.BYGGR);
            });


    }

}
