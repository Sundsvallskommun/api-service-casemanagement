package se.sundsvall.casemanagement.integration.alkt;

import generated.client.alkt.ModelCase;
import generated.client.alkt.Owner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.sundsvall.casemanagement.api.model.CaseStatusDTO;
import se.sundsvall.casemanagement.api.model.enums.SystemType;

import java.util.List;

@Service
public class AlkTService {

	private static final Logger LOG = LoggerFactory.getLogger(AlkTService.class);
	static final String CASE_TYPE = "ALKOHOLTOBAK";
	static final String ONGOING = "Pågående";
	static final String FINISHED = "Avslutad";

	private final AlkTClient alkTClient;

	public AlkTService(final AlkTClient alkTClient) {
		this.alkTClient = alkTClient;
	}

	public List<CaseStatusDTO> getStatusesByPartyId(final String partyId, final String municipalityId) {
		var owners = getOwnersByPartyId(partyId, municipalityId);
		var modelCases = owners.stream()
			.flatMap(owner -> owner.getEstablishments().stream())
			.flatMap(establishment -> establishment.getCases().stream())
			.toList();
		LOG.info("Found {} cases for partyId {}", modelCases.size(), partyId);
		return mapToCaseStatuses(modelCases);
	}

	List<Owner> getOwnersByPartyId(final String partyId, final String municipalityId) {
		LOG.info("Fetching owners for partyId: {} and municipalityId: {}", partyId, municipalityId);
		var owners = alkTClient.getOwners(municipalityId, partyId);
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
			.withStatus(modelCase.getOpen() ? ONGOING : FINISHED)
			.withServiceName(modelCase.getDescription())
			.withTimestamp(modelCase.getChanged().toLocalDateTime())
			.build();
	}
}
