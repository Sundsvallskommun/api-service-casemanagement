package se.sundsvall.casemanagement.api.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casemanagement.TestUtil.createByggRCaseDTO;
import static se.sundsvall.casemanagement.TestUtil.createFacilityDTO;

import java.util.List;

import jakarta.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.validation.impl.OneMainFacilityConstraintValidator;

@ExtendWith(MockitoExtension.class)
class OneMainFacilityConstraintValidatorTest {

	@InjectMocks
	private OneMainFacilityConstraintValidator validator;

	@Mock
	private ConstraintValidatorContext context;

	@Test
	void isValid_WithOneMainFacility() {
		final var byggRCase = createByggRCaseDTO(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP, AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		byggRCase.setFacilities(List.of(createFacilityDTO(true), createFacilityDTO(false)));
		assertThat(validator.isValid(byggRCase, context)).isTrue();
	}

	@Test
	void isValid_WithNoMainFacility() {
		final var byggRCase = createByggRCaseDTO(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP, AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		byggRCase.setFacilities(List.of(createFacilityDTO(false), createFacilityDTO(false)));
		assertThat(validator.isValid(byggRCase, context)).isFalse();
	}

	@Test
	void isValid_withTwoMainFacilities() {
		final var byggRCase = createByggRCaseDTO(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP, AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		byggRCase.setFacilities(List.of(createFacilityDTO(true), createFacilityDTO(true)));
		assertThat(validator.isValid(byggRCase, context)).isFalse();
	}
}
