package se.sundsvall.casemanagement.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
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

	private static final long serialVersionUID = 3543604692890119457L;

	@Schema(description = "Decimal Degrees (DD)", examples = "62.390205")
	private double latitude;

	@Schema(description = "Decimal Degrees (DD)", examples = "17.306616")
	private double longitude;
}
