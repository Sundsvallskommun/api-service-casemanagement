package se.sundsvall.casemanagement.future;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

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
import se.sundsvall.casemanagement.integration.db.model.DeliveryStatus;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@Testcontainers
@WireMockAppTestSuite(files = "classpath:/EDPFutureCaseIT", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
@DirtiesContext
class EDPFutureCaseIT extends AbstractAppTest {

	private static final String MUNICIPALITY_ID = "2281";

	private static final String REQUEST = "request.json";
	private static final String RESPONSE = "response.json";

	private static final String PATH = "/" + MUNICIPALITY_ID + "/cases";

	@Autowired
	private CaseMappingRepository caseMappingRepository;

	@Autowired
	private CaseRepository caseRepository;

	@Test
	void test1_PostFutureCase() throws ClassNotFoundException {

		final var EXTERNAL_CASE_ID = "7001";

		final var result = setupCall()
			.withHttpMethod(POST)
			.withServicePath(PATH)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequest()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		await().atMost(15, SECONDS).until(() ->
			caseMappingRepository.findByExternalCaseIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID) != null);

		verifyStubs();

		final var caseMapping = caseMappingRepository.findByExternalCaseIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID);
		assertThat(caseMapping.getExternalCaseId()).isEqualTo(EXTERNAL_CASE_ID);
		assertThat(caseMapping.getCaseId()).isEqualTo(EXTERNAL_CASE_ID);
		assertThat(caseMapping.getCaseType()).isEqualTo(CaseType.EXTRA_SACK.toString());
		assertThat(caseMapping.getSystem()).isEqualTo(SystemType.EDPFUTURE);
	}

	@Test
	void test2_PostFutureCaseSOAPError() throws ClassNotFoundException {

		final var EXTERNAL_CASE_ID = "7002";

		final var result = setupCall()
			.withHttpMethod(POST)
			.withServicePath(PATH)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequest()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		// Wait for async failure handling to complete (sets DeliveryStatus.FAILED)
		await().atMost(15, SECONDS).until(() ->
			caseRepository.findByIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID)
				.map(entity -> entity.getDeliveryStatus() == DeliveryStatus.FAILED)
				.orElse(false));

		verifyStubs();

		// Verify no CaseMapping was created since processing failed
		assertThat(caseMappingRepository.findByExternalCaseIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID)).isNull();
	}

}
