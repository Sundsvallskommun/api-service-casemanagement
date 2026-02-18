package se.sundsvall.casemanagement.integration.ecos;

import java.util.Map;
import java.util.UUID;
import minutmiljo.CreateOrganizationParty;
import minutmiljo.CreateOrganizationPartyResponse;
import minutmiljo.CreatePersonParty;
import minutmiljo.CreatePersonPartyResponse;
import minutmiljo.SearchParty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casemanagement.TestUtil;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.integration.party.PartyIntegration;

import static generated.client.party.PartyType.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casemanagement.api.model.enums.AttachmentCategory.UNDERLAG_RISKKLASSNING;

@ExtendWith(MockitoExtension.class)
class PartyServiceTest {

	@Mock
	private MinutMiljoClient minutMiljoClientMock;

	@Mock
	private PartyIntegration partyIntegrationMock;

	@InjectMocks
	private PartyService partyService;

	@Test
	void findAndAddPartyToCaseWithOrganizationStakeholder() {

		final var createdOrganizationPartyID = UUID.randomUUID().toString();
		when(minutMiljoClientMock.createOrganizationParty(any(CreateOrganizationParty.class)))
			.thenReturn(new CreateOrganizationPartyResponse().withCreateOrganizationPartyResult(createdOrganizationPartyID));

		final var result = partyService.findAndAddPartyToCase(TestUtil.createEcosCaseDTO(CaseType.REGISTRERING_AV_LIVSMEDEL, UNDERLAG_RISKKLASSNING), "someCaseId", "someMunicipalityId");

		assertThat(result).isNotNull().isNotEmpty();
		assertThat(result.getFirst().get(createdOrganizationPartyID)).isNotNull().satisfies(party -> assertThat(party.getGuid()).isNotEmpty());

		verify(minutMiljoClientMock, times(1)).searchParty(any(SearchParty.class));
		verify(minutMiljoClientMock, times(1)).createOrganizationParty(any(CreateOrganizationParty.class));
		verify(minutMiljoClientMock, times(1)).addPartyToCase(any());

		verifyNoMoreInteractions(minutMiljoClientMock);
		verifyNoInteractions(partyIntegrationMock);
	}

	@Test
	void findAndAddPartyToCaseWithPersonStakeholder() {

		final var createdPersonPartyID = UUID.randomUUID().toString();
		final var municipalityId = "someMunicipalityId";

		final var caseDTO = TestUtil.createEcosCaseDTO(CaseType.REGISTRERING_AV_LIVSMEDEL, UNDERLAG_RISKKLASSNING);
		caseDTO.getStakeholders().removeFirst();

		when(partyIntegrationMock.getLegalIdByPartyId(eq(municipalityId), any(String.class))).thenReturn(Map.of(PRIVATE, "19800101-1234"));

		when(minutMiljoClientMock.createPersonParty(any(CreatePersonParty.class)))
			.thenReturn(new CreatePersonPartyResponse().withCreatePersonPartyResult(createdPersonPartyID));

		final var result = partyService.findAndAddPartyToCase(caseDTO, "someCaseId", municipalityId);

		assertThat(result).isNotNull().isNotEmpty();
		assertThat(result.getFirst().get(createdPersonPartyID)).isNotNull().satisfies(party -> assertThat(party.getGuid()).isNotEmpty());

	}
}
