package se.sundsvall.casemanagement.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(setterPrefix = "with")
@NoArgsConstructor
@Schema(description = "EDPFuture case model")
public class FutureCaseDTO extends CaseDTO implements Serializable {

	private static final long serialVersionUID = 4270693784029868560L;

	@Override
	@JsonIgnore
	@Schema(hidden = true)
	public List<AttachmentDTO> getAttachments() {
		return Collections.emptyList();
	}

	@Override
	@JsonIgnore
	public void setAttachments(final List<AttachmentDTO> attachments) {
		// No-op: FutureCaseDTO does not support attachments
	}

}
