package se.sundsvall.casemanagement.integration.messaging;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(MessagingProperties.class)
public class MessagingIntegration {

	static final String INTEGRATION_NAME = "Messaging";

	private final MessagingClient messagingClient;

	private final MessagingIntegrationMapper mapper;

	public MessagingIntegration(final MessagingClient messagingClient, final MessagingIntegrationMapper mapper) {
		this.messagingClient = messagingClient;
		this.mapper = mapper;
	}

	public void sendSlack(final String message) {
		messagingClient.sendSlack(mapper.toRequest(message));
	}

	public void sendMail(String subject, String message) {
		messagingClient.sendEmail(mapper.toEmailRequest(subject, message));
	}
}
