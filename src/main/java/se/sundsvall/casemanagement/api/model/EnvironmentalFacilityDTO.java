package se.sundsvall.casemanagement.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class EnvironmentalFacilityDTO extends FacilityDTO {

    @NotBlank
    @Schema(description = "The name on the sign.", example = "Sundsvalls testfabrik")
    private String facilityCollectionName;

}
