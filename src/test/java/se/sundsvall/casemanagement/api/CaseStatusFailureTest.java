package se.sundsvall.casemanagement.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.zalando.problem.Status.BAD_REQUEST;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.casemanagement.Application;
import se.sundsvall.casemanagement.integration.byggr.ByggrService;
import se.sundsvall.casemanagement.integration.casedata.CaseDataService;
import se.sundsvall.casemanagement.integration.ecos.EcosService;
import se.sundsvall.casemanagement.service.CaseMappingService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class CaseStatusFailureTest {

	@MockBean
	private ByggrService byggrService;

	@MockBean
	private EcosService ecosService;

	@MockBean
	private CaseDataService caseDataService;

	@MockBean
	private CaseMappingService caseMappingService;

	@Autowired
	private WebTestClient webTestClient;

	@AfterEach
	void afterEach() {
		verifyNoInteractions(byggrService, ecosService, caseDataService, caseMappingService);
	}

	@Test
	void getStatusByOrgNr_invalidMunicipalityId() {
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
	void getStatusByOrgNr_invalidOrganizationNumber() {
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
	void getStatusByExternalCaseId_invalidMunicipalityId() {
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
	void getStatusesByPartyId_invalidMunicipalityId() {
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
	void getStatusesByPartyId_invalidPartyId() {
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
