package se.sundsvall.casemanagement.service.event;

import se.sundsvall.casemanagement.api.model.PlanningPermissionCaseDTO;

public class IncomingByggrCase extends Event<PlanningPermissionCaseDTO> {
    
    public IncomingByggrCase(final Object source, final PlanningPermissionCaseDTO payload) {
        super(source, payload);
    }
}
