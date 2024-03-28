package se.sundsvall.casemanagement.api.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import se.sundsvall.casemanagement.api.model.EcosCaseDTO;
import se.sundsvall.casemanagement.api.validation.SingularFacility;

public class SingularFacilityConstraintValidator implements ConstraintValidator<SingularFacility, EcosCaseDTO> {

	@Override
	public boolean isValid(EcosCaseDTO value, ConstraintValidatorContext context) {
		final var facilityList = value.getFacilities();
		return facilityList != null && facilityList.size() == 1;
	}
}
