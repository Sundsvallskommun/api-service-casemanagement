package se.sundsvall.casemanagement.integration.ecos.configuration;

import se.sundsvall.casemanagement.integration.util.AbstractErrorDecoder;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.ThrowableProblem;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;

public class MinutMiljoErrorDecoder extends AbstractErrorDecoder {

	@Override
	protected ThrowableProblem defaultError(String message) {
		return Problem.valueOf(BAD_GATEWAY, "Bad request exception from MinutMiljo (Ecos): " + message);
	}
}
