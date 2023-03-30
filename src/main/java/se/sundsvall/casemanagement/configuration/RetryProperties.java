package se.sundsvall.casemanagement.configuration;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@ConfigurationProperties(prefix = "retry")
public class RetryProperties {

    private final int maxAttempts = 3;

    private final Duration initialDelay = Duration.ofSeconds(2);

    private final Duration maxDelay = Duration.ofSeconds(20);
}
