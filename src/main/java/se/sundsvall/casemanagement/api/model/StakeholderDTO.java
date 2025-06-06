package se.sundsvall.casemanagement.api.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.ConvertGroup;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import se.sundsvall.casemanagement.api.model.enums.StakeholderType;
import se.sundsvall.casemanagement.api.validation.ByggRConstraints;
import se.sundsvall.casemanagement.api.validation.ByggRStakeholderRole;
import se.sundsvall.casemanagement.api.validation.DefaultConstraints;
import se.sundsvall.casemanagement.api.validation.EcosConstraints;
import se.sundsvall.casemanagement.api.validation.EcosStakeholderRole;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY, visible = true)
@JsonSubTypes({
	@Type(value = PersonDTO.class, name = StakeholderType.Constants.PERSON_VALUE),
	@Type(value = OrganizationDTO.class, name = StakeholderType.Constants.ORGANIZATION_VALUE)
})
@JsonPropertyOrder({
	"type", "roles", "organizationName", "organizationNumber", "firstName", "lastName", "personId",
	"phoneNumber", "emailAddress", "address", "billingAddress"
})
@Data
@SuperBuilder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Stakeholder model")
public abstract class StakeholderDTO {

	@NotNull
	@Schema(description = "The type of stakeholder", example = "PERSON")
	private StakeholderType type;

	@Schema(description = "A stakeholder can have one or more roles.", enumAsRef = true)
	@EcosStakeholderRole(groups = EcosConstraints.class)
	@ByggRStakeholderRole(groups = ByggRConstraints.class)
	private List<String> roles;

	@Schema(description = "Stakeholder phone number", example = "060123456")
	private String phoneNumber;

	@Schema(description = "Stakeholder cellphone number", example = "0701234567")
	private String cellphoneNumber;

	@Email
	@Schema(description = "Stakeholder emailaddress", example = "test.testorsson@sundsvall.se")
	private String emailAddress;

	@Valid
	@ConvertGroup(from = ByggRConstraints.class, to = DefaultConstraints.class)
	@ConvertGroup(from = EcosConstraints.class, to = DefaultConstraints.class)
	@Schema(description = "A stakeholder may have one or more addresses. For example one POSTAL_ADDRESS and another INVOICE_ADDRESS.")
	private List<AddressDTO> addresses;

	@Schema(description = "The stakeholder's billing address")
	private Map<String, String> extraParameters;

}
