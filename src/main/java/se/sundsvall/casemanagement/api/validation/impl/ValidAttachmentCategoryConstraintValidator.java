package se.sundsvall.casemanagement.api.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.casemanagement.api.validation.ValidAttachmentCategory;

public class ValidAttachmentCategoryConstraintValidator implements ConstraintValidator<ValidAttachmentCategory, String> {

	@Override
	public void initialize(final ValidAttachmentCategory constraintAnnotation) {
		ConstraintValidator.super.initialize(constraintAnnotation);
	}

	@Override
	public boolean isValid(final String value, final ConstraintValidatorContext context) {
		if (value == null || value.isEmpty()) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Attachment category. cannot be null or empty. Valid categories are: " + Arrays.toString(AttachmentCategory.values()))
				.addConstraintViolation();
			return false;
		}

		final var isValid = Arrays.stream(AttachmentCategory.values())
			.anyMatch(attachmentCategory -> attachmentCategory.toString().equals(value));
		if (!isValid) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Invalid attachment category. Valid categories are: " + Arrays.toString(AttachmentCategory.values()))
				.addConstraintViolation();
		}
		return isValid;
	}

}
