package se.sundsvall.casemanagement.integration.opene;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import callback.ConfirmDelivery;
import callback.ExternalID;

@Component
public class OpenEIntegration {

	private static final Logger LOG = LoggerFactory.getLogger(OpenEIntegration.class);
	private final OpeneClient openeClient;

	public OpenEIntegration(final OpeneClient openeClient) {
		this.openeClient = openeClient;
	}

	public void confirmDelivery(final String externalCaseId, final String system, final String dnr) {
		try {
			LOG.info("Confirming delivery for case with parameters [ externalCaseId/flowInstanceId: {}, system: {}, caseId: {} ]", externalCaseId, system, dnr);
			openeClient.confirmDelivery(new ConfirmDelivery()
				.withDelivered(true)
				.withExternalID(new ExternalID()
					.withSystem(system)
					.withID(dnr))
				.withFlowInstanceID(Integer.parseInt(externalCaseId)));
		} catch (Exception e) {
			LOG.info("Unable to confirm delivery to OpenE for case with parameters [ externalCaseId/flowInstanceId: {}, system: {}, caseId: {} ]", externalCaseId, system, dnr, e);
		}
	}
}