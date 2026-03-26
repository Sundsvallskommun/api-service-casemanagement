package se.sundsvall.casemanagement.integration.db.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import se.sundsvall.casemanagement.api.model.enums.SystemType;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class CaseTypeEntityTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(CaseTypeEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testFields() {
		final var name = "NYBYGGNAD_ANSOKAN_OM_BYGGLOV";
		final var systemType = SystemType.BYGGR;
		final var facilityTypeRule = "ATTEFALL_REJECTED";

		final var entity = CaseTypeEntity.builder()
			.withName(name)
			.withSystemType(systemType)
			.withNullableFacilityType(true)
			.withNullableFacility(true)
			.withFacilityTypeRule(facilityTypeRule)
			.build();

		assertThat(entity).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(entity.getName()).isEqualTo(name);
		assertThat(entity.getSystemType()).isEqualTo(systemType);
		assertThat(entity.isNullableFacilityType()).isTrue();
		assertThat(entity.isNullableFacility()).isTrue();
		assertThat(entity.getFacilityTypeRule()).isEqualTo(facilityTypeRule);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(CaseTypeEntity.builder().build())
			.hasAllNullFieldsOrPropertiesExcept("nullableFacilityType", "nullableFacility");
		assertThat(new CaseTypeEntity())
			.hasAllNullFieldsOrPropertiesExcept("nullableFacilityType", "nullableFacility");
	}

}
