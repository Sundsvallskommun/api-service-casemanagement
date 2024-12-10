package se.sundsvall.casemanagement.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import se.sundsvall.casemanagement.api.validation.impl.EcosCaseDateOrderValidator;
import se.sundsvall.casemanagement.util.Constants;

@Target({
	ElementType.TYPE
})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EcosCaseDateOrderValidator.class)
public @interface EcosCaseDateOrder {

	String message() default Constants.ERR_START_MUST_BE_BEFORE_END;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
