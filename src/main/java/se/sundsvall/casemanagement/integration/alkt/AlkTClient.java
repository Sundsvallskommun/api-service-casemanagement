package se.sundsvall.casemanagement.integration.alkt;

import static se.sundsvall.casemanagement.integration.alkt.configuration.AlkTConfiguration.REGISTRATION_ID;

import generated.client.alkt.Owner;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import se.sundsvall.casemanagement.integration.alkt.configuration.AlkTConfiguration;

@FeignClient(name = REGISTRATION_ID, url = "${integration.alk-t.url}", configuration = AlkTConfiguration.class, dismiss404 = true)
@CircuitBreaker(name = REGISTRATION_ID)
public interface AlkTClient {

	@GetMapping(path = "/{municipalityId}/owners/{partyId}")
	List<Owner> getOwners(
		@PathVariable final String municipalityId,
		@PathVariable final String partyId);
}
