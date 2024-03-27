package se.sundsvall.casemanagement.api.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import se.sundsvall.casemanagement.api.validation.impl.OnlyOneMainFacilityConstraintValidator;
import se.sundsvall.casemanagement.util.Constants;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OnlyOneMainFacilityConstraintValidator.class)
public @interface OnlyOneMainFacility {
	String message() default Constants.ERR_MSG_ONLY_ONE_MAIN_FACILITY;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
