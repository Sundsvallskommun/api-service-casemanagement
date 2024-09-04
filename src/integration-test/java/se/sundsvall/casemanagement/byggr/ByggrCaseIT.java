package se.sundsvall.casemanagement.byggr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
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
@DirtiesContext
class ByggrCaseIT extends AbstractAppTest {

	private static final String REQUEST = "request.json";
	private static final String PATH = "/cases";

	@Autowired
	private CaseMappingRepository caseMappingRepository;

	@Autowired
	private CaseRepository caseRepository;

	@Test
	void test1_PostByggrCase() throws JsonProcessingException, ClassNotFoundException {

		final var EXTERNAL_CASE_ID = "5123";

		final var result = setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath(PATH)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse("expected-response.json")
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		// Make sure that there doesn't exist a case entity
		assertThat(caseRepository.findById(EXTERNAL_CASE_ID)).isEmpty();
		// Make sure that there exists a case mapping
		assertThat(caseMappingRepository.findAllByExternalCaseId(EXTERNAL_CASE_ID))
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
	void test2_putByggrCase() {
		setupCall()
			.withHttpMethod(PUT)
			.withServicePath(uriBuilder -> uriBuilder.path(PATH + "/{externalCaseId}").build(Map.of("externalCaseId", "5123")))
			.withRequest(REQUEST)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();
	}

}
