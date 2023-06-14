package se.sundsvall.casemanagement.api.model;

import javax.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class OrganizationDTO extends StakeholderDTO {

    @NotBlank
    @Schema(example = "Sundsvalls testfabrik")
    private String organizationName;

    @NotBlank
    @Schema(description = "Organization number with 10 or 12 digits.", example = "20220622-2396")
    private String organizationNumber;

    @Schema(example = "Test Testorsson")
    private String authorizedSignatory;

}
