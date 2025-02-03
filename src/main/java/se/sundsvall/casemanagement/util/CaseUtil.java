package se.sundsvall.casemanagement.util;

import generated.client.party.PartyType;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

public final class CaseUtil {

	private CaseUtil() {
		throw new IllegalStateException("Utility class");
	}

	public static String getFormattedLegalId(final PartyType partyType, final String legalId) {
		if (partyType == PartyType.ENTERPRISE) {
			return getSokigoFormattedOrganizationNumber(legalId);
		}
		return getSokigoFormattedPersonalNumber(legalId);
	}

	public static String getSokigoFormattedOrganizationNumber(final String organizationNumber) {
		System.out.println(organizationNumber);

		// Control that the organizationNumber is not null and that it is a valid length
		if (IntStream.of(13, 12, 11, 10).anyMatch(i -> organizationNumber.length() == i)) {
			// Remove all non-digit characters
			final String cleanNumber = organizationNumber.replaceAll("[\\D]", "");

			if (cleanNumber.length() == 12) {
				// Insert the hyphen at the correct position
				return cleanNumber.substring(0, 8) + "-" + cleanNumber.substring(8);

			}
			if (cleanNumber.length() == 10) {
				// Add "16" at the beginning and insert the hyphen at the correct position
				return "16" + cleanNumber.substring(0, 6) + "-" + cleanNumber.substring(6);
			}
		}

		throw Problem.valueOf(Status.BAD_REQUEST, MessageFormat.format("Invalid organizationNumber format: {0}", organizationNumber));

	}

	public static String getSokigoFormattedPersonalNumber(final String personalNumber) {

		if (personalNumber == null) {
			throw Problem.valueOf(Status.BAD_REQUEST, "personalNumber must not be null");
		}

		// Validates that the personalNumber is correct example: 20220622-2396
		final String properlyFormattedRegex = "^(19|20)\\d{2}(0[1-9]|1[012])(0[1-9]|1[\\d]|2[\\d]|3[0-1])-\\d{4}$";
		// Validates that the personalNumber is correct but missing - example: 202206222396
		final String missingHypen = "^(19|20)\\d{2}(0[1-9]|1[012])(0[1-9]|1[\\d]|2[\\d]|3[0-1])\\d{4}$";

		if (personalNumber.matches(properlyFormattedRegex)) {
			return personalNumber;
		}
		if (personalNumber.matches(missingHypen)) {
			return new StringBuilder(personalNumber).insert(8, "-").toString();
		}
		throw Problem.valueOf(Status.BAD_REQUEST, "personalNumber must be 12 digits");
	}

	/**
	 * Returns boolean, true or false.
	 */
	public static boolean parseBoolean(final Object value) {
		final String stringValue = parseString(value);
		return Boolean.parseBoolean(stringValue);
	}

	/**
	 * Returns Integer or null.
	 */
	public static Integer parseInteger(final Object value) {
		final String stringValue = parseString(value);
		return (stringValue != null) ? Integer.parseInt(stringValue) : null;
	}

	/**
	 * Returns LocalDateTime or null. Parses a date without an offset, such as '2011-12-03'.
	 */
	public static LocalDateTime parseLocalDateTime(final Object value) {
		final String stringValue = parseString(value);
		return (stringValue != null) ? LocalDate.parse(stringValue, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay() : null;
	}

	/**
	 * Returns Double or null.
	 */
	public static Double parseDouble(final Object value) {
		final String stringValue = parseString(value);
		return (stringValue != null) ? Double.parseDouble(stringValue) : null;
	}

	/**
	 * Returns String or null.
	 */
	public static String parseString(final Object value) {
		return Objects.toString(value, null);
	}

	public static boolean notNullOrEmpty(final List<?> list) {
		return ((list != null) && !list.isEmpty());
	}
}
