package se.sundsvall.casemanagement.integration.ecos;

import java.io.BufferedReader;
import java.sql.SQLException;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import se.sundsvall.casemanagement.api.model.EnvironmentalCaseDTO;
import se.sundsvall.casemanagement.configuration.RetryProperties;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.CaseRepository;
import se.sundsvall.casemanagement.integration.opene.OpeneClient;
import se.sundsvall.casemanagement.service.event.IncomingEcosCase;
import se.sundsvall.casemanagement.util.Processor;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import minutmiljoV2.RegisterDocumentCaseResultSvcDto;

@Component
class EcosProcessor extends Processor {

    private final EcosService ecosService;
    private final RetryPolicy<RegisterDocumentCaseResultSvcDto> retryPolicy;

    EcosProcessor(final OpeneClient openeClient, final CaseRepository caseRepository,
        final RetryProperties retryProperties, final EcosService ecosService,
        final CaseMappingRepository caseMappingRepository) {
        super(openeClient, caseRepository, caseMappingRepository);
        this.ecosService = ecosService;

        retryPolicy = RetryPolicy.<RegisterDocumentCaseResultSvcDto>builder()
            .withMaxAttempts(retryProperties.getMaxAttempts())
            .withBackoff(retryProperties.getInitialDelay(), retryProperties.getMaxDelay())
            .handle(Exception.class)
            .handleResultIf(response -> response.getCaseNumber().isEmpty())
            .onFailedAttempt(event -> log.debug("Unable to create ecos errand ({}/{}): {}",
                event.getAttemptCount(), retryProperties.getMaxAttempts(), event.getLastException().getMessage()))
            .build();
    }

    @Transactional
    @EventListener(IncomingEcosCase.class)
    public void handleIncomingErrand(final IncomingEcosCase event) throws JsonProcessingException, SQLException {

        var caseEntity = caseRepository.findById(event.getPayload().getExternalCaseId()).orElse(null);


        if (caseEntity == null) {
            cleanAttachmentBase64(event);
            log.warn("Unable to process ecos errand {}", event.getPayload());
            return;
        }

        String json = new BufferedReader(caseEntity.getDto().getCharacterStream()).lines().collect(Collectors.joining());
        var objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        var environmentalCaseDTO = objectMapper.readValue(json, EnvironmentalCaseDTO.class);

        try {
            Failsafe
                .with(retryPolicy)
                .onSuccess(successEvent -> handleSuccessfulDelivery(caseEntity, "ECOS"))
                .onFailure(failureEvent -> handleMaximumDeliveryAttemptsExceeded(failureEvent.getException(), caseEntity))
                .get(() -> ecosService.postCase(environmentalCaseDTO));
        } catch (Exception e) {
            cleanAttachmentBase64(event);
            log.warn("Unable to create ecos errand {}: {}", event.getPayload(), e.getMessage());
        }

    }
}
