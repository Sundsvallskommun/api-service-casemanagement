package se.sundsvall.casemanagement.integration.util;

import java.io.IOException;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.zalando.problem.ThrowableProblem;

import feign.Response;
import feign.codec.ErrorDecoder;

public abstract class AbstractErrorDecoder implements ErrorDecoder {

	@Override
	public Exception decode(String methodKey, Response response) {
		if (response.body() != null) {
			try {
				final String soapProtocol = SOAPConstants.SOAP_1_1_PROTOCOL;
				final SOAPMessage message = MessageFactory.newInstance(soapProtocol).createMessage(null, response.body().asInputStream());
				if ((message.getSOAPBody() != null) &&
					message.getSOAPBody().hasFault()) {
					return defaultError(message.getSOAPBody().getFault().getFaultString());
				}

			} catch (IOException | SOAPException exception) {
				return defaultError(exception.getMessage());
			}
		}
		return defaultError(response.reason());
	}

	protected abstract ThrowableProblem defaultError(String message);

}
