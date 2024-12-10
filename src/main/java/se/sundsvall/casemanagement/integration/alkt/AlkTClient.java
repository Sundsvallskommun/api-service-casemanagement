package se.sundsvall.casemanagement.integration.alkt;

import generated.client.alkt.Owner;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import se.sundsvall.casemanagement.integration.alkt.configuration.AlkTConfiguration;

@FeignClient(name = AlkTConfiguration.REGISTRATION_ID, url = "${integration.alk-t.url}", configuration = AlkTConfiguration.class, dismiss404 = true)
public interface AlkTClient {

	@GetMapping(path = "/{municipalityId}/owners/{partyId}")
	List<Owner> getOwners(
		@PathVariable(name = "municipalityId") final String municipalityId,
		@PathVariable(name = "partyId") final String partyId);
}
