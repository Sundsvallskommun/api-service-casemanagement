package se.sundsvall.casemanagement.api.model;

import se.sundsvall.casemanagement.api.model.enums.FacilityType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class PlanningPermissionFacilityDTO extends FacilityDTO {
    
    private boolean mainFacility;
    
    private FacilityType facilityType;
    
}
