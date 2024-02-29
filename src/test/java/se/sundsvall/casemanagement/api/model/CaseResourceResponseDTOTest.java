package se.sundsvall.casemanagement.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

class CaseResourceResponseDTOTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(CaseResourceResponseDTO.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testFields() {
		// Arrange
		final var caseId = RandomStringUtils.random(10);
		// Act
		final var dto = new CaseResourceResponseDTO();
		dto.setCaseId(caseId);
		// Assert
		assertThat(dto).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(dto.getCaseId()).isNotNull().isEqualTo(caseId);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new CaseResourceResponseDTO()).hasAllNullFieldsOrProperties();
	}

}
