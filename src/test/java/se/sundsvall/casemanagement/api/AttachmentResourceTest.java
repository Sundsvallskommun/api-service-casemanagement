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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

	private static final String EXTERNAL_CASE_ID = "externalCaseId";

	private static final String MUNICIPALITY_ID = "2281";

	private static final String PATH = "/{municipalityId}/cases/{externalCaseId}/attachments";

	@MockitoBean
	private ByggrService byggrService;

	@MockitoBean
	private EcosService ecosService;

	@MockitoBean
	private CaseDataService caseDataService;

	@MockitoBean
	private CaseMappingService caseMappingService;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void postAttachmentsToCaseWithSystemEcos() {
		final var attachments = List.of(createAttachment(AttachmentCategory.BUILDING_PERMIT_APPLICATION));
		final var caseMapping = createCaseMapping(c -> c.setSystem(SystemType.ECOS));

		when(caseMappingService.getCaseMapping(EXTERNAL_CASE_ID, MUNICIPALITY_ID)).thenReturn(caseMapping);

		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(PATH).build(MUNICIPALITY_ID, EXTERNAL_CASE_ID))
			.bodyValue(attachments)
			.exchange()
			.expectStatus().isNoContent();

		verify(ecosService).addDocumentsToCase(caseMapping.getCaseId(), attachments);
		verifyNoInteractions(byggrService, caseDataService);
	}

	@Test
	void postAttachmentsToCaseWithSystemByggR() {
		final var attachments = List.of(createAttachment(AttachmentCategory.BUILDING_PERMIT_APPLICATION));
		final var caseMapping = createCaseMapping(c -> c.setSystem(SystemType.BYGGR));

		when(caseMappingService.getCaseMapping(EXTERNAL_CASE_ID, MUNICIPALITY_ID)).thenReturn(caseMapping);

		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(PATH).build(MUNICIPALITY_ID, EXTERNAL_CASE_ID))
			.bodyValue(attachments)
			.exchange()
			.expectStatus().isNoContent();

		verify(byggrService).saveNewIncomingAttachmentHandelse(caseMapping.getCaseId(), attachments);
		verifyNoInteractions(ecosService, caseDataService);
	}

	@Test
	void postAttachmentsToCaseWithSystemCaseData() {
		final var attachments = List.of(createAttachment(AttachmentCategory.BUILDING_PERMIT_APPLICATION));
		final var caseMapping = createCaseMapping(c -> c.setSystem(SystemType.CASE_DATA));

		when(caseMappingService.getCaseMapping(EXTERNAL_CASE_ID, MUNICIPALITY_ID)).thenReturn(caseMapping);

		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(PATH).build(MUNICIPALITY_ID, EXTERNAL_CASE_ID))
			.bodyValue(attachments)
			.exchange()
			.expectStatus().isNoContent();

		verify(caseDataService).patchErrandWithAttachment(caseMapping, attachments, MUNICIPALITY_ID);
		verifyNoInteractions(byggrService, ecosService);
	}
}
