package se.sundsvall.casemanagement.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import se.sundsvall.casemanagement.api.validation.impl.SingularFacilityConstraintValidator;

@Target({
	ElementType.TYPE, ElementType.FIELD
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = SingularFacilityConstraintValidator.class)
public @interface SingularFacility {

	String message() default "must be exactly one facility";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
