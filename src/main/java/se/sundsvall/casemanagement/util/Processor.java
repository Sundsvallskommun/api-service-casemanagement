package se.sundsvall.casemanagement.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.CaseRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseEntity;
import se.sundsvall.casemanagement.integration.db.model.DeliveryStatus;
import se.sundsvall.casemanagement.integration.opene.OpeneClient;

import callback.ConfirmDelivery;
import callback.ExternalID;

public abstract class Processor {
    
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final OpeneClient openeClient;
    
    protected final CaseRepository caseRepository;
    protected final CaseMappingRepository caseMappingRepository;
    
    protected Processor(final OpeneClient openeClient, final CaseRepository caseRepository,
        CaseMappingRepository caseMappingRepository) {
        this.openeClient = openeClient;
        this.caseRepository = caseRepository;
        this.caseMappingRepository = caseMappingRepository;
    }
    
    @Transactional
    protected void handleSuccessfulDelivery(final CaseEntity entity) {
        
        log.info("Successful created errand for externalCaseId {})", entity.getId());
        
        caseRepository.deleteById(entity.getId());
        
        var caseMapping = caseMappingRepository.findAllByExternalCaseId(entity.getId()).get(0);
        
        openeClient.confirmDelivery(new ConfirmDelivery()
            .withDelivered(true)
            .withExternalID(new ExternalID().withID(caseMapping.getCaseId()))
            .withFlowInstanceID(Integer.parseInt(entity.getId())));
    }
    
    @Transactional
    protected void handleMaximumDeliveryAttemptsExceeded(final CaseEntity entity) {
        log.info("Exceeded max sending attempts case with externalCaseId {}", entity.getId());
        caseRepository.save(entity.withDeliveryStatus(DeliveryStatus.FAILED));
    }
    
}
