package se.sundsvall.casemanagement.integration.edpfuture.configuration;

import feign.jaxb.JAXBContextFactory;
import feign.soap.SOAPDecoder;
import feign.soap.SOAPEncoder;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import java.nio.charset.StandardCharsets;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;

@Import(FeignConfiguration.class)
public class EDPFutureConfiguration {

	public static final String REGISTRATION_ID = "edpfuture";

	private static final JAXBContextFactory JAXB_FACTORY = new JAXBContextFactory.Builder()
		.withMarshallerJAXBEncoding(StandardCharsets.UTF_8.toString())
		.build();

	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer(final EDPFutureProperties properties) {
		return FeignMultiCustomizer.create()
			.withEncoder(new EDPFutureSOAPEncoder(JAXB_FACTORY, properties.apiKey()))
			.withDecoder(new SOAPDecoder(JAXB_FACTORY))
			.withErrorDecoder(new EDPFutureErrorDecoder())
			.withRequestTimeoutsInSeconds(properties.connectTimeout(), properties.readTimeout())
			.composeCustomizersToOne();
	}

	static class EDPFutureSOAPEncoder extends SOAPEncoder {

		private final String token;

		public EDPFutureSOAPEncoder(final JAXBContextFactory jaxbContextFactory, final String token) {
			super(jaxbContextFactory);
			this.token = token;
		}

		@Override
		protected SOAPMessage modifySOAPMessage(final SOAPMessage soapMessage) throws SOAPException {
			var tokenElement = soapMessage.getSOAPHeader()
				.addChildElement("token", "ns", "ns");
			tokenElement.setTextContent(token);
			return soapMessage;
		}
	}
}
