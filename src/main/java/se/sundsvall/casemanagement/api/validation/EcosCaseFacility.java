package se.sundsvall.casemanagement.api.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import se.sundsvall.casemanagement.api.validation.impl.EcosCaseFacilityConstraintValidator;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EcosCaseFacilityConstraintValidator.class)
public @interface EcosCaseFacility {

	String message() default "must be exactly one facility";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
