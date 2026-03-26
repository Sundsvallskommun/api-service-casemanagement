package se.sundsvall.casemanagement.integration.byggr.handler;

import arendeexport.SaveNewHandelse;
import generated.client.oep_integrator.CaseStatusChangeRequest;
import generated.client.oep_integrator.InstanceType;
import generated.client.party.PartyType;
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
import se.sundsvall.casemanagement.integration.party.PartyIntegration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.casemanagement.TestUtil.createAttachmentDTO;
import static se.sundsvall.casemanagement.util.Constants.ERRAND_NR;
import static se.sundsvall.casemanagement.util.Constants.OTHER_INFORMATION;

@ExtendWith(MockitoExtension.class)
class AddInspectorHandlerTest {

	@Mock
	private ArendeExportClient arendeExportClient;

	@Mock
	private OepIntegratorClient oepIntegratorClient;

	@Mock
	private PartyIntegration partyIntegration;

	@InjectMocks
	private AddInspectorHandler handler;

	@Test
	void handle() {
		// Arrange
		final var municipalityId = "2281";
		final var externalCaseId = "ext-123";
		final var errandNr = "BYGG 2024-000123";
		final var otherInformation = "Some other information";
		final var personId = "3ed5bc30-6308-4fd5-a5a7-78d7f96f4438";
		final var personalNumber = "199001011234";

		final var stakeholder = PersonDTO.builder()
			.withType(StakeholderType.PERSON)
			.withFirstName("Test")
			.withLastName("Testorsson")
			.withPersonId(personId)
			.build();

		final var extraParameters = Map.of(
			ERRAND_NR, errandNr,
			OTHER_INFORMATION, otherInformation,
			"certificateAuthType", "N",
			"certificateNumber", "12345",
			"certificateIssuer", "RISE",
			"certificateValidDate", "2025-12-31");

		final var byggRCase = ByggRCaseDTO.builder()
			.withMunicipalityId(municipalityId)
			.withExternalCaseId(externalCaseId)
			.withStakeholders(List.of(stakeholder))
			.withAttachments(List.of(createAttachmentDTO(AttachmentCategory.BUILDING_PERMIT_APPLICATION)))
			.withExtraParameters(extraParameters)
			.build();

		when(partyIntegration.getLegalIdByPartyId(municipalityId, personId))
			.thenReturn(Map.of(PartyType.PRIVATE, personalNumber));

		// Act
		handler.handle(byggRCase);

		// Assert
		verify(arendeExportClient, times(2)).saveNewHandelse(any(SaveNewHandelse.class));
		verify(oepIntegratorClient).setStatus(eq(municipalityId), eq(InstanceType.EXTERNAL), eq(externalCaseId), any(CaseStatusChangeRequest.class));
	}
}
