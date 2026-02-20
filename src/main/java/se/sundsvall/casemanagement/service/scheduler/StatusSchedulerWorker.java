package se.sundsvall.casemanagement.service.scheduler;

import arendeexport.Arende2;
import arendeexport.ArendeBatch;
import arendeexport.ArrayOfArende;
import arendeexport.BatchFilter;
import arendeexport.GetUpdatedArenden;
import arendeexport.GetUpdatedArendenResponse;
import generated.client.eventlog.Event;
import generated.client.eventlog.Metadata;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import minutmiljo.ArrayOfFilterSvcDto;
import minutmiljo.ArrayOfOccurrenceListItemSvcDto;
import minutmiljo.ArrayOfSearchCaseResultSvcDto;
import minutmiljo.CaseStatusFilterSvcDto;
import minutmiljo.CaseSvcDto;
import minutmiljo.GetCase;
import minutmiljo.GetCaseResponse;
import minutmiljo.OccurrenceListItemSvcDto;
import minutmiljo.SearchCase;
import minutmiljo.SearchCaseResponse;
import minutmiljo.SearchCaseResultSvcDto;
import minutmiljo.SearchCaseSvcDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.casemanagement.integration.byggr.ArendeExportClient;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.ExecutionInformationRepository;
import se.sundsvall.casemanagement.integration.db.model.ExecutionInformationEntity;
import se.sundsvall.casemanagement.integration.ecos.MinutMiljoClient;
import se.sundsvall.casemanagement.integration.eventlog.EventlogClient;

import static generated.client.eventlog.EventType.UPDATE;
import static java.time.OffsetDateTime.now;

@Component
public class StatusSchedulerWorker {

	private static final Logger LOG = LoggerFactory.getLogger(StatusSchedulerWorker.class);

	private static final String EVENT_OWNER = "CaseManagement";
	private static final String EVENT_SOURCE_TYPE = "Errand";
	private static final String METADATA_KEY_STATUS = "Status";

	private final MinutMiljoClient minutMiljoClient;
	private final ArendeExportClient arendeExportClient;
	private final CaseMappingRepository caseMappingRepository;
	private final ExecutionInformationRepository executionInformationRepository;
	private final EventlogClient eventlogClient;

	public StatusSchedulerWorker(final MinutMiljoClient minutMiljoClient,
		final ArendeExportClient arendeExportClient,
		final CaseMappingRepository caseMappingRepository,
		final ExecutionInformationRepository executionInformationRepository,
		final EventlogClient eventlogClient) {

		this.minutMiljoClient = minutMiljoClient;
		this.arendeExportClient = arendeExportClient;
		this.caseMappingRepository = caseMappingRepository;
		this.executionInformationRepository = executionInformationRepository;
		this.eventlogClient = eventlogClient;
	}

	public void updateStatuses(final String municipalityId) {
		final var executionInfo = executionInformationRepository.findById(municipalityId)
			.orElseGet(() -> initializeExecutionInfo(municipalityId));

		final var lastExecution = executionInfo.getLastSuccessfulExecution();
		final var currentExecution = now();

		LOG.info("Updating statuses for municipality {} since {}", municipalityId, lastExecution);

		updateByggrStatuses(municipalityId, lastExecution);
		updateEcosStatuses(municipalityId, lastExecution);

		executionInfo.setLastSuccessfulExecution(currentExecution);
		executionInformationRepository.save(executionInfo);

		LOG.info("Status update completed for municipality {}", municipalityId);
	}

	private void updateByggrStatuses(final String municipalityId, final OffsetDateTime since) {
		final var filter = new BatchFilter()
			.withLowerExclusiveBound(since.toLocalDateTime())
			.withUpperInclusiveBound(LocalDateTime.now());

		final var response = arendeExportClient.getUpdatedArenden(new GetUpdatedArenden().withFilter(filter));

		final var arenden = Optional.ofNullable(response)
			.map(GetUpdatedArendenResponse::getGetUpdatedArendenResult)
			.map(ArendeBatch::getArenden)
			.map(ArrayOfArende::getArende)
			.orElse(List.of());

		LOG.info("Found {} updated ByggR cases for municipality {}", arenden.size(), municipalityId);

		arenden.forEach(arende -> processByggrArende(arende, municipalityId));
	}

