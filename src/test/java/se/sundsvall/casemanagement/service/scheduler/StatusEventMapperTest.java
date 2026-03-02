package se.sundsvall.casemanagement.service.scheduler;

import generated.client.eventlog.Event;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casemanagement.integration.eventlog.EventlogClient;

import static generated.client.eventlog.EventType.UPDATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StatusEventMapperTest {

	@Mock
	private EventlogClient eventlogClientMock;

	@Captor
	private ArgumentCaptor<Event> eventCaptor;

	@Test
	void createStatusEvent() {
		StatusEventMapper.createStatusEvent(eventlogClientMock, "2281", "ext-123", "Pågående");

		verify(eventlogClientMock).createEvent(eq("2281"), any(String.class), eventCaptor.capture());
		final var event = eventCaptor.getValue();
		assertThat(event.getType()).isEqualTo(UPDATE);
		assertThat(event.getOwner()).isEqualTo("CaseManagement");
		assertThat(event.getMessage()).isEqualTo("Status updated to Pågående");
		assertThat(event.getSourceType()).isEqualTo("Errand");
		assertThat(event.getMetadata()).hasSize(2);
		assertThat(event.getMetadata().getFirst().getKey()).isEqualTo("Status");
		assertThat(event.getMetadata().getFirst().getValue()).isEqualTo("Pågående");
		assertThat(event.getMetadata().getLast().getKey()).isEqualTo("ExternalCaseId");
		assertThat(event.getMetadata().getLast().getValue()).isEqualTo("ext-123");
	}

}
