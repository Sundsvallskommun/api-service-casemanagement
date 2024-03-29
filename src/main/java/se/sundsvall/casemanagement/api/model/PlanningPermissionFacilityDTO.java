package se.sundsvall.casemanagement.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class PlanningPermissionFacilityDTO extends FacilityDTO {

    private boolean mainFacility;
	
    private String facilityType;
}
