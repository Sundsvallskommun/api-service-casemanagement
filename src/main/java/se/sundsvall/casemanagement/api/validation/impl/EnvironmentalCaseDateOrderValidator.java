package se.sundsvall.casemanagement.api.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import se.sundsvall.casemanagement.api.model.EcosCaseDTO;
import se.sundsvall.casemanagement.api.validation.EnvironmentalCaseDateOrder;

public class EnvironmentalCaseDateOrderValidator implements ConstraintValidator<EnvironmentalCaseDateOrder, EcosCaseDTO> {
	@Override
	public boolean isValid(EcosCaseDTO environmentalCase, ConstraintValidatorContext context) {
		if ((environmentalCase.getStartDate() != null) && (environmentalCase.getEndDate() != null)) {
			return environmentalCase.getStartDate().isBefore(environmentalCase.getEndDate());
		}
		return true;
	}
}
