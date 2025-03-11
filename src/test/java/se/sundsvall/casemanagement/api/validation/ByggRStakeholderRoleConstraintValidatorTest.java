package se.sundsvall.casemanagement.api.validation;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casemanagement.api.validation.impl.ByggRStakeholderRoleConstraintValidator;

@ExtendWith(MockitoExtension.class)
class ByggRStakeholderRoleConstraintValidatorTest {

	@InjectMocks
	private ByggRStakeholderRoleConstraintValidator validator;

	@Mock
	private ConstraintValidatorContext context;

	@Test
	void isValidValidValues() {
		final var values = List.of("PROPERTY_OWNER", "PAYMENT_PERSON", "PROPERTY_OWNER", "APPLICANT", "CONTROL_OFFICIAL");
		assertThat(validator.isValid(values, context)).isTrue();
	}

	@Test
	void isValidOneInvalidRole() {
		final var values = List.of("PROPERTY_OWNER", "PAYMENT_PERSON", "PROPERTY_OWNER", "APPLICANT", "CONTROL_OFFICIAL", "INVALID");
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
