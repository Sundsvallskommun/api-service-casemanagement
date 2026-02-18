package se.sundsvall.casemanagement.api.validation;

import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casemanagement.api.validation.impl.OnlyOneMainFacilityConstraintValidator;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casemanagement.TestUtil.createFacilityDTO;

@ExtendWith(MockitoExtension.class)
class OnlyOneMainFacilityConstraintValidatorTest {

	@InjectMocks
	private OnlyOneMainFacilityConstraintValidator validator;

	@Mock
	private ConstraintValidatorContext context;

	@Test
	void isValidWithOneMainFacility() {
		final var facilities = List.of(createFacilityDTO(true), createFacilityDTO(false));
		assertThat(validator.isValid(facilities, context)).isTrue();
	}

	@Test
	void isValidWithNoMainFacility() {
		final var facilities = List.of(createFacilityDTO(false), createFacilityDTO(false));
		assertThat(validator.isValid(facilities, context)).isTrue();
	}

	@Test
	void isValidWithTwoMainFacilities() {
		final var facilities = List.of(createFacilityDTO(true), createFacilityDTO(true));
		assertThat(validator.isValid(facilities, context)).isFalse();
	}
}
