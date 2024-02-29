package se.sundsvall.casemanagement.service.event;

import java.io.Serial;
import java.io.Serializable;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public abstract class Event<T extends Serializable> extends ApplicationEvent implements Serializable {

	@Serial
	private static final long serialVersionUID = 7360184846126086166L;

	private final T payload;

	protected Event(final Object source, final T payload) {
		super(source);

		this.payload = payload;
	}

}
