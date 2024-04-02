package se.sundsvall.casemanagement.api.validation.impl;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.api.validation.EcosStakeholderRole;

public class EcosStakeholderRoleConstraintValidator implements ConstraintValidator<EcosStakeholderRole, List<String>> {

	private static final Set<String> validRoles = Set.of(StakeholderRole.CONTACT_PERSON.toString(),
		StakeholderRole.INVOICE_RECIPENT.toString(),
		StakeholderRole.INVOICE_RECIPIENT.toString(),
		StakeholderRole.OPERATOR.toString(),
		StakeholderRole.APPLICANT.toString(),
		StakeholderRole.INSTALLER.toString());

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
