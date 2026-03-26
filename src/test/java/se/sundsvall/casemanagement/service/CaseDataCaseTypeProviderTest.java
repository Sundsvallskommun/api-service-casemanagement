package se.sundsvall.casemanagement.service;

import generated.client.casedata.CaseType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casemanagement.integration.casedata.CaseDataClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseDataCaseTypeProviderTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String NAMESPACE = "SBK_PARKING";

	@Mock
	private CaseDataClient caseDataClient;

	@InjectMocks
	private CaseDataCaseTypeProvider caseDataCaseTypeProvider;

	@Test
	void getCaseDataTypesByNamespace_returnsTypes() {
		final var caseType1 = new CaseType().type("PARKING_PERMIT").displayName("Parking Permit");
		final var caseType2 = new CaseType().type("LOST_PARKING_PERMIT").displayName("Lost Parking Permit");
		when(caseDataClient.getCaseTypes(MUNICIPALITY_ID, NAMESPACE)).thenReturn(List.of(caseType1, caseType2));

		final var result = caseDataCaseTypeProvider.getCaseDataTypesByNamespace(MUNICIPALITY_ID, NAMESPACE);

		assertThat(result).hasSize(2)
			.containsEntry("PARKING_PERMIT", "Parking Permit")
			.containsEntry("LOST_PARKING_PERMIT", "Lost Parking Permit");
		verify(caseDataClient).getCaseTypes(MUNICIPALITY_ID, NAMESPACE);
	}

	@Test
	void getCaseDataTypesByNamespace_withEmptyResult_returnsEmptyMap() {
		when(caseDataClient.getCaseTypes(MUNICIPALITY_ID, NAMESPACE)).thenReturn(List.of());

		final var result = caseDataCaseTypeProvider.getCaseDataTypesByNamespace(MUNICIPALITY_ID, NAMESPACE);

		assertThat(result).isEmpty();
		verify(caseDataClient).getCaseTypes(MUNICIPALITY_ID, NAMESPACE);
	}

	@Test
	void getCaseDataTypesByNamespace_whenClientThrows_propagatesException() {
		when(caseDataClient.getCaseTypes(MUNICIPALITY_ID, NAMESPACE)).thenThrow(new RuntimeException("Connection refused"));

		assertThatThrownBy(() -> caseDataCaseTypeProvider.getCaseDataTypesByNamespace(MUNICIPALITY_ID, NAMESPACE))
			.isInstanceOf(RuntimeException.class)
			.hasMessage("Connection refused");
	}

}
