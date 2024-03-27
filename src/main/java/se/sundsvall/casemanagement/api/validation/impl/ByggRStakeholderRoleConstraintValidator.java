package se.sundsvall.casemanagement.api.validation.impl;

import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.api.validation.ByggRStakeholderRole;

public class ByggRStakeholderRoleConstraintValidator implements ConstraintValidator<ByggRStakeholderRole, List<String>> {

	@Override
	public boolean isValid(List<String> roles, ConstraintValidatorContext context) {
		return roles.stream().allMatch(this::isValidRole);
	}

	private boolean isValidRole(String role) {

		if (role == null || role.isEmpty()) {
			return false;
		}

		// Valid roles
		final Set<String> validRoles = Set.of(
			StakeholderRole.CONTACT_PERSON.toString(),
			StakeholderRole.PAYMENT_PERSON.toString(),
			StakeholderRole.PROPERTY_OWNER.toString(),
			StakeholderRole.APPLICANT.toString(),
			StakeholderRole.CONTROL_OFFICIAL.toString());

		// Check if provided role is one of the valid roles.
		return validRoles.contains(role);
	}
}
