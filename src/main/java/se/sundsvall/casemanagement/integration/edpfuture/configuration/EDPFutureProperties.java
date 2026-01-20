package se.sundsvall.casemanagement.integration.edpfuture.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("integration.edpfuture")
public record EDPFutureProperties(int connectTimeout, int readTimeout, String apiKey) {

}
