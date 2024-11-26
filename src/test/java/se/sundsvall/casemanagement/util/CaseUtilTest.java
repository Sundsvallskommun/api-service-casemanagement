package se.sundsvall.casemanagement.util;

import generated.client.party.PartyType;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casemanagement.TestUtil;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CaseUtilTest {

	@Test
	void testGetFormattedLegalId_Private() {
		final var legalId = TestUtil.generateRandomPersonalNumber();
		final var partyType = PartyType.PRIVATE;

		final var result = CaseUtil.getFormattedLegalId(partyType, legalId);

		final var expected = new StringBuilder(legalId).insert(8, "-").toString();

		assertThat(result).isEqualTo(expected);
	}

	@Test
	void testGetFormattedLegalId_Enterprise() {
		final var legalId = TestUtil.generateRandomOrganizationNumber();
		final var partyType = PartyType.ENTERPRISE;

		final var result = CaseUtil.getFormattedLegalId(partyType, legalId);

		assertThat(result)
			.startsWith("16")
			.hasSize(13)
			.isEqualTo("16" + legalId);
	}

	@Test
	void testGetSokigoFormattedOrganizationNumber() {
		// Arrange
		final var organizationNumber = TestUtil.generateRandomOrganizationNumber();
		// Act
		final String result = CaseUtil.getSokigoFormattedOrganizationNumber(organizationNumber);
		// Assert
		assertThat(result)
			.startsWith("16")
			.hasSize(13)
			.isEqualTo("16" + organizationNumber);
	}

	@Test
	void testGetSokigoFormattedOrganizationNumberNoDifference() {
		// Arrange
		var organizationNumber = TestUtil.generateRandomOrganizationNumber();
		organizationNumber = "10" + organizationNumber;
		// Act
		final String result = CaseUtil.getSokigoFormattedOrganizationNumber(organizationNumber);
		// Assert
		assertThat(result).isEqualTo(organizationNumber);
	}

	@Test
	void testGetSokigoFormattedOrganizationNumberWrongNumberOfDigits() {

		assertThatThrownBy(() -> CaseUtil.getSokigoFormattedOrganizationNumber("123456-123"))
			.isInstanceOf(ThrowableProblem.class)
			.hasMessage("Bad Request: Invalid organizationNumber format: 123456-123")
			.hasFieldOrPropertyWithValue("status", Status.BAD_REQUEST);
	}

	@Test
	void testParseBoolean() {
		assertThat(CaseUtil.parseBoolean("true")).isTrue();
		assertThat(CaseUtil.parseBoolean("false")).isFalse();
		assertThat(CaseUtil.parseBoolean(true)).isTrue();
		assertThat(CaseUtil.parseBoolean(false)).isFalse();
	}

	@Test
	void testGetSokigoFormattedPersonalNumberFromNonHyphenatedPersonalNumber() {
		final var personalNumber = TestUtil.generateRandomPersonalNumber();
		final var result = CaseUtil.getSokigoFormattedPersonalNumber(personalNumber);

		final var expected = new StringBuilder(personalNumber).insert(8, "-").toString();

		assertThat(result)
			.isNotEqualTo(personalNumber)
			.isEqualTo(expected);
	}

	@Test
	void testGetSokigoFormattedPersonalNumberFromProperlyFormattedPersonalNumber() {
		final var personalNumber = new StringBuilder(TestUtil.generateRandomPersonalNumber()).insert(8, "-").toString();
		final var result = CaseUtil.getSokigoFormattedPersonalNumber(personalNumber);
		assertThat(result).isEqualTo(personalNumber);
	}

	@Test
	void testGetSokigoFormattedPersonalNumberFromNull() {
		assertThatThrownBy(() -> CaseUtil.getSokigoFormattedPersonalNumber("null"))
			.isInstanceOf(ThrowableProblem.class)
			.hasMessage("Bad Request: personalNumber must be 12 digits");
	}

	@Test
	void testGetSokigoFormattedPersonalNumberFromWronglyFormattedPersonalNumber() {

		assertThatThrownBy(() -> CaseUtil.getSokigoFormattedPersonalNumber("19000101"))
			.isInstanceOf(ThrowableProblem.class)
			.hasMessage("Bad Request: personalNumber must be 12 digits");
	}

	@Test
	void testParseBooleanWierdText() {
		assertThat(CaseUtil.parseBoolean("wierd text")).isFalse();
	}

	@Test
	void testParseBooleanNull() {
		assertThat(CaseUtil.parseBoolean(null)).isFalse();
	}

	@Test
	void testParseInteger() {
		assertThat(CaseUtil.parseInteger("100")).isEqualTo(100);
		assertThat(CaseUtil.parseInteger(100)).isEqualTo(100);
	}

	@Test
	void testParseIntegerNull() {
		assertThat(CaseUtil.parseInteger(null)).isNull();
	}

	@Test
	void testParseIntegerWierdText() {
		assertThatThrownBy(() -> CaseUtil.parseInteger("wierd text"))
			.isInstanceOf(NumberFormatException.class)
			.hasMessage("For input string: \"wierd text\"");
	}

	@Test
	void testParseDouble() {
		assertThat(CaseUtil.parseDouble("100.0")).isEqualTo(Double.valueOf(100.0));
		assertThat(CaseUtil.parseDouble(100.0)).isEqualTo(Double.valueOf(100.0));
	}

	@Test
	void testParseDoubleNull() {
		assertThat(CaseUtil.parseDouble(null)).isNull();
	}

	@Test
	void testParseDoubleWierdText() {
		assertThatThrownBy(() -> CaseUtil.parseDouble("wierd text"))
			.isInstanceOf(NumberFormatException.class)
			.hasMessage("For input string: \"wierd text\"");
	}

	@Test
	void testParseLocalDateTime() {
		final LocalDate localDate = LocalDate.now();
		assertThat(CaseUtil.parseLocalDateTime(localDate.toString())).isEqualTo(localDate.atStartOfDay());
	}

	@Test
	void testParseLocalDateTimeNull() {
		assertThat(CaseUtil.parseLocalDateTime(null)).isNull();
	}

	@Test
	void testParseLocalDateTimeWierdText() {

		assertThatThrownBy(() -> CaseUtil.parseLocalDateTime("wierd text"))
			.isInstanceOf(DateTimeParseException.class)
			.hasMessage("Text 'wierd text' could not be parsed at index 0");
	}

	@Test
	void testParseString() {
		assertThat(CaseUtil.parseString("test")).isEqualTo("test");
	}

	@Test
	void testParseStringNull() {
		assertThat(CaseUtil.parseString(null)).isNull();
	}

	@Test
	void testParseStringEmpty() {
		assertThat(CaseUtil.parseString("")).isEmpty();
	}

	@Test
	void testParseStringBlank() {
		assertThat(CaseUtil.parseString(" ")).isEqualTo(" ");
	}

}
