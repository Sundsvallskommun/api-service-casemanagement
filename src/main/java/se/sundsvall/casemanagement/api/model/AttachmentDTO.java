package se.sundsvall.casemanagement.api.model;

import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.dept44.common.validators.annotation.ValidBase64;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AttachmentDTO {
    @NotNull
    private AttachmentCategory category;

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
    @ValidBase64(message = "file must be a valid Base64 string. Plain text - only the Base64 value.")
    @Schema(type = "string", format = "byte", description = "Base64-encoded file (plain text)", example = "dGVzdA==")
    private String file;

    private Map<String, String> extraParameters;
}
