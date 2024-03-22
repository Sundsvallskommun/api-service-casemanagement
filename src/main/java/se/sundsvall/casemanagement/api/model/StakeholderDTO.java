package se.sundsvall.casemanagement.api.model;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.ConvertGroup;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import se.sundsvall.casemanagement.api.model.enums.StakeholderType;
import se.sundsvall.casemanagement.api.validation.DefaultConstraints;
import se.sundsvall.casemanagement.api.validation.EnvironmentStakeholderRole;
import se.sundsvall.casemanagement.api.validation.EnvironmentalConstraints;
import se.sundsvall.casemanagement.api.validation.PlanningConstraints;
import se.sundsvall.casemanagement.api.validation.PlanningStakeholderRole;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY, visible = true)
@JsonSubTypes({@Type(value = PersonDTO.class, name = StakeholderType.Constants.PERSON_VALUE),
	@Type(value = OrganizationDTO.class, name = StakeholderType.Constants.ORGANIZATION_VALUE)})
@JsonPropertyOrder({"type", "roles", "organizationName", "organizationNumber", "firstName", "lastName", "personId",
	"phoneNumber", "emailAddress", "address", "billingAddress"})
@Data
@SuperBuilder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public abstract class StakeholderDTO {

	@NotNull
	private StakeholderType type;

	@Schema(description = "An stakeholder can have one or more roles. Please note that INVOICE_RECIPENT is deprecated and should not be used. Use INVOICE_RECIPIENT instead.", enumAsRef = true)
	@EnvironmentStakeholderRole(groups = EnvironmentalConstraints.class)
	@PlanningStakeholderRole(groups = PlanningConstraints.class)
	private List<String> roles;

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
