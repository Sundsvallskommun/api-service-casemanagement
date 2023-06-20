package se.sundsvall.casemanagement.byggr;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;

import se.sundsvall.casemanagement.Application;
import se.sundsvall.casemanagement.testutils.CustomAbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@Testcontainers
@WireMockAppTestSuite(files = "classpath:/ByggrCreateCaseFailuresIT/", classes = Application.class)
public class ByggrCreateCaseFailuresIT extends CustomAbstractAppTest {


    @Test
    void test1_postWithExistingExternalCaseId() {

        setupCall()
            .withHttpMethod(HttpMethod.POST)
            .withServicePath("/cases")
            .withRequest("request.json")
            .withExpectedResponseStatus(HttpStatus.BAD_REQUEST)
            .withExpectedResponse("expected-response.json")
            .sendRequestAndVerifyResponse();
    }


    @Test
    void test2_MultipleMainFacility() {
        setupCall()
            .withHttpMethod(HttpMethod.POST)
            .withServicePath("/cases")
            .withRequest("request.json")
            .withExpectedResponseStatus(HttpStatus.BAD_REQUEST)
            .withExpectedResponse("expected-response.json")
            .sendRequestAndVerifyResponse();
    }

    @Test
    void test3_CaseWithoutFacility() {
        setupCall()
            .withHttpMethod(HttpMethod.POST)
            .withServicePath("/cases")
            .withRequest("request.json")
            .withExpectedResponseStatus(HttpStatus.BAD_REQUEST)
            .withExpectedResponse("expected-response.json")
            .sendRequestAndVerifyResponse();
    }

    @Test
    void test4_CaseAttefallFacility() {
        setupCall()
            .withHttpMethod(HttpMethod.POST)
            .withServicePath("/cases")
            .withRequest("request.json")
            .withExpectedResponseStatus(HttpStatus.BAD_REQUEST)
            .withExpectedResponse("expected-response.json")
            .sendRequestAndVerifyResponse();

    }
}
