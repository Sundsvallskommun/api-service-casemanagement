package se.sundsvall.casemanagement.integration.ecos.configuration;

import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.casemanagement.integration.util.AbstractErrorDecoder;
import se.sundsvall.dept44.exception.ClientProblem;

public class MinutMiljoErrorDecoder extends AbstractErrorDecoder {

    public MinutMiljoErrorDecoder() {
    }

    protected ThrowableProblem defaultError(String message) {
        return new ClientProblem(Status.BAD_GATEWAY, "Bad request exception from MinutMiljo " +
            "(Ecos): " + message);
    }
}