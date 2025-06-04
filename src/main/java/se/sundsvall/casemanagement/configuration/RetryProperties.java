package se.sundsvall.casemanagement.configuration;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "retry")
public record RetryProperties(
	@DefaultValue("1") int maxAttempts,
	@DefaultValue("PT0.1S") Duration initialDelay,
	@DefaultValue("PT1S") Duration maxDelay) {

	// This is required for ByggrProcessor tests.
	public RetryProperties() {
		this(1, Duration.parse("PT0.1S"), Duration.parse("PT1S"));
	}
}
