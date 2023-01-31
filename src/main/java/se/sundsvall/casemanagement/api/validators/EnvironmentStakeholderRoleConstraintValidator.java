package se.sundsvall.casemanagement.api.validators;

import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class EnvironmentStakeholderRoleConstraintValidator
        implements ConstraintValidator<EnvironmentStakeholderRole, List<StakeholderRole>> {

    @Override
    public boolean isValid(List<StakeholderRole> value, ConstraintValidatorContext context) {
        for (StakeholderRole role : value) {
            switch (role) {
                case CONTACT_PERSON, INVOICE_RECIPENT, OPERATOR, APPLICANT, INSTALLER:
                    break;
                default:
                    return false;
            }
        }

        return true;

    }

}
