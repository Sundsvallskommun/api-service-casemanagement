package se.sundsvall.casemanagement.api.model;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import se.sundsvall.casemanagement.api.model.enums.CaseType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "caseType", include = JsonTypeInfo.As.EXISTING_PROPERTY, visible = true)
@JsonSubTypes({
    @Type(value = PlanningPermissionCaseDTO.class, names = {
        CaseType.Constants.NYBYGGNAD_ANSOKAN_OM_BYGGLOV_VALUE,
        CaseType.Constants.NYBYGGNAD_FORHANDSBESKED_VALUE,
        CaseType.Constants.ANMALAN_ATTEFALL_VALUE,
        CaseType.Constants.UPPSATTANDE_SKYLT_VALUE,
        CaseType.Constants.TILLBYGGNAD_ANSOKAN_OM_BYGGLOV_VALUE,
        CaseType.Constants.ANDRING_ANSOKAN_OM_BYGGLOV_VALUE,
        CaseType.Constants.STRANDSKYDD_NYBYGGNAD_VALUE,
        CaseType.Constants.STRANDSKYDD_ANDRAD_ANVANDNING_VALUE,
        CaseType.Constants.STRANDSKYDD_ANORDNANDE_VALUE,
        CaseType.Constants.STRANDSKYDD_ANLAGGANDE_VALUE,
        CaseType.Constants.ANMALAN_ELDSTAD_VALUE}),
    @Type(value = EnvironmentalCaseDTO.class, names = {
        CaseType.Constants.REGISTRERING_AV_LIVSMEDEL_VALUE,
        CaseType.Constants.ANMALAN_INSTALLATION_VARMEPUMP_VALUE,
        CaseType.Constants.ANSOKAN_TILLSTAND_VARMEPUMP_VALUE,
        CaseType.Constants.ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC_VALUE,
        CaseType.Constants.ANMALAN_ANDRING_AVLOPPSANLAGGNING_VALUE,
        CaseType.Constants.ANMALAN_ANDRING_AVLOPPSANORDNING_VALUE,
        CaseType.Constants.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP_VALUE,
        CaseType.Constants.UPPDATERING_RISKKLASSNING_VALUE,
        CaseType.Constants.ANMALAN_HALSOSKYDDSVERKSAMHET_VALUE}),
    @Type(value = OtherCaseDTO.class, names = {
        CaseType.Constants.PARKING_PERMIT_VALUE,
        CaseType.Constants.LOST_PARKING_PERMIT_VALUE,
        CaseType.Constants.PARKING_PERMIT_RENEWAL_VALUE})})
@Data
public abstract class CaseDTO {
    
    @NotBlank
    @Schema(description = "Case ID from the client.", example = "caa230c6-abb4-4592-ad9a-34e263c2787b")
    private String externalCaseId;
    
    @NotNull
    private CaseType caseType;
    
    @Schema(example = "Some description of the case.")
    private String description;
    
    @Schema(description = "Additions to the case title. Right now only applicable to cases of CaseType: NYBYGGNAD_ANSOKAN_OM_BYGGLOV.", example = "Eldstad/rökkanal, Skylt")
    private String caseTitleAddition;
    
    @NotEmpty
    @Valid
    private List<StakeholderDTO> stakeholders;
    
    @NotEmpty
    @Valid
    private List<AttachmentDTO> attachments;
    
    private Map<String, String> extraParameters;
}
