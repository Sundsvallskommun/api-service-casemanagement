package se.sundsvall.casemanagement.api.model;

import java.io.Serializable;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import se.sundsvall.casemanagement.api.validators.OnlyOneMainFacility;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "ByggR-cases")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class PlanningPermissionCaseDTO extends CaseDTO implements Serializable {

	@NotEmpty
	@OnlyOneMainFacility
	@Valid
	private List<PlanningPermissionFacilityDTO> facilities;

	private String diaryNumber;

}
