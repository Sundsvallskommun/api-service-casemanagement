package se.sundsvall.casemanagement.byggr;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.casemanagement.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

//@Testcontainers
@WireMockAppTestSuite(files = "classpath:/ByggrCreateCaseFailuresIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class ByggrCreateCaseFailuresIT extends AbstractAppTest {

	private static final String REQUEST = "request.json";
	private static final String RESPONSE = "response.json";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String PATH = "/" + MUNICIPALITY_ID + "/cases";

	@Test
	void test1_postWithExistingExternalCaseId() {

		setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath(PATH)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(BAD_REQUEST)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_MultipleMainFacility() {
		setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath(PATH)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(BAD_REQUEST)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_CaseWithoutFacility() {
		setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath(PATH)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(BAD_REQUEST)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test4_CaseAttefallFacility() {
		setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath(PATH)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(BAD_REQUEST)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

}
