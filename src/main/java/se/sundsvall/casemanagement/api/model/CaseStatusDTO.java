package se.sundsvall.casemanagement.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.SystemType;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class CaseStatusDTO {

    private SystemType system;
    private CaseType caseType;
    @Schema(example = "caa230c6-abb4-4592-ad9a-34e263c2787d")
    private String externalCaseId;
    @Schema(example = "BYGG 2022-000100")
    private String caseId;
    @Schema(example = "Pågående")
    private String status;
    @Schema(example = "Nybyggnad - Ansökan om bygglov")
    private String serviceName;
    private LocalDateTime timestamp;
}
