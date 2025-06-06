package se.sundsvall.casemanagement.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.zalando.problem.Status.BAD_REQUEST;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.casemanagement.Application;
import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;
import se.sundsvall.casemanagement.api.model.CaseDTO;
import se.sundsvall.casemanagement.api.model.CaseResourceResponseDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.integration.casedata.CaseDataService;
import se.sundsvall.casemanagement.service.CaseMappingService;
import se.sundsvall.casemanagement.service.CaseService;
import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
@ExtendWith(ResourceLoaderExtension.class)
class CaseResourceFailureTest {

	private static final String MUNICIPALITY_ID = "2281";

	private static final String PATH = "/" + MUNICIPALITY_ID + "/cases";

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private WebTestClient webTestClient;

	@MockitoBean
	private CaseMappingService caseMappingServiceMock;

	@MockitoBean
	private CaseService caseServiceMock;

	@MockitoBean
	private CaseDataService caseDataServiceMock;

	@Captor
	private ArgumentCaptor<String> caseIdCaptor;

	@Captor
	private ArgumentCaptor<CaseDTO> caseDTOCaptor;

	private static Stream<Arguments> noExternalCaseId() {
		return Stream.of(
			Arguments.of("/case-resource-failure/ecos/no-externalCaseId.json"),
			Arguments.of("/case-resource-failure/byggr/no-externalCaseId.json"),
			Arguments.of("/case-resource-failure/other/no-externalCaseId.json"));
	}

	private static Stream<Arguments> emptyAttachments() {
		return Stream.of(
			Arguments.of("/case-resource-failure/ecos/no-attachment.json"),
			Arguments.of("/case-resource-failure/byggr/no-attachment.json"),
			Arguments.of("/case-resource-failure/other/no-attachment.json"));
	}

	private static Stream<Arguments> emptyStakeholders() {
		return Stream.of(
			Arguments.of("/case-resource-failure/ecos/no-stakeholder.json"),
			Arguments.of("/case-resource-failure/byggr/no-stakeholder.json"),
			Arguments.of("/case-resource-failure/other/no-stakeholder.json"));
	}

	@ParameterizedTest
	@MethodSource("emptyStakeholders")
	void postCaseEmptyStakeholders(final String path) throws IOException {
		final var body = resourceLoader.getResource("classpath:" + path)
			.getContentAsString(Charset.defaultCharset());

		final var result = webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(PATH).build())
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(result).satisfies(problem -> {
			assertThat(problem.getTitle()).isEqualTo("Constraint Violation");
			assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(problem.getViolations()).extracting("field", "message").containsExactlyInAnyOrder(
				tuple("stakeholders", "must not be empty"));
		});

		verifyNoInteractions(caseMappingServiceMock, caseServiceMock, caseDataServiceMock);
	}

	@ParameterizedTest
	@MethodSource("emptyAttachments")
	void postCaseEmptyAttachments(final String path) throws IOException {
		final var body = resourceLoader.getResource("classpath:" + path)
			.getContentAsString(Charset.defaultCharset());

		final var result = webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(PATH).build())
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(result).satisfies(problem -> {
			assertThat(problem.getTitle()).isEqualTo("Constraint Violation");
			assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(problem.getViolations()).extracting("field", "message").containsExactlyInAnyOrder(
				tuple("attachments", "must not be empty"));
		});

		verifyNoInteractions(caseMappingServiceMock, caseServiceMock, caseDataServiceMock);
	}

	@ParameterizedTest
	@MethodSource("noExternalCaseId")
	void postCaseNoExternalCaseId(final String path) throws IOException {
		final var body = resourceLoader.getResource("classpath:" + path)
			.getContentAsString(Charset.defaultCharset());

		final var result = webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(PATH).build())
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(result).satisfies(problem -> {
			assertThat(problem.getTitle()).isEqualTo("Constraint Violation");
			assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(problem.getViolations()).extracting("field", "message").containsExactlyInAnyOrder(
				tuple("externalCaseId", "must not be blank"));
		});

		verifyNoInteractions(caseMappingServiceMock, caseServiceMock, caseDataServiceMock);
	}

	@Test
	void postCaseEcosNoFacility(@Load("/case-resource-failure/ecos/no-facility.json") final String body) {
		final var result = webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(PATH).build())
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(result).satisfies(problem -> {
			assertThat(problem.getTitle()).isEqualTo("Constraint Violation");
			assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(problem.getViolations()).extracting("field", "message").containsExactlyInAnyOrder(
				tuple("facilities", "size must be 1"), tuple("facilities", "must not be empty"));
		});

		verifyNoInteractions(caseMappingServiceMock, caseServiceMock, caseDataServiceMock);
	}

	@Test
	void postCaseByggRNoFacility(@Load("/case-resource-failure/byggr/no-facility.json") final String body) {
		final ConstraintViolation<ByggRCaseDTO> constraintViolationMock = mock();
		final Path pathMock = mock(Path.class);
		when(pathMock.toString()).thenReturn("facilities");
		when(constraintViolationMock.getMessage()).thenReturn("must not be empty");
		when(constraintViolationMock.getPropertyPath()).thenReturn(pathMock);

		doThrow(new ConstraintViolationException(Set.of(constraintViolationMock))).when(caseServiceMock).handleCase(any(), any());

		final var result = webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(PATH).build())
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(result).satisfies(problem -> {
			assertThat(problem.getTitle()).isEqualTo("Constraint Violation");
			assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(problem.getViolations()).extracting("field", "message").containsExactlyInAnyOrder(
				tuple("facilities", "must not be empty"));
		});

		verifyNoInteractions(caseDataServiceMock);
	}

	// This case does not throw a bad request, this is by design to ensure that the different
	// sub-classes of caseDTO are handled individually.
	@Test
	void postCaseOtherNoFacility(@Load("/case-resource-failure/other/no-facility.json") final String body) {
		final var result = webTestClient
			.post().uri(uriBuilder -> uriBuilder.path(PATH).build())
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isOk()
			.expectBody(CaseResourceResponseDTO.class)
			.returnResult()
			.getResponseBody();

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		verify(caseMappingServiceMock).validateUniqueCase(caseDTOCaptor.capture(), eq(MUNICIPALITY_ID));
		final var caseDTO = caseDTOCaptor.getValue();
		assertThat(caseDTO.getExternalCaseId()).isEqualTo("externalCaseId");
		verify(caseServiceMock).handleCase(caseDTOCaptor.capture(), eq(MUNICIPALITY_ID));
		final var otherCaseDTO = (OtherCaseDTO) caseDTOCaptor.getValue();
		assertThat(otherCaseDTO).satisfies(dto -> {
			assertThat(dto.getExternalCaseId()).isEqualTo("externalCaseId");
			assertThat(dto.getCaseType()).isEqualTo("PARKING_PERMIT");
			assertThat(dto.getDescription()).isEqualTo("description");
			assertThat(dto.getCaseTitleAddition()).isEqualTo("caseTitleAddition");
			assertThat(dto.getFacilities()).isEmpty();
			assertThat(dto.getAttachments()).hasSize(1);
			assertThat(dto.getStakeholders()).hasSize(1);
		});
	}

}
