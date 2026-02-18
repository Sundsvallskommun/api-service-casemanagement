package se.sundsvall.casemanagement.api.model;

import java.util.List;
import java.util.Map;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import se.sundsvall.casemanagement.api.model.enums.AddressCategory;
import se.sundsvall.casemanagement.util.Constants;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class AddressDTOTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(AddressDTO.class, allOf(
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
		final var city = "someCity";
		final var country = Constants.SWEDEN;
		final var propertyDesignation = "SUNDSVALL FILLA 8:185";
		final var street = "someStreet";
		final var houseNumber = "someHouseNumber";
		final var careOf = "someCareOf";
		final var postalCode = "somePostalCode";
		final var apartmentNumber = "someApartmentNumber";
		final var attention = "someAttention";
		final var invoiceMarking = "someInvoiceMarking";
		final var isZoningPlanArea = false;
		final var location = CoordinatesDTO.builder().build();
		final var extraParameters = Map.of("Key", "Value");

		// Act
		final var addressDTO = new AddressDTO();
		addressDTO.setAddressCategories(addressCategories);
		addressDTO.setCity(city);
		addressDTO.setCountry(country);
		addressDTO.setPropertyDesignation(propertyDesignation);
		addressDTO.setStreet(street);
		addressDTO.setHouseNumber(houseNumber);
		addressDTO.setCareOf(careOf);
		addressDTO.setPostalCode(postalCode);
		addressDTO.setAppartmentNumber(apartmentNumber);
		addressDTO.setAttention(attention);
		addressDTO.setInvoiceMarking(invoiceMarking);
		addressDTO.setIsZoningPlanArea(isZoningPlanArea);
		addressDTO.setLocation(location);
		addressDTO.setExtraParameters(extraParameters);

		// Assert
		assertThat(addressDTO).isNotNull();
		assertThat(addressDTO.getAddressCategories()).isEqualTo(addressCategories);
		assertThat(addressDTO.getCity()).isEqualTo(city);
		assertThat(addressDTO.getCountry()).isEqualTo(country);
		assertThat(addressDTO.getPropertyDesignation()).isEqualTo(propertyDesignation);
		assertThat(addressDTO.getStreet()).isEqualTo(street);
		assertThat(addressDTO.getHouseNumber()).isEqualTo(houseNumber);
		assertThat(addressDTO.getCareOf()).isEqualTo(careOf);
		assertThat(addressDTO.getPostalCode()).isEqualTo(postalCode);
		assertThat(addressDTO.getAppartmentNumber()).isEqualTo(apartmentNumber);
		assertThat(addressDTO.getAttention()).isEqualTo(attention);
		assertThat(addressDTO.getInvoiceMarking()).isEqualTo(invoiceMarking);
		assertThat(addressDTO.getIsZoningPlanArea()).isEqualTo(isZoningPlanArea);
		assertThat(addressDTO.getLocation()).isEqualTo(location);
		assertThat(addressDTO.getExtraParameters()).isEqualTo(extraParameters);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new AddressDTO()).hasAllNullFieldsOrProperties();
	}

}
