package se.sundsvall.casemanagement.service;

import generated.client.party.PartyType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.byggr.ByggrService;
import se.sundsvall.casemanagement.integration.casedata.CaseDataService;
import se.sundsvall.casemanagement.integration.ecos.EcosService;
import se.sundsvall.casemanagement.integration.party.PartyIntegration;

import java.util.List;
import java.util.Map;

import static generated.client.party.PartyType.ENTERPRISE;
import static generated.client.party.PartyType.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casemanagement.TestUtil.createCaseMapping;
import static se.sundsvall.casemanagement.TestUtil.createCaseStatusDTO;
import static se.sundsvall.casemanagement.service.StatusService.CASE_DATA_ORGANIZATION_FILTER;
import static se.sundsvall.casemanagement.service.StatusService.CASE_DATA_PERSON_FILTER;

@ExtendWith(MockitoExtension.class)
class StatusServiceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String EXTERNAL_CASE_ID = "1234567890";
	private static final String ORGANIZATION_NUMBER = "1234567890";
	private static final String PERSONAL_NUMBER = "199901011234";
	private static final String PARTY_ID = "bb24e7cd-ba51-4828-89d9-3b42194e91c0";

	@Mock
	private ByggrService byggrServiceMock;

	@Mock
	private EcosService ecosServiceMock;

	@Mock
	private CaseDataService caseDataServiceMock;

	@Mock
	private CaseMappingService caseMappingServiceMock;

	@Mock
	private PartyIntegration partyIntegrationMock;

	@InjectMocks
	private StatusService statusService;

	@Test
	void getStatusByOrgNr() {
		var caseStatuses = List.of(createCaseStatusDTO(), createCaseStatusDTO());
		when(byggrServiceMock.getByggrStatusByLegalId(ORGANIZATION_NUMBER, ENTERPRISE, MUNICIPALITY_ID)).thenReturn(caseStatuses);
		when(ecosServiceMock.getEcosStatusByLegalId(ORGANIZATION_NUMBER, ENTERPRISE, MUNICIPALITY_ID)).thenReturn(caseStatuses);

		var result = statusService.getStatusByOrgNr(MUNICIPALITY_ID, ORGANIZATION_NUMBER);

		assertThat(result).hasSize(4);
		verify(byggrServiceMock).getByggrStatusByLegalId(ORGANIZATION_NUMBER, ENTERPRISE, MUNICIPALITY_ID);
		verify(ecosServiceMock).getEcosStatusByLegalId(ORGANIZATION_NUMBER, ENTERPRISE, MUNICIPALITY_ID);
		verifyNoMoreInteractions(byggrServiceMock, ecosServiceMock);
		verifyNoInteractions(caseDataServiceMock, caseMappingServiceMock, partyIntegrationMock);
	}

	@Test
	void getStatusByExternalCaseId_ByggR() {
		var caseMapping = createCaseMapping(mapping -> mapping.setSystem(SystemType.BYGGR));
		var caseStatusDTO = createCaseStatusDTO();

		when(caseMappingServiceMock.getCaseMapping(EXTERNAL_CASE_ID, MUNICIPALITY_ID)).thenReturn(caseMapping);
		when(byggrServiceMock.toByggrStatus(caseMapping)).thenReturn(caseStatusDTO);

		var result = statusService.getStatusByExternalCaseId(MUNICIPALITY_ID, EXTERNAL_CASE_ID);

		assertThat(result).isEqualTo(caseStatusDTO);
		verify(caseMappingServiceMock).getCaseMapping(EXTERNAL_CASE_ID, MUNICIPALITY_ID);
		verify(byggrServiceMock).toByggrStatus(caseMapping);
		verifyNoMoreInteractions(caseMappingServiceMock, byggrServiceMock);
		verifyNoInteractions(ecosServiceMock, caseDataServiceMock, partyIntegrationMock);
	}

	@Test
	void getStatusByExternalCaseId_Ecos() {
		var caseMapping = createCaseMapping(mapping -> mapping.setSystem(SystemType.ECOS));
		var caseStatusDTO = createCaseStatusDTO();

		when(caseMappingServiceMock.getCaseMapping(EXTERNAL_CASE_ID, MUNICIPALITY_ID)).thenReturn(caseMapping);
		when(ecosServiceMock.getStatus(caseMapping.getCaseId(), caseMapping.getExternalCaseId(), MUNICIPALITY_ID)).thenReturn(caseStatusDTO);

		var result = statusService.getStatusByExternalCaseId(MUNICIPALITY_ID, EXTERNAL_CASE_ID);

		assertThat(result).isEqualTo(caseStatusDTO);
		verify(caseMappingServiceMock).getCaseMapping(EXTERNAL_CASE_ID, MUNICIPALITY_ID);
		verify(ecosServiceMock).getStatus(caseMapping.getCaseId(), caseMapping.getExternalCaseId(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(caseMappingServiceMock, ecosServiceMock);
		verifyNoInteractions(byggrServiceMock, caseDataServiceMock, partyIntegrationMock);
	}

	@Test
	void getStatusByExternalCaseId_CaseData() {
		var caseMapping = createCaseMapping(mapping -> mapping.setSystem(SystemType.CASE_DATA));
		var caseStatusDTO = createCaseStatusDTO();

		when(caseMappingServiceMock.getCaseMapping(EXTERNAL_CASE_ID, MUNICIPALITY_ID)).thenReturn(caseMapping);
		when(caseDataServiceMock.getStatus(caseMapping, MUNICIPALITY_ID)).thenReturn(caseStatusDTO);

		var result = statusService.getStatusByExternalCaseId(MUNICIPALITY_ID, EXTERNAL_CASE_ID);

		assertThat(result).isEqualTo(caseStatusDTO);
		verify(caseMappingServiceMock).getCaseMapping(EXTERNAL_CASE_ID, MUNICIPALITY_ID);
		verify(caseDataServiceMock).getStatus(caseMapping, MUNICIPALITY_ID);
		verifyNoMoreInteractions(caseMappingServiceMock, caseDataServiceMock);
		verifyNoInteractions(ecosServiceMock, partyIntegrationMock);
	}

	@Test
	void getStatusesByPartyId_Enterprise() {
		Map<PartyType, String> partyTypeStringMap = Map.of(ENTERPRISE, ORGANIZATION_NUMBER);
		var caseStatusDTO = createCaseStatusDTO();

		when(partyIntegrationMock.getLegalIdByPartyId(MUNICIPALITY_ID, PARTY_ID)).thenReturn(partyTypeStringMap);
		when(caseDataServiceMock.getStatusesByFilter(CASE_DATA_ORGANIZATION_FILTER.formatted(ORGANIZATION_NUMBER), MUNICIPALITY_ID)).thenReturn(List.of(caseStatusDTO));
		when(byggrServiceMock.getByggrStatusByLegalId(ORGANIZATION_NUMBER, ENTERPRISE, MUNICIPALITY_ID)).thenReturn(List.of(caseStatusDTO));
		when(ecosServiceMock.getEcosStatusByLegalId(ORGANIZATION_NUMBER, ENTERPRISE, MUNICIPALITY_ID)).thenReturn(List.of(caseStatusDTO));

		var result = statusService.getStatusesByPartyId(MUNICIPALITY_ID, PARTY_ID);

		assertThat(result).hasSize(3);
		verify(partyIntegrationMock).getLegalIdByPartyId(MUNICIPALITY_ID, PARTY_ID);
		verify(caseDataServiceMock).getStatusesByFilter(CASE_DATA_ORGANIZATION_FILTER.formatted(ORGANIZATION_NUMBER), MUNICIPALITY_ID);
		verify(byggrServiceMock).getByggrStatusByLegalId(ORGANIZATION_NUMBER, ENTERPRISE, MUNICIPALITY_ID);
		verify(ecosServiceMock).getEcosStatusByLegalId(ORGANIZATION_NUMBER, ENTERPRISE, MUNICIPALITY_ID);
		verifyNoMoreInteractions(partyIntegrationMock, caseDataServiceMock, byggrServiceMock, ecosServiceMock);
		verifyNoInteractions(caseMappingServiceMock);
	}

	@Test
	void getStatusesByPartyId_Private() {
		Map<PartyType, String> partyTypeStringMap = Map.of(PRIVATE, PERSONAL_NUMBER);
		var caseStatusDTO = createCaseStatusDTO();

		when(partyIntegrationMock.getLegalIdByPartyId(MUNICIPALITY_ID, PARTY_ID)).thenReturn(partyTypeStringMap);
		when(caseDataServiceMock.getStatusesByFilter(CASE_DATA_PERSON_FILTER.formatted(PARTY_ID), MUNICIPALITY_ID)).thenReturn(List.of(caseStatusDTO));
		when(byggrServiceMock.getByggrStatusByLegalId(PERSONAL_NUMBER, PRIVATE, MUNICIPALITY_ID)).thenReturn(List.of(caseStatusDTO));
		when(ecosServiceMock.getEcosStatusByLegalId(PERSONAL_NUMBER, PRIVATE, MUNICIPALITY_ID)).thenReturn(List.of(caseStatusDTO));

		var result = statusService.getStatusesByPartyId(MUNICIPALITY_ID, PARTY_ID);

		assertThat(result).hasSize(3);
		verify(partyIntegrationMock).getLegalIdByPartyId(MUNICIPALITY_ID, PARTY_ID);
		verify(caseDataServiceMock).getStatusesByFilter(CASE_DATA_PERSON_FILTER.formatted(PARTY_ID), MUNICIPALITY_ID);
		verify(byggrServiceMock).getByggrStatusByLegalId(PERSONAL_NUMBER, PRIVATE, MUNICIPALITY_ID);
		verify(ecosServiceMock).getEcosStatusByLegalId(PERSONAL_NUMBER, PRIVATE, MUNICIPALITY_ID);
		verifyNoMoreInteractions(partyIntegrationMock, caseDataServiceMock, byggrServiceMock, ecosServiceMock);
		verifyNoInteractions(caseMappingServiceMock);
	}

	@Test
	void getStatusesByPartyId_NoMatch() {
		Map<PartyType, String> partyTypeStringMap = Map.of();
		when(partyIntegrationMock.getLegalIdByPartyId(MUNICIPALITY_ID, PARTY_ID)).thenReturn(partyTypeStringMap);

		var result = statusService.getStatusesByPartyId(MUNICIPALITY_ID, PARTY_ID);

		assertThat(result).isEmpty();
		verify(partyIntegrationMock).getLegalIdByPartyId(MUNICIPALITY_ID, PARTY_ID);
		verifyNoMoreInteractions(partyIntegrationMock);
		verifyNoInteractions(caseDataServiceMock, byggrServiceMock, ecosServiceMock, caseMappingServiceMock);

	}

	@Test
	void getCaseStatusesByLegalId() {
		var caseStatusDTO = createCaseStatusDTO();

		when(byggrServiceMock.getByggrStatusByLegalId(ORGANIZATION_NUMBER, ENTERPRISE, MUNICIPALITY_ID)).thenReturn(List.of(caseStatusDTO));
		when(ecosServiceMock.getEcosStatusByLegalId(ORGANIZATION_NUMBER, ENTERPRISE, MUNICIPALITY_ID)).thenReturn(List.of(caseStatusDTO));

		var result = statusService.getCaseStatusesByLegalId(ORGANIZATION_NUMBER, ENTERPRISE, MUNICIPALITY_ID);

		assertThat(result).hasSize(2);
		verify(byggrServiceMock).getByggrStatusByLegalId(ORGANIZATION_NUMBER, ENTERPRISE, MUNICIPALITY_ID);
		verify(ecosServiceMock).getEcosStatusByLegalId(ORGANIZATION_NUMBER, ENTERPRISE, MUNICIPALITY_ID);
		verifyNoMoreInteractions(byggrServiceMock, ecosServiceMock);
		verifyNoInteractions(caseDataServiceMock, caseMappingServiceMock, partyIntegrationMock);
	}

}
