package se.sundsvall.casemanagement.util;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

	@Override
	public LocalDate unmarshal(final String s) {
		if (s == null) {
			return null;
		}

		return DateTimeFormatter.ISO_DATE.parse(s, LocalDate::from);
	}

	@Override
	public String marshal(final LocalDate localDate) {
		if (localDate == null) {
			return null;
		}

		return DateTimeFormatter.ISO_DATE.format(localDate);
	}
}
