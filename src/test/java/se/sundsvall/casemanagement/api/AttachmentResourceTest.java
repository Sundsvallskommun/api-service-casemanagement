package se.sundsvall.casemanagement.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static se.sundsvall.casemanagement.TestUtil.createAttachment;
import static se.sundsvall.casemanagement.TestUtil.createCaseMapping;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.casemanagement.Application;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.byggr.ByggrService;
import se.sundsvall.casemanagement.integration.casedata.CaseDataService;
import se.sundsvall.casemanagement.integration.ecos.EcosService;
import se.sundsvall.casemanagement.service.CaseMappingService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class AttachmentResourceTest {

	private static final String PATH_VARIABLE = "externalCaseId";

	@MockBean
	private ByggrService byggrService;

	@MockBean
	private EcosService ecosService;

	@MockBean
	private CaseDataService caseDataService;

	@MockBean
	private CaseMappingService caseMappingService;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void postAttachmentsToCase_Ecos() {
		final var attachments = List.of(createAttachment(AttachmentCategory.BUILDING_PERMIT_APPLICATION));
		final var caseMapping = createCaseMapping(c -> c.setSystem(SystemType.ECOS));

		when(caseMappingService.getCaseMapping(PATH_VARIABLE)).thenReturn(caseMapping);

		webTestClient.post().uri(uriBuilder -> uriBuilder.path("/cases/{externalCaseId}/attachments").build(PATH_VARIABLE))
			.bodyValue(attachments)
			.exchange()
			.expectStatus().isNoContent();

		verify(ecosService).addDocumentsToCase(caseMapping.getCaseId(), attachments);
		verifyNoInteractions(byggrService, caseDataService);
	}

	@Test
	void postAttachmentsToCase_ByggR() {
		final var attachments = List.of(createAttachment(AttachmentCategory.BUILDING_PERMIT_APPLICATION));
		final var caseMapping = createCaseMapping(c -> c.setSystem(SystemType.BYGGR));

		when(caseMappingService.getCaseMapping(PATH_VARIABLE)).thenReturn(caseMapping);

		webTestClient.post().uri(uriBuilder -> uriBuilder.path("/cases/{externalCaseId}/attachments").build(PATH_VARIABLE))
			.bodyValue(attachments)
			.exchange()
			.expectStatus().isNoContent();

		verify(byggrService).saveNewIncomingAttachmentHandelse(caseMapping.getCaseId(), attachments);
		verifyNoInteractions(ecosService, caseDataService);
	}

	@Test
	void postAttachmentsToCase_CaseData() {
		final var attachments = List.of(createAttachment(AttachmentCategory.BUILDING_PERMIT_APPLICATION));
		final var caseMapping = createCaseMapping(c -> c.setSystem(SystemType.CASE_DATA));

		when(caseMappingService.getCaseMapping(PATH_VARIABLE)).thenReturn(caseMapping);

		webTestClient.post().uri(uriBuilder -> uriBuilder.path("/cases/{externalCaseId}/attachments").build(PATH_VARIABLE))
			.bodyValue(attachments)
			.exchange()
			.expectStatus().isNoContent();

		verify(caseDataService).patchErrandWithAttachment(caseMapping.getExternalCaseId(), attachments);
		verifyNoInteractions(byggrService, ecosService);
	}

}
