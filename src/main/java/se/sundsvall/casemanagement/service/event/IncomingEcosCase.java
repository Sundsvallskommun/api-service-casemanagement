package se.sundsvall.casemanagement.service.event;

import java.io.Serial;

import se.sundsvall.casemanagement.api.model.EcosCaseDTO;

public class IncomingEcosCase extends Event<EcosCaseDTO> {

	@Serial
	private static final long serialVersionUID = 6678986011569774100L;

	public IncomingEcosCase(final Object source, final EcosCaseDTO payload) {
		super(source, payload);
	}

}
