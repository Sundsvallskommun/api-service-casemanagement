package se.sundsvall.casemanagement;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@Testcontainers
@WireMockAppTestSuite(files = "classpath:/AttachmentsIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class AttachmentsIT extends AbstractAppTest {

	private static final String REQUEST = "request.json";
	private static final String RESPONSE = "response.json";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String PATH = "/" + MUNICIPALITY_ID + "/cases/";

	@Test
	void test1_postEcosAttachment() {

		setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath(PATH + "2222/attachments")
			.withRequest(REQUEST)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse()
			.verifyAllStubs();
	}

	@Test
	void test2_postByggrAttachment() {

		setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath(PATH + "3522/attachments")
			.withRequest(REQUEST)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse()
			.verifyAllStubs();
	}

	@Test
	void test3_postCasedataAttachment() {

		setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath(PATH + "231/attachments")
			.withRequest(REQUEST)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse()
			.verifyAllStubs();

	}

	@Test
	void test4_CaseNotFound() {

		setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath(PATH + "123/attachments")
			.withRequest(REQUEST)
			.withExpectedResponse(RESPONSE)
			.withExpectedResponseStatus(NOT_FOUND)
			.sendRequestAndVerifyResponse()
			.verifyAllStubs();

	}

	@Test
	void test5_InvalidRequest() {
		setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath(PATH + "231/attachments")
			.withRequest(REQUEST)
			.withExpectedResponse(RESPONSE)
			.withExpectedResponseStatus(BAD_REQUEST)
			.sendRequestAndVerifyResponse()
			.verifyAllStubs();
	}

}
