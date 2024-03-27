package se.sundsvall.casemanagement.api.validation.impl;

import java.util.List;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import se.sundsvall.casemanagement.api.model.FacilityDTO;
import se.sundsvall.casemanagement.api.validation.OnlyOneMainFacility;

public class OnlyOneMainFacilityConstraintValidator implements ConstraintValidator<OnlyOneMainFacility, List<FacilityDTO>> {

	@Override
	public boolean isValid(List<FacilityDTO> planningPermissionFacilityList, ConstraintValidatorContext context) {
		if (planningPermissionFacilityList == null) {
			// We can't do this validation if the list is null
			return true;
		}
		return planningPermissionFacilityList.stream().filter(FacilityDTO::isMainFacility).count() <= 1;
	}
}
