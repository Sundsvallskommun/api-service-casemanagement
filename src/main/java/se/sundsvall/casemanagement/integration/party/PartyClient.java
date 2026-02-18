package se.sundsvall.casemanagement.integration.party;

import generated.client.party.PartyType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import se.sundsvall.casemanagement.integration.party.configuration.PartyConfiguration;

import static se.sundsvall.casemanagement.integration.party.configuration.PartyConfiguration.CLIENT_ID;

@FeignClient(name = CLIENT_ID,
	url = "${integration.party.url}",
	configuration = PartyConfiguration.class,
	dismiss404 = true)
@CircuitBreaker(name = CLIENT_ID)
public interface PartyClient {

	@GetMapping("/{municipalityId}/{type}/{partyId}/legalId")
	Optional<String> getLegalIdByPartyId(
		@PathVariable String municipalityId,
		@PathVariable PartyType type,
		@PathVariable String partyId);
}
