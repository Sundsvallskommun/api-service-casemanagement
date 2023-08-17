package se.sundsvall.casemanagement.api.model;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import se.sundsvall.casemanagement.api.validators.PersonConstraints;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class PersonDTO extends StakeholderDTO {

    @NotBlank
    @Schema(example = "Test")
    private String firstName;

    @NotBlank
    @Schema(example = "Testorsson")
    private String lastName;

    @NotBlank(groups = PersonConstraints.class)
    @ValidUuid(nullable = true, message = "personId must be a valid GUID")
    @Schema(example = "3ed5bc30-6308-4fd5-a5a7-78d7f96f4438")
    private String personId;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String personalNumber;
}
