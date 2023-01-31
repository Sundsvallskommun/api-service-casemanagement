package se.sundsvall.casemanagement.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import se.sundsvall.casemanagement.service.util.Constants;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class OrganizationDTO extends StakeholderDTO {

    @NotBlank
    @Schema(example = "Sundsvalls testfabrik")
    private String organizationName;

    @NotBlank
    @Pattern(regexp = Constants.ORGNR_PATTERN_REGEX, message = Constants.ORGNR_PATTERN_MESSAGE)
    @Schema(description = "Organization number with 10 or 12 digits.", example = "20220622-2396")
    private String organizationNumber;

    @Schema(example = "Test Testorsson")
    private String authorizedSignatory;

}
