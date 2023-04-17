package se.sundsvall.casemanagement.api.model;

import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANDRING_ANSOKAN_OM_BYGGLOV;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANDRING_BARANDE_KONSTRUKTION;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANDRING_BRANDSKYDD;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANDRING_PLANLOSNING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANDRING_VA;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANDRING_VENTILATION;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_ANDRING_AVLOPPSANLAGGNING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_ANDRING_AVLOPPSANORDNING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_ATTEFALL;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_ELDSTAD;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_HALSOSKYDDSVERKSAMHET;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_INSTALLATION_VARMEPUMP;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANSOKAN_TILLSTAND_VARMEPUMP;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.INSTALLATION_VA;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.INSTALLATION_VENTILATION;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.INSTALLLATION_HISS;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.LOST_PARKING_PERMIT;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MARKLOV_FYLL;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MARKLOV_OVRIGT;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MARKLOV_SCHAKTNING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MARKLOV_TRADFALLNING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.NYBYGGNAD_ANSOKAN_OM_BYGGLOV;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.NYBYGGNAD_FORHANDSBESKED;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.PARKING_PERMIT;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.PARKING_PERMIT_RENEWAL;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.REGISTRERING_AV_LIVSMEDEL;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.STRANDSKYDD_ANDRAD_ANVANDNING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.STRANDSKYDD_ANLAGGANDE;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.STRANDSKYDD_ANORDNANDE;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.STRANDSKYDD_NYBYGGNAD;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.STRANDSKYDD_OVRIGT;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.TILLBYGGNAD_ANSOKAN_OM_BYGGLOV;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.UPPDATERING_RISKKLASSNING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.UPPSATTANDE_SKYLT;

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
        NYBYGGNAD_ANSOKAN_OM_BYGGLOV,
        NYBYGGNAD_FORHANDSBESKED,
        ANMALAN_ATTEFALL,
        UPPSATTANDE_SKYLT,
        TILLBYGGNAD_ANSOKAN_OM_BYGGLOV,
        ANDRING_ANSOKAN_OM_BYGGLOV,
        STRANDSKYDD_NYBYGGNAD,
        STRANDSKYDD_ANDRAD_ANVANDNING,
        STRANDSKYDD_ANORDNANDE,
        STRANDSKYDD_ANLAGGANDE,
        ANMALAN_ELDSTAD,
        ANDRING_VENTILATION,
        INSTALLATION_VENTILATION,
        ANDRING_VA,
        INSTALLATION_VA,
        ANDRING_PLANLOSNING,
        ANDRING_BARANDE_KONSTRUKTION,
        ANDRING_BRANDSKYDD,
        INSTALLLATION_HISS,
        MARKLOV_SCHAKTNING,
        MARKLOV_FYLL,
        MARKLOV_TRADFALLNING,
        MARKLOV_OVRIGT,
        STRANDSKYDD_OVRIGT
        
    }),
    @Type(value = EnvironmentalCaseDTO.class, names = {
        REGISTRERING_AV_LIVSMEDEL,
        ANMALAN_INSTALLATION_VARMEPUMP,
        ANSOKAN_TILLSTAND_VARMEPUMP,
        ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC,
        ANMALAN_ANDRING_AVLOPPSANLAGGNING,
        ANMALAN_ANDRING_AVLOPPSANORDNING,
        ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP,
        UPPDATERING_RISKKLASSNING,
        ANMALAN_HALSOSKYDDSVERKSAMHET}),
    @Type(value = OtherCaseDTO.class, names = {
        PARKING_PERMIT,
        LOST_PARKING_PERMIT,
        PARKING_PERMIT_RENEWAL})})
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
