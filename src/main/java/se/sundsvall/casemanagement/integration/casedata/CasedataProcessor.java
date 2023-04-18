package se.sundsvall.casemanagement.integration.casedata;

import java.io.BufferedReader;
import java.sql.SQLException;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.configuration.RetryProperties;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.CaseRepository;
import se.sundsvall.casemanagement.integration.opene.OpeneClient;
import se.sundsvall.casemanagement.service.event.IncomingOtherCase;
import se.sundsvall.casemanagement.util.Processor;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;

@Component
class CasedataProcessor extends Processor {
    
    private final CaseDataService service;
    private final RetryPolicy<String> retryPolicy;
    
    CasedataProcessor(final OpeneClient openeClient,
        final CaseRepository caseRepository,
        final CaseDataService service,
        final RetryProperties retryProperties,
        final CaseMappingRepository caseMappingRepository) {
        super(openeClient, caseRepository, caseMappingRepository);
        this.service = service;
        this.retryPolicy = RetryPolicy.<String>builder()
            .withMaxAttempts(retryProperties.getMaxAttempts())
            .withBackoff(retryProperties.getInitialDelay(), retryProperties.getMaxDelay())
            .handle(Exception.class)
            .handleResultIf(String::isEmpty)
            .onFailedAttempt(event -> log.debug("Unable to create CaseData errand ({}/{}): {}",
                event.getAttemptCount(), retryProperties.getMaxAttempts(), event.getLastException().getMessage()))
            .build();
    }
    
    @Transactional
    @EventListener(IncomingOtherCase.class)
    public void handleIncomingErrand(final IncomingOtherCase event) throws SQLException, JsonProcessingException {
        
        var caseEntity = caseRepository.findById(event.getPayload().getExternalCaseId()).orElse(null);
        
        if (caseEntity == null) {
            log.warn("Unable to process CaseData errand {}", event.getPayload());
            return;
        }
        String json = new BufferedReader(caseEntity.getDto().getCharacterStream()).lines().collect(Collectors.joining());
        var objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        var otherCaseDTO = objectMapper.readValue(json, OtherCaseDTO.class);
        
        try {
            Failsafe
                .with(retryPolicy)
                .onSuccess(successEvent -> handleSuccessfulDelivery(caseEntity))
                .onFailure(failureEvent -> handleMaximumDeliveryAttemptsExceeded(caseEntity))
                .get(() -> service.postErrand(otherCaseDTO));
        } catch (Exception e) {
            log.warn("Unable to create CaseData errand {}: {}", event.getPayload(), e.getMessage());
        }
        
    }
}