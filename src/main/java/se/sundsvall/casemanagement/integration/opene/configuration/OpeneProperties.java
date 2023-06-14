package se.sundsvall.casemanagement.integration.opene.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("integration.opene")
public record OpeneProperties(int connectTimeout, int readTimeout, String username, String password) {
}
