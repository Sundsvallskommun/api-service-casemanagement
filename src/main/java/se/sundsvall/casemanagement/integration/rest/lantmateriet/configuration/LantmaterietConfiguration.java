package se.sundsvall.casemanagement.integration.rest.lantmateriet.configuration;


import feign.Logger;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import se.sundsvall.casemanagement.integration.CustomLogger;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

@Import(FeignConfiguration.class)
public class LantmaterietConfiguration {
    public static final String REGISTRATION_ID = "lantmateriet";

    private final LantmaterietProperties lantmaterietProperties;

    public LantmaterietConfiguration(LantmaterietProperties lantmaterietProperties) {
        this.lantmaterietProperties = lantmaterietProperties;
    }

    @Bean
    @Primary
    Logger feignLogger() {
        return new CustomLogger(REGISTRATION_ID);
    }

    @Bean
    FeignBuilderCustomizer feignBuilderCustomizer() {
        return FeignMultiCustomizer.create()
                .withErrorDecoder(new ProblemErrorDecoder(REGISTRATION_ID))
                .withRetryableOAuth2InterceptorForClientRegistration(ClientRegistration.withRegistrationId(REGISTRATION_ID)
                        .tokenUri(lantmaterietProperties.oauth2TokenUrl())
                        .clientId(lantmaterietProperties.oauth2ClientId())
                        .clientSecret(lantmaterietProperties.oauth2ClientSecret())
                        .authorizationGrantType(new AuthorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS.getValue()))
                        .build())
                .composeCustomizersToOne();
    }

}