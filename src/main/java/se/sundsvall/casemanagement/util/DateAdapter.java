package se.sundsvall.casemanagement.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateAdapter {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_DATE;

    private DateAdapter() {
    }

    public static LocalDateTime parseDateTime(String isoFormatDatetime) {
        return LocalDateTime.parse(isoFormatDatetime, dateTimeFormatter);
    }

    public static LocalDate parseDate(String isoFormatDate) {
        return LocalDate.parse(isoFormatDate, dateFormatter);
    }

    public static String printLocalDateTime(LocalDateTime date) { return date.truncatedTo(ChronoUnit.MICROS).format(dateTimeFormatter); }

    public static String printLocalDate(LocalDate date) {
        return date.format(dateFormatter);
    }

}