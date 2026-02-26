package se.sundsvall.casemanagement.integration.party;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.problem.Problem;

import static generated.client.party.PartyType.ENTERPRISE;
import static generated.client.party.PartyType.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casemanagement.integration.party.PartyIntegration.INVALID_PARTY_ID;

@ExtendWith(MockitoExtension.class)
class PartyIntegrationTest {

	private static final String PARTY_ID = "partyId";
	private static final String MUNICIPALITY_ID = "municipalityId";
	private static final String LEGAL_ID = "legalId";

	@Mock
	private PartyClient partyClientMock;

	@InjectMocks
	private PartyIntegration partyIntegration;

	@Test
	void getLegalIdByPartyIdPrivateFound() {
		when(partyClientMock.getLegalIdByPartyId(MUNICIPALITY_ID, PRIVATE, PARTY_ID)).thenReturn(Optional.of(LEGAL_ID));

		final var result = partyIntegration.getLegalIdByPartyId(MUNICIPALITY_ID, PARTY_ID);

		assertThat(result).containsOnlyKeys(PRIVATE).containsEntry(PRIVATE, LEGAL_ID);
		verify(partyClientMock).getLegalIdByPartyId(MUNICIPALITY_ID, PRIVATE, PARTY_ID);
		verifyNoMoreInteractions(partyClientMock);
	}

	@Test
	void getLegalIdByPartyIdEnterpriseFound() {
		when(partyClientMock.getLegalIdByPartyId(MUNICIPALITY_ID, PRIVATE, PARTY_ID)).thenReturn(Optional.empty());
		when(partyClientMock.getLegalIdByPartyId(MUNICIPALITY_ID, ENTERPRISE, PARTY_ID)).thenReturn(Optional.of(LEGAL_ID));

		final var result = partyIntegration.getLegalIdByPartyId(MUNICIPALITY_ID, PARTY_ID);

		assertThat(result).containsOnlyKeys(ENTERPRISE).containsEntry(ENTERPRISE, LEGAL_ID);
		verify(partyClientMock).getLegalIdByPartyId(MUNICIPALITY_ID, PRIVATE, PARTY_ID);
		verify(partyClientMock).getLegalIdByPartyId(MUNICIPALITY_ID, ENTERPRISE, PARTY_ID);
		verifyNoMoreInteractions(partyClientMock);
	}

	@Test
	void getLegalIdByPartyIdBadRequest() {
		when(partyClientMock.getLegalIdByPartyId(MUNICIPALITY_ID, PRIVATE, PARTY_ID)).thenReturn(Optional.empty());
		when(partyClientMock.getLegalIdByPartyId(MUNICIPALITY_ID, ENTERPRISE, PARTY_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> partyIntegration.getLegalIdByPartyId(MUNICIPALITY_ID, PARTY_ID))
			.isInstanceOf(Problem.class)
			.hasMessageContaining(INVALID_PARTY_ID.formatted(PARTY_ID));

		verify(partyClientMock).getLegalIdByPartyId(MUNICIPALITY_ID, PRIVATE, PARTY_ID);
		verify(partyClientMock).getLegalIdByPartyId(MUNICIPALITY_ID, ENTERPRISE, PARTY_ID);
		verifyNoMoreInteractions(partyClientMock);
	}

	@Test
	void getLegalIdByPartyIdNullPartyId() {

		final var result = partyIntegration.getLegalIdByPartyId(MUNICIPALITY_ID, null);
		assertThat(result).isEmpty();
	}
}
