package se.sundsvall.casemanagement.integration.casedata.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "integration.case-data")
public record CaseDataProperties(String url, String oauth2TokenUrl, String oauth2ClientId, String oauth2ClientSecret, Map<String, List<String>> namespaces) {
}
