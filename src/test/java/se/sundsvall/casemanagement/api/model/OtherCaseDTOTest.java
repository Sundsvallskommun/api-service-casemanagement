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
import se.sundsvall.casemanagement.api.model.enums.CaseType;

class OtherCaseDTOTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDateTime.now().plusDays(new Random().nextInt()), LocalDateTime.class);
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt()), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(OtherCaseDTO.class, allOf(
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
		final var caseType = CaseType.STRANDSKYDD_ANORDNANDE.toString();
		final var caseTitleAddition = "Some case title addition";
		final var description = "Some description";
		final var externalCaseId = "Some external case id";
		final var extraParameters = Map.of("Key", "Value");
		final var facilities = List.of(new FacilityDTO());

		// Act
		final var oCase = new OtherCaseDTO();
		oCase.setAttachments(attachments);
		oCase.setStakeholders(stakeholders);
		oCase.setCaseType(caseType);
		oCase.setCaseTitleAddition(caseTitleAddition);
		oCase.setDescription(description);
		oCase.setExternalCaseId(externalCaseId);
		oCase.setExtraParameters(extraParameters);
		oCase.setFacilities(facilities);

		// Assert
		assertThat(oCase).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(oCase.getAttachments()).isEqualTo(attachments);
		assertThat(oCase.getStakeholders()).isEqualTo(stakeholders);
		assertThat(oCase.getCaseType()).isEqualTo(caseType);
		assertThat(oCase.getCaseTitleAddition()).isEqualTo(caseTitleAddition);
		assertThat(oCase.getDescription()).isEqualTo(description);
		assertThat(oCase.getExternalCaseId()).isEqualTo(externalCaseId);
		assertThat(oCase.getExtraParameters()).isEqualTo(extraParameters);
		assertThat(oCase.getFacilities()).isEqualTo(facilities);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new OtherCaseDTO()).hasAllNullFieldsOrPropertiesExcept("facilities");
	}

}
