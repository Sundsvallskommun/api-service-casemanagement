package se.sundsvall.casemanagement.integration.oepintegrator.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.oep-integrator")
public record OepIntegratorProperties(String url, String oauth2TokenUrl, String oauth2ClientId, String oauth2ClientSecret) {
}
