package se.sundsvall.casemanagement.integration.casedata;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.stream.Collectors;

import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.configuration.RetryProperties;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.CaseRepository;
import se.sundsvall.casemanagement.integration.messaging.MessagingIntegration;
import se.sundsvall.casemanagement.integration.opene.OpenEIntegration;
import se.sundsvall.casemanagement.service.event.IncomingOtherCase;
import se.sundsvall.casemanagement.util.Processor;

@Component
class CasedataProcessor extends Processor {

	private final CaseDataService service;

	private final RetryPolicy<String> retryPolicy;

	CasedataProcessor(final OpenEIntegration openEIntegration,
		final CaseRepository caseRepository,
		final CaseDataService service,
		final RetryProperties retryProperties,
		final CaseMappingRepository caseMappingRepository,
		final MessagingIntegration messagingIntegration,
		final Environment environment) {
		super(openEIntegration, caseRepository, caseMappingRepository, messagingIntegration, environment);
		this.service = service;
		this.retryPolicy = RetryPolicy.<String>builder()
			.withMaxAttempts(retryProperties.maxAttempts())
			.withBackoff(retryProperties.initialDelay(), retryProperties.maxDelay())
			.handle(Exception.class)
			.handleResultIf(String::isEmpty)
			.onFailedAttempt(event -> log.debug("Unable to create CaseData errand ({}/{}): {}",
				event.getAttemptCount(), retryProperties.maxAttempts(), event.getLastException().getMessage()))
			.build();
	}

	@EventListener(IncomingOtherCase.class)
	public void handleIncomingErrand(final IncomingOtherCase event) throws SQLException, IOException {

		final var caseEntity = caseRepository.findById(event.getPayload().getExternalCaseId()).orElse(null);

		if (caseEntity == null) {
			cleanAttachmentBase64(event);
			log.warn("Unable to process CaseData errand {}", event.getPayload());
			return;
		}

		final String json;
		try (BufferedReader reader = new BufferedReader(caseEntity.getDto().getCharacterStream())) {
			json = reader.lines().collect(Collectors.joining());
		}

		final var objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
		final var otherCaseDTO = objectMapper.readValue(json, OtherCaseDTO.class);

		try {
			Failsafe
				.with(retryPolicy)
				.onSuccess(successEvent -> handleSuccessfulDelivery(caseEntity.getId(), "CASEDATA", successEvent.getResult(), event.getMunicipalityId()))
				.onFailure(failureEvent -> handleMaximumDeliveryAttemptsExceeded(failureEvent.getException(), caseEntity, "CASEDATA", event.getMunicipalityId()))
				.get(() -> service.postErrand(otherCaseDTO, event.getMunicipalityId()));
		} catch (final Exception e) {
			cleanAttachmentBase64(event);
			log.warn("Unable to create CaseData errand {}: {}", event.getPayload(), e.getMessage());
		}
	}
}
