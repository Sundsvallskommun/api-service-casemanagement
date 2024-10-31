package se.sundsvall.casemanagement.integration.citizen.configuration;

import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

@Import(FeignConfiguration.class)
public class CitizenConfiguration {

	public static final String CLIENT_ID = "citizen";

	private final CitizenProperties citizenProperties;

	public CitizenConfiguration(CitizenProperties citizenProperties) {
		this.citizenProperties = citizenProperties;
	}

	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer() {
		return FeignMultiCustomizer.create()
			.withErrorDecoder(new ProblemErrorDecoder(CLIENT_ID))
			.withRetryableOAuth2InterceptorForClientRegistration(ClientRegistration.withRegistrationId(CLIENT_ID)
				.tokenUri(citizenProperties.oauth2TokenUrl())
				.clientId(citizenProperties.oauth2ClientId())
				.clientSecret(citizenProperties.oauth2ClientSecret())
				.authorizationGrantType(new AuthorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS.getValue()))
				.build())
			.composeCustomizersToOne();
	}
}
