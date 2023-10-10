package se.sundsvall.casemanagement.api.validators;

import se.sundsvall.casemanagement.api.model.PlanningPermissionFacilityDTO;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class OnlyOneMainFacilityValidator implements ConstraintValidator<OnlyOneMainFacility, List<PlanningPermissionFacilityDTO>> {

    @Override
    public boolean isValid(List<PlanningPermissionFacilityDTO> planningPermissionFacilityList, ConstraintValidatorContext context) {
        if (planningPermissionFacilityList == null) {
            // We can't do this validation if the list is null
            return true;
        }
        return planningPermissionFacilityList.stream().filter(PlanningPermissionFacilityDTO::isMainFacility).count() <= 1;
    }
}
