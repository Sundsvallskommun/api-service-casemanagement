package se.sundsvall.casemanagement.integration.opene.configuration;

import static org.zalando.problem.Status.BAD_REQUEST;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.zalando.problem.ThrowableProblem;

import feign.Response;
import feign.codec.ErrorDecoder;
import se.sundsvall.dept44.exception.ClientProblem;

public class OpeneErrorDecoder implements ErrorDecoder {

	private final String soapProtocol;

	public OpeneErrorDecoder() {
		this.soapProtocol = SOAPConstants.SOAP_1_1_PROTOCOL;
	}

	@Override
	public Exception decode(String methodKey, Response response) {
		if (response.body() != null) {
			try {
				final SOAPMessage message = createSOAPMessage(response.body().asInputStream());

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

	protected SOAPMessage createSOAPMessage(InputStream inputStream) throws SOAPException, IOException {
		return MessageFactory.newInstance(this.soapProtocol).createMessage(null, inputStream);
	}

	private ThrowableProblem defaultError(String message) {
		return new ClientProblem(BAD_REQUEST, "Bad request exception from OpenE " + message);
	}
}
