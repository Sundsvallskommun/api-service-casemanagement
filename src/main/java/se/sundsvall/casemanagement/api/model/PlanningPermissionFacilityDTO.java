package se.sundsvall.casemanagement.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import se.sundsvall.casemanagement.api.model.enums.FacilityType;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class PlanningPermissionFacilityDTO extends FacilityDTO {

	private boolean mainFacility;
	private FacilityType facilityType;
}
