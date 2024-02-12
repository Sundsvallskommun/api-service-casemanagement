package se.sundsvall.casemanagement.integration.citizen.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.citizen")
public record CitizenProperties(String url, String oauth2TokenUrl, String oauth2ClientId, String oauth2ClientSecret) {}