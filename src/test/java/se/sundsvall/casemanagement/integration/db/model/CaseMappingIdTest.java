package se.sundsvall.casemanagement.integration.db.model;

import java.time.LocalDateTime;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class CaseMappingIdTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDateTime.now().plusDays(new Random().nextInt()), LocalDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(CaseMappingId.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testFields() {
		// Arrange
		final var caseId = "123";
		final var externalCaseId = "456";

		// Act
		final var caseMappingId = new CaseMappingId();
		caseMappingId.setCaseId(caseId);
		caseMappingId.setExternalCaseId(externalCaseId);

		// Assert
		assertThat(caseMappingId).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(caseMappingId.getCaseId()).isEqualTo(caseId);
		assertThat(caseMappingId.getExternalCaseId()).isEqualTo(externalCaseId);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new CaseMappingId()).hasAllNullFieldsOrProperties();
	}

}
