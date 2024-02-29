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
		final CaseTypeData object = CaseTypeData.builder()
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
		assertThat(object).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(object.getValue()).isEqualTo(value);
		assertThat(object.getArendeSlag()).isEqualTo(arendeSlag);
		assertThat(object.getArendeGrupp()).isEqualTo(arendeGrupp);
		assertThat(object.getArendeTyp()).isEqualTo(arendeTyp);
		assertThat(object.getHandelseTyp()).isEqualTo(handelseTyp);
		assertThat(object.getHandelseRubrik()).isEqualTo(handelseRubrik);
		assertThat(object.getHandelseSlag()).isEqualTo(handelseSlag);
		assertThat(object.getArendeMening()).isEqualTo(arendeMening);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(CaseTypeData.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new CaseTypeData()).hasAllNullFieldsOrProperties();
	}

}
