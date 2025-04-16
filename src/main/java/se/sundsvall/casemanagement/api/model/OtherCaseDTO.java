package se.sundsvall.casemanagement.api.model;

import static java.util.Collections.emptyList;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Other case model")
public class OtherCaseDTO extends CaseDTO implements Serializable {

	private static final long serialVersionUID = -1627503596853775503L;

	@Schema(description = "The facilities in the case")
	@NotNull
	@Builder.Default
	private List<FacilityDTO> facilities = emptyList();
}
