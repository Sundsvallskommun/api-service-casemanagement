package se.sundsvall.casemanagement.util;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.time.temporal.ChronoUnit.MICROS;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;
import java.util.Optional;

public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

	@Override
	public LocalDateTime unmarshal(final String string) {
		return Optional.ofNullable(string)
			.map(s -> ISO_DATE_TIME.parse(s, LocalDateTime::from))
			.orElse(null);
	}

	@Override
	public String marshal(final LocalDateTime localDateTime) {
		return Optional.ofNullable(localDateTime)
			.map(d -> ISO_DATE_TIME.format(d.truncatedTo(MICROS)))
			.orElse(null);
	}
}
