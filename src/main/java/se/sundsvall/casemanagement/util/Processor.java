package se.sundsvall.casemanagement.util;

import generated.client.oep_integrator.ConfirmDeliveryRequest;
import generated.client.oep_integrator.InstanceType;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.api.model.CaseDTO;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.CaseRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseEntity;
import se.sundsvall.casemanagement.integration.db.model.DeliveryStatus;
import se.sundsvall.casemanagement.integration.messaging.MessagingIntegration;
import se.sundsvall.casemanagement.integration.oepintegrator.OepIntegratorClient;
import se.sundsvall.casemanagement.service.event.Event;

public abstract class Processor {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected final OepIntegratorClient oepIntegratorClient;
	protected final CaseRepository caseRepository;
	protected final CaseMappingRepository caseMappingRepository;
	private final MessagingIntegration messagingIntegration;
	private final EnvironmentUtil environmentUtil;

	protected Processor(final OepIntegratorClient oepIntegratorClient, final CaseRepository caseRepository,
		final CaseMappingRepository caseMappingRepository, final MessagingIntegration messagingIntegration, final EnvironmentUtil environmentUtil) {
		this.oepIntegratorClient = oepIntegratorClient;
		this.caseRepository = caseRepository;
		this.caseMappingRepository = caseMappingRepository;
		this.messagingIntegration = messagingIntegration;
		this.environmentUtil = environmentUtil;
	}

	public void cleanAttachmentBase64(final Event<?> event) {

		final var payload = (CaseDTO) event.getPayload();

		Optional.ofNullable(payload.getAttachments()).orElse(List.of(new AttachmentDTO()))
			.forEach(attachment -> attachment.setFile("<BASE64 ENCODED FILE CONTENT>"));
	}

	public void handleSuccessfulDelivery(final String flowInstanceID, final String system, final String caseID, final String municipalityId) {

		log.info("Successful processed errand for externalCaseId {} and municipalityId: {})", flowInstanceID, municipalityId);

		final var confirmDeliveryRequest = new ConfirmDeliveryRequest().caseId(caseID).system(system);
		oepIntegratorClient.confirmDelivery(municipalityId, InstanceType.EXTERNAL, flowInstanceID, confirmDeliveryRequest);

		caseRepository.findByIdAndMunicipalityId(flowInstanceID, municipalityId)
			.ifPresent(caseRepository::delete);
	}

	public void handleMaximumDeliveryAttemptsExceeded(final Throwable failureEvent, final CaseEntity entity, final String system, final String municipalityId) {

		log.info("Exceeded max sending attempts case with externalCaseId {}", entity.getId());
		caseRepository.save(entity.withDeliveryStatus(DeliveryStatus.FAILED));

		final var subject = "Incident from CaseManagement [%s]".formatted(environmentUtil.extractEnvironment());
		final var message = "[" + municipalityId + "][" + system + "]" + "Exceeded max sending attempts case with externalCaseId " + entity.getId() + " Exception: " + failureEvent.getMessage();

		messagingIntegration.sendSlack(message, municipalityId);
		messagingIntegration.sendMail(subject, message, municipalityId);
	}
}
