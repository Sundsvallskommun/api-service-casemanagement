package se.sundsvall.casemanagement.api.model;

import jakarta.validation.constraints.NotBlank;

import se.sundsvall.casemanagement.api.validation.PersonConstraints;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

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
@Schema(description = "Person model")
public class PersonDTO extends StakeholderDTO {

	@NotBlank
	@Schema(description = "The person's first name", example = "Test")
	private String firstName;

	@NotBlank
	@Schema(description = "The person's last name", example = "Testorsson")
	private String lastName;

	@NotBlank(groups = PersonConstraints.class)
	@ValidUuid(nullable = true, message = "personId must be a valid GUID")
	@Schema(description = "The person's id", example = "3ed5bc30-6308-4fd5-a5a7-78d7f96f4438")
	private String personId;

	@Schema(description = "The person's personal number", example = "20000101-1234", accessMode = Schema.AccessMode.READ_ONLY)
	private String personalNumber;

}
