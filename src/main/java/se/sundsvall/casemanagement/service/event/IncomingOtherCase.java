package se.sundsvall.casemanagement.service.event;

import se.sundsvall.casemanagement.api.model.OtherCaseDTO;

public class IncomingOtherCase extends Event<OtherCaseDTO> {

	private static final long serialVersionUID = 1241698039063666913L;

	public IncomingOtherCase(final Object source, final OtherCaseDTO payload) {
		super(source, payload);
	}
}
