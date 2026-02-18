package se.sundsvall.casemanagement.integration.byggr;

import arendeexport.SaveNewArendeResponse2;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.stream.Collectors;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;
import se.sundsvall.casemanagement.configuration.RetryProperties;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.CaseRepository;
import se.sundsvall.casemanagement.integration.messaging.MessagingIntegration;
import se.sundsvall.casemanagement.integration.oepintegrator.OepIntegratorClient;
import se.sundsvall.casemanagement.service.event.IncomingByggrCase;
import se.sundsvall.casemanagement.service.event.UpdateByggrCase;
import se.sundsvall.casemanagement.util.EnvironmentUtil;
import se.sundsvall.casemanagement.util.Processor;
import se.sundsvall.dept44.requestid.RequestId;

import static se.sundsvall.casemanagement.util.Constants.BYGGR;

@Component
class ByggrProcessor extends Processor {

	private final ByggrService service;

	private final RetryPolicy<SaveNewArendeResponse2> retryPolicy;
	private final ByggrService byggrService;

	ByggrProcessor(
		final OepIntegratorClient oepIntegratorClient,
		final CaseRepository caseRepository,
		final RetryProperties retryProperties,
		final ByggrService service,
		final CaseMappingRepository caseMappingRepository,
		final MessagingIntegration messagingIntegration,
		final ByggrService byggrService,
		final EnvironmentUtil environmentUtil) {

		super(oepIntegratorClient, caseRepository, caseMappingRepository, messagingIntegration, environmentUtil);
		this.service = service;

		retryPolicy = RetryPolicy.<SaveNewArendeResponse2>builder()
			.withMaxAttempts(retryProperties.maxAttempts())
			.withBackoff(retryProperties.initialDelay(), retryProperties.maxDelay())
			.handle(Exception.class)
			.onSuccess(successEvent -> log.debug("Created byggR errand {}", successEvent.getResult().getDnr()))
			.handleResultIf(response -> response.getDnr().isEmpty())
			.onFailedAttempt(event -> log.debug("Unable to create byggR errand ({}/{}): {}", event.getAttemptCount(), retryProperties.maxAttempts(), event.getLastException().getMessage()))
			.build();
		this.byggrService = byggrService;
	}

	@EventListener(UpdateByggrCase.class)
	public void handleUpdateByggrCase(final UpdateByggrCase event) throws IOException, SQLException {
		RequestId.init(event.getRequestId());
		final var caseEntity = caseRepository.findByIdAndMunicipalityId(event.getPayload().getExternalCaseId(), event.getMunicipalityId()).orElse(null);

		if (caseEntity == null) {
			cleanAttachmentBase64(event);
			log.warn("Unable to process byggR errand {}", event.getPayload());
			return;
		}

		final String json;
		try (final BufferedReader reader = new BufferedReader(caseEntity.getDto().getCharacterStream())) {
			json = reader.lines().collect(Collectors.joining());
		}

		final var objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
		final var byggRCase = objectMapper.readValue(json, ByggRCaseDTO.class);

		byggrService.updateByggRCase(byggRCase, event.getMunicipalityId());
	}

	@EventListener(IncomingByggrCase.class)
	public void handleIncomingErrand(final IncomingByggrCase event) throws SQLException, IOException {
		RequestId.init(event.getRequestId());

		final var caseEntity = caseRepository.findByIdAndMunicipalityId(event.getPayload().getExternalCaseId(), event.getMunicipalityId()).orElse(null);

		if (caseEntity == null) {
			cleanAttachmentBase64(event);
			log.warn("Unable to process byggR errand {}", event.getPayload());
			return;
		}

		final String json;
		try (final BufferedReader reader = new BufferedReader(caseEntity.getDto().getCharacterStream())) {
			json = reader.lines().collect(Collectors.joining());
		}

		final var objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
		final var byggRCaseDTO = objectMapper.readValue(json, ByggRCaseDTO.class);

		try {
			Failsafe
				.with(retryPolicy)
				.onSuccess(successEvent -> handleSuccessfulDelivery(caseEntity.getId(), BYGGR, successEvent.getResult().getDnr(), event.getMunicipalityId()))
				.onFailure(failureEvent -> handleMaximumDeliveryAttemptsExceeded(failureEvent.getException(), caseEntity, BYGGR, event.getMunicipalityId()))
				.get(() -> service.saveNewCase(byggRCaseDTO, event.getMunicipalityId()));
		} catch (final Exception e) {
			cleanAttachmentBase64(event);
			log.warn("Unable to create byggR errand {}: {}", event.getPayload(), e.getMessage());
		}
	}

}
