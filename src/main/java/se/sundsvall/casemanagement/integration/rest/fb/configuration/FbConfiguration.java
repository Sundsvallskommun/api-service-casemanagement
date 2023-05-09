package se.sundsvall.casemanagement.integration.rest.fb.configuration;


import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

@Import(FeignConfiguration.class)
public class FbConfiguration {
    public static final String REGISTRATION_ID = "fb";
    
    @Bean
    FeignBuilderCustomizer feignBuilderCustomizer() {
        return FeignMultiCustomizer.create()
            .withErrorDecoder(new ProblemErrorDecoder(REGISTRATION_ID))
            .composeCustomizersToOne();
    }
    
}