package se.sundsvall.casemanagement.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static se.sundsvall.casemanagement.TestUtil.createCaseMapping;
import static se.sundsvall.casemanagement.TestUtil.createCaseStatusDTO;

import generated.client.party.PartyType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.casemanagement.Application;
import se.sundsvall.casemanagement.api.model.CaseStatusDTO;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.byggr.ByggrService;
import se.sundsvall.casemanagement.integration.casedata.CaseDataService;
import se.sundsvall.casemanagement.integration.ecos.EcosService;
import se.sundsvall.casemanagement.service.CaseMappingService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class CaseStatusResourceTest {

	private static final String MUNICIPALITY_ID = "2281";

	private static final String ORG_PATH = "/" + MUNICIPALITY_ID + "/organization/{organizationNumber}/cases/status";

	private static final String PATH = "/" + MUNICIPALITY_ID + "/cases/{externalCaseId}/status";

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
	void getStatusByOrgNr() {
		final var organizationNumber = "20220622-2396";
		when(byggrService.getByggrStatusByLegalId(organizationNumber, PartyType.ENTERPRISE, MUNICIPALITY_ID)).thenReturn(List.of(createCaseStatusDTO()));
		when(ecosService.getEcosStatusByLegalId(organizationNumber, PartyType.ENTERPRISE, MUNICIPALITY_ID)).thenReturn(List.of(createCaseStatusDTO()));

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(ORG_PATH).build(organizationNumber))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(CaseStatusDTO.class)
			.returnResult().getResponseBody();

		assertThat(response).hasSize(2);

		verify(byggrService).getByggrStatusByLegalId(organizationNumber, PartyType.ENTERPRISE, MUNICIPALITY_ID);
		verify(ecosService).getEcosStatusByLegalId(organizationNumber, PartyType.ENTERPRISE, MUNICIPALITY_ID);
		verifyNoMoreInteractions(byggrService, ecosService);
		verifyNoInteractions(caseMappingService, caseDataService);
	}

	@Test
	void getStatusByOrgNr_NoMatch() {
		final var organizationNumber = "20220622-2396";
		when(byggrService.getByggrStatusByLegalId(organizationNumber, PartyType.ENTERPRISE, MUNICIPALITY_ID)).thenReturn(List.of());
		when(ecosService.getEcosStatusByLegalId(organizationNumber, PartyType.ENTERPRISE, MUNICIPALITY_ID)).thenReturn(List.of());

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(ORG_PATH).build(organizationNumber))
			.exchange()
			.expectStatus().isNotFound()
			.expectBodyList(CaseStatusDTO.class)
			.returnResult().getResponseBody();

		assertThat(response).isEmpty();

		verify(byggrService).getByggrStatusByLegalId(organizationNumber, PartyType.ENTERPRISE, MUNICIPALITY_ID);
		verify(ecosService).getEcosStatusByLegalId(organizationNumber, PartyType.ENTERPRISE, MUNICIPALITY_ID);
		verifyNoMoreInteractions(byggrService, ecosService);
		verifyNoInteractions(caseMappingService, caseDataService);
	}

	@Test
	void getStatusByExternalCaseId_Ecos() {
		final var caseMapping = createCaseMapping(caseMappingConsumer -> caseMappingConsumer.setSystem(SystemType.ECOS));
		final var caseStatusDTO = createCaseStatusDTO(caseStatusConsumer -> caseStatusConsumer.setSystem(SystemType.ECOS));
		when(caseMappingService.getCaseMapping("externalCaseId", MUNICIPALITY_ID)).thenReturn(caseMapping);
		when(ecosService.getStatus(caseMapping.getCaseId(), caseMapping.getExternalCaseId(), MUNICIPALITY_ID)).thenReturn(caseStatusDTO);

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(PATH).build("externalCaseId"))
			.exchange()
			.expectStatus().isOk()
			.expectBody(CaseStatusDTO.class)
			.returnResult().getResponseBody();

		assertThat(response).satisfies(statusDTO -> {
			assertThat(statusDTO.getSystem()).isEqualTo(SystemType.ECOS);
			assertThat(statusDTO.getCaseId()).isEqualTo(caseStatusDTO.getCaseId());
			assertThat(statusDTO.getStatus()).isEqualTo(caseStatusDTO.getStatus());
		});

		verify(caseMappingService).getCaseMapping("externalCaseId", MUNICIPALITY_ID);
		verify(ecosService).getStatus(caseMapping.getCaseId(), caseMapping.getExternalCaseId(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(caseMappingService, ecosService);
		verifyNoInteractions(byggrService, caseDataService);
	}

	@Test
	void getStatusByExternalCaseId_ByggR() {
		final var caseMapping = createCaseMapping(caseMappingConsumer -> caseMappingConsumer.setSystem(SystemType.BYGGR));
		final var caseStatusDTO = createCaseStatusDTO(caseStatusConsumer -> caseStatusConsumer.setSystem(SystemType.BYGGR));
		when(caseMappingService.getCaseMapping("externalCaseId", MUNICIPALITY_ID)).thenReturn(caseMapping);
		when(byggrService.toByggrStatus(caseMapping)).thenReturn(caseStatusDTO);

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(PATH).build("externalCaseId"))
			.exchange()
			.expectStatus().isOk()
			.expectBody(CaseStatusDTO.class)
			.returnResult().getResponseBody();

		assertThat(response).satisfies(statusDTO -> {
			assertThat(statusDTO.getSystem()).isEqualTo(SystemType.BYGGR);
			assertThat(statusDTO.getCaseId()).isEqualTo(caseStatusDTO.getCaseId());
			assertThat(statusDTO.getStatus()).isEqualTo(caseStatusDTO.getStatus());
		});

		verify(caseMappingService).getCaseMapping("externalCaseId", MUNICIPALITY_ID);
		verify(byggrService).toByggrStatus(caseMapping);
		verifyNoMoreInteractions(caseMappingService, byggrService);
		verifyNoInteractions(ecosService, caseDataService);
	}

	@Test
	void getStatusByExternalCaseId_CaseData() {
		final var caseMapping = createCaseMapping(caseMappingConsumer -> caseMappingConsumer.setSystem(SystemType.CASE_DATA));
		final var caseStatusDTO = createCaseStatusDTO(caseStatusConsumer -> caseStatusConsumer.setSystem(SystemType.CASE_DATA));
		when(caseMappingService.getCaseMapping("externalCaseId", MUNICIPALITY_ID)).thenReturn(caseMapping);
		when(caseDataService.getStatus(caseMapping, MUNICIPALITY_ID)).thenReturn(caseStatusDTO);

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(PATH).build("externalCaseId"))
			.exchange()
			.expectStatus().isOk()
			.expectBody(CaseStatusDTO.class)
			.returnResult().getResponseBody();

		assertThat(response).satisfies(statusDTO -> {
			assertThat(statusDTO.getSystem()).isEqualTo(SystemType.CASE_DATA);
			assertThat(statusDTO.getCaseId()).isEqualTo(caseStatusDTO.getCaseId());
			assertThat(statusDTO.getStatus()).isEqualTo(caseStatusDTO.getStatus());
		});

		verify(caseMappingService).getCaseMapping("externalCaseId", MUNICIPALITY_ID);
		verify(caseDataService).getStatus(caseMapping, MUNICIPALITY_ID);
		verifyNoMoreInteractions(caseMappingService, caseDataService);
		verifyNoInteractions(byggrService, ecosService);
	}

}
