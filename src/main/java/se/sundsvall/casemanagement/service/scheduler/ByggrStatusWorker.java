package se.sundsvall.casemanagement.service.scheduler;

import arendeexport.Arende2;
import arendeexport.ArendeBatch;
import arendeexport.ArrayOfArende;
import arendeexport.BatchFilter;
import arendeexport.GetUpdatedArenden;
import arendeexport.GetUpdatedArendenResponse;
import arendeexport.Handelse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.casemanagement.integration.byggr.ArendeExportClient;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.ExecutionInformationRepository;
import se.sundsvall.casemanagement.integration.db.model.ExecutionInformationEntity;
import se.sundsvall.casemanagement.integration.eventlog.EventlogClient;
import se.sundsvall.casemanagement.service.ByggrSystemConfigProvider;

import static java.time.OffsetDateTime.now;

@Component
public class ByggrStatusWorker {

	private static final Logger LOG = LoggerFactory.getLogger(ByggrStatusWorker.class);

	static final String JOB_NAME = "BYGGR_STATUS";

	private static final Duration CLOCK_SKEW_BUFFER = Duration.ofMinutes(10);

	private final ArendeExportClient arendeExportClient;
	private final CaseMappingRepository caseMappingRepository;
	private final ExecutionInformationRepository executionInformationRepository;
	private final EventlogClient eventlogClient;
	private final ByggrSystemConfigProvider byggrSystemConfigProvider;

	public ByggrStatusWorker(final ArendeExportClient arendeExportClient,
		final CaseMappingRepository caseMappingRepository,
		final ExecutionInformationRepository executionInformationRepository,
		final EventlogClient eventlogClient, ByggrSystemConfigProvider byggrSystemConfigProvider) {

		this.arendeExportClient = arendeExportClient;
		this.caseMappingRepository = caseMappingRepository;
		this.executionInformationRepository = executionInformationRepository;
		this.eventlogClient = eventlogClient;
		this.byggrSystemConfigProvider = byggrSystemConfigProvider;
	}

	public void updateStatuses(final String municipalityId) {
		final var executionInfo = executionInformationRepository.findByMunicipalityIdAndJobName(municipalityId, JOB_NAME)
			.orElseGet(() -> initializeExecutionInfo(municipalityId));

		final var lastExecution = executionInfo.getLastSuccessfulExecution();

		LOG.info("Updating ByggR statuses for municipality {} since {}", municipalityId, lastExecution);

		final var byggrBatchEnd = updateByggrStatuses(municipalityId, lastExecution);

		final var nextExecution = byggrBatchEnd
			.map(batchEnd -> batchEnd.minus(CLOCK_SKEW_BUFFER))
			.map(batchEnd -> batchEnd.atOffset(lastExecution.getOffset()))
			.orElseGet(OffsetDateTime::now);

		executionInfo.setLastSuccessfulExecution(nextExecution);
		executionInformationRepository.save(executionInfo);

		LOG.info("ByggR status update completed for municipality {}. Next lower bound: {}", municipalityId, nextExecution);
	}

	private Optional<LocalDateTime> updateByggrStatuses(final String municipalityId, final OffsetDateTime since) {
		final var filter = new BatchFilter()
			.withLowerExclusiveBound(since.toLocalDateTime())
			.withUpperInclusiveBound(LocalDateTime.now());

		final var response = arendeExportClient.getUpdatedArenden(new GetUpdatedArenden().withFilter(filter));

		final var batch = Optional.ofNullable(response)
			.map(GetUpdatedArendenResponse::getGetUpdatedArendenResult);

		final var arenden = batch
			.map(ArendeBatch::getArenden)
			.map(ArrayOfArende::getArende)
			.orElse(List.of());

		LOG.info("Found {} updated ByggR cases for municipality {}", arenden.size(), municipalityId);

		arenden.forEach(arende -> processByggrArende(arende, municipalityId));

		return batch.map(ArendeBatch::getBatchEnd);
	}

	private void processByggrArende(final Arende2 arende, final String municipalityId) {
		try {
			final var mapping = caseMappingRepository.findFirstByCaseIdAndMunicipalityId(arende.getDnr(), municipalityId)
				.orElse(null);

			if (mapping == null) {
				LOG.debug("No case mapping found for ByggR case {}, skipping", arende.getDnr());
				return;
			}

			if (hasHandelseList(arende)) {
				final var handelseLista = arende.getHandelseLista().getHandelse();
				handelseLista.sort(Comparator.comparing(Handelse::getStartDatum, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

				final var handelse = handelseLista.getFirst();
				var status = byggrSystemConfigProvider.resolveHandelseStatus(handelse.getHandelsetyp(), handelse.getHandelseslag(), handelse.getHandelseutfall());

				if (status != null) {
					StatusEventMapper.createStatusEvent(eventlogClient, municipalityId, mapping.getExternalCaseId(), status);
				}
			}
		} catch (final Exception e) {
			LOG.warn("Failed to process ByggR case {}: {}", arende.getDnr(), e.getMessage());
		}
	}

	static boolean hasHandelseList(final Arende2 arende) {
		return arende.getHandelseLista() != null
			&& arende.getHandelseLista().getHandelse() != null
			&& !arende.getHandelseLista().getHandelse().isEmpty();
	}

	private ExecutionInformationEntity initializeExecutionInfo(final String municipalityId) {
		return executionInformationRepository.save(ExecutionInformationEntity.create()
			.withMunicipalityId(municipalityId)
			.withJobName(JOB_NAME)
			.withLastSuccessfulExecution(now()));
	}

}
