package se.sundsvall.casemanagement.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CoordinatesDTO {

    @Schema(description = "Decimal Degrees (DD)", example = "62.390205")
    private double latitude;
    @Schema(description = "Decimal Degrees (DD)", example = "17.306616")
    private double longitude;

}
