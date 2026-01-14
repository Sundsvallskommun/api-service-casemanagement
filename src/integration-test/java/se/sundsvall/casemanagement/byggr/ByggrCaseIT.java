package se.sundsvall.casemanagement.byggr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
@WireMockAppTestSuite(files = "classpath:/ByggrCaseIT", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
@DirtiesContext
class ByggrCaseIT extends AbstractAppTest {

	private static final String MUNICIPALITY_ID = "2281";

	private static final String REQUEST = "request.json";
	private static final String RESPONSE = "response.json";

	private static final String PATH = "/" + MUNICIPALITY_ID + "/cases";

	@Autowired
	private CaseMappingRepository caseMappingRepository;

	@Autowired
	private CaseRepository caseRepository;

	@Test
	void test1_PostByggrCase() throws JsonProcessingException, ClassNotFoundException {

		final var EXTERNAL_CASE_ID = "5123";

		final var result = setupCall()
			.withHttpMethod(POST)
			.withServicePath(PATH)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		// Make sure that there doesn't exist a case entity
		assertThat(caseRepository.findById(EXTERNAL_CASE_ID)).isEmpty();
		// Make sure that there exists a case mapping
		assertThat(caseMappingRepository.findAllByExternalCaseIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID))
			.isNotNull()
			.hasSize(1)
			.allSatisfy(caseMapping -> {
				assertThat(caseMapping.getExternalCaseId()).isEqualTo(EXTERNAL_CASE_ID);
				assertThat(caseMapping.getCaseId()).isEqualTo("BYGG 2021-000200");
				assertThat(caseMapping.getCaseType()).isEqualTo(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV.toString());
				assertThat(caseMapping.getSystem()).isEqualTo(SystemType.BYGGR);
			});
	}

	@Test
	void test2_respondToNeighborhoodNotification() {
		setupCall()
			.withHttpMethod(POST)
			.withServicePath(PATH)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_addCertifiedInspector() {
		setupCall()
			.withHttpMethod(POST)
			.withServicePath(PATH)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test4_addAdditionalDocuments() {
		setupCall()
			.withHttpMethod(POST)
			.withServicePath(PATH)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test5_postByggrCaseSundsvallsKommunPropertyOwner() throws JsonProcessingException, ClassNotFoundException {

		final var EXTERNAL_CASE_ID = "5124";

		final var result = setupCall()
			.withHttpMethod(POST)
			.withServicePath(PATH)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");
		// Make sure that there doesn't exist a case entity
		assertThat(caseRepository.findById(EXTERNAL_CASE_ID)).isEmpty();
		// Make sure that there exists a case mapping
		final var all = caseMappingRepository.findAll();
		assertThat(caseMappingRepository.findAllByExternalCaseIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID))
			.isNotNull()
			.hasSize(1)
			.allSatisfy(caseMapping -> {
				assertThat(caseMapping.getExternalCaseId()).isEqualTo(EXTERNAL_CASE_ID);
				assertThat(caseMapping.getCaseId()).isEqualTo("BYGG 2021-000200");
				assertThat(caseMapping.getCaseType()).isEqualTo(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV.toString());
				assertThat(caseMapping.getSystem()).isEqualTo(SystemType.BYGGR);
			});

	}

}
