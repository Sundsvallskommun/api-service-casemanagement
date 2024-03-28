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
@Schema(description = "Attachment model")
public class AttachmentDTO {

	@ValidAttachmentCategory
	@Schema(description = "The attachment category", example = "DOCUMENT")
	private String category;

	@NotBlank
	@Schema(description = "The attachment name", example = "Attachment name")
	private String name;

	@Schema(description = "A note on an attachment", example = "A note on an attachment.")
	private String note;

	@NotBlank
	@Schema(description = "The file extension", example = ".pdf")
	private String extension;

	@NotBlank
	@Schema(description = "the mime type of the attahcment", example = "application/pdf")
	private String mimeType;

	@NotBlank
	@Schema(type = "string", format = "byte", description = "Base64-encoded file (plain text)", example = "dGVzdA==")
	private String file;

	@Schema(description = "Extra parameters for the attachment")
	private Map<String, String> extraParameters;

}
