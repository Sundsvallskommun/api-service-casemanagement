package se.sundsvall.casemanagement.api.model;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;

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
    @Schema(type = "string", format = "byte", description = "Base64-encoded file (plain text)", example = "dGVzdA==")
    private String file;

    private Map<String, String> extraParameters;
}
