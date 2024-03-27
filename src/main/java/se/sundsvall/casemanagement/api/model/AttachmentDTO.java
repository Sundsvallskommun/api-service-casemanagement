package se.sundsvall.casemanagement.api.model;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;

import se.sundsvall.casemanagement.api.validation.ValidAttachmentCategory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDTO {

	@ValidAttachmentCategory
	private String category;

	@NotBlank
	@Schema(example = "The attachment name")
	private String name;

	@Schema(example = "A note on an attachment.")
	private String note;

	@NotBlank
	@Schema(example = ".pdf")
	private String extension;

	@NotBlank
	@Schema(example = "application/pdf")
	private String mimeType;

	@NotBlank
	@Schema(type = "string", format = "byte", description = "Base64-encoded file (plain text)", example = "dGVzdA==")
	private String file;

	private Map<String, String> extraParameters;

}
