package se.sundsvall.casemanagement.integration.casedata.configuration;


import feign.Logger;
import feign.codec.ErrorDecoder;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import se.sundsvall.casemanagement.integration.CustomLogger;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

import java.util.List;

@Import(FeignConfiguration.class)
public class CaseDataConfiguration {
    public static final String REGISTRATION_ID = "case-data";

    private final CaseDataProperties caseDataProperties;

    public CaseDataConfiguration(CaseDataProperties caseDataProperties) {
        this.caseDataProperties = caseDataProperties;
    }

    private ErrorDecoder errorDecoder() {
        //We want to return 404 as a 404.
        return new ProblemErrorDecoder(REGISTRATION_ID, List.of(HttpStatus.NOT_FOUND.value()));
    }

    @Bean
    @Primary
    Logger feignLogger() {
        return new CustomLogger(REGISTRATION_ID);
    }

    @Bean
    FeignBuilderCustomizer feignBuilderCustomizer() {
        return FeignMultiCustomizer.create()
                .withErrorDecoder(errorDecoder())
                .withRetryableOAuth2InterceptorForClientRegistration(ClientRegistration.withRegistrationId(REGISTRATION_ID)
                        .tokenUri(caseDataProperties.oauth2TokenUrl())
                        .clientId(caseDataProperties.oauth2ClientId())
                        .clientSecret(caseDataProperties.oauth2ClientSecret())
                        .authorizationGrantType(new AuthorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS.getValue()))
                        .build())
                .composeCustomizersToOne();
    }

}