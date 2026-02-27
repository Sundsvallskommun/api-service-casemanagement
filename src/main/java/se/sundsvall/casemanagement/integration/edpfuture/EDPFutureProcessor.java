package se.sundsvall.casemanagement.integration.edpfuture;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import se.sundsvall.casemanagement.api.model.FutureCaseDTO;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.CaseRepository;
import se.sundsvall.casemanagement.integration.messaging.MessagingIntegration;
import se.sundsvall.casemanagement.integration.oepintegrator.OepIntegratorClient;
import se.sundsvall.casemanagement.service.event.IncomingFutureCase;
import se.sundsvall.casemanagement.util.EnvironmentUtil;
import se.sundsvall.casemanagement.util.Processor;
import se.sundsvall.dept44.requestid.RequestId;
import tools.jackson.databind.ObjectMapper;

@Component
class EDPFutureProcessor extends Processor {

	private static final Logger LOGGER = LoggerFactory.getLogger(EDPFutureProcessor.class);

	private final EDPFutureService edpFutureService;

	EDPFutureProcessor(
		final OepIntegratorClient oepIntegratorClient,
		final CaseRepository caseRepository,
		final CaseMappingRepository caseMappingRepository,
		final MessagingIntegration messagingIntegration,
		final EnvironmentUtil environmentUtil,
		final EDPFutureService edpFutureService) {

		super(oepIntegratorClient, caseRepository, caseMappingRepository, messagingIntegration, environmentUtil);
		this.edpFutureService = edpFutureService;
	}

	@EventListener(IncomingFutureCase.class)
	public void handleIncomingErrand(final IncomingFutureCase event) throws SQLException, IOException {
		RequestId.init(event.getRequestId());
		LOGGER.info("Received EDPFuture errand with externalCaseId: {} and municipalityId: {}", event.getPayload().getExternalCaseId(), event.getMunicipalityId());

		final var caseEntity = caseRepository.findByIdAndMunicipalityId(event.getPayload().getExternalCaseId(), event.getMunicipalityId()).orElse(null);

		if (caseEntity == null) {
			LOGGER.warn("Unable to process EDPFuture errand {}", event.getPayload());
			return;
		}

		final String json;
		try (final BufferedReader reader = new BufferedReader(caseEntity.getDto().getCharacterStream())) {
			json = reader.lines().collect(Collectors.joining());
		}

		final var objectMapper = new ObjectMapper();
		final var futureCaseDTO = objectMapper.readValue(json, FutureCaseDTO.class);

		try {
			edpFutureService.handleOrder(futureCaseDTO, event.getMunicipalityId());
		} catch (Exception e) {
			LOGGER.error("Error while processing EDPFuture errand with externalCaseId: {} and municipalityId: {}. Error: {}", event.getPayload().getExternalCaseId(), event.getMunicipalityId(), e.getMessage());
			handleFailedDelivery(e, caseEntity, "EDPFuture", event.getMunicipalityId());
		}
	}

}
