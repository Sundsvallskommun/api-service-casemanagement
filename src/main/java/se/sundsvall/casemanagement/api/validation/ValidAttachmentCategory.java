package se.sundsvall.casemanagement.api.validation;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.casemanagement.api.validation.impl.ValidAttachmentCategoryConstraintValidator;

/**
 * The annotated element must be a valid {@link AttachmentCategory}.
 * Not allowed to be null or empty.
 */
@Documented
@Target({
	FIELD, CONSTRUCTOR, PARAMETER
})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidAttachmentCategoryConstraintValidator.class)
public @interface ValidAttachmentCategory {

	String message() default "Invalid attachment category";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
