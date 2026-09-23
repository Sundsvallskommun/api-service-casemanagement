package se.sundsvall.casemanagement.integration.edpfuture;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.rowset.serial.SerialClob;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casemanagement.api.model.FutureCaseDTO;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.CaseRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseEntity;
import se.sundsvall.casemanagement.integration.db.model.DeliveryStatus;
import se.sundsvall.casemanagement.integration.messaging.MessagingIntegration;
import se.sundsvall.casemanagement.integration.oepintegrator.OepIntegratorClient;
import se.sundsvall.casemanagement.service.CaseService;
import se.sundsvall.casemanagement.service.event.IncomingFutureCase;
import se.sundsvall.casemanagement.util.EnvironmentUtil;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EDPFutureProcessorTest {

	private static final String REQUEST_ID = "requestId";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String EXTERNAL_CASE_ID = "ext-1";

	@Mock
	private OepIntegratorClient oepIntegratorClientMock;

	@Mock
	private CaseRepository caseRepositoryMock;

	@Mock
	private CaseMappingRepository caseMappingRepositoryMock;

	@Mock
	private MessagingIntegration messagingIntegrationMock;

	@Mock
	private EnvironmentUtil environmentUtilMock;

	@Mock
	private EDPFutureService edpFutureServiceMock;

	@InjectMocks
	private EDPFutureProcessor edpFutureProcessor;

	@Test
	void handleIncomingErrand() throws SQLException, IOException {
		final var event = createEvent();
		final var entity = createEntity(event);
		when(caseRepositoryMock.findByIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID)).thenReturn(Optional.of(entity));

		edpFutureProcessor.handleIncomingErrand(event);

		verify(edpFutureServiceMock).handleOrder(any(FutureCaseDTO.class), eq(MUNICIPALITY_ID));
		verify(caseRepositoryMock).findByIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID);
		verify(caseRepositoryMock).delete(entity);
		verifyNoMoreInteractions(caseRepositoryMock, edpFutureServiceMock);
		verifyNoInteractions(oepIntegratorClientMock, messagingIntegrationMock);
	}

	@Test
	void handleIncomingErrandFailure() throws SQLException, IOException {
		final var event = createEvent();
		final var entity = createEntity(event);
		when(caseRepositoryMock.findByIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID)).thenReturn(Optional.of(entity));
		doThrow(new RuntimeException("test")).when(edpFutureServiceMock).handleOrder(any(FutureCaseDTO.class), eq(MUNICIPALITY_ID));

		edpFutureProcessor.handleIncomingErrand(event);

		verify(caseRepositoryMock).findByIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID);
		verify(caseRepositoryMock).save(argThat(saved -> EXTERNAL_CASE_ID.equals(saved.getId()) && saved.getDeliveryStatus() == DeliveryStatus.FAILED));
		verify(messagingIntegrationMock).sendSlack(contains(EXTERNAL_CASE_ID), eq(MUNICIPALITY_ID));
		verifyNoMoreInteractions(caseRepositoryMock);
		verifyNoInteractions(oepIntegratorClientMock);
	}

	@Test
	void handleIncomingErrandNoErrandFound() throws SQLException, IOException {
		final var event = createEvent();

		edpFutureProcessor.handleIncomingErrand(event);

		verify(caseRepositoryMock).findByIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID);
		verifyNoMoreInteractions(caseRepositoryMock);
		verifyNoInteractions(edpFutureServiceMock, oepIntegratorClientMock, messagingIntegrationMock);
	}

	private IncomingFutureCase createEvent() {
		return new IncomingFutureCase(CaseService.class, FutureCaseDTO.builder().withExternalCaseId(EXTERNAL_CASE_ID).build(), MUNICIPALITY_ID, REQUEST_ID);
	}

	private CaseEntity createEntity(final IncomingFutureCase event) throws SQLException {
		final var json = new ObjectMapper().writeValueAsString(event.getPayload());
		return CaseEntity.builder()
			.withId(EXTERNAL_CASE_ID)
			.withDeliveryStatus(DeliveryStatus.PENDING)
			.withDto(new SerialClob(json.toCharArray()))
			.build();
	}
}
