package se.sundsvall.casemanagement.integration.edpfuture.configuration;

import feign.Response;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.problem.ThrowableProblem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EDPFutureErrorDecoderTest {

	private EDPFutureErrorDecoder errorDecoder;

	@BeforeEach
	void setUp() {
		errorDecoder = new EDPFutureErrorDecoder();
	}

	@Test
	void shouldDecodeEmptyResponse() {
		final var response = mock(Response.class);
		when(response.body()).thenReturn(null);
		when(response.reason()).thenReturn("Bad Request");

		final var decodedError = errorDecoder.decode("someMethodKey", response);

		assertThat(decodedError).isInstanceOf(ThrowableProblem.class);
		assertThat(decodedError.getMessage()).isEqualTo("Bad Gateway: Unknown problem in communication with EDPFuture: Bad Request");

	}

	@Test
	void shouldHandleIOException() throws IOException {
		// given
		final var response = mock(Response.class);
		final var body = mock(Response.Body.class);
		when(response.body()).thenReturn(body);
		when(body.asInputStream()).thenThrow(new IOException("Failed to read response body"));

		// when
		final var decodedError = errorDecoder.decode("someMethodKey", response);

		// then
		assertThat(decodedError).isInstanceOf(ThrowableProblem.class);
		assertThat(decodedError.getMessage()).isEqualTo("Bad Gateway: Unknown problem in communication with EDPFuture: Failed to read response body");
	}

}
