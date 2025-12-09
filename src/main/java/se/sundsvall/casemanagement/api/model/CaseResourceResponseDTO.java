package se.sundsvall.casemanagement.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Case resource response")
public class CaseResourceResponseDTO {

	@Schema(description = "The case id", examples = "1234")
	private String caseId;

}
