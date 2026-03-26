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

class ByggrCaseTypeConfigEntityTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(ByggrCaseTypeConfigEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testFields() {
		final var caseTypeName = "NYBYGGNAD_ANSOKAN_OM_BYGGLOV";
		final var arendeSlag = "A";
		final var arendeGrupp = "LOV";
		final var arendeTyp = "BL";
		final var handelseTyp = "ANSÖKAN";
		final var handelseRubrik = "Bygglov";
		final var handelseSlag = "Bygglov";
		final var arendeMening = "Bygglov för nybyggnad av";

		final var entity = ByggrCaseTypeConfigEntity.builder()
			.withCaseTypeName(caseTypeName)
			.withArendeSlag(arendeSlag)
			.withArendeGrupp(arendeGrupp)
			.withArendeTyp(arendeTyp)
			.withHandelseTyp(handelseTyp)
			.withHandelseRubrik(handelseRubrik)
			.withHandelseSlag(handelseSlag)
			.withArendeMening(arendeMening)
			.build();

		assertThat(entity).isNotNull();
		assertThat(entity.getCaseTypeName()).isEqualTo(caseTypeName);
		assertThat(entity.getArendeSlag()).isEqualTo(arendeSlag);
		assertThat(entity.getArendeGrupp()).isEqualTo(arendeGrupp);
		assertThat(entity.getArendeTyp()).isEqualTo(arendeTyp);
		assertThat(entity.getHandelseTyp()).isEqualTo(handelseTyp);
		assertThat(entity.getHandelseRubrik()).isEqualTo(handelseRubrik);
		assertThat(entity.getHandelseSlag()).isEqualTo(handelseSlag);
		assertThat(entity.getArendeMening()).isEqualTo(arendeMening);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(ByggrCaseTypeConfigEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new ByggrCaseTypeConfigEntity()).hasAllNullFieldsOrProperties();
	}

}
