package se.sundsvall.casemanagement.integration.citizenmapping;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.casemanagement.integration.citizenmapping.configuration.CitizenMappingConfiguration;

@FeignClient(name = CitizenMappingConfiguration.REGISTRATION_ID, url = "${integration.citizen-mapping.url}", configuration = CitizenMappingConfiguration.class)
@CircuitBreaker(name = CitizenMappingConfiguration.REGISTRATION_ID)
public interface CitizenMappingClient {

	@GetMapping(path = "/citizenmapping/{personId}/personalnumber", produces = TEXT_PLAIN_VALUE)
	String getPersonalNumber(@PathVariable String personId);
}
