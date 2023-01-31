package se.sundsvall.casemanagement.integration.rest.lantmateriet.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.lantmateriet")
public record LantmaterietProperties(String url, String oauth2TokenUrl, String oauth2ClientId, String oauth2ClientSecret) {}
