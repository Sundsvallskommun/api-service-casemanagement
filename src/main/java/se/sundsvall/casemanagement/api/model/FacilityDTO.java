package se.sundsvall.casemanagement.api.model;

import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class FacilityDTO {

	@Schema(description = "Description of the facility", example = "En fritextbeskrivning av facility.")
	private String description;

	@Schema(description = "The type of facility", example = "GARAGE")
	private String facilityType;

	@Schema(description = "The name on the sign.", example = "Sundsvalls testfabrik")
	private String facilityCollectionName;

	@Schema(description = "Is it a main facility?", example = "true")
	private boolean mainFacility;

	@Schema(description = "The facility address", implementation = AddressDTO.class)
	private AddressDTO address;

	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();

}
