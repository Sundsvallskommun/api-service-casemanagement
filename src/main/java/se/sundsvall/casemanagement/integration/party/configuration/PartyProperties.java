package se.sundsvall.casemanagement.integration.party.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.party")
public record PartyProperties(String url, String oauth2TokenUrl, String oauth2ClientId, String oauth2ClientSecret) {
}
