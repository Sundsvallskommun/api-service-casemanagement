package se.sundsvall.casemanagement.service.event;

import se.sundsvall.casemanagement.api.model.EnvironmentalCaseDTO;

public class IncomingEcosCase extends Event<EnvironmentalCaseDTO> {

	private static final long serialVersionUID = 6678986011569774100L;

	public IncomingEcosCase(final Object source, final EnvironmentalCaseDTO payload) {
		super(source, payload);
	}
}
