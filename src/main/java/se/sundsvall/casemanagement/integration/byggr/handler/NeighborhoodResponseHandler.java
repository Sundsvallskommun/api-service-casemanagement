package se.sundsvall.casemanagement.integration.byggr.handler;

import arendeexport.SaveNewRemissvar;
import arendeexport.SaveNewRemissvarMessage;
import generated.client.oep_integrator.CaseStatusChangeRequest;
import generated.client.oep_integrator.InstanceType;
import org.springframework.stereotype.Component;
import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;
import se.sundsvall.casemanagement.integration.byggr.ArendeExportClient;
import se.sundsvall.casemanagement.integration.byggr.ByggrMapper;
import se.sundsvall.casemanagement.integration.byggr.ByggrUpdateHandler;
import se.sundsvall.casemanagement.integration.oepintegrator.OepIntegratorClient;

import static se.sundsvall.casemanagement.util.Constants.COMMENT;
import static se.sundsvall.casemanagement.util.Constants.DONE;
import static se.sundsvall.casemanagement.util.Constants.ERRAND_INFORMATION;
import static se.sundsvall.casemanagement.util.Constants.PROPERTY;
import static se.sundsvall.casemanagement.util.Constants.SYSTEM;

@Component("neighborhoodResponse")
public class NeighborhoodResponseHandler implements ByggrUpdateHandler {

	private final ArendeExportClient arendeExportClient;
	private final OepIntegratorClient oepIntegratorClient;

	public NeighborhoodResponseHandler(final ArendeExportClient arendeExportClient,
		final OepIntegratorClient oepIntegratorClient) {
		this.arendeExportClient = arendeExportClient;
		this.oepIntegratorClient = oepIntegratorClient;
	}

	@Override
	public void handle(final ByggRCaseDTO byggRCase) {
		final var comment = byggRCase.getExtraParameters().get(COMMENT);
		final var property = byggRCase.getExtraParameters().get(PROPERTY);
		final var errandInformation = byggRCase.getExtraParameters().get(ERRAND_INFORMATION);
		// Extracts the remiss id placed within [] in the property string
		final var remissId = Integer.parseInt(property.replaceAll("^[^\\[]*\\[([^]]+)].*", "$1"));

		final var saveNewRemissvar = new SaveNewRemissvar()
			.withMessage(new SaveNewRemissvarMessage()
				.withHandlaggarSign(SYSTEM)
				.withErinran(comment.equals("Jag har synpunkter"))
				.withMeddelande(errandInformation)
				.withRemissId(remissId)
				.withHandlingar(ByggrMapper.createNeighborhoodNotificationArrayOfHandling(byggRCase)));

		arendeExportClient.saveNewRemissvar(saveNewRemissvar);
		oepIntegratorClient.setStatus(byggRCase.getMunicipalityId(), InstanceType.EXTERNAL, byggRCase.getExternalCaseId(), new CaseStatusChangeRequest().name(DONE));
	}

}
