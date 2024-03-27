package se.sundsvall.casemanagement.api.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.validation.ConstraintValidatorContext;

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
	void isValid_ValidValues() {
		final var values = List.of("PROPERTY_OWNER", "PAYMENT_PERSON", "PROPERTY_OWNER", "APPLICANT", "CONTROL_OFFICIAL");
		assertThat(validator.isValid(values, context)).isTrue();
	}

	@Test
	void isValid_OneInvalidRole() {
		final var values = List.of("PROPERTY_OWNER", "PAYMENT_PERSON", "PROPERTY_OWNER", "APPLICANT", "CONTROL_OFFICIAL", "INVALID");
		assertThat(validator.isValid(values, context)).isFalse();
	}
}
