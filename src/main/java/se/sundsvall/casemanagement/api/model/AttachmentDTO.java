package se.sundsvall.casemanagement.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sundsvall.casemanagement.api.validation.ValidAttachmentCategory;

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
	@Schema(description = "the mime type of the attachment", example = "application/pdf")
	private String mimeType;

	@NotBlank
	@Schema(type = "string", format = "byte", description = "Base64-encoded file (plain text)", example = "dGVzdA==")
	private String file;

	@Schema(description = "Extra parameters for the attachment")
	private Map<String, String> extraParameters;

}
