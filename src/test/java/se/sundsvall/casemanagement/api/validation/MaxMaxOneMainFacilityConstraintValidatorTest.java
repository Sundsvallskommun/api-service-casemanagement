package se.sundsvall.casemanagement.api.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casemanagement.TestUtil.createFacilityDTO;

import java.util.List;

import jakarta.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.casemanagement.api.validation.impl.MaxOneMainFacilityConstraintValidator;

@ExtendWith(MockitoExtension.class)
class MaxMaxOneMainFacilityConstraintValidatorTest {

	@InjectMocks
	private MaxOneMainFacilityConstraintValidator validator;

	@Mock
	private ConstraintValidatorContext context;

	@Test
	void isValid_WithOneMainFacility() {
		final var facilities = List.of(createFacilityDTO(true), createFacilityDTO(false));
		assertThat(validator.isValid(facilities, context)).isTrue();
	}

	@Test
	void isValid_WithNoMainFacility() {
		final var facilities = List.of(createFacilityDTO(false), createFacilityDTO(false));
		assertThat(validator.isValid(facilities, context)).isFalse();
	}

	@Test
	void isValid_withTwoMainFacilities() {
		final var facilities = List.of(createFacilityDTO(true), createFacilityDTO(true));
		assertThat(validator.isValid(facilities, context)).isFalse();
	}
}
