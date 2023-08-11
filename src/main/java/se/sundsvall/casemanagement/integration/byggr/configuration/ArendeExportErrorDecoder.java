package se.sundsvall.casemanagement.integration.byggr.configuration;

import static org.zalando.problem.Status.BAD_GATEWAY;

import org.zalando.problem.ThrowableProblem;

import se.sundsvall.casemanagement.integration.util.AbstractErrorDecoder;
import se.sundsvall.dept44.exception.ServerProblem;

public class ArendeExportErrorDecoder extends AbstractErrorDecoder {

	@Override
	protected ThrowableProblem defaultError(String message) {
		return new ServerProblem(BAD_GATEWAY, "Unknown problem in communication with ArendeExport (ByggR) " + message);
	}
}
