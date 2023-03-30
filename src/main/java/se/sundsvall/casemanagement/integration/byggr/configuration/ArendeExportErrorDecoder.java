package se.sundsvall.casemanagement.integration.byggr.configuration;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.dept44.exception.ClientProblem;
import se.sundsvall.dept44.exception.ServerProblem;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.IOException;

public class ArendeExportErrorDecoder implements ErrorDecoder {
    private final String soapProtocol;

    public ArendeExportErrorDecoder() {
        this.soapProtocol = SOAPConstants.SOAP_1_1_PROTOCOL;
    }

    public Exception decode(String methodKey, Response response) {
        if (response.body() != null) {
            try {
                SOAPMessage message = MessageFactory.newInstance(this.soapProtocol).createMessage(null, response.body().asInputStream());
                if (message.getSOAPBody() != null
                        && message.getSOAPBody().hasFault()) {
                    if (message.getSOAPBody().getFault().getFaultString().contains("Object reference not set to an instance of an object")) {
                        return new ClientProblem(Status.BAD_REQUEST, "Bad request exception from ArendeExport (ByggR)");
                    } else if (message.getSOAPBody().getFault().getFaultString().contains("Arende missing for Dnr")
                            || message.getSOAPBody().getFault().getFaultString().contains("Arende not found")) {
                        return new ClientProblem(Status.NOT_FOUND, "No case was found in ArendeExport (ByggR)");
                    }
                }
            } catch (IOException | SOAPException ignored) {
                return defaultError();
            }
        }
        return defaultError();
    }

    private ThrowableProblem defaultError() {
        return new ServerProblem(Status.BAD_GATEWAY, "Unknown problem in communication with ArendeExport (ByggR)");
    }

}
