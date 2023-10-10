package se.sundsvall.casemanagement.service.event;

import se.sundsvall.casemanagement.api.model.PlanningPermissionCaseDTO;

public class IncomingByggrCase extends Event<PlanningPermissionCaseDTO> {

	private static final long serialVersionUID = -6097420394998607304L;

	public IncomingByggrCase(final Object source, final PlanningPermissionCaseDTO payload) {
		super(source, payload);
	}
}
