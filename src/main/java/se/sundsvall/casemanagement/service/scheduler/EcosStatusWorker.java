package se.sundsvall.casemanagement.service.scheduler;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import minutmiljo.ArrayOfFilterSvcDto;
import minutmiljo.ArrayOfOccurrenceListItemSvcDto;
import minutmiljo.ArrayOfSearchCaseResultSvcDto;
import minutmiljo.CaseStatusFilterSvcDto;
import minutmiljo.CaseSvcDto;
import minutmiljo.GetCase;
import minutmiljo.GetCaseResponse;
import minutmiljo.OccurrenceListItemSvcDto;
import minutmiljo.OrFilterSvcDto;
import minutmiljo.SearchCase;
import minutmiljo.SearchCaseResponse;
import minutmiljo.SearchCaseResultSvcDto;
import minutmiljo.SearchCaseSvcDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.ExecutionInformationRepository;
import se.sundsvall.casemanagement.integration.db.model.ExecutionInformationEntity;
import se.sundsvall.casemanagement.integration.ecos.MinutMiljoClient;
import se.sundsvall.casemanagement.integration.eventlog.EventlogClient;

import static java.time.OffsetDateTime.now;
import static se.sundsvall.casemanagement.util.Constants.ECOS_CASE_STATUS_ID_AVSKRIVET;
import static se.sundsvall.casemanagement.util.Constants.ECOS_CASE_STATUS_ID_AVSLUTAT;
import static se.sundsvall.casemanagement.util.Constants.ECOS_CASE_STATUS_ID_MAKULERAT;
import static se.sundsvall.casemanagement.util.Constants.ECOS_CASE_STATUS_ID_UNDER_BEREDNING;

@Component
public class EcosStatusWorker {

	private static final Logger LOG = LoggerFactory.getLogger(EcosStatusWorker.class);

	static final String JOB_NAME = "ECOS_STATUS";

	private final MinutMiljoClient minutMiljoClient;
	private final CaseMappingRepository caseMappingRepository;
	private final ExecutionInformationRepository executionInformationRepository;
	private final EventlogClient eventlogClient;

	public EcosStatusWorker(final MinutMiljoClient minutMiljoClient,
		final CaseMappingRepository caseMappingRepository,
		final ExecutionInformationRepository executionInformationRepository,
		final EventlogClient eventlogClient) {

		this.minutMiljoClient = minutMiljoClient;
		this.caseMappingRepository = caseMappingRepository;
		this.executionInformationRepository = executionInformationRepository;
		this.eventlogClient = eventlogClient;
	}

	public void updateStatuses(final String municipalityId) {
		final var executionInfo = executionInformationRepository.findByMunicipalityIdAndJobName(municipalityId, JOB_NAME)
			.orElseGet(() -> initializeExecutionInfo(municipalityId));

		final var lastExecution = executionInfo.getLastSuccessfulExecution();

		LOG.info("Updating ECOS statuses for municipality {} since {}", municipalityId, lastExecution);

		updateEcosStatuses(municipalityId, lastExecution);

		executionInfo.setLastSuccessfulExecution(now());
		executionInformationRepository.save(executionInfo);

		LOG.info("ECOS status update completed for municipality {}", municipalityId);
	}

	private void updateEcosStatuses(final String municipalityId, final OffsetDateTime since) {
		final var searchResults = searchEcosCasesSince(since);

		LOG.info("Found {} updated ECOS cases for municipality {}", searchResults.size(), municipalityId);

		searchResults.forEach(result -> processEcosCase(result, municipalityId));
	}

	private List<SearchCaseResultSvcDto> searchEcosCasesSince(final OffsetDateTime since) {
		final var sinceLocal = since.toLocalDateTime();

		final var orFilters = new ArrayOfFilterSvcDto();
		Stream.of(ECOS_CASE_STATUS_ID_AVSKRIVET, ECOS_CASE_STATUS_ID_UNDER_BEREDNING, ECOS_CASE_STATUS_ID_AVSLUTAT, ECOS_CASE_STATUS_ID_MAKULERAT)
			.map(statusId -> new CaseStatusFilterSvcDto()
				.withCaseStatusFromDate(sinceLocal)
				.withCaseStatusId(statusId))
			.forEach(orFilters.getFilterSvcDto()::add);

		final var orFilter = new OrFilterSvcDto().withFilters(orFilters);

		final var filters = new ArrayOfFilterSvcDto();
		filters.getFilterSvcDto().add(orFilter);

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

			StatusEventMapper.createStatusEvent(eventlogClient, municipalityId, mapping.get().getExternalCaseId(), status);
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

	private ExecutionInformationEntity initializeExecutionInfo(final String municipalityId) {
		return executionInformationRepository.save(ExecutionInformationEntity.create()
			.withMunicipalityId(municipalityId)
			.withJobName(JOB_NAME)
			.withLastSuccessfulExecution(now()));
	}

}
