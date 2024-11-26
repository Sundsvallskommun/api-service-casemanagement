package se.sundsvall.casemanagement;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import se.sundsvall.casemanagement.api.model.CaseStatusDTO;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@Testcontainers
@WireMockAppTestSuite(files = "classpath:/CaseStatusIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class CaseStatusIT extends AbstractAppTest {

	private static final String RESPONSE_FILE = "response.json";
	private static final String PARTY_ID = "f2af73ba-fa39-474b-b2ff-cc420158266d";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String PATH = "/" + MUNICIPALITY_ID + "/cases/{externalCaseId}/status";
	private static final String ORG_PATH = "/" + MUNICIPALITY_ID + "/organization/{organizationNumber}/cases/status";
	private static final String STATUS_BY_PARTY_PATH = "/{municipalityId}/{partyId}/statuses";

	@Test
	void test1_GetEcosStatusByExternalCaseId() throws JsonProcessingException, ClassNotFoundException {
		final var ECOS_NUMBER = "MK-2021-837";
		final var externalCaseId = "2223";

		final var result = setupCall()
			.withHttpMethod(GET)
			.withServicePath(PATH.replace("{externalCaseId}", externalCaseId))
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseStatusDTO.class);

		assertThat(result.getSystem()).isEqualTo(SystemType.ECOS);
		assertThat(result.getCaseType()).isEqualTo(CaseType.REGISTRERING_AV_LIVSMEDEL.toString());
		assertThat(result.getExternalCaseId()).isEqualTo("2223");
		assertThat(result.getCaseId()).isEqualTo(ECOS_NUMBER);
		assertThat(result.getStatus()).isEqualTo("Begäran om anstånd");
		assertThat(result.getServiceName()).isEqualTo("Registrering av livsmedelsanläggning");

	}

	@Test
	void test2_GetByggrStatusByExternalCaseId() throws JsonProcessingException, ClassNotFoundException {
		final var BYGGR_NUMBER = "BYGG 2021-000200";
		final var externalCaseId = "3522";

		final var result = setupCall()
			.withHttpMethod(GET)
			.withServicePath(PATH.replace("{externalCaseId}", externalCaseId))
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseStatusDTO.class);

		assertThat(result.getSystem()).isEqualTo(SystemType.BYGGR);
		assertThat(result.getCaseType()).isEqualTo(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV.toString());
		assertThat(result.getExternalCaseId()).isEqualTo(externalCaseId);
		assertThat(result.getCaseId()).isEqualTo(BYGGR_NUMBER);
		assertThat(result.getStatus()).isEqualTo("ANSÖKAN");
		assertThat(result.getServiceName()).isEqualTo("Ansökan - strandskyddsdispens");
	}

	@Test
	void test3_GetCaseDataStatusByExternalCaseId() throws JsonProcessingException, ClassNotFoundException {
		final var externalCaseId = "231";

		final var result = setupCall()
			.withHttpMethod(GET)
			.withServicePath(PATH.replace("{externalCaseId}", externalCaseId))
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseStatusDTO.class);

		assertThat(result.getSystem()).isEqualTo(SystemType.CASE_DATA);
		assertThat(result.getCaseType()).isEqualTo(CaseType.PARKING_PERMIT.toString());
		assertThat(result.getExternalCaseId()).isEqualTo("231");
		assertThat(result.getCaseId()).isEqualTo("24");
		assertThat(result.getStatus()).isEqualTo("Ärende inkommit");
		assertThat(result.getServiceName()).isEqualTo("Parkeringstillstånd");

	}

	@Test
	void test4_GetStatusByOrgNr() throws JsonProcessingException, ClassNotFoundException {
		final String organizationNumber = "123456-4321";

		final var getStatusResponse = Arrays.asList(setupCall()
			.withHttpMethod(GET)
			.withServicePath(ORG_PATH.replace("{organizationNumber}", organizationNumber))
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseStatusDTO[].class));

		assertThat(getStatusResponse.stream().anyMatch(caseStatus -> "Begäran om anstånd".equals(caseStatus.getStatus()))).isTrue();
		assertThat(getStatusResponse.stream().anyMatch(caseStatus -> "ANSÖKAN".equals(caseStatus.getStatus()))).isTrue();
	}

	/**
	 * Tests the scenario where PartyType is PRIVATE and only CaseData case is found.
	 */
	@Test
	void test5_getStatusesByPartyId_1() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(STATUS_BY_PARTY_PATH
				.replace("{municipalityId}", MUNICIPALITY_ID)
				.replace("{partyId}", PARTY_ID))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * Tests the scenario where PartyType is ENTERPRISE and only CaseData case is found.
	 */
	@Test
	void test6_getStatusesByPartyId_2() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(STATUS_BY_PARTY_PATH
				.replace("{municipalityId}", MUNICIPALITY_ID)
				.replace("{partyId}", PARTY_ID))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * Tests the scenario where PartyType is PRIVATE and only Byggr case is found.
	 */
	@Test
	void test7_getStatusesByPartyId_3() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(STATUS_BY_PARTY_PATH
				.replace("{municipalityId}", MUNICIPALITY_ID)
				.replace("{partyId}", PARTY_ID))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * Tests the scenario where PartyType is ENTERPRISE and only Byggr case is found.
	 */
	@Test
	void test8_getStatusesByPartyId_4() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(STATUS_BY_PARTY_PATH
				.replace("{municipalityId}", MUNICIPALITY_ID)
				.replace("{partyId}", PARTY_ID))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * Tests the scenario where PartyType is PRIVATE and only Ecos case is found.
	 */
	@Test
	void test9_getStatusesByPartyId_5() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(STATUS_BY_PARTY_PATH
				.replace("{municipalityId}", MUNICIPALITY_ID)
				.replace("{partyId}", PARTY_ID))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * Tests the scenario where PartyType is ENTERPRISE and only Ecos case is found.
	 */
	@Test
	void test10_getStatusesByPartyId_6() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(STATUS_BY_PARTY_PATH
				.replace("{municipalityId}", MUNICIPALITY_ID)
				.replace("{partyId}", PARTY_ID))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

}
