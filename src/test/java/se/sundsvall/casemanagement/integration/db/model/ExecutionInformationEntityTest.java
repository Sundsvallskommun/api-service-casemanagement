package se.sundsvall.casemanagement.integration.db.model;

import java.time.OffsetDateTime;
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

class ExecutionInformationEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(ExecutionInformationEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var id = 1L;
		final var municipalityId = "2281";
		final var jobName = "BYGGR_STATUS";
		final var lastSuccessfulExecution = OffsetDateTime.now();

		final var entity = ExecutionInformationEntity.create()
			.withId(id)
			.withMunicipalityId(municipalityId)
			.withJobName(jobName)
			.withLastSuccessfulExecution(lastSuccessfulExecution);

		assertThat(entity).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(entity.getId()).isEqualTo(id);
		assertThat(entity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(entity.getJobName()).isEqualTo(jobName);
		assertThat(entity.getLastSuccessfulExecution()).isEqualTo(lastSuccessfulExecution);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(ExecutionInformationEntity.create()).hasAllNullFieldsOrProperties();
		assertThat(new ExecutionInformationEntity()).hasAllNullFieldsOrProperties();
	}

}
