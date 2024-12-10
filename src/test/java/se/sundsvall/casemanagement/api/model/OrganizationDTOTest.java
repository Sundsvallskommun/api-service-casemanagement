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
import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.api.model.enums.StakeholderType;

class OrganizationDTOTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDateTime.now().plusDays(new Random().nextInt()), LocalDateTime.class);
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt()), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(OrganizationDTO.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testFields() {
		// Arrange
		final var type = StakeholderType.ORGANIZATION;
		final var organizationNumber = "SomeOrganizationNumber";
		final var organizationName = "SomeOrganizationName";
		final var roles = List.of(StakeholderRole.CHAIRMAN.toString(), StakeholderRole.APPLICANT.toString(), StakeholderRole.LEASEHOLDER.toString());
		final var emailAddress = "someEmailAddress";
		final var cellphoneNumber = "someCellphoneNumber";
		final var phoneNumber = "somePhoneNumber";
		final var addresses = List.of(new AddressDTO());
		final var authorizedSignatory = "Some authorized signatory";
		final var extraParameters = Map.of("Key", "Value");

		// Act
		final OrganizationDTO organizationDTO = new OrganizationDTO();
		organizationDTO.setType(type);
		organizationDTO.setOrganizationNumber(organizationNumber);
		organizationDTO.setOrganizationName(organizationName);
		organizationDTO.setRoles(roles);
		organizationDTO.setEmailAddress(emailAddress);
		organizationDTO.setCellphoneNumber(cellphoneNumber);
		organizationDTO.setPhoneNumber(phoneNumber);
		organizationDTO.setAddresses(addresses);
		organizationDTO.setAuthorizedSignatory(authorizedSignatory);
		organizationDTO.setExtraParameters(extraParameters);

		// Assert
		assertThat(organizationDTO).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(organizationDTO.getType()).isEqualTo(type);
		assertThat(organizationDTO.getOrganizationNumber()).isEqualTo(organizationNumber);
		assertThat(organizationDTO.getOrganizationName()).isEqualTo(organizationName);
		assertThat(organizationDTO.getRoles()).isEqualTo(roles);
		assertThat(organizationDTO.getEmailAddress()).isEqualTo(emailAddress);
		assertThat(organizationDTO.getCellphoneNumber()).isEqualTo(cellphoneNumber);
		assertThat(organizationDTO.getPhoneNumber()).isEqualTo(phoneNumber);
		assertThat(organizationDTO.getAddresses()).isEqualTo(addresses);
		assertThat(organizationDTO.getAuthorizedSignatory()).isEqualTo(authorizedSignatory);
		assertThat(organizationDTO.getExtraParameters()).isEqualTo(extraParameters);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new OrganizationDTO()).hasAllNullFieldsOrProperties();
	}

}
