package se.sundsvall.casemanagement.api.validators;

import se.sundsvall.casemanagement.util.Constants;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnvironmentalCaseDateOrderValidator.class)
public @interface EnvironmentalCaseDateOrder {

    String message() default Constants.ERR_START_MUST_BE_BEFORE_END;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}