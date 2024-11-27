package se.sundsvall.casemanagement.integration.alkt.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.casemanagement.Application;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

@SpringBootTest(classes = Application.class, webEnvironment = MOCK)
@ActiveProfiles("junit")
class AlkTPropertiesTest {

	@Autowired
	private AlkTProperties properties;

	@Test
	void testProperties() {
		assertThat(properties.oauth2ClientId()).isEqualTo("clientId");
		assertThat(properties.oauth2ClientSecret()).isEqualTo("clientSecret");
		assertThat(properties.oauth2TokenUrl()).isEqualTo("token.url");
		assertThat(properties.url()).isEqualTo("alk-t.url");
	}
}
