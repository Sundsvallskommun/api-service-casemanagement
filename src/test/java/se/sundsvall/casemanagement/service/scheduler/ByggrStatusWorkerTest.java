package se.sundsvall.casemanagement.service.scheduler;

import arendeexport.Arende2;
import arendeexport.ArendeBatch;
import arendeexport.ArrayOfArende;
import arendeexport.GetUpdatedArendenResponse;
import generated.client.eventlog.Event;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casemanagement.integration.byggr.ArendeExportClient;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.ExecutionInformationRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.integration.db.model.ExecutionInformationEntity;
import se.sundsvall.casemanagement.integration.eventlog.EventlogClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ByggrStatusWorkerTest {

	private static final String MUNICIPALITY_ID = "2281";

	@Mock
	private ArendeExportClient arendeExportClientMock;

	@Mock
	private CaseMappingRepository caseMappingRepositoryMock;

	@Mock
	private ExecutionInformationRepository executionInformationRepositoryMock;

	@Mock
	private EventlogClient eventlogClientMock;

	@Captor
	private ArgumentCaptor<Event> eventCaptor;

	@Captor
	private ArgumentCaptor<ExecutionInformationEntity> executionInfoCaptor;

	@InjectMocks
	private ByggrStatusWorker worker;

	@Test
	void updateStatusesWithExistingExecutionInfo() {
		// Arrange
		final var lastExecution = OffsetDateTime.now().minusHours(1);
		final var executionInfo = ExecutionInformationEntity.create()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withJobName(ByggrStatusWorker.JOB_NAME)
			.withLastSuccessfulExecution(lastExecution);

		when(executionInformationRepositoryMock.findByMunicipalityIdAndJobName(MUNICIPALITY_ID, ByggrStatusWorker.JOB_NAME))
			.thenReturn(Optional.of(executionInfo));
		when(arendeExportClientMock.getUpdatedArenden(any())).thenReturn(createEmptyByggrResponse());

		// Act
		worker.updateStatuses(MUNICIPALITY_ID);

		// Assert
		verify(executionInformationRepositoryMock).findByMunicipalityIdAndJobName(MUNICIPALITY_ID, ByggrStatusWorker.JOB_NAME);
		verify(arendeExportClientMock).getUpdatedArenden(any());
		verify(executionInformationRepositoryMock).save(executionInfo);
		verifyNoInteractions(eventlogClientMock);
	}

	@Test
	void updateStatusesInitializesExecutionInfoWhenMissing() {
		// Arrange
		final var newEntity = ExecutionInformationEntity.create()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withJobName(ByggrStatusWorker.JOB_NAME)
			.withLastSuccessfulExecution(OffsetDateTime.now());

		when(executionInformationRepositoryMock.findByMunicipalityIdAndJobName(MUNICIPALITY_ID, ByggrStatusWorker.JOB_NAME))
			.thenReturn(Optional.empty());
		when(executionInformationRepositoryMock.save(any(ExecutionInformationEntity.class))).thenReturn(newEntity);
		when(arendeExportClientMock.getUpdatedArenden(any())).thenReturn(createEmptyByggrResponse());

		// Act
		worker.updateStatuses(MUNICIPALITY_ID);

		// Assert
		verify(executionInformationRepositoryMock).findByMunicipalityIdAndJobName(MUNICIPALITY_ID, ByggrStatusWorker.JOB_NAME);
		verify(executionInformationRepositoryMock, times(2)).save(any(ExecutionInformationEntity.class));
	}

	@Test
	void updateStatusesProcessesByggrArende() {
		// Arrange
		final var lastExecution = OffsetDateTime.now().minusHours(1);
		final var executionInfo = ExecutionInformationEntity.create()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withJobName(ByggrStatusWorker.JOB_NAME)
			.withLastSuccessfulExecution(lastExecution);

		final var arende = new Arende2();
		arende.setDnr("BYGG 2024-000001");
		arende.setStatus("Pågående");

		final var caseMapping = CaseMapping.builder()
			.withExternalCaseId("ext-123")
			.withCaseId("BYGG 2024-000001")
			.withMunicipalityId(MUNICIPALITY_ID)
			.build();

		when(executionInformationRepositoryMock.findByMunicipalityIdAndJobName(MUNICIPALITY_ID, ByggrStatusWorker.JOB_NAME))
			.thenReturn(Optional.of(executionInfo));
		when(arendeExportClientMock.getUpdatedArenden(any())).thenReturn(createByggrResponse(arende));
		when(caseMappingRepositoryMock.findFirstByCaseIdAndMunicipalityId("BYGG 2024-000001", MUNICIPALITY_ID))
			.thenReturn(Optional.of(caseMapping));

		// Act
		worker.updateStatuses(MUNICIPALITY_ID);

		// Assert
		verify(eventlogClientMock).createEvent(eq(MUNICIPALITY_ID), any(String.class), eventCaptor.capture());
		final var capturedEvent = eventCaptor.getValue();
		assertThat(capturedEvent.getMessage()).isEqualTo("Status updated to Pågående");
		verify(executionInformationRepositoryMock).save(executionInfo);
	}

	@Test
	void updateStatusesSkipsByggrArendeWithNoMapping() {
		// Arrange
		final var lastExecution = OffsetDateTime.now().minusHours(1);
		final var executionInfo = ExecutionInformationEntity.create()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withJobName(ByggrStatusWorker.JOB_NAME)
			.withLastSuccessfulExecution(lastExecution);

		final var arende = new Arende2();
		arende.setDnr("BYGG-UNKNOWN");
		arende.setStatus("Pågående");

		when(executionInformationRepositoryMock.findByMunicipalityIdAndJobName(MUNICIPALITY_ID, ByggrStatusWorker.JOB_NAME))
			.thenReturn(Optional.of(executionInfo));
		when(arendeExportClientMock.getUpdatedArenden(any())).thenReturn(createByggrResponse(arende));
		when(caseMappingRepositoryMock.findFirstByCaseIdAndMunicipalityId("BYGG-UNKNOWN", MUNICIPALITY_ID))
			.thenReturn(Optional.empty());

		// Act
		worker.updateStatuses(MUNICIPALITY_ID);

		// Assert
		verifyNoInteractions(eventlogClientMock);
		verify(executionInformationRepositoryMock).save(executionInfo);
	}

	@Test
	void updateStatusesSetsNextExecutionFromByggrBatchEndMinusClockSkew() {
		// Arrange
		final var lastExecution = OffsetDateTime.now().minusHours(1);
		final var batchEnd = LocalDateTime.now().minusMinutes(5);
		final var executionInfo = ExecutionInformationEntity.create()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withJobName(ByggrStatusWorker.JOB_NAME)
			.withLastSuccessfulExecution(lastExecution);

		final var byggrResponse = new GetUpdatedArendenResponse();
		final var batch = new ArendeBatch();
		batch.setBatchEnd(batchEnd);
		batch.setArenden(new ArrayOfArende());
		byggrResponse.setGetUpdatedArendenResult(batch);

		when(executionInformationRepositoryMock.findByMunicipalityIdAndJobName(MUNICIPALITY_ID, ByggrStatusWorker.JOB_NAME))
			.thenReturn(Optional.of(executionInfo));
		when(arendeExportClientMock.getUpdatedArenden(any())).thenReturn(byggrResponse);

		// Act
		worker.updateStatuses(MUNICIPALITY_ID);

		// Assert
		verify(executionInformationRepositoryMock).save(executionInfoCaptor.capture());
		final var savedExecution = executionInfoCaptor.getValue().getLastSuccessfulExecution();
		final var expectedExecution = batchEnd.minus(Duration.ofMinutes(10)).atOffset(lastExecution.getOffset());
		assertThat(savedExecution).isEqualTo(expectedExecution);
	}

	@Test
	void updateStatusesFallsBackToNowWhenBatchEndIsNull() {
		// Arrange
		final var lastExecution = OffsetDateTime.now().minusHours(1);
		final var executionInfo = ExecutionInformationEntity.create()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withJobName(ByggrStatusWorker.JOB_NAME)
			.withLastSuccessfulExecution(lastExecution);

		final var byggrResponse = new GetUpdatedArendenResponse();
		byggrResponse.setGetUpdatedArendenResult(null);

		when(executionInformationRepositoryMock.findByMunicipalityIdAndJobName(MUNICIPALITY_ID, ByggrStatusWorker.JOB_NAME))
			.thenReturn(Optional.of(executionInfo));
		when(arendeExportClientMock.getUpdatedArenden(any())).thenReturn(byggrResponse);

		// Act
		final var before = OffsetDateTime.now();
		worker.updateStatuses(MUNICIPALITY_ID);
		final var after = OffsetDateTime.now();

		// Assert
		verify(executionInformationRepositoryMock).save(executionInfoCaptor.capture());
		final var savedExecution = executionInfoCaptor.getValue().getLastSuccessfulExecution();
		assertThat(savedExecution).isBetween(before, after);
	}

	private GetUpdatedArendenResponse createEmptyByggrResponse() {
		final var response = new GetUpdatedArendenResponse();
		final var batch = new ArendeBatch();
		batch.setBatchEnd(LocalDateTime.now());
		batch.setArenden(new ArrayOfArende());
		response.setGetUpdatedArendenResult(batch);
		return response;
	}

	private GetUpdatedArendenResponse createByggrResponse(final Arende2 arende) {
		final var response = new GetUpdatedArendenResponse();
		final var batch = new ArendeBatch();
		batch.setBatchEnd(LocalDateTime.now());
		final var array = new ArrayOfArende();
		array.getArende().add(arende);
		batch.setArenden(array);
		response.setGetUpdatedArendenResult(batch);
		return response;
	}

}
