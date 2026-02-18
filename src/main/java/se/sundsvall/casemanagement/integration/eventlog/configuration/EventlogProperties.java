package se.sundsvall.casemanagement.integration.eventlog.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("integration.eventlog")
public record EventlogProperties(String url, String oauth2TokenUrl, String oauth2ClientId, String oauth2ClientSecret) {
}
