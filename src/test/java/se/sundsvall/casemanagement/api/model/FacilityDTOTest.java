package se.sundsvall.casemanagement.api.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class FacilityDTOTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(FacilityDTO.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters()));
	}

	@Test
	void testFields() {
		final var facilityType = "someFacilityType";
		final var mainFacility = true;
		final var facilityCollectionName = "someFacilityCollectionName";
		final var address = new AddressDTO();
		final var description = "someDescription";

		final var facilityDTO = new FacilityDTO();
		facilityDTO.setFacilityType(facilityType);
		facilityDTO.setMainFacility(mainFacility);
		facilityDTO.setFacilityCollectionName(facilityCollectionName);
		facilityDTO.setAddress(address);
		facilityDTO.setDescription(description);

		assertThat(facilityDTO).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(facilityDTO.getFacilityType()).isEqualTo(facilityType);
		assertThat(facilityDTO.isMainFacility()).isEqualTo(mainFacility);
		assertThat(facilityDTO.getFacilityCollectionName()).isEqualTo(facilityCollectionName);
		assertThat(facilityDTO.getAddress()).isEqualTo(address);
		assertThat(facilityDTO.getDescription()).isEqualTo(description);
	}

	@Test
	void testBuilder() {
		final var facilityType = "someFacilityType";
		final var mainFacility = true;
		final var facilityCollectionName = "someFacilityCollectionName";
		final var address = new AddressDTO();
		final var description = "someDescription";

		final var facilityDTO = FacilityDTO.builder()
			.withAddress(address)
			.withDescription(description)
			.withFacilityCollectionName(facilityCollectionName)
			.withFacilityType(facilityType)
			.withMainFacility(mainFacility)
			.build();

		assertThat(facilityDTO).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(facilityDTO.getFacilityType()).isEqualTo(facilityType);
		assertThat(facilityDTO.isMainFacility()).isEqualTo(mainFacility);
		assertThat(facilityDTO.getFacilityCollectionName()).isEqualTo(facilityCollectionName);
		assertThat(facilityDTO.getAddress()).isEqualTo(address);
		assertThat(facilityDTO.getDescription()).isEqualTo(description);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new FacilityDTO())
			.hasAllNullFieldsOrPropertiesExcept("mainFacility", "extraParameters");

		assertThat(FacilityDTO.builder().build())
			.hasAllNullFieldsOrPropertiesExcept("mainFacility", "extraParameters");
	}

}
