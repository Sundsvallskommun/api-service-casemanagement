package se.sundsvall.casemanagement.service.event;

import java.io.Serial;
import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;

public class UpdateByggrCase extends Event<ByggRCaseDTO> {

	@Serial
	private static final long serialVersionUID = -6097420394998607304L;

	public UpdateByggrCase(final Object source, final ByggRCaseDTO payload, final String municipalityId, final String requestId) {
		super(source, payload, municipalityId, requestId);
	}
}
