package se.sundsvall.casemanagement.service;

import static generated.client.party.PartyType.ENTERPRISE;
import static generated.client.party.PartyType.PRIVATE;
import static java.util.Collections.emptyList;
import static se.sundsvall.casemanagement.util.Constants.CASE_DATA_STATUS_ROLE_SEARCH;
import static se.sundsvall.dept44.util.LogUtils.sanitizeForLogging;

import generated.client.party.PartyType;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import se.sundsvall.casemanagement.api.model.CaseStatusDTO;
import se.sundsvall.casemanagement.integration.alkt.AlkTService;
import se.sundsvall.casemanagement.integration.byggr.ByggrService;
import se.sundsvall.casemanagement.integration.casedata.CaseDataService;
import se.sundsvall.casemanagement.integration.ecos.EcosService;
import se.sundsvall.casemanagement.integration.party.PartyIntegration;

@Service
public class StatusService {

	static final String CASE_DATA_ORGANIZATION_FILTER = "stakeholders.organizationNumber:'%s'";
	static final String CASE_DATA_PERSON_FILTER = "stakeholders.personId:'%s' and exists(stakeholders.roles:'%s')";

	private static final Logger LOG = Logger.getLogger(StatusService.class.getName());

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
		final var byggrFuture = getByggrStatus(municipalityId, organizationNumber, ENTERPRISE);
		final var ecosFuture = getEcosStatus(municipalityId, organizationNumber, ENTERPRISE);
		final var caseDataFuture = getCaseDataStatus(municipalityId, CASE_DATA_ORGANIZATION_FILTER.formatted(organizationNumber));

		return Stream.of(byggrFuture, ecosFuture, caseDataFuture)
			.map(CompletableFuture::join)
			.flatMap(List::stream)
			.toList();
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
		final Map<PartyType, String> partyTypeAndLegalIdMap = partyIntegration.getLegalIdByPartyId(municipalityId, partyId);

		if (partyTypeAndLegalIdMap.containsKey(PRIVATE)) {

			final var legalId = partyTypeAndLegalIdMap.get(PRIVATE);

			final var alktFuture = getAlktStatus(municipalityId, partyId);
			final var caseDataFuture = getCaseDataStatus(municipalityId, CASE_DATA_PERSON_FILTER.formatted(partyId, CASE_DATA_STATUS_ROLE_SEARCH));
			final var byggrFuture = getByggrStatus(municipalityId, legalId, PRIVATE);
			final var ecosFuture = getEcosStatus(municipalityId, legalId, PRIVATE);

			return Stream.of(alktFuture, caseDataFuture, byggrFuture, ecosFuture)
				.map(CompletableFuture::join)
				.flatMap(List::stream)
				.toList();
		}

		if (partyTypeAndLegalIdMap.containsKey(ENTERPRISE)) {
			final var legalId = partyTypeAndLegalIdMap.get(ENTERPRISE);

			final var alktFuture = getAlktStatus(municipalityId, partyId);
			final var caseDataFuture = getCaseDataStatus(municipalityId, CASE_DATA_ORGANIZATION_FILTER.formatted(legalId));
			final var byggrFuture = getByggrStatus(municipalityId, legalId, ENTERPRISE);
			final var ecosFuture = getEcosStatus(municipalityId, legalId, ENTERPRISE);

			return Stream.of(alktFuture, caseDataFuture, byggrFuture, ecosFuture)
				.map(CompletableFuture::join)
				.flatMap(List::stream)
				.toList();
		}

		return emptyList();
	}

	private CompletableFuture<List<CaseStatusDTO>> getAlktStatus(final String municipalityId, final String partyId) {
		return CompletableFuture.supplyAsync(() -> alkTService.getStatusesByPartyId(partyId, municipalityId))
			.exceptionally(ex -> {
				LOG.log(Level.WARNING, String.format("AlkT status fetch failed for party %s in municipality %s: %s", partyId, sanitizeForLogging(municipalityId), ex.getMessage()));
				return emptyList();
			});
	}

	private CompletableFuture<List<CaseStatusDTO>> getCaseDataStatus(final String municipalityId, final String filter) {
		return CompletableFuture.supplyAsync(() -> caseDataService.getStatusesByFilter(filter, sanitizeForLogging(municipalityId)))
			.exceptionally(ex -> {
				LOG.log(Level.WARNING, String.format("CaseData status fetch failed for filter %s in municipality %s: %s", sanitizeForLogging(filter), sanitizeForLogging(municipalityId), ex.getMessage()));
				return emptyList();
			});
	}

	private CompletableFuture<List<CaseStatusDTO>> getByggrStatus(final String municipalityId, final String legalId, final PartyType partyType) {
		return CompletableFuture.supplyAsync(() -> byggrService.getByggrStatusByLegalId(legalId, partyType, municipalityId))
			.exceptionally(ex -> {
				LOG.log(Level.WARNING, String.format("Byggr status fetch failed for %s party with legalId %s in municipality %s: %s", partyType, sanitizeForLogging(legalId), sanitizeForLogging(municipalityId), ex.getMessage()));
				return emptyList();
			});
	}

	private CompletableFuture<List<CaseStatusDTO>> getEcosStatus(final String municipalityId, final String legalId, final PartyType partyType) {
		return CompletableFuture.supplyAsync(() -> ecosService.getEcosStatusByLegalId(legalId, partyType, municipalityId))
			.exceptionally(ex -> {
				LOG.log(Level.WARNING, String.format("Ecos status fetch failed for %s party with legalId %s in municipality %s: %s", partyType, sanitizeForLogging(legalId), sanitizeForLogging(municipalityId), ex.getMessage()));
				return emptyList();
			});
	}
}
