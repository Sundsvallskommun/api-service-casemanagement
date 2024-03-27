package se.sundsvall.casemanagement.api.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casemanagement.TestUtil.createEnvironmentalCase;
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
import se.sundsvall.casemanagement.api.validation.impl.EcosCaseFacilityConstraintValidator;

@ExtendWith(MockitoExtension.class)
class EcosCaseFacilityConstraintValidatorTest {

	@InjectMocks
	private EcosCaseFacilityConstraintValidator validator;

	@Mock
	private ConstraintValidatorContext context;

	@Test
	void isValid_OneFacility() {
		final var ecosCase = createEnvironmentalCase(CaseType.REGISTRERING_AV_LIVSMEDEL, AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		ecosCase.setFacilities(List.of(createFacilityDTO(false)));
		assertThat(validator.isValid(ecosCase, context)).isTrue();
	}

	@Test
	void isValid_NoFacility() {
		final var ecosCase = createEnvironmentalCase(CaseType.REGISTRERING_AV_LIVSMEDEL, AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		ecosCase.setFacilities(List.of());
		assertThat(validator.isValid(ecosCase, context)).isFalse();
	}

	@Test
	void isValid_TwoFacilities() {
		final var ecosCase = createEnvironmentalCase(CaseType.REGISTRERING_AV_LIVSMEDEL, AttachmentCategory.BUILDING_PERMIT_APPLICATION);
		ecosCase.setFacilities(List.of(createFacilityDTO(false), createFacilityDTO(true)));
		assertThat(validator.isValid(ecosCase, context)).isFalse();
	}
}
