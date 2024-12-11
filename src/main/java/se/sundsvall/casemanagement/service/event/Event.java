package se.sundsvall.casemanagement.service.event;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public abstract class Event<T extends Serializable> extends ApplicationEvent implements Serializable {

	@Serial
	private static final long serialVersionUID = 7360184846126086166L;

	private final T payload;

	private final String municipalityId;

	protected Event(final Object source, final T payload, final String municipalityId) {
		super(source);
		this.payload = payload;
		this.municipalityId = municipalityId;
	}

}
