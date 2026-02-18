package se.sundsvall.casemanagement.api.validation;

import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casemanagement.api.validation.impl.EcosStakeholderRoleConstraintValidator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class EcosStakeholderRoleConstraintValidatorTest {

	@InjectMocks
	private EcosStakeholderRoleConstraintValidator validator;

	@Mock
	private ConstraintValidatorContext context;

	@Test
	void isValidValidValues() {
		final var values = List.of("CONTACT_PERSON", "INVOICE_RECIPIENT", "OPERATOR", "APPLICANT", "INSTALLER");
		assertThat(validator.isValid(values, context)).isTrue();
	}

	@Test
	void isValidOneInvalidRole() {
		final var values = List.of("CONTACT_PERSON", "INVOICE_RECIPIENT", "OPERATOR", "APPLICANT", "INSTALLER", "INVALID");
		assertThat(validator.isValid(values, context)).isFalse();
	}

	@Test
	void isValidBlankRole() {
		assertThat(validator.isValid(List.of(""), context)).isFalse();
	}

	@Test
	void isValidNullRole() {
		assertThat(validator.isValid(null, context)).isTrue();
	}
}
