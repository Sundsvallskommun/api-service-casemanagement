package se.sundsvall.casemanagement.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static se.sundsvall.casemanagement.TestUtil.createCaseMapping;
import static se.sundsvall.casemanagement.TestUtil.createCaseStatusDTO;

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
		when(byggrService.getByggrStatusByOrgNr(organizationNumber)).thenReturn(List.of(createCaseStatusDTO()));
		when(ecosService.getEcosStatusByOrgNr(organizationNumber)).thenReturn(List.of(createCaseStatusDTO()));

		final var response = webTestClient.get().uri(uriBuilder -> uriBuilder.path("organization/{organizationNumber}/cases/status").build(organizationNumber))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(CaseStatusDTO.class)
			.returnResult().getResponseBody();

		assertThat(response).hasSize(2);

		verify(byggrService).getByggrStatusByOrgNr(organizationNumber);
		verify(ecosService).getEcosStatusByOrgNr(organizationNumber);
		verifyNoMoreInteractions(byggrService, ecosService);
		verifyNoInteractions(caseMappingService, caseDataService);
	}

	@Test
	void getStatusByOrgNr_NoMatch() {
		final var organizationNumber = "20220622-2396";
		when(byggrService.getByggrStatusByOrgNr(organizationNumber)).thenReturn(List.of());
		when(ecosService.getEcosStatusByOrgNr(organizationNumber)).thenReturn(List.of());

		final var response = webTestClient.get().uri(uriBuilder -> uriBuilder.path("organization/{organizationNumber}/cases/status").build(organizationNumber))
			.exchange()
			.expectStatus().isNotFound()
			.expectBodyList(CaseStatusDTO.class)
			.returnResult().getResponseBody();

		assertThat(response).isEmpty();

		verify(byggrService).getByggrStatusByOrgNr(organizationNumber);
		verify(ecosService).getEcosStatusByOrgNr(organizationNumber);
		verifyNoMoreInteractions(byggrService, ecosService);
		verifyNoInteractions(caseMappingService, caseDataService);
	}

	@Test
	void getStatusByExternalCaseId_Ecos() {
		final var caseMapping = createCaseMapping(c -> c.setSystem(SystemType.ECOS));
		final var caseStatusDTO = createCaseStatusDTO(c -> c.setSystem(SystemType.ECOS));
		when(caseMappingService.getCaseMapping("externalCaseId")).thenReturn(caseMapping);
		when(ecosService.getStatus(caseMapping.getCaseId(), caseMapping.getExternalCaseId())).thenReturn(caseStatusDTO);

		final var response = webTestClient.get().uri(uriBuilder -> uriBuilder.path("cases/{externalCaseId}/status").build("externalCaseId"))
			.exchange()
			.expectStatus().isOk()
			.expectBody(CaseStatusDTO.class)
			.returnResult().getResponseBody();

		assertThat(response).satisfies(r -> {
			assertThat(r.getSystem()).isEqualTo(SystemType.ECOS);
			assertThat(r.getCaseId()).isEqualTo(caseStatusDTO.getCaseId());
			assertThat(r.getStatus()).isEqualTo(caseStatusDTO.getStatus());
		});

		verify(caseMappingService).getCaseMapping("externalCaseId");
		verify(ecosService).getStatus(caseMapping.getCaseId(), caseMapping.getExternalCaseId());
		verifyNoMoreInteractions(caseMappingService, ecosService);
		verifyNoInteractions(byggrService, caseDataService);
	}

	@Test
	void getStatusByExternalCaseId_ByggR() {
		final var caseMapping = createCaseMapping(c -> c.setSystem(SystemType.BYGGR));
		final var caseStatusDTO = createCaseStatusDTO(c -> c.setSystem(SystemType.BYGGR));
		when(caseMappingService.getCaseMapping("externalCaseId")).thenReturn(caseMapping);
		when(byggrService.toByggrStatus(caseMapping)).thenReturn(caseStatusDTO);

		final var response = webTestClient.get().uri(uriBuilder -> uriBuilder.path("cases/{externalCaseId}/status").build("externalCaseId"))
			.exchange()
			.expectStatus().isOk()
			.expectBody(CaseStatusDTO.class)
			.returnResult().getResponseBody();

		assertThat(response).satisfies(r -> {
			assertThat(r.getSystem()).isEqualTo(SystemType.BYGGR);
			assertThat(r.getCaseId()).isEqualTo(caseStatusDTO.getCaseId());
			assertThat(r.getStatus()).isEqualTo(caseStatusDTO.getStatus());
		});

		verify(caseMappingService).getCaseMapping("externalCaseId");
		verify(byggrService).toByggrStatus(caseMapping);
		verifyNoMoreInteractions(caseMappingService, byggrService);
		verifyNoInteractions(ecosService, caseDataService);
	}

	@Test
	void getStatusByExternalCaseId_CaseData() {
		final var caseMapping = createCaseMapping(c -> c.setSystem(SystemType.CASE_DATA));
		final var caseStatusDTO = createCaseStatusDTO(c -> c.setSystem(SystemType.CASE_DATA));
		when(caseMappingService.getCaseMapping("externalCaseId")).thenReturn(caseMapping);
		when(caseDataService.getStatus(caseMapping)).thenReturn(caseStatusDTO);

		final var response = webTestClient.get().uri(uriBuilder -> uriBuilder.path("cases/{externalCaseId}/status").build("externalCaseId"))
			.exchange()
			.expectStatus().isOk()
			.expectBody(CaseStatusDTO.class)
			.returnResult().getResponseBody();

		assertThat(response).satisfies(r -> {
			assertThat(r.getSystem()).isEqualTo(SystemType.CASE_DATA);
			assertThat(r.getCaseId()).isEqualTo(caseStatusDTO.getCaseId());
			assertThat(r.getStatus()).isEqualTo(caseStatusDTO.getStatus());
		});

		verify(caseMappingService).getCaseMapping("externalCaseId");
		verify(caseDataService).getStatus(caseMapping);
		verifyNoMoreInteractions(caseMappingService, caseDataService);
		verifyNoInteractions(byggrService, ecosService);
	}

}
