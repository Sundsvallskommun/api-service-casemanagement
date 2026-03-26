package se.sundsvall.casemanagement.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import se.sundsvall.casemanagement.api.validation.AttachmentConstraints;

@Data
@SuperBuilder(setterPrefix = "with")
@NoArgsConstructor
@Schema(description = "Base case model")
public abstract class CaseDTO {

	@NotBlank
	@Schema(description = "Case ID from the client.", examples = "caa230c6-abb4-4592-ad9a-34e263c2787b")
	private String externalCaseId;

	@NotNull
	@Schema(description = "The case type", examples = "NYBYGGNAD_ANSOKAN_OM_BYGGLOV")
	private String caseType;

	@Schema(description = "Some description of the case.", examples = "En fritextbeskrivning av case.")
	private String description;

	@Schema(description = "Additions to the case title. Right now only applicable to cases of CaseType: NYBYGGNAD_ANSOKAN_OM_BYGGLOV.", examples = "Eldstad/rökkanal, Skylt")
	private String caseTitleAddition;

	@NotEmpty
	@Valid
	@Schema(description = "The stakeholders in the case", oneOf = {
		PersonDTO.class, OrganizationDTO.class
	})
	private List<StakeholderDTO> stakeholders;

	@NotEmpty(groups = AttachmentConstraints.class)
	@Valid
	@Schema(description = "The attachments in the case")
	private List<AttachmentDTO> attachments;

	@Schema(description = "Extra parameters for the case.")
	private Map<String, String> extraParameters;

}
