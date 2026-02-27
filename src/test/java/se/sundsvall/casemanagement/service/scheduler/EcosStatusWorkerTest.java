package se.sundsvall.casemanagement.service.scheduler;

import generated.client.eventlog.Event;
import java.time.OffsetDateTime;
import java.util.Optional;
import minutmiljo.ArrayOfOccurrenceListItemSvcDto;
import minutmiljo.ArrayOfSearchCaseResultSvcDto;
import minutmiljo.CaseSvcDto;
import minutmiljo.GetCaseResponse;
import minutmiljo.OccurrenceListItemSvcDto;
import minutmiljo.SearchCaseResponse;
import minutmiljo.SearchCaseResultSvcDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.ExecutionInformationRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.integration.db.model.ExecutionInformationEntity;
import se.sundsvall.casemanagement.integration.ecos.MinutMiljoClient;
import se.sundsvall.casemanagement.integration.eventlog.EventlogClient;

import static java.time.LocalDateTime.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EcosStatusWorkerTest {

	private static final String MUNICIPALITY_ID = "2281";

	@Mock
	private MinutMiljoClient minutMiljoClientMock;

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
	private EcosStatusWorker worker;

	@Test
	void updateStatusesWithExistingExecutionInfo() {
		// Arrange
		final var lastExecution = OffsetDateTime.now().minusHours(1);
		final var executionInfo = ExecutionInformationEntity.create()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withJobName(EcosStatusWorker.JOB_NAME)
			.withLastSuccessfulExecution(lastExecution);

		when(executionInformationRepositoryMock.findByMunicipalityIdAndJobName(MUNICIPALITY_ID, EcosStatusWorker.JOB_NAME))
			.thenReturn(Optional.of(executionInfo));
		when(minutMiljoClientMock.searchCase(any())).thenReturn(createEmptyEcosResponse());

		// Act
		worker.updateStatuses(MUNICIPALITY_ID);

		// Assert
		verify(executionInformationRepositoryMock).findByMunicipalityIdAndJobName(MUNICIPALITY_ID, EcosStatusWorker.JOB_NAME);
		verify(minutMiljoClientMock).searchCase(any());
		verify(executionInformationRepositoryMock).save(executionInfo);
		verifyNoInteractions(eventlogClientMock);
	}

	@Test
	void updateStatusesInitializesExecutionInfoWhenMissing() {
		// Arrange
		final var newEntity = ExecutionInformationEntity.create()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withJobName(EcosStatusWorker.JOB_NAME)
			.withLastSuccessfulExecution(OffsetDateTime.now());

		when(executionInformationRepositoryMock.findByMunicipalityIdAndJobName(MUNICIPALITY_ID, EcosStatusWorker.JOB_NAME))
			.thenReturn(Optional.empty());
		when(executionInformationRepositoryMock.save(any(ExecutionInformationEntity.class))).thenReturn(newEntity);
		when(minutMiljoClientMock.searchCase(any())).thenReturn(createEmptyEcosResponse());

		// Act
		worker.updateStatuses(MUNICIPALITY_ID);

		// Assert
		verify(executionInformationRepositoryMock).findByMunicipalityIdAndJobName(MUNICIPALITY_ID, EcosStatusWorker.JOB_NAME);
		verify(executionInformationRepositoryMock, times(2)).save(any(ExecutionInformationEntity.class));
	}

	@Test
	void updateStatusesProcessesEcosCase() {
		// Arrange
		final var lastExecution = OffsetDateTime.now().minusHours(1);
		final var executionInfo = ExecutionInformationEntity.create()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withJobName(EcosStatusWorker.JOB_NAME)
			.withLastSuccessfulExecution(lastExecution);

		final var searchResult = new SearchCaseResultSvcDto();
		searchResult.setCaseId("ecos-case-123");

		final var occurrence = new OccurrenceListItemSvcDto();
		occurrence.setOccurrenceDate(of(2024, 1, 15, 10, 0));
		occurrence.setOccurrenceDescription("Avslutat");

		final var caseMapping = CaseMapping.builder()
			.withExternalCaseId("ext-123")
			.withCaseId("ecos-case-123")
			.withMunicipalityId(MUNICIPALITY_ID)
			.build();

		when(executionInformationRepositoryMock.findByMunicipalityIdAndJobName(MUNICIPALITY_ID, EcosStatusWorker.JOB_NAME))
			.thenReturn(Optional.of(executionInfo));
		when(minutMiljoClientMock.searchCase(any())).thenReturn(createEcosResponse(searchResult));
		when(caseMappingRepositoryMock.findFirstByCaseIdAndMunicipalityId("ecos-case-123", MUNICIPALITY_ID))
			.thenReturn(Optional.of(caseMapping));
		when(minutMiljoClientMock.getCase(any())).thenReturn(createGetCaseResponse(occurrence));

		// Act
		worker.updateStatuses(MUNICIPALITY_ID);

		// Assert
		verify(eventlogClientMock).createEvent(eq(MUNICIPALITY_ID), any(String.class), eventCaptor.capture());
		final var capturedEvent = eventCaptor.getValue();
		assertThat(capturedEvent.getMessage()).isEqualTo("Status updated to Avslutat");
		verify(executionInformationRepositoryMock).save(executionInfo);
	}

	@Test
	void updateStatusesSkipsEcosCaseWithNoMapping() {
		// Arrange
		final var lastExecution = OffsetDateTime.now().minusHours(1);
		final var executionInfo = ExecutionInformationEntity.create()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withJobName(EcosStatusWorker.JOB_NAME)
			.withLastSuccessfulExecution(lastExecution);

		final var searchResult = new SearchCaseResultSvcDto();
		searchResult.setCaseId("ecos-unknown");

		when(executionInformationRepositoryMock.findByMunicipalityIdAndJobName(MUNICIPALITY_ID, EcosStatusWorker.JOB_NAME))
			.thenReturn(Optional.of(executionInfo));
		when(minutMiljoClientMock.searchCase(any())).thenReturn(createEcosResponse(searchResult));
		when(caseMappingRepositoryMock.findFirstByCaseIdAndMunicipalityId("ecos-unknown", MUNICIPALITY_ID))
			.thenReturn(Optional.empty());

		// Act
		worker.updateStatuses(MUNICIPALITY_ID);

		// Assert
		verifyNoInteractions(eventlogClientMock);
		verify(executionInformationRepositoryMock).save(executionInfo);
	}

	@Test
	void updateStatusesSetsNextExecutionToNow() {
		// Arrange
		final var lastExecution = OffsetDateTime.now().minusHours(1);
		final var executionInfo = ExecutionInformationEntity.create()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withJobName(EcosStatusWorker.JOB_NAME)
			.withLastSuccessfulExecution(lastExecution);

		when(executionInformationRepositoryMock.findByMunicipalityIdAndJobName(MUNICIPALITY_ID, EcosStatusWorker.JOB_NAME))
			.thenReturn(Optional.of(executionInfo));
		when(minutMiljoClientMock.searchCase(any())).thenReturn(createEmptyEcosResponse());

		// Act
		final var before = OffsetDateTime.now();
		worker.updateStatuses(MUNICIPALITY_ID);
		final var after = OffsetDateTime.now();

		// Assert
		verify(executionInformationRepositoryMock).save(executionInfoCaptor.capture());
		final var savedExecution = executionInfoCaptor.getValue().getLastSuccessfulExecution();
		assertThat(savedExecution).isBetween(before, after);
	}

	private SearchCaseResponse createEmptyEcosResponse() {
		final var response = new SearchCaseResponse();
		final var results = new ArrayOfSearchCaseResultSvcDto();
		response.setSearchCaseResult(results);
		return response;
	}

	private SearchCaseResponse createEcosResponse(final SearchCaseResultSvcDto result) {
		final var response = new SearchCaseResponse();
		final var results = new ArrayOfSearchCaseResultSvcDto();
		results.getSearchCaseResultSvcDto().add(result);
		response.setSearchCaseResult(results);
		return response;
	}

	private GetCaseResponse createGetCaseResponse(final OccurrenceListItemSvcDto occurrence) {
		final var response = new GetCaseResponse();
		final var caseSvc = new CaseSvcDto();
		final var occurrences = new ArrayOfOccurrenceListItemSvcDto();
		occurrences.getOccurrenceListItemSvcDto().add(occurrence);
		caseSvc.setOccurrences(occurrences);
		response.setGetCaseResult(caseSvc);
		return response;
	}

}
