package se.sundsvall.casemanagement.integration.opene.configuration;

import java.nio.charset.StandardCharsets;

import javax.xml.soap.SOAPConstants;

import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import se.sundsvall.casemanagement.integration.CustomLogger;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;

import feign.Logger;
import feign.jaxb.JAXBContextFactory;
import feign.soap.SOAPDecoder;
import feign.soap.SOAPEncoder;

@Import(FeignConfiguration.class)
public class OpeneConfiguration {
    
    public static final String REGISTRATION_ID = "opene";
    
    private static final JAXBContextFactory JAXB_FACTORY = new JAXBContextFactory.Builder()
        .withMarshallerJAXBEncoding(StandardCharsets.UTF_8.toString())
        .build();
    
    private static final SOAPEncoder.Builder ENCODER_BUILDER = new SOAPEncoder.Builder()
        .withCharsetEncoding(StandardCharsets.UTF_8)
        .withFormattedOutput(false)
        .withJAXBContextFactory(JAXB_FACTORY)
        .withSOAPProtocol(SOAPConstants.SOAP_1_1_PROTOCOL)
        .withWriteXmlDeclaration(true);
    
    @Bean
    @Primary
    Logger feignLogger() {
        return new CustomLogger(REGISTRATION_ID);
    }
    
    @Bean
    FeignBuilderCustomizer feignBuilderCustomizer(OpeneProperties properties) {
        return FeignMultiCustomizer.create()
            .withEncoder(ENCODER_BUILDER.build())
            .withDecoder(new SOAPDecoder(JAXB_FACTORY))
            .withErrorDecoder(new OpeneErrorDecoder())
            .withRequestTimeoutsInSeconds(properties.connectTimeout(), properties.readTimeout())
            .composeCustomizersToOne();
    }
    
}
