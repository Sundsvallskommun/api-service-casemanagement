package se.sundsvall.casemanagement.integration.casedata;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.casemanagement.Application;
import se.sundsvall.dept44.exception.ClientProblem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * CaseDataClient declares two circuit breakers: 'case-data' on the interface and 'caseDataMetadata' on getCaseTypes,
 * where the method-level annotation takes precedence. Both must ignore ClientProblem so that a 4xx answer from CaseData
 * is not counted as a failure towards the breaker.
 *
 * <p>
 * The instance names are map keys under 'resilience4j.circuitbreaker.instances' and are therefore bound verbatim -
 * relaxed binding does not apply, so a name that does not match the annotation silently falls back to the default
 * config instead of failing at startup. This test asserts the resulting behaviour rather than the property value.
 */
@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class CaseDataCircuitBreakerConfigurationTest {

	@Autowired
	private CircuitBreakerRegistry circuitBreakerRegistry;

	@ParameterizedTest
	@ValueSource(strings = {
		"case-data", "caseDataMetadata"
	})
	void ignoresClientProblem(final String instanceName) {
		final var config = circuitBreakerRegistry.circuitBreaker(instanceName).getCircuitBreakerConfig();

		assertThat(config.getIgnoreExceptionPredicate())
			.accepts(new ClientProblem(NOT_FOUND, "CaseType not found in database"));
	}
}
