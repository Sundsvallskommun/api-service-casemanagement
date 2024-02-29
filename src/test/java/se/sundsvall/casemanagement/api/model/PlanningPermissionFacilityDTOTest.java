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
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import se.sundsvall.casemanagement.api.model.enums.AddressCategory;
import se.sundsvall.casemanagement.api.model.enums.FacilityType;

class PlanningPermissionFacilityDTOTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDateTime.now().plusDays(new Random().nextInt()), LocalDateTime.class);
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt()), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(PlanningPermissionFacilityDTO.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testFields() {
		// Arrange
		final var addressCategories = List.of(AddressCategory.VISITING_ADDRESS);
		final var propertyDesignation = "SomePropertyDesignation";
		final var facilityType = FacilityType.ONE_FAMILY_HOUSE.toString();
		final var description = "SomeDescription";
		final var extraParameters = Map.of("Key", "Value");
		final var mainFacility = true;

		// Act
		final PlanningPermissionFacilityDTO facility = new PlanningPermissionFacilityDTO();
		final AddressDTO addressDTO = new AddressDTO();
		addressDTO.setAddressCategories(addressCategories);
		addressDTO.setPropertyDesignation(propertyDesignation);
		facility.setAddress(addressDTO);
		facility.setFacilityType(facilityType);
		facility.setDescription(description);
		facility.setExtraParameters(extraParameters);
		facility.setMainFacility(mainFacility);

		// Assert
		assertThat(facility).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(facility.getAddress().getAddressCategories()).isEqualTo(addressCategories);
		assertThat(facility.getAddress().getPropertyDesignation()).isEqualTo(propertyDesignation);
		assertThat(facility.getFacilityType()).isEqualTo(facilityType);
		assertThat(facility.getDescription()).isEqualTo(description);
		assertThat(facility.getExtraParameters()).isEqualTo(extraParameters);
		assertThat(facility.isMainFacility()).isEqualTo(mainFacility);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new PlanningPermissionFacilityDTO()).hasAllNullFieldsOrPropertiesExcept("mainFacility")
			.satisfies(facility -> assertThat(facility.isMainFacility()).isFalse());
	}

}
