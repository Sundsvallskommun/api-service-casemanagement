package se.sundsvall.casemanagement;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;

import se.sundsvall.casemanagement.api.model.CaseStatusDTO;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.testutils.CustomAbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@Testcontainers
@WireMockAppTestSuite(files = "classpath:/CaseStatusIT/", classes = Application.class)
public class CaseStatusIT extends CustomAbstractAppTest {

    @Autowired
    private CaseMappingRepository caseMappingRepository;

    @Test
    void test1_GetEcosStatusByExternalCaseId() throws JsonProcessingException, ClassNotFoundException {
        System.out.println(" CHEEZ CaseMappingRepository.findAll()");
        caseMappingRepository.findAll().forEach(System.out::println);


        var ECOS_NUMBER = "MK-2021-837";

        var result = setupCall()
            .withHttpMethod(HttpMethod.GET)
            .withServicePath("/cases/" + "2223" + "/status")
            .withExpectedResponseStatus(HttpStatus.OK)
            .sendRequestAndVerifyResponse()
            .andReturnBody(CaseStatusDTO.class);

        assertThat(result.getSystem()).isEqualTo(SystemType.ECOS);
        assertThat(result.getCaseType()).isEqualTo(CaseType.REGISTRERING_AV_LIVSMEDEL);
        assertThat(result.getExternalCaseId()).isEqualTo("2223");
        assertThat(result.getCaseId()).isEqualTo(ECOS_NUMBER);
        assertThat(result.getStatus()).isEqualTo("Begäran om anstånd");
        assertThat(result.getServiceName()).isEqualTo("Registrering av livsmedelsanläggning");

    }

    @Test
    void test2_GetByggrStatusByExternalCaseId() throws JsonProcessingException, ClassNotFoundException {

        var BYGGR_NUMBER = "BYGG 2021-000200";

        var result = setupCall()
            .withHttpMethod(HttpMethod.GET)
            .withServicePath("/cases/" + "3522" + "/status")
            .withExpectedResponseStatus(HttpStatus.OK)
            .sendRequestAndVerifyResponse()
            .andReturnBody(CaseStatusDTO.class);

        assertThat(result.getSystem()).isEqualTo(SystemType.BYGGR);
        assertThat(result.getCaseType()).isEqualTo(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV);
        assertThat(result.getExternalCaseId()).isEqualTo("3522");
        assertThat(result.getCaseId()).isEqualTo(BYGGR_NUMBER);
        assertThat(result.getStatus()).isEqualTo("ANSÖKAN");
        assertThat(result.getServiceName()).isEqualTo("Ansökan - strandskyddsdispens");
    }

    @Test
    void test3_GetCaseDataStatusByExternalCaseId() throws JsonProcessingException, ClassNotFoundException {

        var result = setupCall()
            .withHttpMethod(HttpMethod.GET)
            .withServicePath("/cases/" + "231" + "/status")
            .withExpectedResponseStatus(HttpStatus.OK)
            .sendRequestAndVerifyResponse()
            .andReturnBody(CaseStatusDTO.class);

        assertThat(result.getSystem()).isEqualTo(SystemType.CASE_DATA);
        assertThat(result.getCaseType()).isEqualTo(CaseType.PARKING_PERMIT);
        assertThat(result.getExternalCaseId()).isEqualTo("231");
        assertThat(result.getCaseId()).isEqualTo("24");
        assertThat(result.getStatus()).isEqualTo("Ärende inkommit");
        assertThat(result.getServiceName()).isEqualTo("Parkeringstillstånd");

    }

    @Test
    void test4_GetStatusByOrgNr() throws JsonProcessingException, ClassNotFoundException {
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
}