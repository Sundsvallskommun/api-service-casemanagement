package se.sundsvall.casemanagement.api.model;

import java.io.Serializable;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import se.sundsvall.casemanagement.api.validation.OnlyOneMainFacility;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "ByggR-cases")
public class ByggRCaseDTO extends CaseDTO implements Serializable {

	private String diaryNumber;

	@NotEmpty
	@OnlyOneMainFacility
	@Valid
	private List<@Valid FacilityDTO> facilities;

}
