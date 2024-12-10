package se.sundsvall.casemanagement.integration.messaging;

import feign.Request;
import feign.codec.ErrorDecoder;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

@Import(FeignConfiguration.class)
@EnableConfigurationProperties(MessagingProperties.class)
public class MessagingConfiguration {

	static final String REGISTRATION_ID = "messaging";
	private final MessagingProperties properties;

	public MessagingConfiguration(MessagingProperties properties) {
		this.properties = properties;
	}

	@Bean
	FeignBuilderCustomizer customizer() {
		return FeignMultiCustomizer.create()
			.withErrorDecoder(errorDecoder())
			.withRequestOptions(requestOptions())
			.withRetryableOAuth2InterceptorForClientRegistration(clientRegistration())
			.composeCustomizersToOne();
	}

	Request.Options requestOptions() {
		return new Request.Options(
			properties.getConnectTimeout().toMillis(), TimeUnit.MILLISECONDS,
			properties.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS,
			true);

	}

	ClientRegistration clientRegistration() {
		return ClientRegistration
			.withRegistrationId(MessagingIntegration.INTEGRATION_NAME)
			.tokenUri(properties.getTokenUri())
			.clientId(properties.getClientId())
			.clientSecret(properties.getClientSecret())
			.authorizationGrantType(new AuthorizationGrantType(properties.getGrantType()))
			.build();
	}

	ErrorDecoder errorDecoder() {
		return new ProblemErrorDecoder(MessagingIntegration.INTEGRATION_NAME);
	}
}
