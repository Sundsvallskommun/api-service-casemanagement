package se.sundsvall.casemanagement.integration.soap.arendeexport.configuration;


import feign.Logger;
import feign.jaxb.JAXBContextFactory;
import feign.soap.SOAPDecoder;
import feign.soap.SOAPEncoder;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import se.sundsvall.casemanagement.integration.CustomLogger;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;

import javax.xml.soap.SOAPConstants;
import java.nio.charset.StandardCharsets;

@Import(FeignConfiguration.class)
public class ArendeExportConfiguration {

    public static final String REGISTRATION_ID = "arendeexport";

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
    FeignBuilderCustomizer feignBuilderCustomizer(ArendeExportProperties properties) {
        return FeignMultiCustomizer.create()
                .withEncoder(ENCODER_BUILDER.build())
                .withDecoder(new SOAPDecoder(JAXB_FACTORY))
                .withErrorDecoder(new ArendeExportErrorDecoder())
                .withRequestTimeoutsInSeconds(properties.connectTimeout(), properties.readTimeout())
                .composeCustomizersToOne();
    }
}