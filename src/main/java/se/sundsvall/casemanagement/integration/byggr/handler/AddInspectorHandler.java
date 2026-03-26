package se.sundsvall.casemanagement.integration.byggr.handler;

import arendeexport.SaveNewHandelse;
import arendeexport.SaveNewHandelseMessage;
import generated.client.oep_integrator.CaseStatusChangeRequest;
import generated.client.oep_integrator.InstanceType;
import org.springframework.stereotype.Component;
import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;
import se.sundsvall.casemanagement.integration.byggr.ArendeExportClient;
import se.sundsvall.casemanagement.integration.byggr.ByggrMapper;
import se.sundsvall.casemanagement.integration.byggr.ByggrUpdateHandler;
import se.sundsvall.casemanagement.integration.byggr.ByggrUtil;
import se.sundsvall.casemanagement.integration.oepintegrator.OepIntegratorClient;
import se.sundsvall.casemanagement.integration.party.PartyIntegration;

import static se.sundsvall.casemanagement.util.Constants.DONE;
import static se.sundsvall.casemanagement.util.Constants.ERRAND_NR;
import static se.sundsvall.casemanagement.util.Constants.OTHER_INFORMATION;
import static se.sundsvall.casemanagement.util.Constants.SYSTEM;

@Component("ADD_INSPECTOR")
public class AddInspectorHandler implements ByggrUpdateHandler {

	private final ArendeExportClient arendeExportClient;
	private final OepIntegratorClient oepIntegratorClient;
	private final PartyIntegration partyIntegration;

	public AddInspectorHandler(final ArendeExportClient arendeExportClient,
		final OepIntegratorClient oepIntegratorClient,
		final PartyIntegration partyIntegration) {
		this.arendeExportClient = arendeExportClient;
		this.oepIntegratorClient = oepIntegratorClient;
		this.partyIntegration = partyIntegration;
	}

	@Override
	public void handle(final ByggRCaseDTO byggRCase) {
		final var stakeholder = byggRCase.getStakeholders().getFirst();
		final var stakeholderId = ByggrUtil.extractStakeholderId(byggRCase.getStakeholders(), byggRCase.getMunicipalityId(), partyIntegration);
		final var errandNr = byggRCase.getExtraParameters().get(ERRAND_NR);
		final var otherInformation = byggRCase.getExtraParameters().get(OTHER_INFORMATION);
		final var arrayOfHandling = ByggrMapper.createArrayOfHandling(byggRCase);

		final var handelseIntressent = ByggrMapper.createAddCertifiedInspectorHandelseIntressent(stakeholder, stakeholderId, byggRCase.getExtraParameters());
		final var newHandelse = ByggrMapper.createAddCertifiedInspectorHandelse(otherInformation, handelseIntressent);

		final var saveNewHandelse = new SaveNewHandelse()
			.withMessage(new SaveNewHandelseMessage()
				.withDnr(errandNr)
				.withHandlaggarSign(SYSTEM)
				.withHandelse(newHandelse)
				.withHandlingar(arrayOfHandling)
				.withAnkomststamplaHandlingar(false)
				.withAutoGenereraBeslutNr(false)
				.withAnkomststamplaHandlingar(true));

		arendeExportClient.saveNewHandelse(saveNewHandelse);
		arendeExportClient.saveNewHandelse(ByggrMapper.createAlertCaseManagerEvent(errandNr));
		oepIntegratorClient.setStatus(byggRCase.getMunicipalityId(), InstanceType.EXTERNAL, byggRCase.getExternalCaseId(), new CaseStatusChangeRequest().name(DONE));
	}

}
