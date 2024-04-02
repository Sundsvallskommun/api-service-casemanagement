package se.sundsvall.casemanagement.api.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.casemanagement.api.validation.impl.EcosStakeholderRoleConstraintValidator;

@ExtendWith(MockitoExtension.class)
class EcosStakeholderRoleConstraintValidatorTest {

	@InjectMocks
	private EcosStakeholderRoleConstraintValidator validator;

	@Mock
	private ConstraintValidatorContext context;

	@Test
	void isValid_ValidValues() {
		final var values = List.of("CONTACT_PERSON", "INVOICE_RECIPENT", "INVOICE_RECIPIENT", "OPERATOR", "APPLICANT", "INSTALLER");
		assertThat(validator.isValid(values, context)).isTrue();
	}

	@Test
	void isValid_OneInvalidRole() {
		final var values = List.of("CONTACT_PERSON", "INVOICE_RECIPENT", "INVOICE_RECIPIENT", "OPERATOR", "APPLICANT", "INSTALLER", "INVALID");
		assertThat(validator.isValid(values, context)).isFalse();
	}

	@Test
	void isValid_BlankRole() {
		assertThat(validator.isValid(List.of(""), context)).isFalse();
	}

	@Test
	void isValid_NullRole() {
		assertThat(validator.isValid(null, context)).isTrue();
	}
}
