package se.sundsvall.casemanagement.integration.db.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class EcosCaseTypeConfigEntityTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(EcosCaseTypeConfigEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testFields() {
		final var caseTypeName = "REGISTRERING_AV_LIVSMEDEL";
		final var diaryPlanId = "DIARY_PLAN_123";
		final var processTypeId = "PROCESS_TYPE_456";
		final var facilityHandler = EcosFacilityHandler.FOOD;

		final var entity = EcosCaseTypeConfigEntity.builder()
			.withCaseTypeName(caseTypeName)
			.withDiaryPlanId(diaryPlanId)
			.withProcessTypeId(processTypeId)
			.withFacilityHandler(facilityHandler)
			.build();

		assertThat(entity).isNotNull();
		assertThat(entity.getCaseTypeName()).isEqualTo(caseTypeName);
		assertThat(entity.getDiaryPlanId()).isEqualTo(diaryPlanId);
		assertThat(entity.getProcessTypeId()).isEqualTo(processTypeId);
		assertThat(entity.getFacilityHandler()).isEqualTo(facilityHandler);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(EcosCaseTypeConfigEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new EcosCaseTypeConfigEntity()).hasAllNullFieldsOrProperties();
	}

}
