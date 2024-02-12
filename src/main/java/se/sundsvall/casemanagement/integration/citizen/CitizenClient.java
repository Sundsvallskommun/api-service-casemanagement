package se.sundsvall.casemanagement.integration.citizen;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import se.sundsvall.casemanagement.integration.citizen.configuration.CitizenConfiguration;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static se.sundsvall.casemanagement.integration.citizen.configuration.CitizenConfiguration.CLIENT_ID;

@FeignClient(name = CLIENT_ID, url = "${integration.citizen.url}", configuration = CitizenConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface CitizenClient {

	@GetMapping(path = "/{personId}/personnumber", produces = TEXT_PLAIN_VALUE)
	String getPersonalNumber(@PathVariable String personId);
}
