package se.sundsvall.casemanagement.service.event;

import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;

import java.io.Serial;

public class UpdateByggrCase extends Event<ByggRCaseDTO> {

	@Serial
	private static final long serialVersionUID = -6097420394998607304L;

	public UpdateByggrCase(final Object source, final ByggRCaseDTO payload, final String municipalityId) {
		super(source, payload, municipalityId);
	}
}
