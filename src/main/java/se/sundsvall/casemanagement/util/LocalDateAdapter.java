package se.sundsvall.casemanagement.util;

import static java.time.format.DateTimeFormatter.ISO_DATE;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.util.Optional;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

	@Override
	public LocalDate unmarshal(final String string) {
		return Optional.ofNullable(string)
			.map(s -> ISO_DATE.parse(s, LocalDate::from))
			.orElse(null);
	}

	@Override
	public String marshal(final LocalDate localDate) {
		return Optional.ofNullable(localDate)
			.map(ISO_DATE::format)
			.orElse(null);
	}
}
