package se.sundsvall.casemanagement.service;

import static generated.client.party.PartyType.ENTERPRISE;
import static generated.client.party.PartyType.PRIVATE;
import static java.util.Collections.emptyList;
import static se.sundsvall.casemanagement.util.Constants.CASE_DATA_STATUS_ROLE_SEARCH;

import generated.client.party.PartyType;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.sundsvall.casemanagement.api.model.CaseStatusDTO;
import se.sundsvall.casemanagement.integration.alkt.AlkTService;
import se.sundsvall.casemanagement.integration.byggr.ByggrService;
import se.sundsvall.casemanagement.integration.casedata.CaseDataService;
import se.sundsvall.casemanagement.integration.ecos.EcosService;
import se.sundsvall.casemanagement.integration.party.PartyIntegration;

@Slf4j
@Service
public class StatusService {

	static final String CASE_DATA_ORGANIZATION_FILTER = "stakeholders.organizationNumber:'%s'";
	static final String CASE_DATA_PERSON_FILTER = "stakeholders.personId:'%s' and exists(stakeholders.roles:'%s')";

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
		final var byggrFuture = CompletableFuture.supplyAsync(() -> byggrService.getByggrStatusByLegalId(organizationNumber, ENTERPRISE, municipalityId))
			.exceptionally(ex -> {
				log.warn("Byggr status fetch failed for org {} in municipality {}: {}", organizationNumber, municipalityId, ex.getMessage());
				return emptyList();
			});
		final var ecosFuture = CompletableFuture.supplyAsync(() -> ecosService.getEcosStatusByLegalId(organizationNumber, ENTERPRISE, municipalityId))
			.exceptionally(ex -> {
				log.warn("Ecos status fetch failed for org {} in municipality {}: {}", organizationNumber, municipalityId, ex.getMessage());
				return emptyList();
			});
		final var caseDataFuture = CompletableFuture.supplyAsync(() -> caseDataService.getStatusesByFilter(CASE_DATA_ORGANIZATION_FILTER.formatted(organizationNumber), municipalityId))
			.exceptionally(ex -> {
				log.warn("CaseData status fetch failed for org {} in municipality {}: {}", organizationNumber, municipalityId, ex.getMessage());
				return emptyList();
			});

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

			final var alktFuture = CompletableFuture.supplyAsync(() -> alkTService.getStatusesByPartyId(partyId, municipalityId))
				.exceptionally(ex -> {
					log.warn("AlkT status fetch failed for party {} (PRIVATE) in municipality {}: {}", partyId, municipalityId, ex.getMessage());
					return emptyList();
				});
			final var caseDataFuture = CompletableFuture.supplyAsync(() -> caseDataService.getStatusesByFilter(CASE_DATA_PERSON_FILTER.formatted(partyId, CASE_DATA_STATUS_ROLE_SEARCH), municipalityId))
				.exceptionally(ex -> {
					log.warn("CaseData status fetch failed for party {} (PRIVATE) in municipality {}: {}", partyId, municipalityId, ex.getMessage());
					return emptyList();
				});
			final var byggrFuture = CompletableFuture.supplyAsync(() -> byggrService.getByggrStatusByLegalId(legalId, PRIVATE, municipalityId))
				.exceptionally(ex -> {
					log.warn("Byggr status fetch failed for party {} (PRIVATE) with legalId {} in municipality {}: {}", partyId, legalId, municipalityId, ex.getMessage());
					return emptyList();
				});
			final var ecosFuture = CompletableFuture.supplyAsync(() -> ecosService.getEcosStatusByLegalId(legalId, PRIVATE, municipalityId))
				.exceptionally(ex -> {
					log.warn("Ecos status fetch failed for party {} (PRIVATE) with legalId {} in municipality {}: {}", partyId, legalId, municipalityId, ex.getMessage());
					return emptyList();
				});

			return Stream.of(alktFuture, caseDataFuture, byggrFuture, ecosFuture)
				.map(CompletableFuture::join)
				.flatMap(List::stream)
				.toList();
		}

		if (partyTypeAndLegalIdMap.containsKey(ENTERPRISE)) {
			final var legalId = partyTypeAndLegalIdMap.get(ENTERPRISE);

			final var alktFuture = CompletableFuture.supplyAsync(() -> alkTService.getStatusesByPartyId(partyId, municipalityId))
				.exceptionally(ex -> {
					log.warn("AlkT status fetch failed for party {} (ENTERPRISE) in municipality {}: {}", partyId, municipalityId, ex.getMessage());
					return emptyList();
				});
			final var caseDataFuture = CompletableFuture.supplyAsync(() -> caseDataService.getStatusesByFilter(CASE_DATA_ORGANIZATION_FILTER.formatted(legalId), municipalityId))
				.exceptionally(ex -> {
					log.warn("CaseData status fetch failed for party {} (ENTERPRISE) with legalId {} in municipality {}: {}", partyId, legalId, municipalityId, ex.getMessage());
					return emptyList();
				});
			final var byggrFuture = CompletableFuture.supplyAsync(() -> byggrService.getByggrStatusByLegalId(legalId, ENTERPRISE, municipalityId))
				.exceptionally(ex -> {
					log.warn("Byggr status fetch failed for party {} (ENTERPRISE) with legalId {} in municipality {}: {}", partyId, legalId, municipalityId, ex.getMessage());
					return emptyList();
				});
			final var ecosFuture = CompletableFuture.supplyAsync(() -> ecosService.getEcosStatusByLegalId(legalId, ENTERPRISE, municipalityId))
				.exceptionally(ex -> {
					log.warn("Ecos status fetch failed for party {} (ENTERPRISE) with legalId {} in municipality {}: {}", partyId, legalId, municipalityId, ex.getMessage());
					return emptyList();
				});

			return Stream.of(alktFuture, caseDataFuture, byggrFuture, ecosFuture)
				.map(CompletableFuture::join)
				.flatMap(List::stream)
				.toList();
		}

		return emptyList();
	}

	List<CaseStatusDTO> getCaseStatusesByLegalId(final String legalId, final PartyType partyType, final String municipalityId) {
		final var byggrFuture = CompletableFuture.supplyAsync(() -> byggrService.getByggrStatusByLegalId(legalId, partyType, municipalityId))
			.exceptionally(ex -> {
				log.warn("Byggr status fetch failed for legalId {} (partyType {}) in municipality {}: {}", legalId, partyType, municipalityId, ex.getMessage());
				return emptyList();
			});
		final var ecosFuture = CompletableFuture.supplyAsync(() -> ecosService.getEcosStatusByLegalId(legalId, partyType, municipalityId))
			.exceptionally(ex -> {
				log.warn("Ecos status fetch failed for legalId {} (partyType {}) in municipality {}: {}", legalId, partyType, municipalityId, ex.getMessage());
				return emptyList();
			});

		return Stream.of(byggrFuture, ecosFuture)
			.map(CompletableFuture::join)
			.flatMap(List::stream)
			.toList();
	}
}
