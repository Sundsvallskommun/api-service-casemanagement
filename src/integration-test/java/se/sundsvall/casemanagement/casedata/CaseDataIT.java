package se.sundsvall.casemanagement.casedata;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import se.sundsvall.casemanagement.Application;
import se.sundsvall.casemanagement.api.model.CaseResourceResponseDTO;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.CaseRepository;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

@Testcontainers
@WireMockAppTestSuite(files = "classpath:/CasedataIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
@DirtiesContext
class CaseDataIT extends AbstractAppTest {

	private static final String REQUEST = "request.json";
	private static final String RESPONSE = "response.json";
	private static final String CASE_DATA_ID = "24";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String MUNICIPALITY_ID_ANGE = "2260";
	private static final String PATH = "/" + MUNICIPALITY_ID + "/cases";
	private static final String PATH_ANGE = "/" + MUNICIPALITY_ID_ANGE + "/cases";

	@Autowired
	private CaseMappingRepository caseMappingRepository;

	@Autowired
	private CaseRepository caseRepository;

	@Test
	void test1_PostParkingPermitCase() throws JsonProcessingException, ClassNotFoundException {
		final var EXTERNAL_CASE_ID = "40621444";

		final var result = setupCall()
			.withHttpMethod(POST)
			.withServicePath(PATH)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		// Make sure that there doesn't exist a case entity
		assertThat(caseRepository.findByIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID)).isEmpty();
		// Make sure that there exists a case mapping
		final var caseMapping = caseMappingRepository.findByExternalCaseIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID);
		assertThat(caseMapping).isNotNull();
		assertThat(caseMapping.getExternalCaseId()).isEqualTo(EXTERNAL_CASE_ID);
		assertThat(caseMapping.getCaseId()).isEqualTo(CASE_DATA_ID);
		assertThat(caseMapping.getCaseType()).isEqualTo(CaseType.PARKING_PERMIT.toString());
		assertThat(caseMapping.getSystem()).isEqualTo(SystemType.CASE_DATA);
	}

	@Test
	void test2_postCaseInternalError() {

		final var EXTERNAL_CASE_ID = "40621445";

		setupCall()
			.withHttpMethod(POST)
			.withServicePath(PATH)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();

		// Make sure that there exists a case entity
		assertThat(caseRepository.findByIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID)).isPresent();
		// Make sure that there doesn't exist a case mapping
		assertThat(caseMappingRepository.findByExternalCaseIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID))
			.isNull();
	}

	@Test
	void test3_updateCase() {

		setupCall()
			.withHttpMethod(PUT)
			.withServicePath("/" + MUNICIPALITY_ID + "/cases/231")
			.withRequest(REQUEST)
			.withExpectedResponseStatus(HttpStatus.NO_CONTENT)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test4_PostParkingPermitCaseAndAnge() throws JsonProcessingException, ClassNotFoundException {
		final var EXTERNAL_CASE_ID = "40621446";

		final var result = setupCall()
			.withHttpMethod(POST)
			.withServicePath(PATH_ANGE)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		// Make sure that there doesn't exist a case entity
		assertThat(caseRepository.findByIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID_ANGE)).isEmpty();
		// Make sure that there exists a case mapping
		final var caseMapping = caseMappingRepository.findByExternalCaseIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID_ANGE);
		assertThat(caseMapping).isNotNull();
		assertThat(caseMapping.getExternalCaseId()).isEqualTo(EXTERNAL_CASE_ID);
		assertThat(caseMapping.getCaseId()).isEqualTo(CASE_DATA_ID);
		assertThat(caseMapping.getCaseType()).isEqualTo(CaseType.PARKING_PERMIT.toString());
		assertThat(caseMapping.getSystem()).isEqualTo(SystemType.CASE_DATA);
	}
}
