package se.sundsvall.casemanagement.integration.byggr.configuration;

import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.casemanagement.integration.util.AbstractErrorDecoder;
import se.sundsvall.dept44.exception.ServerProblem;

public class ArendeExportErrorDecoder extends AbstractErrorDecoder {

    public ArendeExportErrorDecoder() {
    }

    protected ThrowableProblem defaultError(String message) {
        return new ServerProblem(Status.BAD_GATEWAY, "Unknown problem in communication with ArendeExport (ByggR) " + message);
    }

}
