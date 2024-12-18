package se.sundsvall.casemanagement.api.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.sundsvall.casemanagement.api.validation.ByggRConstraints;

@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
	"facilityType", "description", "address", "facilityCollectionName", "mainFacility", "extraParameters"
})
@Schema(description = "Facility model")
public class FacilityDTO implements Serializable {

	private static final long serialVersionUID = -2904255346222794001L;

	@Schema(description = "Description of the facility", example = "En fritextbeskrivning av facility.")
	private String description;

	@Schema(description = "The type of facility", example = "GARAGE")
	private String facilityType;

	@Schema(description = "The name on the sign.", example = "Sundsvalls testfabrik")
	private String facilityCollectionName;

	@Schema(description = "Is it a main facility?", example = "true")
	private boolean mainFacility;

	@NotNull(groups = ByggRConstraints.class)
	@Valid
	@Schema(description = "The facility address", implementation = AddressDTO.class)
	private AddressDTO address;

	@Builder.Default
	@Schema(description = "Extra parameters for the facility")
	private Map<String, String> extraParameters = new HashMap<>();

}
