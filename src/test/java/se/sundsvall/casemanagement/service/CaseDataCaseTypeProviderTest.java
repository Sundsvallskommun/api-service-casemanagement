package se.sundsvall.casemanagement.service;

import generated.client.casedata.CaseType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casemanagement.integration.casedata.CaseDataClient;
import se.sundsvall.dept44.exception.ClientProblem;
import se.sundsvall.dept44.exception.ServerProblem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class CaseDataCaseTypeProviderTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String NAMESPACE = "SBK_PARKING";
	private static final ServerProblem CASE_DATA_UNAVAILABLE = new ServerProblem(BAD_GATEWAY, "case-data error: {status=500 Internal Server Error}");

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

	@Test
	void getCaseDataTypesByNamespace_whenCaseDataFailsAfterSuccessfulFetch_returnsLastFetchedTypes() {
		when(caseDataClient.getCaseTypes(MUNICIPALITY_ID, NAMESPACE))
			.thenReturn(List.of(new CaseType().type("PARKING_PERMIT").displayName("Parking Permit")))
			.thenThrow(CASE_DATA_UNAVAILABLE);

		caseDataCaseTypeProvider.getCaseDataTypesByNamespace(MUNICIPALITY_ID, NAMESPACE);
		final var result = caseDataCaseTypeProvider.getCaseDataTypesByNamespace(MUNICIPALITY_ID, NAMESPACE);

		assertThat(result).containsExactlyEntriesOf(Map.of("PARKING_PERMIT", "Parking Permit"));
		verify(caseDataClient, times(2)).getCaseTypes(MUNICIPALITY_ID, NAMESPACE);
	}

	@Test
	void getCaseDataTypesByNamespace_whenCaseDataFails_returnsTypesFromLatestSuccessfulFetch() {
		when(caseDataClient.getCaseTypes(MUNICIPALITY_ID, NAMESPACE))
			.thenReturn(List.of(new CaseType().type("PARKING_PERMIT").displayName("Parking Permit")))
			.thenReturn(List.of(new CaseType().type("LOST_PARKING_PERMIT").displayName("Lost Parking Permit")))
			.thenThrow(CASE_DATA_UNAVAILABLE);

		caseDataCaseTypeProvider.getCaseDataTypesByNamespace(MUNICIPALITY_ID, NAMESPACE);
		caseDataCaseTypeProvider.getCaseDataTypesByNamespace(MUNICIPALITY_ID, NAMESPACE);
		final var result = caseDataCaseTypeProvider.getCaseDataTypesByNamespace(MUNICIPALITY_ID, NAMESPACE);

		assertThat(result).containsOnlyKeys("LOST_PARKING_PERMIT");
	}

	@Test
	void getCaseDataTypesByNamespace_whenCaseDataFailsForNamespaceNeverFetched_propagatesException() {
		final var otherNamespace = "SBK_MEX";
		when(caseDataClient.getCaseTypes(MUNICIPALITY_ID, NAMESPACE))
			.thenReturn(List.of(new CaseType().type("PARKING_PERMIT").displayName("Parking Permit")));
		when(caseDataClient.getCaseTypes(MUNICIPALITY_ID, otherNamespace)).thenThrow(CASE_DATA_UNAVAILABLE);

		caseDataCaseTypeProvider.getCaseDataTypesByNamespace(MUNICIPALITY_ID, NAMESPACE);

		assertThatThrownBy(() -> caseDataCaseTypeProvider.getCaseDataTypesByNamespace(MUNICIPALITY_ID, otherNamespace))
			.isSameAs(CASE_DATA_UNAVAILABLE);
	}

	@Test
	void getCaseDataTypesByNamespace_whenClientProblemAfterSuccessfulFetch_propagatesException() {
		final var clientProblem = new ClientProblem(NOT_FOUND, "case-data error: {status=404 Not Found}");
		when(caseDataClient.getCaseTypes(MUNICIPALITY_ID, NAMESPACE))
			.thenReturn(List.of(new CaseType().type("PARKING_PERMIT").displayName("Parking Permit")))
			.thenThrow(clientProblem);

		caseDataCaseTypeProvider.getCaseDataTypesByNamespace(MUNICIPALITY_ID, NAMESPACE);

		assertThatThrownBy(() -> caseDataCaseTypeProvider.getCaseDataTypesByNamespace(MUNICIPALITY_ID, NAMESPACE))
			.isSameAs(clientProblem);
	}

}
