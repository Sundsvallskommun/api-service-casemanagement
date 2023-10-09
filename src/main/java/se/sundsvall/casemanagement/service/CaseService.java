package se.sundsvall.casemanagement.service;

import java.sql.Clob;
import java.sql.SQLException;

import javax.sql.rowset.serial.SerialClob;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import se.sundsvall.casemanagement.api.model.CaseDTO;
import se.sundsvall.casemanagement.api.model.EnvironmentalCaseDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.api.model.PlanningPermissionCaseDTO;
import se.sundsvall.casemanagement.integration.db.CaseRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseEntity;
import se.sundsvall.casemanagement.integration.db.model.DeliveryStatus;
import se.sundsvall.casemanagement.service.event.IncomingByggrCase;
import se.sundsvall.casemanagement.service.event.IncomingEcosCase;
import se.sundsvall.casemanagement.service.event.IncomingOtherCase;
import se.sundsvall.casemanagement.service.util.Validator;

@Service
public class CaseService {

    private static final Logger log = LoggerFactory.getLogger(CaseService.class);
    private final ApplicationEventPublisher eventPublisher;
    private final CaseRepository caseRepository;
    private final Validator validator;

    public CaseService(ApplicationEventPublisher eventPublisher, CaseRepository caseRepository, Validator validator) {
        this.eventPublisher = eventPublisher;
        this.caseRepository = caseRepository;
        this.validator = validator;
    }

    public void handleCase(CaseDTO dto) {

        if (dto instanceof final PlanningPermissionCaseDTO pCase) {
            validator.validateByggrErrand(pCase);
            saveCase(pCase);
            handleByggRCase(pCase);
        } else if (dto instanceof final EnvironmentalCaseDTO eCase) {
            validator.validateEcosErrand(eCase);
            saveCase(eCase);
            handleEcosCase(eCase);
        } else if (dto instanceof final OtherCaseDTO otherCase) {
            saveCase(otherCase);
            handleOtherCase(otherCase);
        }
    }

    private void saveCase(CaseDTO dto) {
        caseRepository.save(CaseEntity.builder()
            .withId(dto.getExternalCaseId())
            .withDto(doToClob(dto))
            .withDeliveryStatus(DeliveryStatus.PENDING)
            .build());
    }

    private Clob doToClob(CaseDTO dto) {
        try {
            final var objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
            final String jsonString = objectMapper.writeValueAsString(dto);
            return new SerialClob(jsonString.toCharArray());
        } catch (JsonProcessingException | SQLException e) {
            log.error("Failed to convert to Clob", e);
            return null;
        }
    }

    private void handleByggRCase(final PlanningPermissionCaseDTO pCase) {
        eventPublisher.publishEvent(new IncomingByggrCase(this, pCase));
    }

    private void handleEcosCase(final EnvironmentalCaseDTO eCase) {
        eventPublisher.publishEvent(new IncomingEcosCase(this, eCase));
    }

    private void handleOtherCase(OtherCaseDTO dto) {
        eventPublisher.publishEvent(new IncomingOtherCase(this, dto));
    }
}
