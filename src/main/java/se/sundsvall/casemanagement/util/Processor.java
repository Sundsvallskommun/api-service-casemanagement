package se.sundsvall.casemanagement.util;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.api.model.CaseDTO;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.CaseRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseEntity;
import se.sundsvall.casemanagement.integration.db.model.DeliveryStatus;
import se.sundsvall.casemanagement.integration.messaging.MessagingIntegration;
import se.sundsvall.casemanagement.integration.opene.OpeneClient;
import se.sundsvall.casemanagement.service.event.Event;

import callback.ConfirmDelivery;
import callback.ExternalID;

public abstract class Processor {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final OpeneClient openeClient;

    protected final CaseRepository caseRepository;
    protected final CaseMappingRepository caseMappingRepository;
    private final MessagingIntegration messagingIntegration;

    protected Processor(final OpeneClient openeClient, final CaseRepository caseRepository,
        CaseMappingRepository caseMappingRepository, final MessagingIntegration messagingIntegration) {
        this.openeClient = openeClient;
        this.caseRepository = caseRepository;
        this.caseMappingRepository = caseMappingRepository;
        this.messagingIntegration = messagingIntegration;
    }

    public void cleanAttachmentBase64(Event<?> event) {

        var payload = (CaseDTO) event.getPayload();

        Optional.ofNullable(payload.getAttachments()).orElse(List.of(new AttachmentDTO()))
            .forEach(attachment -> attachment.setFile("<BASE64 ENCODED FILE CONTENT>"));
    }

    @Transactional
    public void handleSuccessfulDelivery(final CaseEntity entity, final String system) {

        log.info("Successful created errand for externalCaseId {})", entity.getId());

        caseRepository.deleteById(entity.getId());

        var caseMapping = caseMappingRepository.findAllByExternalCaseId(entity.getId()).get(0);
        try {
            openeClient.confirmDelivery(new ConfirmDelivery()
                .withDelivered(true)
                .withExternalID(new ExternalID()
                    .withSystem(system)
                    .withID(caseMapping.getCaseId()))
                .withFlowInstanceID(Integer.parseInt(entity.getId())));
        } catch (Exception e) {
            log.error("Error while confirming delivery", e);
        }
    }

    @Transactional
    public void handleMaximumDeliveryAttemptsExceeded(Throwable failureEvent, final CaseEntity entity, String system) {

        log.info("Exceeded max sending attempts case with externalCaseId {}", entity.getId());
        caseRepository.save(entity.withDeliveryStatus(DeliveryStatus.FAILED));
        messagingIntegration.sendSlack("[" + system + "]" + "Exceeded max sending attempts case with externalCaseId " + entity.getId() + " Exception: " + failureEvent.getMessage());

    }

}
