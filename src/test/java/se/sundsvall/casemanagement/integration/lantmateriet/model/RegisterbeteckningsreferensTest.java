package se.sundsvall.casemanagement.integration.lantmateriet.model;

import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.allOf;

class RegisterbeteckningsreferensTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(Registerbeteckningsreferens.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testFields() {
		final Registerbeteckningsreferens object = new Registerbeteckningsreferens();
		object.setRegisterenhet(RandomStringUtils.secure().nextAlphabetic(10));
		object.setBeteckning(RandomStringUtils.secure().nextAlphabetic(10));
		object.setBeteckningsid(RandomStringUtils.secure().nextAlphabetic(10));

		Assertions.assertThat(object).isNotNull().hasNoNullFieldsOrProperties();
	}
}
