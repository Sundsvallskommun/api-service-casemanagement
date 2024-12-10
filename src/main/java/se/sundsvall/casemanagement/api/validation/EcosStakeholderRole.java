package se.sundsvall.casemanagement.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import se.sundsvall.casemanagement.api.validation.impl.EcosStakeholderRoleConstraintValidator;
import se.sundsvall.casemanagement.util.Constants;

@Target({
	ElementType.FIELD
})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EcosStakeholderRoleConstraintValidator.class)
public @interface EcosStakeholderRole {

	String message() default Constants.ERR_MSG_WRONG_ROLE_ENV_CASE;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
