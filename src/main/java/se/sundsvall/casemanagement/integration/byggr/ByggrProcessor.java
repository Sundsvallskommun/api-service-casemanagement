package se.sundsvall.casemanagement.integration.byggr;

import java.io.BufferedReader;
import java.sql.SQLException;
import java.util.stream.Collectors;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import arendeexport.SaveNewArendeResponse2;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;
import se.sundsvall.casemanagement.configuration.RetryProperties;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.CaseRepository;
import se.sundsvall.casemanagement.integration.messaging.MessagingIntegration;
import se.sundsvall.casemanagement.integration.opene.OpeneClient;
import se.sundsvall.casemanagement.service.event.IncomingByggrCase;
import se.sundsvall.casemanagement.util.Processor;

@Component
class ByggrProcessor extends Processor {

	private final ByggrService service;

	private final RetryPolicy<SaveNewArendeResponse2> retryPolicy;

	ByggrProcessor(final OpeneClient openeClient, final CaseRepository caseRepository,
		final RetryProperties retryProperties, final ByggrService service, final CaseMappingRepository caseMappingRepository, final MessagingIntegration messagingIntegration) {
		super(openeClient, caseRepository, caseMappingRepository, messagingIntegration);
		this.service = service;

		retryPolicy = RetryPolicy.<SaveNewArendeResponse2>builder()
			.withMaxAttempts(retryProperties.maxAttempts())
			.withBackoff(retryProperties.initialDelay(), retryProperties.maxDelay())
			.handle(Exception.class)
			.onSuccess(successEvent -> log.debug("Created byggR errand {}", successEvent.getResult().getDnr()))
			.handleResultIf(response -> response.getDnr().isEmpty())
			.onFailedAttempt(event -> log.debug("Unable to create byggR errand ({}/{}): {}", event.getAttemptCount(), retryProperties.maxAttempts(), event.getLastException().getMessage()))
			.build();
	}

	@EventListener(IncomingByggrCase.class)
	public void handleIncomingErrand(final IncomingByggrCase event) throws JsonProcessingException, SQLException {

		final var caseEntity = caseRepository.findById(event.getPayload().getExternalCaseId()).orElse(null);

		if (caseEntity == null) {
			cleanAttachmentBase64(event);
			log.warn("Unable to process byggR errand {}", event.getPayload());
			return;
		}
		final String json = new BufferedReader(caseEntity.getDto().getCharacterStream()).lines().collect(Collectors.joining());
		final var objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
		final var planningPermissionCaseDTO = objectMapper.readValue(json, ByggRCaseDTO.class);

		try {
			Failsafe
				.with(retryPolicy)
				.onSuccess(successEvent -> handleSuccessfulDelivery(caseEntity.getId(), "BYGGR", successEvent.getResult().getDnr()))
				.onFailure(failureEvent -> handleMaximumDeliveryAttemptsExceeded(failureEvent.getException(), caseEntity, "BYGGR"))
				.get(() -> service.saveNewCase(planningPermissionCaseDTO));
		} catch (final Exception e) {
			cleanAttachmentBase64(event);
			log.warn("Unable to create byggR errand {}: {}", event.getPayload(), e.getMessage());
		}
	}
}
