package se.sundsvall.casemanagement.integration.ecos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.stream.Collectors;
import minutmiljoV2.RegisterDocumentCaseResultSvcDto;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import se.sundsvall.casemanagement.api.model.EcosCaseDTO;
import se.sundsvall.casemanagement.configuration.RetryProperties;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.CaseRepository;
import se.sundsvall.casemanagement.integration.messaging.MessagingIntegration;
import se.sundsvall.casemanagement.integration.oepintegrator.OepIntegratorClient;
import se.sundsvall.casemanagement.service.event.IncomingEcosCase;
import se.sundsvall.casemanagement.util.EnvironmentUtil;
import se.sundsvall.casemanagement.util.Processor;

@Component
class EcosProcessor extends Processor {

	private final EcosService ecosService;

	private final RetryPolicy<RegisterDocumentCaseResultSvcDto> retryPolicy;

	EcosProcessor(
		final OepIntegratorClient oepIntegratorClient,
		final CaseRepository caseRepository,
		final RetryProperties retryProperties,
		final EcosService ecosService,
		final MessagingIntegration messagingIntegration,
		final CaseMappingRepository caseMappingRepository,
		final EnvironmentUtil environmentUtil) {
		super(oepIntegratorClient, caseRepository, caseMappingRepository, messagingIntegration, environmentUtil);
		this.ecosService = ecosService;

		retryPolicy = RetryPolicy.<RegisterDocumentCaseResultSvcDto>builder()
			.withMaxAttempts(retryProperties.maxAttempts())
			.withBackoff(retryProperties.initialDelay(), retryProperties.maxDelay())
			.handle(Exception.class)
			.handleResultIf(response -> response.getCaseNumber().isEmpty())
			.onFailedAttempt(event -> log.debug("Unable to create ecos errand ({}/{}): {}",
				event.getAttemptCount(), retryProperties.maxAttempts(), event.getLastException().getMessage()))
			.build();
	}

	@EventListener(IncomingEcosCase.class)
	public void handleIncomingErrand(final IncomingEcosCase event) throws SQLException, IOException {

		final var caseEntity = caseRepository.findByIdAndMunicipalityId(event.getPayload().getExternalCaseId(), event.getMunicipalityId()).orElse(null);

		if (caseEntity == null) {
			cleanAttachmentBase64(event);
			log.warn("Unable to process ecos errand {}", event.getPayload());
			return;
		}

		final String json;
		try (final BufferedReader reader = new BufferedReader(caseEntity.getDto().getCharacterStream())) {
			json = reader.lines().collect(Collectors.joining());
		}

		final var objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
		final var ecosCaseDTO = objectMapper.readValue(json, EcosCaseDTO.class);

		try {
			Failsafe
				.with(retryPolicy)
				.onSuccess(successEvent -> handleSuccessfulDelivery(caseEntity.getId(), "ECOS", successEvent.getResult().getCaseNumber(), event.getMunicipalityId()))
				.onFailure(failureEvent -> handleMaximumDeliveryAttemptsExceeded(failureEvent.getException(), caseEntity, "ECOS", event.getMunicipalityId()))
				.get(() -> ecosService.postCase(ecosCaseDTO, event.getMunicipalityId()));
		} catch (final Exception e) {
			cleanAttachmentBase64(event);
			log.warn("Unable to create ecos errand {}: {}", event.getPayload(), e.getMessage());
		}
	}

}
