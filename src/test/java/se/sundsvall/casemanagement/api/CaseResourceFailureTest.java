package se.sundsvall.casemanagement.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.zalando.problem.Status.BAD_REQUEST;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;

import se.sundsvall.casemanagement.Application;
import se.sundsvall.casemanagement.api.model.CaseResourceResponseDTO;
import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
@ExtendWith(ResourceLoaderExtension.class)
class CaseResourceFailureTest {

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private WebTestClient webTestClient;

	@ParameterizedTest
	@MethodSource("emptyStakeholders")
	void postCase_EmptyStakeholders(final String path) throws IOException {
		final var body = resourceLoader.getResource("classpath:" + path)
			.getContentAsString(Charset.defaultCharset());

		final var result = webTestClient.post().uri(uriBuilder -> uriBuilder.path("/cases").build())
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
	}

	@ParameterizedTest
	@MethodSource("emptyAttachments")
	void postCase_EmptyAttachments(final String path) throws IOException {
		final var body = resourceLoader.getResource("classpath:" + path)
			.getContentAsString(Charset.defaultCharset());

		final var result = webTestClient.post().uri(uriBuilder -> uriBuilder.path("/cases").build())
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
	}

	@ParameterizedTest
	@MethodSource("noExternalCaseId")
	void postCase_NoExternalCaseId(final String path) throws IOException {
		final var body = resourceLoader.getResource("classpath:" + path)
			.getContentAsString(Charset.defaultCharset());

		final var result = webTestClient.post().uri(uriBuilder -> uriBuilder.path("/cases").build())
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
	}

	@Test
	void postCase_EcosNoFacility(@Load("/case-resource-failure/ecos/no-facility.json") final String body) {
		final var result = webTestClient.post().uri(uriBuilder -> uriBuilder.path("/cases").build())
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
	}

	@Test
	void postCase_ByggRNoFacility(@Load("/case-resource-failure/byggr/no-facility.json") final String body) {
		final var result = webTestClient.post().uri(uriBuilder -> uriBuilder.path("/cases").build())
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
	}

	//This case does not throw a bad request, this is by design to ensure that the different
	//sub-classes of caseDTO are handled  individually.
	@Test
	void postCase_OtherNoFacility(@Load("/case-resource-failure/other/no-facility.json") final String body) {
		final var result = webTestClient.post().uri(uriBuilder -> uriBuilder.path("/cases").build())
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isOk()
			.expectBody(CaseResourceResponseDTO.class)
			.returnResult()
			.getResponseBody();

		assertThat(result.getCaseId()).isEqualTo("Inskickat");

	}

	private static Stream<Arguments> noExternalCaseId() {
		return Stream.of(
			Arguments.of("/case-resource-failure/ecos/no-externalCaseId.json"),
			Arguments.of("/case-resource-failure/byggr/no-externalCaseId.json"),
			Arguments.of("/case-resource-failure/other/no-externalCaseId.json")
		);
	}

	private static Stream<Arguments> emptyAttachments() {
		return Stream.of(
			Arguments.of("/case-resource-failure/ecos/no-attachment.json"),
			Arguments.of("/case-resource-failure/byggr/no-attachment.json"),
			Arguments.of("/case-resource-failure/other/no-attachment.json")
		);
	}

	private static Stream<Arguments> emptyStakeholders() {
		return Stream.of(
			Arguments.of("/case-resource-failure/ecos/no-stakeholder.json"),
			Arguments.of("/case-resource-failure/byggr/no-stakeholder.json"),
			Arguments.of("/case-resource-failure/other/no-stakeholder.json")
		);
	}
}
