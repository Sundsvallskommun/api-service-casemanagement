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
		final var municipalityId = "2281";

		// Act
		final var caseMapping = CaseMapping.builder()
			.withCaseId(caseId)
			.withExternalCaseId(externalCaseId)
			.withSystem(systemType)
			.withCaseType(caseType)
			.withServiceName(serviceName)
			.withTimestamp(timestamp)
			.withMunicipalityId(municipalityId)
			.build();

		// Assert
		assertThat(caseMapping).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(caseMapping.getCaseId()).isEqualTo(caseId);
		assertThat(caseMapping.getExternalCaseId()).isEqualTo(externalCaseId);
		assertThat(caseMapping.getSystem()).isEqualTo(systemType);
		assertThat(caseMapping.getCaseType()).isEqualTo(caseType);
		assertThat(caseMapping.getServiceName()).isEqualTo(serviceName);
		assertThat(caseMapping.getTimestamp()).isEqualTo(timestamp);
		assertThat(caseMapping.getMunicipalityId()).isEqualTo(municipalityId);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(CaseMapping.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new CaseMapping()).hasAllNullFieldsOrProperties();
	}

}
