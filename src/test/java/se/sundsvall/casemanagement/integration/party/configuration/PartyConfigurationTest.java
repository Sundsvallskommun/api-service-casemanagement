package se.sundsvall.casemanagement.integration.party.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.casemanagement.integration.party.configuration.PartyConfiguration.CLIENT_ID;

import feign.codec.ErrorDecoder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

@ExtendWith(MockitoExtension.class)
class PartyConfigurationTest {

	@Mock
	private PartyProperties propertiesMock;

	@Spy
	private FeignMultiCustomizer feignMultiCustomizerSpy;

	@Captor
	private ArgumentCaptor<ErrorDecoder> errorDecoderCaptor;

	@InjectMocks
	private PartyConfiguration configuration;

	@Test
	void testFeignBuilderCustomizer() {

		final var clientId = "clientId";
		final var clientSecret = "clientSecret";
		final var tokenUrl = "tokenUrl";

		when(propertiesMock.oauth2ClientId()).thenReturn(clientId);
		when(propertiesMock.oauth2ClientSecret()).thenReturn(clientSecret);
		when(propertiesMock.oauth2TokenUrl()).thenReturn(tokenUrl);

		// Mock static FeignMultiCustomizer to enable spy and to verify that static method is being called
		try (final var feignMultiCustomizerMock = Mockito.mockStatic(FeignMultiCustomizer.class)) {
			feignMultiCustomizerMock.when(FeignMultiCustomizer::create).thenReturn(feignMultiCustomizerSpy);

			configuration.feignBuilderCustomizer();

			feignMultiCustomizerMock.verify(FeignMultiCustomizer::create);
		}

		// Verifications
		verify(propertiesMock).oauth2ClientId();
		verify(propertiesMock).oauth2ClientSecret();
		verify(propertiesMock).oauth2TokenUrl();
		verify(feignMultiCustomizerSpy).withErrorDecoder(errorDecoderCaptor.capture());
		verify(feignMultiCustomizerSpy).composeCustomizersToOne();

		// Assert ErrorDecoder
		assertThat(errorDecoderCaptor.getValue())
			.isInstanceOf(ProblemErrorDecoder.class)
			.hasFieldOrPropertyWithValue("integrationName", CLIENT_ID);
	}
}
