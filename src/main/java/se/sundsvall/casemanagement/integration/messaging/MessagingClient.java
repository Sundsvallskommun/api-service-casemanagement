package se.sundsvall.casemanagement.integration.messaging;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import generated.client.messaging.EmailRequest;
import generated.client.messaging.SlackRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@FeignClient(name = MessagingConfiguration.REGISTRATION_ID, url = "${integration.messaging.url}", configuration = MessagingConfiguration.class)
@CircuitBreaker(name = MessagingConfiguration.REGISTRATION_ID)
public interface MessagingClient {

	@PostMapping("/slack")
	ResponseEntity<Void> sendSlack(SlackRequest request);

	@PostMapping(path = "/email", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
	ResponseEntity<Void> sendEmail(@RequestBody EmailRequest emailRequest);
}
