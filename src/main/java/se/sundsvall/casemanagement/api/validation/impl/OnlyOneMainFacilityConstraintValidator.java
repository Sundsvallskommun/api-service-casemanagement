package se.sundsvall.casemanagement.api.validation.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import se.sundsvall.casemanagement.api.model.FacilityDTO;
import se.sundsvall.casemanagement.api.validation.OnlyOneMainFacility;

public class OnlyOneMainFacilityConstraintValidator implements ConstraintValidator<OnlyOneMainFacility, List<FacilityDTO>> {

	private boolean nullable;

	@Override
	public void initialize(OnlyOneMainFacility constraintAnnotation) {
		this.nullable = constraintAnnotation.nullable();
	}

	@Override
	public boolean isValid(List<FacilityDTO> value, ConstraintValidatorContext context) {
		if (value == null) {
			return nullable;
		}

		final var mainFacilities = Optional.of(value).orElse(Collections.emptyList())
			.stream().filter(FacilityDTO::isMainFacility)
			.count();

		return mainFacilities <= 1;
	}

}
