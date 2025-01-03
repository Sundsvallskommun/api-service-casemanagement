package se.sundsvall.casemanagement.api.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Stakeholder type")
public enum StakeholderType {

	PERSON(), ORGANIZATION();

	StakeholderType() {}

	public static final class Constants {

		public static final String PERSON_VALUE = "PERSON";

		public static final String ORGANIZATION_VALUE = "ORGANIZATION";

		private Constants() {}

	}
}
