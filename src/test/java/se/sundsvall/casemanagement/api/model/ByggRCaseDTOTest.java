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

class ByggRCaseDTOTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDateTime.now().plusDays(new Random().nextInt()), LocalDateTime.class);
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt()), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(ByggRCaseDTO.class, allOf(
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
		final var caseType = CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV.toString();
		final var caseTitleAddition = "SomeCaseTitleAddition";
		final var description = "SomeDescription";
		final var externalCaseId = "SomeExternalCaseId";
		final var diaryNumber = "SomeDiaryNumber";
		final var extraParameters = Map.of("Key", "Value");
		final var municipalityId = "SomeMunicipalityId";

		// Act
		final var byggRCase = new ByggRCaseDTO();
		byggRCase.setAttachments(attachments);
		byggRCase.setStakeholders(stakeholders);
		byggRCase.setFacilities(facilities);
		byggRCase.setCaseType(caseType);
		byggRCase.setCaseTitleAddition(caseTitleAddition);
		byggRCase.setDescription(description);
		byggRCase.setExternalCaseId(externalCaseId);
		byggRCase.setDiaryNumber(diaryNumber);
		byggRCase.setExtraParameters(extraParameters);
		byggRCase.setMunicipalityId(municipalityId);

		// Assert
		assertThat(byggRCase).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(byggRCase.getAttachments()).isEqualTo(attachments);
		assertThat(byggRCase.getStakeholders()).isEqualTo(stakeholders);
		assertThat(byggRCase.getFacilities()).isEqualTo(facilities);
		assertThat(byggRCase.getCaseType()).isEqualTo(caseType);
		assertThat(byggRCase.getCaseTitleAddition()).isEqualTo(caseTitleAddition);
		assertThat(byggRCase.getDescription()).isEqualTo(description);
		assertThat(byggRCase.getExternalCaseId()).isEqualTo(externalCaseId);
		assertThat(byggRCase.getDiaryNumber()).isEqualTo(diaryNumber);
		assertThat(byggRCase.getExtraParameters()).isEqualTo(extraParameters);
		assertThat(byggRCase.getMunicipalityId()).isEqualTo(municipalityId);

	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new ByggRCaseDTO()).hasAllNullFieldsOrProperties();
	}

}
