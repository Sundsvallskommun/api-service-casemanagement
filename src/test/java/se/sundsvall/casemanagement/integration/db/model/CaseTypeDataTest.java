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

class CaseTypeDataTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDateTime.now().plusDays(new Random().nextInt()), LocalDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(CaseTypeData.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testFields() {
		// Declare and initialize variables
		final String value = "someValue";
		final String arendeSlag = "someArendeSlag";
		final String arendeGrupp = "someArendeGrupp";
		final String arendeTyp = "someArendeTyp";
		final String handelseTyp = "someHandelseTyp";
		final String handelseRubrik = "someHandelseRubrik";
		final String handelseSlag = "someHandelseSlag";
		final String arendeMening = "someArendeMening";

		// Use variables to set properties
		final var caseTypeData = CaseTypeData.builder()
			.withValue(value)
			.withArendeSlag(arendeSlag)
			.withArendeGrupp(arendeGrupp)
			.withArendeTyp(arendeTyp)
			.withHandelseTyp(handelseTyp)
			.withHandelseRubrik(handelseRubrik)
			.withHandelseSlag(handelseSlag)
			.withArendeMening(arendeMening)
			.build();

		// Assert that properties are equal to variables
		assertThat(caseTypeData).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(caseTypeData.getValue()).isEqualTo(value);
		assertThat(caseTypeData.getArendeSlag()).isEqualTo(arendeSlag);
		assertThat(caseTypeData.getArendeGrupp()).isEqualTo(arendeGrupp);
		assertThat(caseTypeData.getArendeTyp()).isEqualTo(arendeTyp);
		assertThat(caseTypeData.getHandelseTyp()).isEqualTo(handelseTyp);
		assertThat(caseTypeData.getHandelseRubrik()).isEqualTo(handelseRubrik);
		assertThat(caseTypeData.getHandelseSlag()).isEqualTo(handelseSlag);
		assertThat(caseTypeData.getArendeMening()).isEqualTo(arendeMening);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(CaseTypeData.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new CaseTypeData()).hasAllNullFieldsOrProperties();
	}

}
