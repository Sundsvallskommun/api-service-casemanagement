package se.sundsvall.casemanagement.integration.byggr.configuration;

import se.sundsvall.casemanagement.integration.util.AbstractErrorDecoder;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.ThrowableProblem;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;

public class ArendeExportErrorDecoder extends AbstractErrorDecoder {

	@Override
	protected ThrowableProblem defaultError(String message) {
		return Problem.valueOf(BAD_GATEWAY, "Unknown problem in communication with ArendeExport (ByggR) " + message);
	}
}
