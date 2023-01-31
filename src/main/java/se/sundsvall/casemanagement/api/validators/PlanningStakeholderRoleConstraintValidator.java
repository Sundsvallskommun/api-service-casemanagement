package se.sundsvall.casemanagement.api.validators;

import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class PlanningStakeholderRoleConstraintValidator
        implements ConstraintValidator<PlanningStakeholderRole, List<StakeholderRole>> {

    @Override
    public boolean isValid(List<StakeholderRole> value, ConstraintValidatorContext context) {
        for (StakeholderRole role : value) {
            switch (role) {
                case CONTACT_PERSON, PAYMENT_PERSON, PROPERTY_OWNER, APPLICANT, CONTROL_OFFICIAL:
                    break;
                default:
                    return false;
            }
        }

        return true;
    }

}
