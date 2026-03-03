package se.sundsvall.casemanagement.service.event;

import java.io.Serial;
import se.sundsvall.casemanagement.api.model.FutureCaseDTO;

public class IncomingFutureCase extends Event<FutureCaseDTO> {

	@Serial
	private static final long serialVersionUID = 3482917560184273951L;

	public IncomingFutureCase(final Object source, final FutureCaseDTO payload, final String municipalityId, final String requestId) {
		super(source, payload, municipalityId, requestId);
	}

}
