package se.sundsvall.casemanagement.integration.edpfuture.configuration;

import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casemanagement.integration.util.AbstractErrorDecoder;
import se.sundsvall.dept44.exception.ServerProblem;

import static org.zalando.problem.Status.BAD_GATEWAY;

public class EDPFutureErrorDecoder extends AbstractErrorDecoder {

	@Override
	protected ThrowableProblem defaultError(String message) {
		return new ServerProblem(BAD_GATEWAY, "Unknown problem in communication with EDPFuture: " + message);
	}
}
