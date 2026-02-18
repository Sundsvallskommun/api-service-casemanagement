package se.sundsvall.casemanagement.api.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.api.validation.ByggRStakeholderRole;

import static java.util.Collections.emptyList;

public class ByggRStakeholderRoleConstraintValidator implements ConstraintValidator<ByggRStakeholderRole, List<String>> {

	private static final Set<String> validRoles = Set.of(StakeholderRole.CONTACT_PERSON.toString(),
		StakeholderRole.PAYMENT_PERSON.toString(),
		StakeholderRole.PROPERTY_OWNER.toString(),
		StakeholderRole.APPLICANT.toString(),
		StakeholderRole.CONTROL_OFFICIAL.toString());

	@Override
	public boolean isValid(final List<String> roles, final ConstraintValidatorContext context) {
		return Optional.ofNullable(roles).orElse(emptyList()).stream()
			.allMatch(this::isValidRole);
	}

	private boolean isValidRole(final String role) {
		return Optional.ofNullable(role).map(validRoles::contains)
			.orElse(false);
	}
}
