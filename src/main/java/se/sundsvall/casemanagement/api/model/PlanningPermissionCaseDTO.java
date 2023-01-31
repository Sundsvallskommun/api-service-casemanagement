package se.sundsvall.casemanagement.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import se.sundsvall.casemanagement.api.validators.OnlyOneMainFacility;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

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
