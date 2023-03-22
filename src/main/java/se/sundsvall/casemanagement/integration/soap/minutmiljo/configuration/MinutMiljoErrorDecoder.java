package se.sundsvall.casemanagement.integration.soap.minutmiljo.configuration;

import java.io.IOException;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.dept44.exception.ClientProblem;

import feign.Response;
import feign.codec.ErrorDecoder;

public class MinutMiljoErrorDecoder implements ErrorDecoder {
    
    private final String soapProtocol;
    
    public MinutMiljoErrorDecoder() {
        this.soapProtocol = SOAPConstants.SOAP_1_1_PROTOCOL;
    }
    
    public Exception decode(String methodKey, Response response) {
        if (response.body() != null) {
            try {
                SOAPMessage message = MessageFactory.newInstance(this.soapProtocol).createMessage(null, response.body().asInputStream());
                if (message.getSOAPBody() != null &&
                    message.getSOAPBody().hasFault() &&
                    message.getSOAPBody().getFault().getFaultString().contains("Sequence contains no elements")) {
                    throw new ClientProblem(Status.NOT_FOUND, "No case was found in ECOS.");
                } else {
                    if (message.getSOAPBody() != null &&
                        message.getSOAPBody().hasFault()) {
                        return defaultError(message.getSOAPBody().getFault().getFaultString());
                    }
                }
            } catch (IOException | SOAPException exception) {
                return defaultError(exception.getMessage());
            }
        }
        return defaultError(response.reason());
    }
    
    private ThrowableProblem defaultError(String message) {
        return new ClientProblem(Status.BAD_REQUEST, "Bad request exception from MinutMiljo " +
                                                     "(Ecos): " + message);
    }
}