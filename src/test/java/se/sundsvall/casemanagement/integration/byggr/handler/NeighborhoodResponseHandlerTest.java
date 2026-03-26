package se.sundsvall.casemanagement.integration.byggr.handler;

import arendeexport.SaveNewRemissvar;
import generated.client.oep_integrator.CaseStatusChangeRequest;
import generated.client.oep_integrator.InstanceType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.casemanagement.api.model.enums.StakeholderType;
import se.sundsvall.casemanagement.integration.byggr.ArendeExportClient;
import se.sundsvall.casemanagement.integration.oepintegrator.OepIntegratorClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static se.sundsvall.casemanagement.TestUtil.createAttachmentDTO;
import static se.sundsvall.casemanagement.util.Constants.COMMENT;
import static se.sundsvall.casemanagement.util.Constants.ERRAND_INFORMATION;
import static se.sundsvall.casemanagement.util.Constants.PROPERTY;

@ExtendWith(MockitoExtension.class)
class NeighborhoodResponseHandlerTest {

	@Mock
	private ArendeExportClient arendeExportClient;

	@Mock
	private OepIntegratorClient oepIntegratorClient;

	@InjectMocks
	private NeighborhoodResponseHandler handler;

	@Test
	void handle() {
		// Arrange
		final var municipalityId = "2281";
		final var externalCaseId = "ext-123";
		final var remissId = 42;
		final var errandInformation = "Some errand information";
		final var comment = "Jag har synpunkter";
		final var property = "Sundsvall BAANSEN 1:3 [" + remissId + "] rest of string";

		final var byggRCase = ByggRCaseDTO.builder()
			.withMunicipalityId(municipalityId)
			.withExternalCaseId(externalCaseId)
			.withStakeholders(List.of(PersonDTO.builder()
				.withType(StakeholderType.PERSON)
				.withFirstName("Test")
				.withLastName("Testorsson")
				.withPersonId("3ed5bc30-6308-4fd5-a5a7-78d7f96f4438")
				.build()))
			.withAttachments(List.of(createAttachmentDTO(AttachmentCategory.BUILDING_PERMIT_APPLICATION)))
			.withExtraParameters(Map.of(
				COMMENT, comment,
				PROPERTY, property,
				ERRAND_INFORMATION, errandInformation))
			.build();

		// Act
		handler.handle(byggRCase);

		// Assert
		verify(arendeExportClient).saveNewRemissvar(any(SaveNewRemissvar.class));
		verify(oepIntegratorClient).setStatus(eq(municipalityId), eq(InstanceType.EXTERNAL), eq(externalCaseId), any(CaseStatusChangeRequest.class));
	}

	@Test
	void handleWithNoComplaints() {
		// Arrange
		final var municipalityId = "2281";
		final var externalCaseId = "ext-456";
		final var remissId = 99;
		final var errandInformation = "No complaints information";
		final var comment = "Jag har inga synpunkter";
		final var property = "Sundsvall BAANSEN 1:3 [" + remissId + "]";

		final var byggRCase = ByggRCaseDTO.builder()
			.withMunicipalityId(municipalityId)
			.withExternalCaseId(externalCaseId)
			.withStakeholders(List.of(PersonDTO.builder()
				.withType(StakeholderType.PERSON)
				.withFirstName("Test")
				.withLastName("Testorsson")
				.withPersonId("3ed5bc30-6308-4fd5-a5a7-78d7f96f4438")
				.build()))
			.withAttachments(List.of(createAttachmentDTO(AttachmentCategory.BUILDING_PERMIT_APPLICATION)))
			.withExtraParameters(Map.of(
				COMMENT, comment,
				PROPERTY, property,
				ERRAND_INFORMATION, errandInformation))
			.build();

		// Act
		handler.handle(byggRCase);

		// Assert
		verify(arendeExportClient).saveNewRemissvar(any(SaveNewRemissvar.class));
		verify(oepIntegratorClient).setStatus(eq(municipalityId), eq(InstanceType.EXTERNAL), eq(externalCaseId), any(CaseStatusChangeRequest.class));
	}
}
