package se.sundsvall.casemanagement.api.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class EcosCaseDTOTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDateTime.now().plusDays(new Random().nextInt()), LocalDateTime.class);
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt()), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(EcosCaseDTO.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testFields() {
		// Arrange
		final var attachments = List.of(new AttachmentDTO());
		final var stakeholders = List.of(new PersonDTO(), new OrganizationDTO());
		final var facilities = List.of(new FacilityDTO());
		final var caseType = "SomeCaseType";
		final var caseTitleAddition = "SomeCaseTitleAddition";
		final var description = "Some description";
		final var startDate = LocalDate.now().plusDays(10);
		final var endDate = LocalDate.now().plusDays(365);
		final var externalCaseId = "SomeExternalCaseId";
		final var extraParameters = Map.of("Key", "Value");

		// Act
		final var eCase = new EcosCaseDTO();
		eCase.setAttachments(attachments);
		eCase.setStakeholders(stakeholders);
		eCase.setFacilities(facilities);
		eCase.setCaseType(caseType);
		eCase.setCaseTitleAddition(caseTitleAddition);
		eCase.setDescription(description);
		eCase.setStartDate(startDate);
		eCase.setEndDate(endDate);
		eCase.setExternalCaseId(externalCaseId);
		eCase.setExtraParameters(extraParameters);

		// Assert
		assertThat(eCase).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(eCase.getAttachments()).isEqualTo(attachments);
		assertThat(eCase.getStartDate()).isEqualTo(startDate);
		assertThat(eCase.getStakeholders()).isEqualTo(stakeholders);
		assertThat(eCase.getFacilities()).isEqualTo(facilities);
		assertThat(eCase.getCaseType()).isEqualTo(caseType);
		assertThat(eCase.getCaseTitleAddition()).isEqualTo(caseTitleAddition);
		assertThat(eCase.getDescription()).isEqualTo(description);
		assertThat(eCase.getEndDate()).isEqualTo(endDate);
		assertThat(eCase.getExternalCaseId()).isEqualTo(externalCaseId);
		assertThat(eCase.getExtraParameters()).isEqualTo(extraParameters);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new EcosCaseDTO()).hasAllNullFieldsOrProperties();
	}

}
