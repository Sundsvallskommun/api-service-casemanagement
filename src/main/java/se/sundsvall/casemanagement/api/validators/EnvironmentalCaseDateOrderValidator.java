package se.sundsvall.casemanagement.api.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import se.sundsvall.casemanagement.api.model.EnvironmentalCaseDTO;

public class EnvironmentalCaseDateOrderValidator implements ConstraintValidator<EnvironmentalCaseDateOrder, EnvironmentalCaseDTO> {
    @Override
    public boolean isValid(EnvironmentalCaseDTO environmentalCase, ConstraintValidatorContext context) {
        if ((environmentalCase.getStartDate() != null) && (environmentalCase.getEndDate() != null)) {
            return environmentalCase.getStartDate().isBefore(environmentalCase.getEndDate());
        }
        return true;
    }
}
