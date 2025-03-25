package se.sundsvall.casemanagement.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sundsvall.casemanagement.api.model.enums.SystemType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(setterPrefix = "with")
@Schema(description = "Case status model")
public class CaseStatusDTO {

	@Schema(description = "The system type", example = "BYGGR")
	private SystemType system;

	@Schema(description = "The case type", example = "BYGGLOV")
	private String caseType;

	@Schema(description = "The external case id", example = "123456")
	private String externalCaseId;

	@Schema(description = "The case id", example = "caa230c6-abb4-4592-ad9a-34e263c2787d")
	private String caseId;

	@Schema(description = "Case status", example = "Pågående")
	private String status;

	@Schema(description = "Service name", example = "Nybyggnad - Ansökan om bygglov")
	private String serviceName;

	@Schema(description = "The timestamp", example = "2022-01-01T12:00:00")
	private LocalDateTime timestamp;

	@Schema(description = "The namespace", example = "NAMESPACE")
	private String namespace;

	@Schema(description = "The errand number", example = "BYGG 2022-000003")
	private String errandNumber;

}
