package se.sundsvall.casemanagement.integration.ecos;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RiskClassMapperTest {

	@Test
	void mapActivities() {
		// Arrange
		final var activityString = ", , , , , , SLHA003, SLHA004, , , SLUA003, SLUA033, SLUA001, SLUA002, , , , , , , , , , , , , SLUA014, , , , , , , , , , , , , , ,";
		// Act
		final var result = RiskClassMapper.mapActivities(activityString);
		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getSaveRiskClass2024ActivityDto()).hasSize(7);
	}

	@Test
	void mapActivitiesNull() {
		// Act
		final var result = RiskClassMapper.mapActivities(null);
		// Assert
		assertThat(result).isNull();
	}

	@Test
	void mapProductGroups() {

		// Arrange
		final var productGroups = ", , , , , , PID1, PID2, , , PID3, PID033, PID001, PID002, , , , , , , , , , , , , PID014, , , , , , , , , , , , , , ,";
		// Act
		final var result = RiskClassMapper.mapProductGroups(productGroups);
		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getSaveRiskClass2024ProductGroupDto()).hasSize(7);
	}

	@Test
	void mapProductGroupsNull() {
		// Act
		final var result = RiskClassMapper.mapProductGroups(null);
		// Assert
		assertThat(result).isNull();
	}

	@Test
	void mapThirdPartyCertifications() {

		// Arrange
		final var thirdPartyCertifications = ", , , , , , TPC1,TPC2, , , TPC3, TPC033, TPC001, TPC002, , , , , , , , , , , , , TPC014, , , , , , , , , , , , , , ,";
		// Act
		final var result = RiskClassMapper.mapThirdPartyCertifications(thirdPartyCertifications);
		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getSaveRiskClass2024CertificationDto()).hasSize(7);
	}

	@Test
	void mapThirdPartyCertificationsNull() {
		// Act
		final var result = RiskClassMapper.mapThirdPartyCertifications(null);
		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getSaveRiskClass2024CertificationDto()).hasSize(1);
		assertThat(result.getSaveRiskClass2024CertificationDto().getFirst()).hasAllNullFieldsOrProperties();
	}

}
