package se.sundsvall.casemanagement.integration.edpfuture.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("integration.edp-future")
public record EDPFutureProperties(int connectTimeout, int readTimeout, String apiKey) {

}
