package se.sundsvall.casemanagement.api.model;

import java.io.Serializable;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import se.sundsvall.casemanagement.api.validation.OnlyOneMainFacility;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "ByggR-cases")
public class ByggRCaseDTO extends CaseDTO implements Serializable {

	@Schema(description = "The case diary number", example = "2021-1234")
	private String diaryNumber;

	@NotEmpty
	@OnlyOneMainFacility(nullable = true)
	@Valid
	@Schema(description = "The facilities in the case")
	private List<@Valid FacilityDTO> facilities;

}
