package se.sundsvall.casemanagement.integration.messaging;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "integration.messaging")
@Getter
@Setter
public class MessagingProperties {

	private String channel;
	private String mailRecipient;
	private String token;

	/// BASE PROPERTIES
	private String baseUrl;
	private Duration readTimeout = Duration.ofSeconds(15);
	private Duration connectTimeout = Duration.ofSeconds(5);
	private String tokenUri;
	private String clientId;
	private String clientSecret;
	private String grantType = "client_credentials";
}
