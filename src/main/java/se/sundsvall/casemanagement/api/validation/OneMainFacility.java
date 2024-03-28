package se.sundsvall.casemanagement.api.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import se.sundsvall.casemanagement.api.validation.impl.OneMainFacilityConstraintValidator;

@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OneMainFacilityConstraintValidator.class)
@Documented
public @interface OneMainFacility {

	String message() default "must be exactly one main facility";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
