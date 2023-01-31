package se.sundsvall.casemanagement.api.validators;

import se.sundsvall.casemanagement.api.model.EnvironmentalCaseDTO;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EnvironmentalCaseDateOrderValidator implements ConstraintValidator<EnvironmentalCaseDateOrder, EnvironmentalCaseDTO> {
    @Override
    public boolean isValid(EnvironmentalCaseDTO environmentalCase, ConstraintValidatorContext context) {
        if (environmentalCase.getStartDate() != null && environmentalCase.getEndDate() != null) {
            return environmentalCase.getStartDate().isBefore(environmentalCase.getEndDate());
        } else {
            return true;
        }
    }
}
