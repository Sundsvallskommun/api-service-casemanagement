package se.sundsvall.casemanagement.api.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.api.model.enums.StakeholderType;
import se.sundsvall.casemanagement.api.validators.DefaultConstraints;
import se.sundsvall.casemanagement.api.validators.EnvironmentStakeholderRole;
import se.sundsvall.casemanagement.api.validators.EnvironmentalConstraints;
import se.sundsvall.casemanagement.api.validators.PlanningConstraints;
import se.sundsvall.casemanagement.api.validators.PlanningStakeholderRole;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.groups.ConvertGroup;
import java.util.List;
import java.util.Map;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY, visible = true)
@JsonSubTypes({@Type(value = PersonDTO.class, name = StakeholderType.Constants.PERSON_VALUE),
        @Type(value = OrganizationDTO.class, name = StakeholderType.Constants.ORGANIZATION_VALUE)})
@JsonPropertyOrder({"type", "roles", "organizationName", "organizationNumber", "firstName", "lastName", "personId",
        "phoneNumber", "emailAddress", "address", "billingAddress"})
@Data
public abstract class StakeholderDTO {
    @NotNull
    private StakeholderType type;

    @NotNull
    @Schema(description = "An stakeholder can have one or more roles.")
    @EnvironmentStakeholderRole(groups = EnvironmentalConstraints.class)
    @PlanningStakeholderRole(groups = PlanningConstraints.class)
    private List<StakeholderRole> roles;

    @Schema(example = "060123456")
    private String phoneNumber;

    @Schema(example = "0701234567")
    private String cellphoneNumber;

    @Email
    @Schema(example = "test.testorsson@sundsvall.se")
    private String emailAddress;

    @Valid
    @ConvertGroup(from = PlanningConstraints.class, to = DefaultConstraints.class)
    @ConvertGroup(from = EnvironmentalConstraints.class, to = DefaultConstraints.class)
    @Schema(description = "An stakeholder may have one or more addresses. For example one POSTAL_ADDRESS and another INVOICE_ADDRESS.")
    private List<AddressDTO> addresses;

    private Map<String, String> extraParameters;

}
