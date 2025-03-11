package se.sundsvall.casemanagement.api.validation;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casemanagement.api.model.EcosCaseDTO;
import se.sundsvall.casemanagement.api.validation.impl.EcosCaseDateOrderValidator;

@ExtendWith(MockitoExtension.class)
class EcosCaseDateOrderValidatorTest {

	@InjectMocks
	private EcosCaseDateOrderValidator validator;

	@Mock
	private ConstraintValidatorContext context;

	@Test
	void isValidStartDateBeforeEndDate() {
		final var ecosCaseDTO = new EcosCaseDTO();
		ecosCaseDTO.setStartDate(LocalDate.of(2021, 1, 1));
		ecosCaseDTO.setEndDate(LocalDate.of(2021, 1, 2));

		assertThat(validator.isValid(ecosCaseDTO, context)).isTrue();
	}

	@Test
	void isValidEndDateBeforeStartDate() {
		final var ecosCaseDTO = new EcosCaseDTO();
		ecosCaseDTO.setStartDate(LocalDate.of(2021, 1, 2));
		ecosCaseDTO.setEndDate(LocalDate.of(2021, 1, 1));

		assertThat(validator.isValid(ecosCaseDTO, context)).isFalse();
	}

	@Test
	void isValidStartDateNull() {
		final var ecosCaseDTO = new EcosCaseDTO();
		ecosCaseDTO.setEndDate(LocalDate.of(2021, 1, 1));

		assertThat(validator.isValid(ecosCaseDTO, context)).isTrue();
	}

	@Test
	void isValidEndDateNull() {
		final var ecosCaseDTO = new EcosCaseDTO();
		ecosCaseDTO.setStartDate(LocalDate.of(2021, 1, 1));

		assertThat(validator.isValid(ecosCaseDTO, context)).isTrue();
	}
}
