package se.sundsvall.casemanagement.integration.messaging;

import generated.client.messaging.EmailRequest;
import generated.client.messaging.EmailSender;
import generated.client.messaging.SlackRequest;
import org.springframework.stereotype.Component;

@Component
public class MessagingIntegrationMapper {

	private final MessagingProperties properties;

	public MessagingIntegrationMapper(final MessagingProperties properties) {
		this.properties = properties;
	}

	public SlackRequest toRequest(String message) {
		return new SlackRequest()
			.message(message)
			.token(properties.getToken())
			.channel(properties.getChannel());
	}

	public EmailRequest toEmailRequest(String subject, String message) {
		return new EmailRequest()
			.sender(new EmailSender()
				.name("CaseManagement")
				.address("noreply@sundsvall.se"))
			.emailAddress(properties.getMailRecipient())
			.subject(subject)
			.message(message);
	}
}
