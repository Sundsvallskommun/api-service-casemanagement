package se.sundsvall.casemanagement.integration.citizenmapping.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.citizen-mapping")
public record CitizenMappingProperties(String url, String oauth2TokenUrl, String oauth2ClientId, String oauth2ClientSecret) {}
