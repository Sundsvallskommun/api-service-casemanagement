package se.sundsvall.casemanagement.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.util.Map;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;

class AttachmentDTOTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(AttachmentDTO.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testFields() {
		// Arrange
		final var category = AttachmentCategory.ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW.toString();
		final var name = "Some attachment name.pdf";
		final var note = "Some attachment note";
		final var extension = ".pdf";
		final var mimeType = "application/pdf";
		final var file = "dGVzdA==";
		final var extraParameters = Map.of("Key", "Value");

		// Act
		final var attachmentDTO = new AttachmentDTO();
		attachmentDTO.setCategory(category);
		attachmentDTO.setName(name);
		attachmentDTO.setNote(note);
		attachmentDTO.setExtension(extension);
		attachmentDTO.setMimeType(mimeType);
		attachmentDTO.setFile(file);
		attachmentDTO.setExtraParameters(extraParameters);

		// Assert
		assertThat(attachmentDTO).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(attachmentDTO.getCategory()).isEqualTo(category);
		assertThat(attachmentDTO.getName()).isEqualTo(name);
		assertThat(attachmentDTO.getNote()).isEqualTo(note);
		assertThat(attachmentDTO.getExtension()).isEqualTo(extension);
		assertThat(attachmentDTO.getMimeType()).isEqualTo(mimeType);
		assertThat(attachmentDTO.getFile()).isEqualTo(file);
		assertThat(attachmentDTO.getExtraParameters()).isEqualTo(extraParameters);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new AttachmentDTO()).hasAllNullFieldsOrProperties();
	}

}
