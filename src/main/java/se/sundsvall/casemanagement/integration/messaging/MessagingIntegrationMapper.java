package se.sundsvall.casemanagement.integration.messaging;

import org.springframework.stereotype.Component;

import generated.client.messaging.SlackRequest;

@Component
public class MessagingIntegrationMapper {

    private final MessagingProperties properties;

    public MessagingIntegrationMapper(final MessagingProperties properties) {this.properties = properties;}

    public SlackRequest toRequest(String message) {
        return new SlackRequest()
            .message(message)
            .token(properties.getToken())
            .channel(properties.getChannel());
    }
}
