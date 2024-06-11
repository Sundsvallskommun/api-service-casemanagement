package se.sundsvall.casemanagement.api.model;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(setterPrefix = "with")
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Coordinates model")
public class CoordinatesDTO implements Serializable {

	@Schema(description = "Decimal Degrees (DD)", example = "62.390205")
	private double latitude;

	@Schema(description = "Decimal Degrees (DD)", example = "17.306616")
	private double longitude;
}
