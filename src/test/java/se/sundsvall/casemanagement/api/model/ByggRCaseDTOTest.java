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

		// Act
		final var pCase = new ByggRCaseDTO();
		pCase.setAttachments(attachments);
		pCase.setStakeholders(stakeholders);
		pCase.setFacilities(facilities);
		pCase.setCaseType(caseType);
		pCase.setCaseTitleAddition(caseTitleAddition);
		pCase.setDescription(description);
		pCase.setExternalCaseId(externalCaseId);
		pCase.setDiaryNumber(diaryNumber);
		pCase.setExtraParameters(extraParameters);

		// Assert
		assertThat(pCase).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(pCase.getAttachments()).isEqualTo(attachments);
		assertThat(pCase.getStakeholders()).isEqualTo(stakeholders);
		assertThat(pCase.getFacilities()).isEqualTo(facilities);
		assertThat(pCase.getCaseType()).isEqualTo(caseType);
		assertThat(pCase.getCaseTitleAddition()).isEqualTo(caseTitleAddition);
		assertThat(pCase.getDescription()).isEqualTo(description);
		assertThat(pCase.getExternalCaseId()).isEqualTo(externalCaseId);
		assertThat(pCase.getDiaryNumber()).isEqualTo(diaryNumber);
		assertThat(pCase.getExtraParameters()).isEqualTo(extraParameters);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new ByggRCaseDTO()).hasAllNullFieldsOrProperties();
	}

}
