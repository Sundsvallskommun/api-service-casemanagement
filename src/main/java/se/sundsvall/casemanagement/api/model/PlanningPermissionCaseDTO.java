package se.sundsvall.casemanagement.api.model;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import se.sundsvall.casemanagement.api.validators.OnlyOneMainFacility;

@Schema(description = "ByggR-cases")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class PlanningPermissionCaseDTO extends CaseDTO {

	@NotEmpty
	@OnlyOneMainFacility
	@Valid
	private List<PlanningPermissionFacilityDTO> facilities;

	private String diaryNumber;
}
