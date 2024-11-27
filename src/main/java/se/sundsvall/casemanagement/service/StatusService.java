package se.sundsvall.casemanagement.service;

import generated.client.party.PartyType;
import org.springframework.stereotype.Service;
import se.sundsvall.casemanagement.api.model.CaseStatusDTO;
import se.sundsvall.casemanagement.integration.alkt.AlkTService;
import se.sundsvall.casemanagement.integration.byggr.ByggrService;
import se.sundsvall.casemanagement.integration.casedata.CaseDataService;
import se.sundsvall.casemanagement.integration.ecos.EcosService;
import se.sundsvall.casemanagement.integration.party.PartyIntegration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static generated.client.party.PartyType.ENTERPRISE;
import static generated.client.party.PartyType.PRIVATE;
import static java.util.Collections.emptyList;

@Service
public class StatusService {

	static final String CASE_DATA_ORGANIZATION_FILTER = "stakeholders.organizationNumber:'%s'";
	static final String CASE_DATA_PERSON_FILTER = "stakeholders.personId:'%s'";

	private final ByggrService byggrService;
	private final EcosService ecosService;
	private final CaseDataService caseDataService;
	private final AlkTService alkTService;
	private final CaseMappingService caseMappingService;
	private final PartyIntegration partyIntegration;

	public StatusService(final ByggrService byggrService,
		final EcosService ecosService,
		final CaseDataService caseDataService,
		final CaseMappingService caseMappingService,
		final PartyIntegration partyIntegration,
		final AlkTService alkTService) {
		this.byggrService = byggrService;
		this.ecosService = ecosService;
		this.caseDataService = caseDataService;
		this.caseMappingService = caseMappingService;
		this.partyIntegration = partyIntegration;
		this.alkTService = alkTService;
	}

	public List<CaseStatusDTO> getStatusByOrgNr(final String municipalityId, final String organizationNumber) {
		final List<CaseStatusDTO> caseStatuses = new ArrayList<>();

		caseStatuses.addAll(byggrService.getByggrStatusByLegalId(organizationNumber, ENTERPRISE, municipalityId));
		caseStatuses.addAll(ecosService.getEcosStatusByLegalId(organizationNumber, ENTERPRISE, municipalityId));

		return caseStatuses;
	}

	public CaseStatusDTO getStatusByExternalCaseId(final String municipalityId, final String externalCaseId) {
		final var caseMapping = caseMappingService.getCaseMapping(externalCaseId, municipalityId);

		return switch (caseMapping.getSystem()) {
			case BYGGR -> byggrService.toByggrStatus(caseMapping);
			case ECOS -> ecosService.getStatus(caseMapping.getCaseId(), caseMapping.getExternalCaseId(), municipalityId);
			case CASE_DATA -> caseDataService.getStatus(caseMapping, municipalityId);
			default -> throw new IllegalStateException("Unexpected value: " + caseMapping.getSystem());
		};
	}

	public List<CaseStatusDTO> getStatusesByPartyId(final String municipalityId, final String partyId) {
		Map<PartyType, String> partyTypeAndLegalIdMap = partyIntegration.getLegalIdByPartyId(municipalityId, partyId);

		if (partyTypeAndLegalIdMap.containsKey(PRIVATE)) {
			List<CaseStatusDTO> caseStatuses = new ArrayList<>();
			// AlkT statuses
			caseStatuses.addAll(alkTService.getStatusesByPartyId(partyId, municipalityId));
			// CaseData statuses
			caseStatuses.addAll(caseDataService.getStatusesByFilter(CASE_DATA_PERSON_FILTER.formatted(partyId), municipalityId));
			// ByggR and Ecos statuses
			caseStatuses.addAll(getCaseStatusesByLegalId(partyTypeAndLegalIdMap.get(PRIVATE), PRIVATE, municipalityId));
			return caseStatuses;
		}

		if (partyTypeAndLegalIdMap.containsKey(ENTERPRISE)) {
			List<CaseStatusDTO> caseStatuses = new ArrayList<>();
			var legalId = partyTypeAndLegalIdMap.get(ENTERPRISE);
			// AlkT statuses
			caseStatuses.addAll(alkTService.getStatusesByPartyId(partyId, municipalityId));
			// CaseData statuses
			caseStatuses.addAll(caseDataService.getStatusesByFilter(CASE_DATA_ORGANIZATION_FILTER.formatted(legalId), municipalityId));
			// ByggR and Ecos statuses
			caseStatuses.addAll(getCaseStatusesByLegalId(legalId, ENTERPRISE, municipalityId));
			return caseStatuses;
		}

		return emptyList();
	}

	List<CaseStatusDTO> getCaseStatusesByLegalId(final String legalId, final PartyType partyType, final String municipalityId) {
		List<CaseStatusDTO> caseStatuses = new ArrayList<>();
		caseStatuses.addAll(byggrService.getByggrStatusByLegalId(legalId, partyType, municipalityId));
		caseStatuses.addAll(ecosService.getEcosStatusByLegalId(legalId, partyType, municipalityId));
		return caseStatuses;
	}
}
