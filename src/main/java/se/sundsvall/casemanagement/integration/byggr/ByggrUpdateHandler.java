package se.sundsvall.casemanagement.integration.byggr;

import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;

/**
 * Strategy interface for handling ByggR case updates. Implementations are registered as Spring beans
 * with a name matching the update_handler value in byggr_case_type_config.
 */
public interface ByggrUpdateHandler {

	void handle(ByggRCaseDTO byggRCase);

}
