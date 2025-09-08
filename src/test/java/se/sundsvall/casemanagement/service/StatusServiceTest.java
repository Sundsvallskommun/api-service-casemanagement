package se.sundsvall.casemanagement.service;

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
import static se.sundsvall.casemanagement.util.Constants.CASE_DATA_STATUS_ROLE_SEARCH;

import generated.client.party.PartyType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.alkt.AlkTService;
import se.sundsvall.casemanagement.integration.byggr.ByggrService;
import se.sundsvall.casemanagement.integration.casedata.CaseDataService;
import se.sundsvall.casemanagement.integration.ecos.EcosService;
import se.sundsvall.casemanagement.integration.party.PartyIntegration;

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

	@Mock
	private AlkTService alkTServiceMock;

	@InjectMocks
	private StatusService statusService;

	@Test
	void getStatusByOrgNr() {
		final var caseStatuses = List.of(createCaseStatusDTO(), createCaseStatusDTO());
		when(byggrServiceMock.getByggrStatusByLegalId(ORGANIZATION_NUMBER, ENTERPRISE, MUNICIPALITY_ID)).thenReturn(caseStatuses);
		when(ecosServiceMock.getEcosStatusByLegalId(ORGANIZATION_NUMBER, ENTERPRISE, MUNICIPALITY_ID)).thenReturn(caseStatuses);
		when(caseDataServiceMock.getStatusesByFilter(CASE_DATA_ORGANIZATION_FILTER.formatted(ORGANIZATION_NUMBER), MUNICIPALITY_ID)).thenReturn(caseStatuses);

		final var result = statusService.getStatusByOrgNr(MUNICIPALITY_ID, ORGANIZATION_NUMBER);

		assertThat(result).hasSize(6);
		verify(byggrServiceMock).getByggrStatusByLegalId(ORGANIZATION_NUMBER, ENTERPRISE, MUNICIPALITY_ID);
		verify(ecosServiceMock).getEcosStatusByLegalId(ORGANIZATION_NUMBER, ENTERPRISE, MUNICIPALITY_ID);
		verify(caseDataServiceMock).getStatusesByFilter(CASE_DATA_ORGANIZATION_FILTER.formatted(ORGANIZATION_NUMBER), MUNICIPALITY_ID);
		verifyNoMoreInteractions(byggrServiceMock, ecosServiceMock, caseDataServiceMock);
		verifyNoInteractions(caseMappingServiceMock, partyIntegrationMock);
	}

	@Test
	void getStatusByExternalCaseIdByggR() {
		final var caseMapping = createCaseMapping(mapping -> mapping.setSystem(SystemType.BYGGR));
		final var caseStatusDTO = createCaseStatusDTO();

		when(caseMappingServiceMock.getCaseMapping(EXTERNAL_CASE_ID, MUNICIPALITY_ID)).thenReturn(caseMapping);
		when(byggrServiceMock.toByggrStatus(caseMapping)).thenReturn(caseStatusDTO);

		final var result = statusService.getStatusByExternalCaseId(MUNICIPALITY_ID, EXTERNAL_CASE_ID);

		assertThat(result).isEqualTo(caseStatusDTO);
		verify(caseMappingServiceMock).getCaseMapping(EXTERNAL_CASE_ID, MUNICIPALITY_ID);
		verify(byggrServiceMock).toByggrStatus(caseMapping);
		verifyNoMoreInteractions(caseMappingServiceMock, byggrServiceMock);
		verifyNoInteractions(ecosServiceMock, caseDataServiceMock, partyIntegrationMock);
	}

	@Test
	void getStatusByExternalCaseIdEcos() {
		final var caseMapping = createCaseMapping(mapping -> mapping.setSystem(SystemType.ECOS));
		final var caseStatusDTO = createCaseStatusDTO();

		when(caseMappingServiceMock.getCaseMapping(EXTERNAL_CASE_ID, MUNICIPALITY_ID)).thenReturn(caseMapping);
		when(ecosServiceMock.getStatus(caseMapping.getCaseId(), caseMapping.getExternalCaseId(), MUNICIPALITY_ID)).thenReturn(caseStatusDTO);

		final var result = statusService.getStatusByExternalCaseId(MUNICIPALITY_ID, EXTERNAL_CASE_ID);

		assertThat(result).isEqualTo(caseStatusDTO);
		verify(caseMappingServiceMock).getCaseMapping(EXTERNAL_CASE_ID, MUNICIPALITY_ID);
		verify(ecosServiceMock).getStatus(caseMapping.getCaseId(), caseMapping.getExternalCaseId(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(caseMappingServiceMock, ecosServiceMock);
		verifyNoInteractions(byggrServiceMock, caseDataServiceMock, partyIntegrationMock);
	}

	@Test
	void getStatusByExternalCaseIdCaseData() {
		final var caseMapping = createCaseMapping(mapping -> mapping.setSystem(SystemType.CASE_DATA));
		final var caseStatusDTO = createCaseStatusDTO();

		when(caseMappingServiceMock.getCaseMapping(EXTERNAL_CASE_ID, MUNICIPALITY_ID)).thenReturn(caseMapping);
		when(caseDataServiceMock.getStatus(caseMapping, MUNICIPALITY_ID)).thenReturn(caseStatusDTO);

		final var result = statusService.getStatusByExternalCaseId(MUNICIPALITY_ID, EXTERNAL_CASE_ID);

		assertThat(result).isEqualTo(caseStatusDTO);
		verify(caseMappingServiceMock).getCaseMapping(EXTERNAL_CASE_ID, MUNICIPALITY_ID);
		verify(caseDataServiceMock).getStatus(caseMapping, MUNICIPALITY_ID);
		verifyNoMoreInteractions(caseMappingServiceMock, caseDataServiceMock);
		verifyNoInteractions(ecosServiceMock, partyIntegrationMock);
	}

	@Test
	void getStatusesByPartyIdEnterprise() {
		final Map<PartyType, String> partyTypeStringMap = Map.of(ENTERPRISE, ORGANIZATION_NUMBER);
		final var caseStatusDTO = createCaseStatusDTO();

		when(partyIntegrationMock.getLegalIdByPartyId(MUNICIPALITY_ID, PARTY_ID)).thenReturn(partyTypeStringMap);
		when(alkTServiceMock.getStatusesByPartyId(PARTY_ID, MUNICIPALITY_ID)).thenReturn(List.of(caseStatusDTO));
		when(caseDataServiceMock.getStatusesByFilter(CASE_DATA_ORGANIZATION_FILTER.formatted(ORGANIZATION_NUMBER), MUNICIPALITY_ID)).thenReturn(List.of(caseStatusDTO));
		when(byggrServiceMock.getByggrStatusByLegalId(ORGANIZATION_NUMBER, ENTERPRISE, MUNICIPALITY_ID)).thenReturn(List.of(caseStatusDTO));
		when(ecosServiceMock.getEcosStatusByLegalId(ORGANIZATION_NUMBER, ENTERPRISE, MUNICIPALITY_ID)).thenReturn(List.of(caseStatusDTO));

		final var result = statusService.getStatusesByPartyId(MUNICIPALITY_ID, PARTY_ID);

		assertThat(result).hasSize(4);
		verify(partyIntegrationMock).getLegalIdByPartyId(MUNICIPALITY_ID, PARTY_ID);
		verify(caseDataServiceMock).getStatusesByFilter(CASE_DATA_ORGANIZATION_FILTER.formatted(ORGANIZATION_NUMBER), MUNICIPALITY_ID);
		verify(byggrServiceMock).getByggrStatusByLegalId(ORGANIZATION_NUMBER, ENTERPRISE, MUNICIPALITY_ID);
		verify(ecosServiceMock).getEcosStatusByLegalId(ORGANIZATION_NUMBER, ENTERPRISE, MUNICIPALITY_ID);
		verifyNoMoreInteractions(partyIntegrationMock, caseDataServiceMock, byggrServiceMock, ecosServiceMock);
		verifyNoInteractions(caseMappingServiceMock);
	}

	@Test
	void getStatusesByPartyIdPrivate() {
		final Map<PartyType, String> partyTypeStringMap = Map.of(PRIVATE, PERSONAL_NUMBER);
		final var caseStatusDTO = createCaseStatusDTO();

		when(partyIntegrationMock.getLegalIdByPartyId(MUNICIPALITY_ID, PARTY_ID)).thenReturn(partyTypeStringMap);
		when(alkTServiceMock.getStatusesByPartyId(PARTY_ID, MUNICIPALITY_ID)).thenReturn(List.of(caseStatusDTO));
		when(caseDataServiceMock.getStatusesByFilter(CASE_DATA_PERSON_FILTER.formatted(PARTY_ID, CASE_DATA_STATUS_ROLE_SEARCH), MUNICIPALITY_ID)).thenReturn(List.of(caseStatusDTO));
		when(byggrServiceMock.getByggrStatusByLegalId(PERSONAL_NUMBER, PRIVATE, MUNICIPALITY_ID)).thenReturn(List.of(caseStatusDTO));
		when(ecosServiceMock.getEcosStatusByLegalId(PERSONAL_NUMBER, PRIVATE, MUNICIPALITY_ID)).thenReturn(List.of(caseStatusDTO));

		final var result = statusService.getStatusesByPartyId(MUNICIPALITY_ID, PARTY_ID);

		assertThat(result).hasSize(4);
		verify(partyIntegrationMock).getLegalIdByPartyId(MUNICIPALITY_ID, PARTY_ID);
		verify(caseDataServiceMock).getStatusesByFilter(CASE_DATA_PERSON_FILTER.formatted(PARTY_ID, CASE_DATA_STATUS_ROLE_SEARCH), MUNICIPALITY_ID);
		verify(byggrServiceMock).getByggrStatusByLegalId(PERSONAL_NUMBER, PRIVATE, MUNICIPALITY_ID);
		verify(ecosServiceMock).getEcosStatusByLegalId(PERSONAL_NUMBER, PRIVATE, MUNICIPALITY_ID);
		verifyNoMoreInteractions(partyIntegrationMock, caseDataServiceMock, byggrServiceMock, ecosServiceMock);
		verifyNoInteractions(caseMappingServiceMock);
	}

	@Test
	void getStatusesByPartyIdNoMatch() {
		final Map<PartyType, String> partyTypeStringMap = Map.of();
		when(partyIntegrationMock.getLegalIdByPartyId(MUNICIPALITY_ID, PARTY_ID)).thenReturn(partyTypeStringMap);

		final var result = statusService.getStatusesByPartyId(MUNICIPALITY_ID, PARTY_ID);

		assertThat(result).isEmpty();
		verify(partyIntegrationMock).getLegalIdByPartyId(MUNICIPALITY_ID, PARTY_ID);
		verifyNoMoreInteractions(partyIntegrationMock);
		verifyNoInteractions(caseDataServiceMock, byggrServiceMock, ecosServiceMock, caseMappingServiceMock);

	}
}
