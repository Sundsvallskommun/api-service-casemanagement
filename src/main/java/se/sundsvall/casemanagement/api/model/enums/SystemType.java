package se.sundsvall.casemanagement.api.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "System type")
public enum SystemType {
	BYGGR, ECOS, CASE_DATA
}