	private void processByggrArende(final Arende2 arende, final String municipalityId) {
		try {
			final var mapping = caseMappingRepository.findByExternalCaseIdAndMunicipalityId(arende.getDnr(), municipalityId);
			if (mapping == null) {
				LOG.debug("No case mapping found for ByggR case {}, skipping", arende.getDnr());
				return;
			}

			createStatusEvent(municipalityId, mapping.getExternalCaseId(), arende.getStatus());
		} catch (final Exception e) {
			LOG.warn("Failed to process ByggR case {}: {}", arende.getDnr(), e.getMessage());
		}
	}

	private void updateEcosStatuses(final String municipalityId, final OffsetDateTime since) {
		final var searchResults = searchEcosCasesSince(since);

		LOG.info("Found {} updated ECOS cases for municipality {}", searchResults.size(), municipalityId);

		searchResults.forEach(result -> processEcosCase(result, municipalityId));
	}

	private List<SearchCaseResultSvcDto> searchEcosCasesSince(final OffsetDateTime since) {
		final var statusFilter = new CaseStatusFilterSvcDto();
		statusFilter.setCaseStatusFromDate(since.toLocalDateTime());

		final var filters = new ArrayOfFilterSvcDto();
		filters.getFilterSvcDto().add(statusFilter);

		final var searchModel = new SearchCaseSvcDto();
		searchModel.setFilters(filters);

		return Optional.ofNullable(minutMiljoClient.searchCase(new SearchCase().withModel(searchModel)))
			.map(SearchCaseResponse::getSearchCaseResult)
			.map(ArrayOfSearchCaseResultSvcDto::getSearchCaseResultSvcDto)
			.orElse(List.of());
	}

	private void processEcosCase(final SearchCaseResultSvcDto searchResult, final String municipalityId) {
		try {
			final var mapping = caseMappingRepository.findFirstByCaseIdAndMunicipalityId(searchResult.getCaseId(), municipalityId);
			if (mapping.isEmpty()) {
				LOG.debug("No case mapping found for ECOS case {}, skipping", searchResult.getCaseId());
				return;
			}

			final var status = fetchLatestEcosStatus(searchResult.getCaseId());
			if (status == null) {
				LOG.debug("No occurrence found for ECOS case {}, skipping", searchResult.getCaseId());
				return;
			}

			createStatusEvent(municipalityId, mapping.get().getExternalCaseId(), status);
		} catch (final Exception e) {
			LOG.warn("Failed to process ECOS case {}: {}", searchResult.getCaseId(), e.getMessage());
		}
	}

	private String fetchLatestEcosStatus(final String caseId) {
		return Optional.ofNullable(minutMiljoClient.getCase(new GetCase().withCaseId(caseId)))
			.map(GetCaseResponse::getGetCaseResult)
			.map(CaseSvcDto::getOccurrences)
			.map(ArrayOfOccurrenceListItemSvcDto::getOccurrenceListItemSvcDto)
			.flatMap(list -> list.stream().max(Comparator.comparing(OccurrenceListItemSvcDto::getOccurrenceDate)))
			.map(OccurrenceListItemSvcDto::getOccurrenceDescription)
			.orElse(null);
	}

	private void createStatusEvent(final String municipalityId, final String externalCaseId, final String status) {
		final var event = new Event()
			.type(UPDATE)
			.owner(EVENT_OWNER)
			.message("Status updated to " + status)
			.sourceType(EVENT_SOURCE_TYPE)
			.metadata(List.of(new Metadata().key(METADATA_KEY_STATUS).value(status)));

		eventlogClient.createEvent(municipalityId, externalCaseId, event);
	}

	private ExecutionInformationEntity initializeExecutionInfo(final String municipalityId) {
		return executionInformationRepository.save(ExecutionInformationEntity.create()
			.withMunicipalityId(municipalityId)
			.withLastSuccessfulExecution(now()));
	}

}
