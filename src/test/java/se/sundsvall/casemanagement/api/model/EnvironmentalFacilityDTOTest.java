package se.sundsvall.casemanagement.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EnvironmentalFacilityDTOTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDateTime.now().plusDays(new Random().nextInt()), LocalDateTime.class);
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt()), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(EnvironmentalFacilityDTO.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testFields() {
		// Arrange
		final var facilityCollectionName = "Some facility collection name";
		final var description = "Some description";
		final var extraParameters = Map.of("Key", "Value");
		final var adressDto = new AddressDTO();

		// Act
		final var facility = new EnvironmentalFacilityDTO();
		facility.setFacilityCollectionName(facilityCollectionName);
		facility.setAddress(adressDto);
		facility.setDescription(description);
		facility.setExtraParameters(extraParameters);

		// Assert
		assertThat(facility).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(facility.getFacilityCollectionName()).isEqualTo(facilityCollectionName);
		assertThat(facility.getAddress()).isEqualTo(adressDto);
		assertThat(facility.getDescription()).isEqualTo(description);
		assertThat(facility.getExtraParameters()).isEqualTo(extraParameters);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new EnvironmentalFacilityDTO()).hasAllNullFieldsOrProperties();
	}

}
