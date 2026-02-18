package se.sundsvall.casemanagement.integration.edpfuture.configuration;

import edpfuture.AgarbyteBlankett;
import feign.RequestTemplate;
import feign.jaxb.JAXBContextFactory;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

import static org.assertj.core.api.Assertions.assertThat;

class EDPFutureSOAPEncoderTest {

	private EDPFutureConfiguration.EDPFutureSOAPEncoder encoder;

	@BeforeEach
	void setUp() {
		JAXBContextFactory jaxbFactory = new JAXBContextFactory.Builder()
			.withMarshallerJAXBEncoding(StandardCharsets.UTF_8.toString())
			.build();
		encoder = new EDPFutureConfiguration.EDPFutureSOAPEncoder(jaxbFactory, "12345==");
	}

	@Test
	void shouldAddTokenToSOAPHeader() {
		var template = new RequestTemplate();
		var request = new AgarbyteBlankett();

		encoder.encode(request, AgarbyteBlankett.class, template);

		var xml = new String(template.body(), StandardCharsets.UTF_8);

		assertThat(xml)
			.contains("<ns:token xmlns:ns=\"ns\">12345==</ns:token>")
			.contains("<SOAP-ENV:Header>")
			.contains("</SOAP-ENV:Header>");
	}

	@Test
	void shouldProduceValidSOAPStructure() throws Exception {
		var template = new RequestTemplate();
		var request = new AgarbyteBlankett();

		encoder.encode(request, AgarbyteBlankett.class, template);

		var xml = new String(template.body(), StandardCharsets.UTF_8);

		var factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		var doc = factory.newDocumentBuilder()
			.parse(new InputSource(new StringReader(xml)));

		var tokenNodes = doc.getElementsByTagNameNS("ns", "token");
		assertThat(tokenNodes.getLength()).isEqualTo(1);
		assertThat(tokenNodes.item(0).getTextContent()).isEqualTo("12345==");
	}
}
