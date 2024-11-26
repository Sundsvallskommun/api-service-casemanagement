package se.sundsvall.casemanagement.integration.party;

import generated.client.party.PartyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;

import java.util.Map;

import static generated.client.party.PartyType.ENTERPRISE;
import static generated.client.party.PartyType.PRIVATE;
import static org.zalando.problem.Status.BAD_REQUEST;

@Component
public class PartyIntegration {

	private static final Logger LOG = LoggerFactory.getLogger(PartyIntegration.class);

	static final String INVALID_PARTY_ID = "Invalid partyId: %s";

	private final PartyClient client;

	public PartyIntegration(final PartyClient client) {
		this.client = client;
	}

	public Map<PartyType, String> getLegalIdByPartyId(final String municipalityId, final String partyId) {
		var personalNumber = client.getLegalIdByPartyId(municipalityId, PRIVATE, partyId);
		if (personalNumber.isPresent()) {
			LOG.debug("Found personal number for partyId: {}", partyId);
			return Map.of(PRIVATE, personalNumber.get());
		}

		var organizationNumber = client.getLegalIdByPartyId(municipalityId, ENTERPRISE, partyId);
		if (organizationNumber.isPresent()) {
			LOG.debug("Found organization number for partyId: {}", partyId);
			return Map.of(ENTERPRISE, organizationNumber.get());
		}

		throw Problem.valueOf(BAD_REQUEST, INVALID_PARTY_ID.formatted(partyId));
	}
}