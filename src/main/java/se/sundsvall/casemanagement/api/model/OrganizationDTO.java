package se.sundsvall.casemanagement.api.model;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
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
