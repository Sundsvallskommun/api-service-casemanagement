package se.sundsvall.casemanagement.integration.fb.model;

import java.util.List;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class ResponseDtoTest {

	private static final Random RANDOM = new Random();

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
		object.setStatusKod(RANDOM.nextInt());
		object.setStatusMeddelande("StatusMeddelande");

		assertThat(object).isNotNull().hasNoNullFieldsOrProperties();
	}
}
