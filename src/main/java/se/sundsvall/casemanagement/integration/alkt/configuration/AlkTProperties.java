package se.sundsvall.casemanagement.integration.alkt.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.alk-t")
public record AlkTProperties(String url, String oauth2TokenUrl, String oauth2ClientId, String oauth2ClientSecret) {
}
