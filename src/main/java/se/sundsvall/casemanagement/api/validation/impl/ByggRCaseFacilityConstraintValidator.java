package se.sundsvall.casemanagement.api.validation.impl;

import java.util.Collections;
import java.util.Optional;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;
import se.sundsvall.casemanagement.api.model.FacilityDTO;
import se.sundsvall.casemanagement.api.validation.ByggRCaseFacility;

public class ByggRCaseFacilityConstraintValidator implements ConstraintValidator<ByggRCaseFacility, ByggRCaseDTO> {

	@Override
	public boolean isValid(ByggRCaseDTO value, ConstraintValidatorContext context) {

		final var mainFacilities = Optional.ofNullable(value.getFacilities()).orElse(Collections.emptyList())
			.stream().filter(FacilityDTO::isMainFacility)
			.count();

		return mainFacilities == 1;
	}

}
