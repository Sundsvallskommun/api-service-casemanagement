package se.sundsvall.casemanagement.service.event;

import java.io.Serial;

import se.sundsvall.casemanagement.api.model.OtherCaseDTO;

public class IncomingOtherCase extends Event<OtherCaseDTO> {

	@Serial
	private static final long serialVersionUID = 1241698039063666913L;

	public IncomingOtherCase(final Object source, final OtherCaseDTO payload) {
		super(source, payload);
	}

}
