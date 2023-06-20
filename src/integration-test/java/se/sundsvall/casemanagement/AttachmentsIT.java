package se.sundsvall.casemanagement;


import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;

import se.sundsvall.casemanagement.testutils.CustomAbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@Testcontainers
@WireMockAppTestSuite(files = "classpath:/AttachmentsIT/", classes = Application.class)
public class AttachmentsIT extends CustomAbstractAppTest {

    @Test
    void test1_postEcosAttachment() {

        setupCall()
            .withHttpMethod(HttpMethod.POST)
            .withServicePath("/cases/2222/attachments")
            .withRequest("request.json")
            .withExpectedResponseStatus(HttpStatus.NO_CONTENT)
            .sendRequestAndVerifyResponse();
    }


    @Test
    void test2_postByggrAttachment() {

        setupCall()
            .withHttpMethod(HttpMethod.POST)
            .withServicePath("/cases/3522/attachments")
            .withRequest("request.json")
            .withExpectedResponseStatus(HttpStatus.NO_CONTENT)
            .sendRequestAndVerifyResponse();
    }


    @Test
    void test3_postCasedataAttachment() {

        setupCall()
            .withHttpMethod(HttpMethod.POST)
            .withServicePath("/cases/231/attachments")
            .withRequest("request.json")
            .withExpectedResponseStatus(HttpStatus.NO_CONTENT)
            .sendRequestAndVerifyResponse();

    }

    @Test
    void test4_CaseNotFound() {

        setupCall()
            .withHttpMethod(HttpMethod.POST)
            .withServicePath("/cases/123/attachments")
            .withRequest("request.json")
            .withExpectedResponse("expected-response.json")
            .withExpectedResponseStatus(HttpStatus.NOT_FOUND)
            .sendRequestAndVerifyResponse();

    }

    @Test
    void test5_InvalidRequest() {
        setupCall()
            .withHttpMethod(HttpMethod.POST)
            .withServicePath("/cases/231/attachments")
            .withRequest("request.json")
            .withExpectedResponse("expected-response.json")
            .withExpectedResponseStatus(HttpStatus.BAD_REQUEST)
            .sendRequestAndVerifyResponse();
    }

}
