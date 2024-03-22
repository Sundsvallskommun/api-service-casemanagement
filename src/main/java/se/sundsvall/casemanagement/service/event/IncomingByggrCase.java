package se.sundsvall.casemanagement.service.event;

import java.io.Serial;

import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;

public class IncomingByggrCase extends Event<ByggRCaseDTO> {

	@Serial
	private static final long serialVersionUID = -6097420394998607304L;

	public IncomingByggrCase(final Object source, final ByggRCaseDTO payload) {
		super(source, payload);
	}

}
