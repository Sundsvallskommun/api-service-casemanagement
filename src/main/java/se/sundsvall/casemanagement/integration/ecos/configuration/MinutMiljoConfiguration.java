package se.sundsvall.casemanagement.integration.ecos.configuration;

import feign.Client;
import feign.jaxb.JAXBContextFactory;
import feign.okhttp.OkHttpClient;
import feign.soap.SOAPDecoder;
import feign.soap.SOAPEncoder;
import jakarta.xml.soap.SOAPConstants;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.net.ssl.X509TrustManager;
import jcifs.CIFSContext;
import jcifs.context.SingletonContext;
import jcifs.ntlmssp.Type1Message;
import jcifs.ntlmssp.Type2Message;
import jcifs.ntlmssp.Type3Message;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.security.Truststore;

@Import(FeignConfiguration.class)
public class MinutMiljoConfiguration {

	public static final String CLIENT_ID = "minutmiljo";

	private static final Logger log = LoggerFactory.getLogger(MinutMiljoConfiguration.class);

	private static final JAXBContextFactory JAXB_FACTORY = new JAXBContextFactory.Builder()
		.withMarshallerJAXBEncoding(StandardCharsets.UTF_8.toString())
		.build();

	private static final SOAPEncoder.Builder ENCODER_BUILDER = new SOAPEncoder.Builder()
		.withCharsetEncoding(StandardCharsets.UTF_8)
		.withFormattedOutput(false)
		.withJAXBContextFactory(JAXB_FACTORY)
		.withSOAPProtocol(SOAPConstants.SOAP_1_1_PROTOCOL)
		.withWriteXmlDeclaration(true);

	private final MinutMiljoProperties minutMiljoProperties;

	public MinutMiljoConfiguration(final MinutMiljoProperties minutMiljoProperties) {
		this.minutMiljoProperties = minutMiljoProperties;
	}

	@Bean
	Client okHttpClient(final Truststore trustStore) {

		final var trustManagerFactory = trustStore.getTrustManagerFactory();
		final var trustManager = (X509TrustManager) trustManagerFactory.getTrustManagers()[0];

		return new OkHttpClient(new okhttp3.OkHttpClient.Builder()
			.sslSocketFactory(trustStore.getSSLContext().getSocketFactory(), trustManager)
			.authenticator(new NTLMAuthenticator(minutMiljoProperties.username(), minutMiljoProperties.password()))
			.build());
	}

	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer(final MinutMiljoProperties properties) {
		return FeignMultiCustomizer.create()
			.withEncoder(ENCODER_BUILDER.build())
			.withDecoder(new SOAPDecoder(JAXB_FACTORY))
			.withErrorDecoder(new MinutMiljoErrorDecoder())
			.withRequestTimeoutsInSeconds(properties.connectTimeout(), properties.readTimeout())
			.composeCustomizersToOne();
	}

	protected static class NTLMAuthenticator implements Authenticator {

		private final String username;

		private final String password;

		private final String ntlmMsg1;
		private final CIFSContext cifsContext;

		NTLMAuthenticator(final String username, final String password) {
			this.username = username;
			this.password = password;
			cifsContext = SingletonContext.getInstance();
			if (cifsContext == null) {
				throw new IllegalStateException("Unable to initialize jcifs context");
			}
			final var type1Message = new Type1Message(cifsContext, Type1Message.getDefaultFlags(cifsContext), null, null);
			ntlmMsg1 = Base64.getEncoder().encodeToString(type1Message.toByteArray());
		}

		@Override
		public Request authenticate(final Route route, final Response response) {
			final var wwwAuthenticate = response.headers().values("WWW-Authenticate");
			if (wwwAuthenticate.contains("NTLM")) {
				return response.request().newBuilder().header("Authorization", "NTLM " + ntlmMsg1).build();
			}
			String ntlmMsg3 = null;
			try {
				final var type2Message = new Type2Message(Base64.getDecoder().decode(wwwAuthenticate.getFirst().substring(5)));
				final var type3Message = new Type3Message(cifsContext, type2Message, null, password, "", username, null, Type3Message.getDefaultFlags(cifsContext));
				ntlmMsg3 = Base64.getEncoder().encodeToString(type3Message.toByteArray());
			} catch (final Exception e) {
				log.error("Error generating NTLM type 3 message", e);
			}
			return response.request().newBuilder().header("Authorization", "NTLM " + ntlmMsg3).build();
		}

	}

}
