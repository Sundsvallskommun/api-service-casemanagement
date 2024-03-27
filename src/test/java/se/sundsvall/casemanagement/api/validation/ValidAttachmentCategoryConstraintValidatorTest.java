package se.sundsvall.casemanagement.api.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.casemanagement.api.validation.impl.ValidAttachmentCategoryConstraintValidator;

@ExtendWith(MockitoExtension.class)
class ValidAttachmentCategoryConstraintValidatorTest {

	@InjectMocks
	private ValidAttachmentCategoryConstraintValidator validator;

	@Mock
	private ConstraintValidatorContext context;

	@ParameterizedTest
	@EnumSource(AttachmentCategory.class)
	void isValid_withValidCategory(final AttachmentCategory category) {
		final var validCategory = category.toString();
		assertThat(validator.isValid(validCategory, context)).isTrue();
	}

	@Test
	void isValid_withInvalidCategory() {
		final var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
		when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

		final var invalidCategory = "INVALID_CATEGORY";
		assertThat(validator.isValid(invalidCategory, context)).isFalse();
	}

	@Test
	void isValid_withNullCategory() {
		final var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
		when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

		assertThat(validator.isValid(null, context)).isFalse();
	}

	@Test
	void isValid_withEmptyCategory() {
		final var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
		when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

		final var emptyCategory = "";
		assertThat(validator.isValid(emptyCategory, context)).isFalse();
	}
}
