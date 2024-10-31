package se.sundsvall.casemanagement.integration.byggr;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casemanagement.api.model.enums.StakeholderRole.CONTROL_OFFICIAL;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_STATUS_AVSLUTAT;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.FacilityDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.StakeholderDTO;

import arendeexport.Arende;
import arendeexport.ArendeIntressent;
import arendeexport.ArrayOfHandelse;
import arendeexport.ArrayOfString2;
import arendeexport.Handelse;

class ByggrUtilTest {

	@Test
	void containsControlOfficial() {

		// Arrange
		final List<StakeholderDTO> stakholderList = List.of(PersonDTO.builder().withRoles(List.of(CONTROL_OFFICIAL.toString())).build());

		// Act
		final var result = ByggrUtil.containsControlOfficial(stakholderList);
		// Assert
		assertThat(result).isTrue();
	}

	@Test
	void containsControlOfficial_NotContainingRole() {

		// Arrange
		final List<StakeholderDTO> stakholderList = List.of(PersonDTO.builder().withRoles(List.of("OtherRole")).build());

		// Act
		final var result = ByggrUtil.containsControlOfficial(stakholderList);
		// Assert
		assertThat(result).isFalse();
	}

	@Test
	void containsPersonDuplicates() {
		// Arrange
		final List<StakeholderDTO> stakholderList = List.of(
			PersonDTO.builder().withPersonId("somePersonId").build(),
			PersonDTO.builder().withPersonId("somePersonId").build());

		// Act
		final var result = ByggrUtil.containsPersonDuplicates(stakholderList);
		// Assert
		assertThat(result).isTrue();
	}

	@Test
	void containsPersonDuplicates_NoDuplicate() {
		// Arrange
		final List<StakeholderDTO> stakholderList = List.of(
			PersonDTO.builder().withPersonId("somePersonId").build(),
			PersonDTO.builder().withPersonId("somePersonId2").build());

		// Act
		final var result = ByggrUtil.containsPersonDuplicates(stakholderList);
		// Assert
		assertThat(result).isFalse();
	}

	@Test
	void containsPropertyOwner() {

		// Arrange
		final List<ArendeIntressent> stakholderList = List.of(new ArendeIntressent()
			.withRollLista(new ArrayOfString2()
				.withRoll("FAG"))); // Really sokigo?
		// Act
		final var result = ByggrUtil.containsPropertyOwner(stakholderList);
		// Assert
		assertThat(result).isTrue();
	}

	@Test
	void containsPropertyOwner_NotContainingOffensiveRoleName() {

		// Arrange
		final List<ArendeIntressent> stakholderList = List.of(new ArendeIntressent().withRollLista(new ArrayOfString2().withRoll("NonOffensiveRoleName")));
		// Act
		final var result = ByggrUtil.containsPropertyOwner(stakholderList);
		// Assert
		assertThat(result).isFalse();
	}

	@Test
	void getPropertyDesignation() {
		// Arrange
		final var facility = new FacilityDTO();
		facility.setAddress(AddressDTO.builder().withPropertyDesignation("Some-Property-Designation").build());
		final var facilityList = List.of(facility);
		// Act
		final var result = ByggrUtil.getPropertyDesignation(facilityList);
		// Assert
		assertThat(result).isEqualTo("SOME-PROPERTY-DESIGNATION");
	}

	@Test
	void getPropertyDesignation_ExpectNull() {
		// Act
		final var result = ByggrUtil.getPropertyDesignation(List.of());
		// Assert
		assertThat(result).isNull();
	}

	@Test
	void isWithinPlan() {
		// Arrange
		final var facility = new FacilityDTO();
		facility.setAddress(AddressDTO.builder().withIsZoningPlanArea(true).build());
		final var facilityList = List.of(facility);
		// Act
		final var result = ByggrUtil.isWithinPlan(facilityList);
		// Assert
		assertThat(result).isTrue();
	}

	@Test
	void isWithinPlan_IsNotTrue() {
		// Arrange
		final var facility = new FacilityDTO();
		facility.setAddress(AddressDTO.builder().withIsZoningPlanArea(false).build());
		final var facilityList = List.of(facility);
		// Act
		final var result = ByggrUtil.isWithinPlan(facilityList);
		// Assert
		assertThat(result).isFalse();
	}

