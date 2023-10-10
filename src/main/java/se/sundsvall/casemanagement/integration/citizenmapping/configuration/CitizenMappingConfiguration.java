package se.sundsvall.casemanagement.integration.citizenmapping.configuration;

import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

@Import(FeignConfiguration.class)
public class CitizenMappingConfiguration {

	public static final String REGISTRATION_ID = "citizen-mapping";

	private final CitizenMappingProperties citizenMappingProperties;

	public CitizenMappingConfiguration(CitizenMappingProperties citizenMappingProperties) {
		this.citizenMappingProperties = citizenMappingProperties;
	}

	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer() {
		return FeignMultiCustomizer.create()
			.withErrorDecoder(new ProblemErrorDecoder(REGISTRATION_ID))
			.withRetryableOAuth2InterceptorForClientRegistration(ClientRegistration.withRegistrationId(REGISTRATION_ID)
				.tokenUri(citizenMappingProperties.oauth2TokenUrl())
				.clientId(citizenMappingProperties.oauth2ClientId())
				.clientSecret(citizenMappingProperties.oauth2ClientSecret())
				.authorizationGrantType(new AuthorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS.getValue()))
				.build())
			.composeCustomizersToOne();
	}
}
