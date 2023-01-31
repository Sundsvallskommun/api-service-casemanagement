package se.sundsvall.casemanagement.api.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import se.sundsvall.casemanagement.api.validators.PlanningConstraints;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;

@JsonPropertyOrder({"facilityType", "description", "address"})
@Data
public abstract class FacilityDTO {

    @Schema(example = "En fritextbeskrivning av facility.")
    private String description;

    @NotNull(groups = PlanningConstraints.class)
    @Valid
    private AddressDTO address;

    private Map<String, String> extraParameters;

}
