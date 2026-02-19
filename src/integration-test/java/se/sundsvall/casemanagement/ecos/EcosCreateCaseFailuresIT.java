package se.sundsvall.casemanagement.ecos;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.casemanagement.Application;
import se.sundsvall.casemanagement.api.model.CaseResourceResponseDTO;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.CaseRepository;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("it")
@Testcontainers
@WireMockAppTestSuite(files = "classpath:/EcosCreateCaseFailuresIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
public class EcosCreateCaseFailuresIT extends AbstractAppTest {

	public static final String ECOS_CASE_ID = "e19981ad-34b2-4e14-88f5-133f61ca85aa";
	private static final String REQUEST = "request.json";
	private static final String RESPONSE = "response.json";
	private static final String MUNICIPALITY_ID = "2281";

	private static final String PATH = "/" + MUNICIPALITY_ID + "/cases";

	@Autowired
	private CaseMappingRepository caseMappingRepository;

	@Autowired
	private CaseRepository caseRepository;

	// Okay this is not actually a failure test from our perspective.
	// But it is a failure test from the perspective of ECOS since the
	// case is not created correctly and a so-called "occurrence" is created on case.
	// And the test uses different mocks than other happy path tests.
	@Test
	void test1_LivsmedelMovingFacility() throws JsonProcessingException, ClassNotFoundException {

		final var result = setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath(PATH)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		// Make sure that there doesn't exist a case entity
		assertThat(caseRepository.findById("1256239125")).isEmpty();
		// Make sure that there exists a case mapping
		final var caseMapping = caseMappingRepository.findByExternalCaseIdAndMunicipalityId("1256239125", MUNICIPALITY_ID);
		assertThat(caseMapping).isNotNull();
		assertThat(caseMapping.getExternalCaseId()).isEqualTo("1256239125");
		assertThat(caseMapping.getCaseType()).isEqualTo(CaseType.REGISTRERING_AV_LIVSMEDEL.toString());
		assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
		assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
		caseMappingRepository.delete(caseMapping);
	}

	@Test
	void test2_LivsmedelEndBeforeStart() throws JsonProcessingException, ClassNotFoundException {
		final var EXTERNAL_CASE_ID = "1722008445";

		setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath(PATH)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(HttpStatus.BAD_REQUEST)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(ConstraintViolationProblem.class);

		// Make sure that there doesn't exist a case entity
		assertThat(caseRepository.findById(EXTERNAL_CASE_ID)).isEmpty();
		// Make sure that there doesn't exist a case mapping
		assertThat(caseMappingRepository.findByExternalCaseIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID))
			.isNull();
	}

	@Test
	void test3_Livsmedel500Response() throws JsonProcessingException, ClassNotFoundException {

		final var EXTERNAL_CASE_ID = "1307815498";

		final var result = setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath(PATH)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(this.wiremock.findAllUnmatchedRequests()).isEmpty();
		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		//  Make sure that there exists a case entity
		assertThat(caseRepository.findById(EXTERNAL_CASE_ID)).isPresent();
		// Make sure that there doesn't exist a case mapping
		assertThat(caseMappingRepository.findByExternalCaseIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID))
			.isNull();
	}

	@Test
	void test4_PropertyDesignationNotFound() throws JsonProcessingException, ClassNotFoundException {

		final var EXTERNAL_CASE_ID = "513913320";

		final var result = setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath(PATH)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(this.wiremock.findAllUnmatchedRequests()).isEmpty();
		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		// Make sure that there exists a case entity
		assertThat(caseRepository.findById(EXTERNAL_CASE_ID)).isPresent();
		// Make sure that there doesn't exist a case mapping
		assertThat(caseMappingRepository.findByExternalCaseIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID))
			.isNull();
	}

}
