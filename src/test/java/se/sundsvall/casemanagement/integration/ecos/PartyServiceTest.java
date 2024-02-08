package se.sundsvall.casemanagement.integration.ecos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casemanagement.api.model.enums.AttachmentCategory.UNDERLAG_RISKKLASSNING;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.casemanagement.TestUtil;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.service.CitizenService;

import minutmiljo.CreateOrganizationParty;
import minutmiljo.CreateOrganizationPartyResponse;
import minutmiljo.CreatePersonParty;
import minutmiljo.CreatePersonPartyResponse;
import minutmiljo.SearchParty;

@ExtendWith(MockitoExtension.class)
class PartyServiceTest {

	@Mock
	private MinutMiljoClient minutMiljoClient;

	@Mock
	private CitizenService citizenService;

	@InjectMocks
	private PartyService partyService;

	@Test
	void findAndAddPartyToCase_withOrganizationStakeholder() {

		final var createdOrganizationPartyID = UUID.randomUUID().toString();
		when(minutMiljoClient.createOrganizationParty(any(CreateOrganizationParty.class)))
			.thenReturn(new CreateOrganizationPartyResponse().withCreateOrganizationPartyResult(createdOrganizationPartyID));

		final var result = partyService.findAndAddPartyToCase(TestUtil.createEnvironmentalCase(CaseType.REGISTRERING_AV_LIVSMEDEL, UNDERLAG_RISKKLASSNING), "someCaseId");

		assertThat(result).isNotNull().isNotEmpty();
		assertThat(result.getFirst().get(createdOrganizationPartyID)).isNotNull().satisfies(party -> assertThat(party.getGuid()).isNotEmpty());

		verify(minutMiljoClient, times(2)).searchParty(any(SearchParty.class));
		verify(minutMiljoClient, times(1)).createOrganizationParty(any(CreateOrganizationParty.class));
		verify(minutMiljoClient, times(1)).addPartyToCase(any());

		verifyNoMoreInteractions(minutMiljoClient);
		verifyNoInteractions(citizenService);
	}


	@Test
	void findAndAddPartyToCase_withPersonStakeholder() {

		final var createdPersonPartyID = UUID.randomUUID().toString();

		final var caseDTO = TestUtil.createEnvironmentalCase(CaseType.REGISTRERING_AV_LIVSMEDEL, UNDERLAG_RISKKLASSNING);
		caseDTO.getStakeholders().removeFirst();

		when(citizenService.getPersonalNumber(any(String.class))).thenReturn("19800101-1234");

		when(minutMiljoClient.createPersonParty(any(CreatePersonParty.class)))
			.thenReturn(new CreatePersonPartyResponse().withCreatePersonPartyResult(createdPersonPartyID));

		final var result = partyService.findAndAddPartyToCase(caseDTO, "someCaseId");

		assertThat(result).isNotNull().isNotEmpty();
		assertThat(result.getFirst().get(createdPersonPartyID)).isNotNull().satisfies(party -> assertThat(party.getGuid()).isNotEmpty());

	}

}
