package se.sundsvall.casemanagement.api.validators;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;

public class EnvironmentStakeholderRoleConstraintValidator implements ConstraintValidator<EnvironmentStakeholderRole, List<StakeholderRole>> {

    @Override
    public boolean isValid(List<StakeholderRole> roles, ConstraintValidatorContext context) {
        return roles.stream().allMatch(this::isValidRole);
    }

    private boolean isValidRole(StakeholderRole role) {
        // Valid roles
        final Set<StakeholderRole> validRoles = EnumSet.of(
            StakeholderRole.CONTACT_PERSON,
            StakeholderRole.INVOICE_RECIPENT,
            StakeholderRole.INVOICE_RECIPIENT,
            StakeholderRole.OPERATOR,
            StakeholderRole.APPLICANT,
            StakeholderRole.INSTALLER);

        // Check if provided role is one of the valid roles.
        return validRoles.contains(role);
    }
}
