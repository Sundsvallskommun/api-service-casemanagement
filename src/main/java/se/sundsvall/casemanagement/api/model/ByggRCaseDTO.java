package se.sundsvall.casemanagement.api.model;

import java.io.Serializable;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import se.sundsvall.casemanagement.api.validation.ByggRFacilityConstraints;
import se.sundsvall.casemanagement.api.validation.OnlyOneMainFacility;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "ByggR-cases")
public class ByggRCaseDTO extends CaseDTO implements Serializable {

	private static final long serialVersionUID = 1688217969349003646L;

	@Schema(description = "The case diary number", example = "2021-1234")
	private String diaryNumber;

	@NotEmpty(groups = ByggRFacilityConstraints.class)
	@OnlyOneMainFacility(nullable = true, groups = ByggRFacilityConstraints.class)
	@Schema(description = "The facilities in the case")
	private List<@Valid FacilityDTO> facilities;

}
