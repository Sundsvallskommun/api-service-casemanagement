package se.sundsvall.casemanagement.integration.alkt;

import static java.lang.Boolean.TRUE;

import generated.client.alkt.ModelCase;
import generated.client.alkt.Owner;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.sundsvall.casemanagement.api.model.CaseStatusDTO;
import se.sundsvall.casemanagement.api.model.enums.SystemType;

@Service
public class AlkTService {

	static final String CASE_TYPE = "ALKOHOLTOBAK";
	static final String ONGOING = "Under granskning";
	static final String FINISHED = "Ärende avslutat";
	private static final Logger LOG = LoggerFactory.getLogger(AlkTService.class);
	private final AlkTClient alkTClient;

	public AlkTService(final AlkTClient alkTClient) {
		this.alkTClient = alkTClient;
	}

	public List<CaseStatusDTO> getStatusesByPartyId(final String partyId, final String municipalityId) {
		final var owners = getOwnersByPartyId(partyId, municipalityId);
		final var modelCases = owners.stream()
			.flatMap(owner -> owner.getEstablishments().stream())
			.flatMap(establishment -> establishment.getCases().stream())
			.toList();
		return mapToCaseStatuses(modelCases);
	}

	List<Owner> getOwnersByPartyId(final String partyId, final String municipalityId) {
		LOG.info("Fetching owners for partyId: {} and municipalityId: {}", partyId, municipalityId);
		final var owners = alkTClient.getOwners(municipalityId, partyId);
		LOG.info("Found {} owners with partyId {}", owners.size(), partyId);
		return owners;
	}

	List<CaseStatusDTO> mapToCaseStatuses(final List<ModelCase> modelCases) {
		return modelCases.stream()
			.map(this::mapToCaseStatus)
			.toList();
	}

	CaseStatusDTO mapToCaseStatus(final ModelCase modelCase) {
		return CaseStatusDTO.builder()
			.withSystem(SystemType.ALKT)
			.withCaseType(CASE_TYPE)
			.withCaseId(modelCase.getRegistrationNumber())
			.withStatus(TRUE.equals(modelCase.getOpen()) ? ONGOING : FINISHED)
			.withServiceName(modelCase.getDescription())
			.withTimestamp(modelCase.getChanged().toLocalDateTime())
			.build();
	}
}
