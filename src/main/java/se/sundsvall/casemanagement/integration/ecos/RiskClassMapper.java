package se.sundsvall.casemanagement.integration.ecos;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import minutmiljo.ArrayOfSaveRiskClass2024ActivityDto;
import minutmiljo.ArrayOfSaveRiskClass2024CertificationDto;
import minutmiljo.ArrayOfSaveRiskClass2024ProductGroupDto;
import minutmiljo.SaveRiskClass2024ActivityDto;
import minutmiljo.SaveRiskClass2024CertificationDto;
import minutmiljo.SaveRiskClass2024ProductGroupDto;

public final class RiskClassMapper {

	private RiskClassMapper() {
		// Intentionally left empty
	}

	static ArrayOfSaveRiskClass2024ActivityDto mapActivities(final String activityString) {

		final var activities = splitString(activityString);

		if (activities.isEmpty()) {
			return null;
		}
		return new ArrayOfSaveRiskClass2024ActivityDto()
			.withSaveRiskClass2024ActivityDto(activities.stream()
				.map(activitySlv -> new SaveRiskClass2024ActivityDto()
					.withSlvCode(activitySlv))
				.filter(activityDto -> !activityDto.getSlvCode().isEmpty())
				.toList());
	}

	static ArrayOfSaveRiskClass2024ProductGroupDto mapProductGroups(final String productGroupIdString) {

		final var productGroupIds = splitString(productGroupIdString);

		if (productGroupIds.isEmpty()) {
			return null;
		}
		return new ArrayOfSaveRiskClass2024ProductGroupDto()
			.withSaveRiskClass2024ProductGroupDto(productGroupIds.stream()
				.map(productGroupId -> new SaveRiskClass2024ProductGroupDto()
					.withSlvCode(productGroupId))
				.toList());
	}

	static ArrayOfSaveRiskClass2024CertificationDto mapThirdPartyCertifications(final String thirdPartyCertString) {

		final var thirdPartyCertifications = splitString(thirdPartyCertString);
		if (thirdPartyCertifications.isEmpty()) {
			return new ArrayOfSaveRiskClass2024CertificationDto().withSaveRiskClass2024CertificationDto(new SaveRiskClass2024CertificationDto());
		}
		return new ArrayOfSaveRiskClass2024CertificationDto()
			.withSaveRiskClass2024CertificationDto(
				thirdPartyCertifications.stream()
					.map(dto -> new SaveRiskClass2024CertificationDto()
						.withThirdPartyCertificationText(dto))
					.toList());
	}

	private static List<String> splitString(final String string) {
		if (string == null) {
			return Collections.emptyList();
		}

		return Arrays.stream(string.split("(,+\\s?)")).filter(s -> !s.isEmpty()).toList();
	}

}
