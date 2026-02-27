package se.sundsvall.casemanagement.api.model;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FutureCaseDTOTest {

	@Test
	void getAttachmentsReturnsEmptyList() {
		final var dto = new FutureCaseDTO();

		assertThat(dto.getAttachments()).isEmpty();
	}

	@Test
	void setAttachmentsIsNoOp() {
		final var dto = new FutureCaseDTO();

		dto.setAttachments(List.of(new AttachmentDTO()));

		assertThat(dto.getAttachments()).isEmpty();
	}

}
