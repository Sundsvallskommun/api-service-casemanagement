package se.sundsvall.casemanagement.integration.casedata.configuration;

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
public class CaseDataConfiguration {

	public static final String CLIENT_ID = "case-data";

	private final CaseDataProperties caseDataProperties;

	public CaseDataConfiguration(CaseDataProperties caseDataProperties) {
		this.caseDataProperties = caseDataProperties;
	}

	private ErrorDecoder errorDecoder() {
		// We want to return 404 as a 404.
		return new ProblemErrorDecoder(CLIENT_ID, List.of(NOT_FOUND.value()));
	}

	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer() {
		return FeignMultiCustomizer.create()
			.withErrorDecoder(errorDecoder())
			.withRetryableOAuth2InterceptorForClientRegistration(ClientRegistration.withRegistrationId(CLIENT_ID)
				.tokenUri(caseDataProperties.oauth2TokenUrl())
				.clientId(caseDataProperties.oauth2ClientId())
				.clientSecret(caseDataProperties.oauth2ClientSecret())
				.authorizationGrantType(new AuthorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS.getValue()))
				.build())
			.composeCustomizersToOne();
	}
}
