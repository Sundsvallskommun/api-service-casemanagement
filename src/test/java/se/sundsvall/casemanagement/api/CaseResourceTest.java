package se.sundsvall.casemanagement.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.zalando.problem.Status.BAD_REQUEST;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;

import se.sundsvall.casemanagement.Application;
import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;
import se.sundsvall.casemanagement.api.model.CaseDTO;
import se.sundsvall.casemanagement.api.model.CaseResourceResponseDTO;
import se.sundsvall.casemanagement.api.model.EcosCaseDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.integration.casedata.CaseDataService;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.service.CaseMappingService;
import se.sundsvall.casemanagement.service.CaseService;
import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
@ExtendWith(ResourceLoaderExtension.class)
class CaseResourceTest {

	@MockBean
	private CaseMappingService caseMappingService;

	@MockBean
	private CaseService caseService;

	@MockBean
	private CaseDataService caseDataService;

	@Captor
	private ArgumentCaptor<CaseDTO> caseDTOCaptor;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void postCase_Ecos(@Load("/case-resource/ecos-case.json") final String body) {
		final var result = webTestClient.post().uri(uriBuilder -> uriBuilder.path("/cases").build())
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isOk()
			.expectBody(CaseResourceResponseDTO.class)
			.returnResult()
			.getResponseBody();

		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		verify(caseService).handleCase(caseDTOCaptor.capture());
		assertThat(caseDTOCaptor.getValue()).satisfies(c -> {
			assertThat(c).isInstanceOf(EcosCaseDTO.class);
			assertThat(c.getExternalCaseId()).isEqualTo("externalCaseId");
			assertThat(c.getCaseType()).isEqualTo("REGISTRERING_AV_LIVSMEDEL");
		});

		verify(caseMappingService).validateUniqueCase(caseDTOCaptor.getValue().getExternalCaseId());
		verifyNoMoreInteractions(caseService, caseMappingService);
		verifyNoInteractions(caseDataService);
	}

	@Test
	void postCase_ByggR(@Load("/case-resource/byggr-case.json") final String body) {
		final var result = webTestClient.post().uri(uriBuilder -> uriBuilder.path("/cases").build())
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isOk()
			.expectBody(CaseResourceResponseDTO.class)
			.returnResult()
			.getResponseBody();

		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		verify(caseService).handleCase(caseDTOCaptor.capture());
		assertThat(caseDTOCaptor.getValue()).satisfies(c -> {
			assertThat(c).isInstanceOf(ByggRCaseDTO.class);
			assertThat(c.getExternalCaseId()).isEqualTo("externalCaseId");
			assertThat(c.getCaseType()).isEqualTo("ANMALAN_ATTEFALL");
		});

		verify(caseMappingService).validateUniqueCase(caseDTOCaptor.getValue().getExternalCaseId());
		verifyNoMoreInteractions(caseService, caseMappingService);
		verifyNoInteractions(caseDataService);
	}

	@Test
	void postCase_Other(@Load("/case-resource/other-case.json") final String body) {
		final var result = webTestClient.post().uri(uriBuilder -> uriBuilder.path("/cases").build())
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isOk()
			.expectBody(CaseResourceResponseDTO.class)
			.returnResult()
			.getResponseBody();

		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		verify(caseService).handleCase(caseDTOCaptor.capture());
		assertThat(caseDTOCaptor.getValue()).satisfies(c -> {
			assertThat(c).isInstanceOf(OtherCaseDTO.class);
			assertThat(c.getExternalCaseId()).isEqualTo("externalCaseId");
			assertThat(c.getCaseType()).isEqualTo("PARKING_PERMIT");
		});

		verify(caseMappingService).validateUniqueCase(caseDTOCaptor.getValue().getExternalCaseId());
		verifyNoMoreInteractions(caseService, caseMappingService);
		verifyNoInteractions(caseDataService);
	}

	@Test
	void putCase_OtherCase(@Load("/case-resource/put-other-case.json") final String body) {
		when(caseMappingService.getCaseMapping("externalCaseId")).thenReturn(CaseMapping.builder()
			.withCaseId("12345")
			.build());

		webTestClient.put().uri(uriBuilder -> uriBuilder.path("/cases/{externalCaseId}").build("externalCaseId"))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isNoContent();

		verify(caseMappingService).getCaseMapping("externalCaseId");
		verify(caseDataService).putErrand(anyLong(), any(OtherCaseDTO.class));
		verifyNoMoreInteractions(caseMappingService, caseDataService);
		verifyNoInteractions(caseService);
	}

	@Test
	void putCase_WrongCaseType(@Load("/case-resource/put-wrong-case.json") final String body) {
		final var result = webTestClient.put().uri(uriBuilder -> uriBuilder.path("/cases/{externalCaseId}").build("externalCaseId"))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult().getResponseBody();

		assertThat(result).satisfies(response -> {
			assertThat(response.getTitle()).isEqualTo("Constraint Violation");
			assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		});

		verifyNoInteractions(caseMappingService, caseDataService, caseService);
	}

}
