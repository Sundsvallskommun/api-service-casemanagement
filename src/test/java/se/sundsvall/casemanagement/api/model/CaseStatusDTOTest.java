package se.sundsvall.casemanagement.api.model;

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

class CaseStatusDTOTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDateTime.now().plusDays(new Random().nextInt()), LocalDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(CaseStatusDTO.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testFields() {
		// Arrange
		final String status = "SomeStatus";
		final String caseId = "SomeCaseId";
		final String externalCaseId = "SomeExternalCaseId";
		final String serviceName = "SomeServiceName";
		final String caseType = CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV.toString();
		final SystemType system = SystemType.BYGGR;
		final LocalDateTime timestamp = LocalDateTime.now();

		// Act
		final CaseStatusDTO dto = CaseStatusDTO.builder()
			.withStatus(status)
			.withCaseId(caseId)
			.withExternalCaseId(externalCaseId)
			.withServiceName(serviceName)
			.withCaseType(caseType)
			.withSystem(system)
			.withTimestamp(timestamp)
			.build();

		// Assert
		assertThat(dto).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(dto.getStatus()).isEqualTo(status);
		assertThat(dto.getCaseId()).isEqualTo(caseId);
		assertThat(dto.getExternalCaseId()).isEqualTo(externalCaseId);
		assertThat(dto.getServiceName()).isEqualTo(serviceName);
		assertThat(dto.getCaseType()).isEqualTo(caseType);
		assertThat(dto.getSystem()).isEqualTo(system);
		assertThat(dto.getTimestamp()).isEqualTo(timestamp);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(CaseStatusDTO.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new CaseStatusDTO()).hasAllNullFieldsOrProperties();
	}

}
