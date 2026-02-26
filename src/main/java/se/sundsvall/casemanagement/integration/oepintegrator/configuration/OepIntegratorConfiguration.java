package se.sundsvall.casemanagement.integration.oepintegrator.configuration;

import feign.codec.ErrorDecoder;
import java.util.List;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Import(FeignConfiguration.class)
public class OepIntegratorConfiguration {

	public static final String REGISTRATION_ID = "oep-integrator";

	private final OepIntegratorProperties oepIntegratorProperties;

	public OepIntegratorConfiguration(final OepIntegratorProperties oepIntegratorProperties) {
		this.oepIntegratorProperties = oepIntegratorProperties;
	}

	private ErrorDecoder errorDecoder() {
		// We want to return 404 as a 404.
		return new ProblemErrorDecoder(REGISTRATION_ID, List.of(NOT_FOUND.value()));
	}

	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer() {
		return FeignMultiCustomizer.create()
			.withErrorDecoder(errorDecoder())
			.withRetryableOAuth2InterceptorForClientRegistration(ClientRegistration.withRegistrationId(REGISTRATION_ID)
				.tokenUri(oepIntegratorProperties.oauth2TokenUrl())
				.clientId(oepIntegratorProperties.oauth2ClientId())
				.clientSecret(oepIntegratorProperties.oauth2ClientSecret())
				.authorizationGrantType(new AuthorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS.getValue()))
				.build())
			.composeCustomizersToOne();
	}

}
