package se.sundsvall.casemanagement.integration.byggr;

import java.io.BufferedReader;
import java.sql.SQLException;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import se.sundsvall.casemanagement.api.model.PlanningPermissionCaseDTO;
import se.sundsvall.casemanagement.configuration.RetryProperties;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.CaseRepository;
import se.sundsvall.casemanagement.integration.opene.OpeneClient;
import se.sundsvall.casemanagement.service.event.IncomingByggrCase;
import se.sundsvall.casemanagement.util.Processor;

import arendeexport.SaveNewArendeResponse2;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;

@Component
class ByggrProcessor extends Processor {

    private final ByggrService service;
    private final RetryPolicy<SaveNewArendeResponse2> retryPolicy;

    ByggrProcessor(final OpeneClient openeClient, final CaseRepository caseRepository,
        final RetryProperties retryProperties, final ByggrService service, final CaseMappingRepository caseMappingRepository) {
        super(openeClient, caseRepository, caseMappingRepository);
        this.service = service;

        retryPolicy = RetryPolicy.<SaveNewArendeResponse2>builder()
            .withMaxAttempts(retryProperties.getMaxAttempts())
            .withBackoff(retryProperties.getInitialDelay(), retryProperties.getMaxDelay())
            .handle(Exception.class)
            .onSuccess(successEvent -> log.debug("Created byggR errand {}", successEvent.getResult().getDnr()))
            .handleResultIf(response -> response.getDnr().isEmpty())
            .onFailedAttempt(event -> log.debug("Unable to create byggR errand ({}/{}): {}", event.getAttemptCount(), retryProperties.getMaxAttempts(), event.getLastException().getMessage()))
            .build();
    }

    @Transactional
    @EventListener(IncomingByggrCase.class)
    public void handleIncomingErrand(IncomingByggrCase event) throws JsonProcessingException, SQLException {

        var caseEntity = caseRepository.findById(event.getPayload().getExternalCaseId()).orElse(null);

        if (caseEntity == null) {
            log.warn("Unable to process byggR errand {}", event.getPayload());
            return;
        }
        String json = new BufferedReader(caseEntity.getDto().getCharacterStream()).lines().collect(Collectors.joining());
        var objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        var planningPermissionCaseDTO = objectMapper.readValue(json, PlanningPermissionCaseDTO.class);

        try {
            Failsafe
                .with(retryPolicy)
                .onSuccess(successEvent -> handleSuccessfulDelivery(caseEntity, "BYGGR"))
                .onFailure(failureEvent -> handleMaximumDeliveryAttemptsExceeded(failureEvent.getException(), caseEntity))
                .get(() -> service.postCase(planningPermissionCaseDTO));
        } catch (Exception e) {
            log.warn("Unable to create byggR errand {}: {}", event.getPayload(), e.getMessage());
        }

    }
}
