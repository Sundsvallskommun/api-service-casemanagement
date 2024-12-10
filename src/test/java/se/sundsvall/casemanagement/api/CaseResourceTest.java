package se.sundsvall.casemanagement.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

	private static final String MUNICIPALITY_ID = "2281";

	private static final String PATH = "/" + MUNICIPALITY_ID + "/cases";

	@MockitoBean
	private CaseMappingService caseMappingService;

	@MockitoBean
	private CaseService caseService;

	@MockitoBean
	private CaseDataService caseDataService;

	@Captor
	private ArgumentCaptor<CaseDTO> caseDTOCaptor;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void postCase_Ecos(@Load("/case-resource/ecos-case.json") final String body) {
		final var result = webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(PATH).build())
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isOk()
			.expectBody(CaseResourceResponseDTO.class)
			.returnResult()
			.getResponseBody();

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		verify(caseService).handleCase(caseDTOCaptor.capture(), eq(MUNICIPALITY_ID));
		assertThat(caseDTOCaptor.getValue()).satisfies(caseDTO -> {
			assertThat(caseDTO).isInstanceOf(EcosCaseDTO.class);
			assertThat(caseDTO.getExternalCaseId()).isEqualTo("externalCaseId");
			assertThat(caseDTO.getCaseType()).isEqualTo("REGISTRERING_AV_LIVSMEDEL");
		});

		verify(caseMappingService).validateUniqueCase(caseDTOCaptor.getValue(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(caseService, caseMappingService);
		verifyNoInteractions(caseDataService);
	}

	@Test
	void postCase_ByggR(@Load("/case-resource/byggr-case.json") final String body) {
		final var result = webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(PATH).build())
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isOk()
			.expectBody(CaseResourceResponseDTO.class)
			.returnResult()
			.getResponseBody();

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		verify(caseService).handleCase(caseDTOCaptor.capture(), eq(MUNICIPALITY_ID));
		assertThat(caseDTOCaptor.getValue()).satisfies(caseDTO -> {
			assertThat(caseDTO).isInstanceOf(ByggRCaseDTO.class);
			assertThat(caseDTO.getExternalCaseId()).isEqualTo("externalCaseId");
			assertThat(caseDTO.getCaseType()).isEqualTo("ANMALAN_ATTEFALL");
		});

		verify(caseMappingService).validateUniqueCase(caseDTOCaptor.getValue(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(caseService, caseMappingService);
		verifyNoInteractions(caseDataService);
	}

	@Test
	void postCase_Other(@Load("/case-resource/other-case.json") final String body) {
		final var result = webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(PATH).build())
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isOk()
			.expectBody(CaseResourceResponseDTO.class)
			.returnResult()
			.getResponseBody();

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		verify(caseService).handleCase(caseDTOCaptor.capture(), eq(MUNICIPALITY_ID));
		assertThat(caseDTOCaptor.getValue()).satisfies(caseDTO -> {
			assertThat(caseDTO).isInstanceOf(OtherCaseDTO.class);
			assertThat(caseDTO.getExternalCaseId()).isEqualTo("externalCaseId");
			assertThat(caseDTO.getCaseType()).isEqualTo("PARKING_PERMIT");
		});

		verify(caseMappingService).validateUniqueCase(caseDTOCaptor.getValue(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(caseService, caseMappingService);
		verifyNoInteractions(caseDataService);
	}

	@Test
	void putCase_ByggRCase(@Load("/case-resource/byggr-neighborhood-notification-case.json") final String body) {
		webTestClient.post()
			.uri(PATH)
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isOk();

		verify(caseMappingService).validateUniqueCase(caseDTOCaptor.capture(), eq(MUNICIPALITY_ID));
		final var caseDTO = caseDTOCaptor.getValue();
		assertThat(caseDTO).isInstanceOf(ByggRCaseDTO.class);
		verify(caseService).handleCase(caseDTO, MUNICIPALITY_ID);
	}

	@Test
	void putCase_OtherCase(@Load("/case-resource/put-other-case.json") final String body) {
		when(caseMappingService.getCaseMapping("externalCaseId", MUNICIPALITY_ID)).thenReturn(CaseMapping.builder()
			.withCaseId("12345")
			.build());

		webTestClient.put()
			.uri(uriBuilder -> uriBuilder.path(PATH + "/{externalCaseId}").build("externalCaseId"))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isNoContent();

		verify(caseMappingService).getCaseMapping("externalCaseId", MUNICIPALITY_ID);
		verify(caseDataService).putErrand(anyLong(), any(OtherCaseDTO.class), eq(MUNICIPALITY_ID));
		verifyNoMoreInteractions(caseMappingService, caseDataService);
		verifyNoInteractions(caseService);
	}

	@Test
	void putCase_WrongCaseType(@Load("/case-resource/put-wrong-case.json") final String body) {
		final var result = webTestClient.put()
			.uri(uriBuilder -> uriBuilder.path(PATH + "/{externalCaseId}").build("externalCaseId"))
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

	@Test
	void postCase_addAdditionalDocuments(@Load("/case-resource/byggr-add-additional-documents.json") final String body) {
		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(PATH).build())
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isOk()
			.expectBody(CaseResourceResponseDTO.class)
			.returnResult();

		verify(caseMappingService).validateUniqueCase(caseDTOCaptor.capture(), eq(MUNICIPALITY_ID));
		final var caseDTO = caseDTOCaptor.getValue();
		assertThat(caseDTO).isInstanceOf(ByggRCaseDTO.class);
		verify(caseService).handleCase(caseDTO, MUNICIPALITY_ID);
	}

}
