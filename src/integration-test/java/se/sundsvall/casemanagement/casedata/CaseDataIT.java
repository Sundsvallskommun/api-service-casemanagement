package se.sundsvall.casemanagement.casedata;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
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

@Testcontainers
@WireMockAppTestSuite(files = "classpath:/CasedataIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
@DirtiesContext
public class CaseDataIT extends AbstractAppTest {

	public static final String CASE_DATA_ID = "24";

	private static final String MUNICIPALITY_ID = "2281";

	private static final String PATH = "/" + MUNICIPALITY_ID + "/cases";

	@Autowired
	private CaseMappingRepository caseMappingRepository;

	@Autowired
	private CaseRepository caseRepository;

	@Test
	void test1_PostParkingPermitCase() throws JsonProcessingException, ClassNotFoundException {
		final var EXTERNAL_CASE_ID = "40621444";

		final var result = setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath(PATH)
			.withRequest("request.json")
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse("expected-response.json")
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		// Make sure that there doesn't exist a case entity
		assertThat(caseRepository.findByIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID)).isEmpty();
		// Make sure that there exists a case mapping
		assertThat(caseMappingRepository.findAllByExternalCaseIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID))
			.isNotNull()
			.hasSize(1)
			.allSatisfy(caseMapping -> {
				assertThat(caseMapping.getExternalCaseId()).isEqualTo(EXTERNAL_CASE_ID);
				assertThat(caseMapping.getCaseId()).isEqualTo(CASE_DATA_ID);
				assertThat(caseMapping.getCaseType()).isEqualTo(CaseType.PARKING_PERMIT.toString());
				assertThat(caseMapping.getSystem()).isEqualTo(SystemType.CASE_DATA);
			});
	}

	@Test
	void test2_postCaseInternalError() {

		final var EXTERNAL_CASE_ID = "40621445";

		setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath(PATH)
			.withRequest("request.json")
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse("expected-response.json")
			.sendRequestAndVerifyResponse();

		// Make sure that there exists a case entity
		assertThat(caseRepository.findByIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID)).isPresent();
		// Make sure that there doesn't exist a case mapping
		assertThat(caseMappingRepository.findAllByExternalCaseIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID))
			.isNotNull()
			.isEmpty();
	}

	@Test
	void test3_updateCase() {

		setupCall()
			.withHttpMethod(HttpMethod.PUT)
			.withServicePath("/" + MUNICIPALITY_ID + "/cases/231")
			.withRequest("request.json")
			.withExpectedResponseStatus(HttpStatus.NO_CONTENT)
			.sendRequestAndVerifyResponse();
	}

}
