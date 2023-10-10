package se.sundsvall.casemanagement.service.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public abstract class Event<T> extends ApplicationEvent {

	private static final long serialVersionUID = 7360184846126086166L;

	private final T payload;

	protected Event(final Object source, final T payload) {
		super(source);

		this.payload = payload;
	}
}
