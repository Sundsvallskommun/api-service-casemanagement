package se.sundsvall.casemanagement.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.dept44.common.validators.annotation.ValidBase64;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Map;

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
    @Pattern(regexp = "^\\.(bmp|gif|tif|tiff|jpeg|jpg|png|htm|html|pdf|rtf|docx|txt|xlsx|odt|ods)$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "extension must be valid. Must match regex: {regexp}")
    @Schema(example = ".pdf")
    private String extension;

    @Pattern(regexp = "^(application|image|text)/(bmp|gif|tiff|jpeg|png|html|pdf|rtf|vnd.openxmlformats-officedocument.wordprocessingml.document|plain|vnd.openxmlformats-officedocument.spreadsheetml.sheet|vnd.oasis.opendocument.text|vnd.oasis.opendocument.spreadsheet)$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "mimeType must be valid. Must match regex: {regexp}")
    @Schema(example = "application/pdf")
    private String mimeType;

    @NotBlank
    @ValidBase64(message = "file must be a valid Base64 string. Plain text - only the Base64 value.")
    @Schema(type = "string", format = "byte", description = "Base64-encoded file (plain text)", example = "dGVzdA==")
    private String file;

    private Map<String, String> extraParameters;
}
