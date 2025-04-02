package se.sundsvall.casemanagement.integration.messaging;

import static se.sundsvall.casemanagement.integration.messaging.MessagingConfiguration.REGISTRATION_ID;

import generated.client.messaging.EmailRequest;
import generated.client.messaging.SlackRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = REGISTRATION_ID, url = "${integration.messaging.url}", configuration = MessagingConfiguration.class)
@CircuitBreaker(name = REGISTRATION_ID)
public interface MessagingClient {

	@PostMapping("/{municipalityId}/slack")
	ResponseEntity<Void> sendSlack(@PathVariable("municipalityId") final String municipalityId, final SlackRequest request);

	@PostMapping(path = "/{municipalityId}/email")
	ResponseEntity<Void> sendEmail(@PathVariable("municipalityId") final String municipalityId, @RequestBody final EmailRequest emailRequest);

}
