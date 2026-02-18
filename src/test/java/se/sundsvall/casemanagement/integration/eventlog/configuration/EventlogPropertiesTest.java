package se.sundsvall.casemanagement.integration.eventlog.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.casemanagement.Application;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class EventlogPropertiesTest {

	@Autowired
	private EventlogProperties properties;

	@Test
	void testProperties() {
		assertThat(properties.oauth2ClientId()).isEqualTo("clientId");
		assertThat(properties.oauth2ClientSecret()).isEqualTo("clientSecret");
		assertThat(properties.oauth2TokenUrl()).isEqualTo("token.url");
		assertThat(properties.url()).isEqualTo("eventlog.url");
	}
}
