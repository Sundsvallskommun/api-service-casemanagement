package se.sundsvall.casemanagement.api.validators;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;

public class PlanningStakeholderRoleConstraintValidator implements ConstraintValidator<PlanningStakeholderRole, List<StakeholderRole>> {

	@Override
	public boolean isValid(List<StakeholderRole> roles, ConstraintValidatorContext context) {
		return roles.stream().allMatch(this::isValidRole);
	}

	private boolean isValidRole(StakeholderRole role) {
		// Valid roles
		final Set<StakeholderRole> validRoles = EnumSet.of(
			StakeholderRole.CONTACT_PERSON,
			StakeholderRole.PAYMENT_PERSON,
			StakeholderRole.PROPERTY_OWNER,
			StakeholderRole.APPLICANT,
			StakeholderRole.CONTROL_OFFICIAL);

		// Check if provided role is one of the valid roles.
		return validRoles.contains(role);
	}
}
