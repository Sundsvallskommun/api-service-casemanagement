package se.sundsvall.casemanagement.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

	@Override
	public LocalDateTime unmarshal(final String s) {
		if (s == null) {
			return null;
		}

		return DateTimeFormatter.ISO_DATE_TIME.parse(s, LocalDateTime::from);
	}

	@Override
	public String marshal(final LocalDateTime localDateTime) {
		if (localDateTime == null) {
			return null;
		}

		return DateTimeFormatter.ISO_DATE_TIME.format(localDateTime.truncatedTo(ChronoUnit.MICROS));
	}
}
