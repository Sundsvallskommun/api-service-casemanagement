package se.sundsvall.casemanagement.integration.fb.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.allOf;

import java.util.List;
import java.util.Random;
import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class ResponseDtoTest {
	@Test
	void testBean() {
		MatcherAssert.assertThat(ResponseDto.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testFields() {
		ResponseDto object = new ResponseDto();
		object.setData(List.of(new DataItem()));
		object.setFel(List.of("Fel"));
		object.setStatusKod(new Random().nextInt());
		object.setStatusMeddelande("StatusMeddelande");

		Assertions.assertThat(object).isNotNull().hasNoNullFieldsOrProperties();
	}

}
