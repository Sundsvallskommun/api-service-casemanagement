package se.sundsvall.casemanagement.api.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import se.sundsvall.casemanagement.api.validation.impl.OneMainFacilityOrNullConstraintValidator;
import se.sundsvall.casemanagement.util.Constants;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OneMainFacilityOrNullConstraintValidator.class)
public @interface OneMainFacilityOrNull {
	String message() default Constants.ERR_MSG_ONLY_ONE_MAIN_FACILITY;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
