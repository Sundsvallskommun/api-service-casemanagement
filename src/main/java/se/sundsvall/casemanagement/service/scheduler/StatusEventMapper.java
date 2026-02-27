package se.sundsvall.casemanagement.service.scheduler;

import generated.client.eventlog.Event;
import generated.client.eventlog.Metadata;
import java.util.List;
import java.util.UUID;
import se.sundsvall.casemanagement.integration.eventlog.EventlogClient;

import static generated.client.eventlog.EventType.UPDATE;

final class StatusEventMapper {

	private static final String EVENT_OWNER = "CaseManagement";
	private static final String EVENT_SOURCE_TYPE = "Errand";
	private static final String METADATA_KEY_STATUS = "Status";
	private static final String METADATA_KEY_CASE_ID = "ExternalCaseId";

	private StatusEventMapper() {}

	static void createStatusEvent(final EventlogClient eventlogClient, final String municipalityId, final String externalCaseId, final String status) {
		final var event = new Event()
			.type(UPDATE)
			.owner(EVENT_OWNER)
			.message("Status updated to " + status)
			.sourceType(EVENT_SOURCE_TYPE)
			.metadata(List.of(
				new Metadata().key(METADATA_KEY_STATUS).value(status),
				new Metadata().key(METADATA_KEY_CASE_ID).value(externalCaseId)));

		final var uuid = UUID.randomUUID().toString();
		eventlogClient.createEvent(municipalityId, uuid, event);
	}

}
