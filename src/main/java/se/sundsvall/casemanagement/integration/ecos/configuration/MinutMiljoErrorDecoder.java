package se.sundsvall.casemanagement.integration.ecos.configuration;

import static org.zalando.problem.Status.BAD_GATEWAY;

import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casemanagement.integration.util.AbstractErrorDecoder;
import se.sundsvall.dept44.exception.ClientProblem;

public class MinutMiljoErrorDecoder extends AbstractErrorDecoder {

	@Override
	protected ThrowableProblem defaultError(String message) {
		return new ClientProblem(BAD_GATEWAY, "Bad request exception from MinutMiljo (Ecos): " + message);
	}
}
