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

import se.sundsvall.casemanagement.api.model.enums.StakeholderType;

class PersonDTOTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDateTime.now().plusDays(new Random().nextInt()), LocalDateTime.class);
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt()), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(PersonDTO.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testFields() {
		// Arrange
		final var type = StakeholderType.PERSON;
		final var personId = "3ed5bc30-6308-4fd5-a5a7-78d7f96f4438";
		final var personalNumber = "SomePersonalNumber";
		final var firstName = "SomeFirstName";
		final var lastName = "SomeLastName";
		final var roles = List.of("SomeRole");
		final var emailAddress = "email@example.com";
		final var cellphoneNumber = "1234567890";
		final var phoneNumber = "0987654321";
		final var addresses = List.of(new AddressDTO());
		final var extraParameters = Map.of("Key", "Value");

		// Act
		final var personDTO = new PersonDTO();
		personDTO.setType(type);
		personDTO.setPersonId(personId);
		personDTO.setPersonalNumber(personalNumber);
		personDTO.setFirstName(firstName);
		personDTO.setLastName(lastName);
		personDTO.setRoles(roles);
		personDTO.setEmailAddress(emailAddress);
		personDTO.setCellphoneNumber(cellphoneNumber);
		personDTO.setPhoneNumber(phoneNumber);
		personDTO.setAddresses(addresses);
		personDTO.setExtraParameters(extraParameters);

		// Assert
		assertThat(personDTO).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(personDTO.getType()).isEqualTo(type);
		assertThat(personDTO.getPersonId()).isEqualTo(personId);
		assertThat(personDTO.getPersonalNumber()).isEqualTo(personalNumber);
		assertThat(personDTO.getFirstName()).isEqualTo(firstName);
		assertThat(personDTO.getLastName()).isEqualTo(lastName);
		assertThat(personDTO.getRoles()).isEqualTo(roles);
		assertThat(personDTO.getEmailAddress()).isEqualTo(emailAddress);
		assertThat(personDTO.getCellphoneNumber()).isEqualTo(cellphoneNumber);
		assertThat(personDTO.getPhoneNumber()).isEqualTo(phoneNumber);
		assertThat(personDTO.getAddresses()).isEqualTo(addresses);
		assertThat(personDTO.getExtraParameters()).isEqualTo(extraParameters);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new PersonDTO()).hasAllNullFieldsOrProperties();
	}

}
