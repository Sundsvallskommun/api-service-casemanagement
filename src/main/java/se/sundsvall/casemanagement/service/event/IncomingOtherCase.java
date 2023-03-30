package se.sundsvall.casemanagement.service.event;

import se.sundsvall.casemanagement.api.model.OtherCaseDTO;

public class IncomingOtherCase extends Event<OtherCaseDTO> {
    public IncomingOtherCase(final Object source, final OtherCaseDTO payload) {
        super(source, payload);
    }
}
