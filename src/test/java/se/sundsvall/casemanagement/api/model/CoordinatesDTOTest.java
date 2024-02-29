package se.sundsvall.casemanagement.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class CoordinatesDTOTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(CoordinatesDTO.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testFields() {

		// Arrange
		final var latitude = Double.parseDouble("1.321");
		final var longitude = Double.parseDouble("1.12345");
		// Act
		final var dto = new CoordinatesDTO();
		dto.setLatitude(latitude);
		dto.setLongitude(longitude);
		// Assert
		assertThat(dto).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(dto.getLatitude()).isEqualTo(latitude);
		assertThat(dto.getLongitude()).isEqualTo(longitude);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new CoordinatesDTO()).hasNoNullFieldsOrPropertiesExcept("latitude", "longitude").satisfies(bean -> {
			assertThat(bean.getLatitude()).isZero();
			assertThat(bean.getLongitude()).isZero();
		});
	}

}
