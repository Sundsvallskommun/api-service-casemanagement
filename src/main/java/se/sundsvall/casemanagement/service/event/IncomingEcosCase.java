package se.sundsvall.casemanagement.service.event;

import se.sundsvall.casemanagement.api.model.EnvironmentalCaseDTO;

public class IncomingEcosCase extends Event<EnvironmentalCaseDTO> {
    
    public IncomingEcosCase(final Object source, final EnvironmentalCaseDTO payload) {
        super(source, payload);
    }
}
