package se.sundsvall.casemanagement.integration.soap.minutmiljo.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("integration.minutmiljo")
public record MinutMiljoProperties(int connectTimeout, int readTimeout, String username, String password) {
}
