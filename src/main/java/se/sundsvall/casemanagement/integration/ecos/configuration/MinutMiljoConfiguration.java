package se.sundsvall.casemanagement.integration.ecos.configuration;


import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.net.ssl.X509TrustManager;

import jakarta.xml.soap.SOAPConstants;

import org.apache.hc.client5.http.impl.auth.NTLMEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.security.Truststore;

import feign.Client;
import feign.jaxb.JAXBContextFactory;
import feign.okhttp.OkHttpClient;
import feign.soap.SOAPDecoder;
import feign.soap.SOAPEncoder;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

@Import(FeignConfiguration.class)
public class MinutMiljoConfiguration {

    public static final String REGISTRATION_ID = "minutmiljo";
    private static final Logger log = LoggerFactory.getLogger(MinutMiljoConfiguration.class);
    private static final JAXBContextFactory JAXB_FACTORY = new JAXBContextFactory.Builder()
        .withMarshallerJAXBEncoding(StandardCharsets.UTF_8.toString())
        .build();
    private static final SOAPEncoder.Builder ENCODER_BUILDER = new SOAPEncoder.Builder()
        .withCharsetEncoding(StandardCharsets.UTF_8)
        .withFormattedOutput(false)
        .withJAXBContextFactory(JAXB_FACTORY)
        .withSOAPProtocol(SOAPConstants.SOAP_1_1_PROTOCOL)
        .withWriteXmlDeclaration(true);
    private final MinutMiljoProperties minutMiljoProperties;

    public MinutMiljoConfiguration(MinutMiljoProperties minutMiljoProperties) {
        this.minutMiljoProperties = minutMiljoProperties;
    }

    @Bean
    Client okHttpClient(final Truststore trustStore) {

        final var trustManagerFactory = trustStore.getTrustManagerFactory();
        final var trustManager = (X509TrustManager) trustManagerFactory.getTrustManagers()[0];

        return new OkHttpClient(new okhttp3.OkHttpClient.Builder()
            .sslSocketFactory(trustStore.getSSLContext().getSocketFactory(), trustManager)
            .authenticator(new NTLMAuthenticator(minutMiljoProperties.username(), minutMiljoProperties.password()))
            .build());
    }

    @Bean
    FeignBuilderCustomizer feignBuilderCustomizer(MinutMiljoProperties properties) {
        return FeignMultiCustomizer.create()
            .withEncoder(ENCODER_BUILDER.build())
            .withDecoder(new SOAPDecoder(JAXB_FACTORY))
            .withErrorDecoder(new MinutMiljoErrorDecoder())
            .withRequestTimeoutsInSeconds(properties.connectTimeout(), properties.readTimeout())
            .composeCustomizersToOne();
    }

    private static class NTLMAuthenticator implements Authenticator {
        final NTLMEngine engine = new NTLMEngineImpl();
        private final String username;
        private final String password;
        private final String ntlmMsg1;

        private NTLMAuthenticator(String username, String password) {
            this.username = username;
            this.password = password;
            String localNtlmMsg1 = null;
            try {
                localNtlmMsg1 = engine.generateType1Msg(null, null);
            } catch (Exception e) {
                log.error("Error generating NTLM type 1 message", e);
            }
            ntlmMsg1 = localNtlmMsg1;
        }

        @Override
        public Request authenticate(Route route, Response response) {
            final List<String> wwwAuthenticate = response.headers().values("WWW-Authenticate");
            if (wwwAuthenticate.contains("NTLM")) {
                return response.request().newBuilder().header("Authorization", "NTLM " + ntlmMsg1).build();
            }
            String ntlmMsg3 = null;
            try {
                ntlmMsg3 = engine.generateType3Msg(username, password.toCharArray(), null, null, wwwAuthenticate.getFirst().substring(5));
            } catch (Exception e) {
                log.error("Error generating NTLM type 3 message", e);
            }
            return response.request().newBuilder().header("Authorization", "NTLM " + ntlmMsg3).build();
        }
    }
}
