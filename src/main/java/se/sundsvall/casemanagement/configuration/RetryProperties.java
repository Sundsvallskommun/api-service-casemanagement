package se.sundsvall.casemanagement.configuration;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "retry")
public record RetryProperties(int maxAttempts, Duration initialDelay, Duration maxDelay) {

    RetryProperties() {
        this(3, Duration.ofMillis(100), Duration.ofMillis(1000));
    }
}
