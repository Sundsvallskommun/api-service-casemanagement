package se.sundsvall.casemanagement.integration.citizen;

import static se.sundsvall.casemanagement.integration.citizen.configuration.CitizenConfiguration.CLIENT_ID;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import se.sundsvall.casemanagement.integration.citizen.configuration.CitizenConfiguration;

@FeignClient(name = CLIENT_ID, url = "${integration.citizen.url}", configuration = CitizenConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface CitizenClient {

	@GetMapping(path = "/{personId}/personnumber")
	String getPersonalNumber(@PathVariable("personId") String personId);

}
