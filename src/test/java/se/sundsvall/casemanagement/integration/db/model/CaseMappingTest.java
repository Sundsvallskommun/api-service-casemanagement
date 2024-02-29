package se.sundsvall.casemanagement.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.LocalDateTime;
import java.util.Random;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.SystemType;

class CaseMappingTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDateTime.now().plusDays(new Random().nextInt()), LocalDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(CaseMapping.class, allOf(
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
		final var systemType = SystemType.BYGGR;
		final var caseType = CaseType.MEX_LAND_RIGHT.toString();
		final var serviceName = "SomeServicename";
		final var timestamp = LocalDateTime.now();

		// Act
		final CaseMapping object = CaseMapping.builder()
			.withCaseId(caseId)
			.withExternalCaseId(externalCaseId)
			.withSystem(systemType)
			.withCaseType(caseType)
			.withServiceName(serviceName)
			.withTimestamp(timestamp)
			.build();

		// Assert
		assertThat(object).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(object.getCaseId()).isEqualTo(caseId);
		assertThat(object.getExternalCaseId()).isEqualTo(externalCaseId);
		assertThat(object.getSystem()).isEqualTo(systemType);
		assertThat(object.getCaseType()).isEqualTo(caseType);
		assertThat(object.getServiceName()).isEqualTo(serviceName);
		assertThat(object.getTimestamp()).isEqualTo(timestamp);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(CaseMapping.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new CaseMapping()).hasAllNullFieldsOrProperties();
	}

}
