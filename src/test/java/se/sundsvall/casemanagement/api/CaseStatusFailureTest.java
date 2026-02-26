package se.sundsvall.casemanagement.api;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.casemanagement.Application;
import se.sundsvall.casemanagement.integration.byggr.ByggrService;
import se.sundsvall.casemanagement.integration.casedata.CaseDataService;
import se.sundsvall.casemanagement.integration.ecos.EcosService;
import se.sundsvall.casemanagement.service.CaseMappingService;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class CaseStatusFailureTest {

	@MockitoBean
	private ByggrService byggrService;

	@MockitoBean
	private EcosService ecosService;

	@MockitoBean
	private CaseDataService caseDataService;

	@MockitoBean
	private CaseMappingService caseMappingService;

	@Autowired
	private WebTestClient webTestClient;

	@AfterEach
	void afterEach() {
		verifyNoInteractions(byggrService, ecosService, caseDataService, caseMappingService);
	}

	@Test
	void getStatusByOrgNrInvalidMunicipalityId() {
		var municipalityId = "invalidMunicipalityId";
		var organizationNumber = "20220622-2396";
		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/" + municipalityId + "/organization/{organizationNumber}/cases/status").build(organizationNumber))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).satisfies(problem -> {
			assertThat(problem.getTitle()).isEqualTo("Constraint Violation");
			assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(problem.getViolations()).extracting("field", "message").containsExactlyInAnyOrder(
				tuple("getStatusByOrgNr.municipalityId", "not a valid municipality ID"));
		});
	}

	@Test
	void getStatusByOrgNrInvalidOrganizationNumber() {
		var municipalityId = "2281";
		var organizationNumber = "invalid organization number";
		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/" + municipalityId + "/organization/{organizationNumber}/cases/status").build(organizationNumber))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).satisfies(problem -> {
			assertThat(problem.getTitle()).isEqualTo("Constraint Violation");
			assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(problem.getViolations()).extracting("field", "message").containsExactlyInAnyOrder(
				tuple("getStatusByOrgNr.organizationNumber", "organizationNumber must consist of 10 or 12 digits. 10 digit orgnr must follow this format: \"XXXXXX-XXXX\". 12 digit orgnr must follow this format: \"(18|19|20)XXXXXX-XXXX\"."));
		});
	}

	@Test
	void getStatusByExternalCaseIdInvalidMunicipalityId() {
		var municipalityId = "invalidMunicipalityId";
		var externalCaseId = "not-validated";
		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/" + municipalityId + "/cases/{externalCaseId}/status").build(externalCaseId))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).satisfies(problem -> {
			assertThat(problem.getTitle()).isEqualTo("Constraint Violation");
			assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(problem.getViolations()).extracting("field", "message").containsExactlyInAnyOrder(
				tuple("getStatusByExternalCaseId.municipalityId", "not a valid municipality ID"));
		});
	}

	@Test
	void getStatusesByPartyIdInvalidMunicipalityId() {
		var municipalityId = "invalidMunicipalityId";
		var partyId = UUID.randomUUID().toString();
		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/" + municipalityId + "/{partyId}/statuses").build(partyId))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).satisfies(problem -> {
			assertThat(problem.getTitle()).isEqualTo("Constraint Violation");
			assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(problem.getViolations()).extracting("field", "message").containsExactlyInAnyOrder(
				tuple("getStatusesByPartyId.municipalityId", "not a valid municipality ID"));
		});
	}

	@Test
	void getStatusesByPartyIdInvalidPartyId() {
		var municipalityId = "2281";
		var partyId = "Not a valid UUID";
		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/" + municipalityId + "/{partyId}/statuses").build(partyId))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).satisfies(problem -> {
			assertThat(problem.getTitle()).isEqualTo("Constraint Violation");
			assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(problem.getViolations()).extracting("field", "message").containsExactlyInAnyOrder(
				tuple("getStatusesByPartyId.partyId", "not a valid UUID"));
		});
	}
}