	@Test
	void parsePropertyDesignation() {
		// Arrange
		final var facility = new FacilityDTO();
		facility.setAddress(AddressDTO.builder().withPropertyDesignation("Sundsvall Some-Property-Designation").build());
		final var facilityList = List.of(facility);
		// Act
		final var result = ByggrUtil.parsePropertyDesignation(facilityList);
		// Assert
		assertThat(result).isEqualTo("SOME-PROPERTY-DESIGNATION");
	}

	@Test
	void parsePropertyDesignation_NotStartingWithSundsvall() {
		// Arrange
		final var facility = new FacilityDTO();
		facility.setAddress(AddressDTO.builder().withPropertyDesignation("Some-Property-Designation").build());
		final var facilityList = List.of(facility);
		// Act
		final var result = ByggrUtil.parsePropertyDesignation(facilityList);
		// Assert
		assertThat(result).isEqualTo("SOME-PROPERTY-DESIGNATION");
	}

	@Test
	void parsePropertyDesignation_ExpectNull() {
		// Act
		final var result = ByggrUtil.parsePropertyDesignation(List.of());
		// Assert
		assertThat(result).isNull();
	}

	@Test
	void getMainOrTheOnlyFacility() {

		// Arrange
		final var facility = new FacilityDTO();
		facility.setMainFacility(true);
		final var facilityList = List.of(facility);
		// Act
		final var result = ByggrUtil.getMainOrTheOnlyFacility(facilityList);
		// Assert
		assertThat(result).isEqualTo(facility);
	}

	@Test
	void getMainOrTheOnlyFacility_ExpectNull() {
		// Act
		final var result = ByggrUtil.getMainOrTheOnlyFacility(List.of());
		// Assert
		assertThat(result).isNull();
	}

	@Test
	void getMainOrTheOnlyFacility_TwoFacilities() {
		// Arrange
		final var facility = new FacilityDTO();
		final var facility2 = new FacilityDTO();
		facility2.setMainFacility(true);
		final var facilityList = List.of(facility, facility2);
		// Act
		final var result = ByggrUtil.getMainOrTheOnlyFacility(facilityList);
		// Assert
		assertThat(result).isEqualTo(facility2);
	}

	@Test
	void writeEventNote() {
		// Arrange
		final var sb = new StringBuilder();
		// Act
		ByggrUtil.writeEventNote("SomeNote", sb);
		// Assert
		assertThat(sb.toString()).hasToString("SomeNote");
	}

	@Test
	void writeEventNote_ExpectNoChange() {
		// Arrange
		final var sb = new StringBuilder("SomeNote");
		// Act
		ByggrUtil.writeEventNote("SomeNote", sb);
		// Assert
		assertThat(sb.toString()).hasToString("SomeNote");
	}

	@Test
	void isCaseClosed() {
		// Arrange
		final var arende = new Arende();
		arende.setStatus("SomeStatus");
		// Act
		final var result = ByggrUtil.isCaseClosed(arende);
		// Assert
		assertThat(result).isFalse();
	}

	@Test
	void isCaseClosed_ExpectTrue() {
		// Arrange
		final var arende = new Arende();
		arende.setStatus(BYGGR_STATUS_AVSLUTAT);
		// Act
		final var result = ByggrUtil.isCaseClosed(arende);
		// Assert
		assertThat(result).isTrue();
	}

	@Test
	void hasHandelseList() {
		// Arrange
		final var arende = new Arende();
		arende.setHandelseLista(new ArrayOfHandelse().withHandelse(new Handelse()));
		// Act
		final var result = ByggrUtil.hasHandelseList(arende);
		// Assert
		assertThat(result).isTrue();
	}

	@Test
	void hasHandelseList_withHandeList() {
		// Arrange
		final var arende = new Arende();
		// Act
		final var result = ByggrUtil.hasHandelseList(arende);
		// Assert
		assertThat(result).isFalse();
	}

}
