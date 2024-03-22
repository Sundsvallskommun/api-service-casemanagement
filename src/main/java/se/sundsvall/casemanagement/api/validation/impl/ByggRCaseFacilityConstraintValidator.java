package se.sundsvall.casemanagement.api.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;
import se.sundsvall.casemanagement.api.model.FacilityDTO;
import se.sundsvall.casemanagement.api.validation.ByggRCaseFacility;

public class ByggRCaseFacilityConstraintValidator implements ConstraintValidator<ByggRCaseFacility, ByggRCaseDTO> {

	@Override
	public boolean isValid(ByggRCaseDTO value, ConstraintValidatorContext context) {
		final var facilityList = value.getFacilities();

		if (facilityList == null) {
			return true;
		}

		return facilityList.stream().filter(FacilityDTO::isMainFacility).count() <= 1;
	}

}
