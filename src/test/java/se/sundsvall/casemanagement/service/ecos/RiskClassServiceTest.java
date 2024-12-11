package se.sundsvall.casemanagement.service.ecos;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import minutmiljo.ArrayOfSearchFacilityResultSvcDto;
import minutmiljo.SearchFacilityResponse;
import minutmiljo.SearchFacilityResultSvcDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casemanagement.api.model.EcosCaseDTO;
import se.sundsvall.casemanagement.api.model.FacilityDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.integration.ecos.MinutMiljoClient;
import se.sundsvall.casemanagement.integration.ecos.RiskClassService;

@ExtendWith(MockitoExtension.class)
class RiskClassServiceTest {

	@Mock
	private MinutMiljoClient minutMiljoClient;

	@InjectMocks
	private RiskClassService service;

	@Test
	void updateRiskClass() {

		final var facility = new FacilityDTO();
		facility.setFacilityCollectionName("someFacilityName");

		when(minutMiljoClient.searchFacility(any()))
			.thenReturn(new SearchFacilityResponse()
				.withSearchFacilityResult(new ArrayOfSearchFacilityResultSvcDto()
					.withSearchFacilityResultSvcDto(new SearchFacilityResultSvcDto()
						.withFacilityName("someFacilityName")
						.withFacilityCollectionName("someFacilityName")
						.withFacilityId("someFacilityId"))));

		final var dto = getEnvironmentalCaseDTO(facility, Map.of("activities", ", , , , , , SLHA003, SLHA004, , , SLUA003, SLUA033, SLUA001, SLUA002, , , , , , , , , , , , , SLUA014, , , , , , , , , , , , , , ,",
			"fixedFacilityType", "Riskklassning",
			"IsSeasonal", "false",
			"MainOrientationId", "SLI",
			"serviceName", "someTest"

		));

		service.updateRiskClass(dto, "someCaseId");

		verify(minutMiljoClient, times(1)).searchFacility(any());
		verify(minutMiljoClient, times(1)).addFacilityToCase(any());
		verify(minutMiljoClient, times(1)).updateRiskClass(any());
		verifyNoMoreInteractions(minutMiljoClient);
	}

	private static EcosCaseDTO getEnvironmentalCaseDTO(final FacilityDTO facility, final Map<String, String> extraParam) {
		final var dto = new EcosCaseDTO();
		final var stakeholder = new OrganizationDTO();
		stakeholder.setOrganizationNumber("123456-7890");
		dto.setFacilities(List.of(facility));
		dto.setStakeholders(List.of(stakeholder));
		dto.setExtraParameters(extraParam);
		return dto;
	}

	@Test
	void updateRiskClass_empty_activites() {

		final var facility = new FacilityDTO();
		facility.setFacilityCollectionName("someFacilityName");

		when(minutMiljoClient.searchFacility(any()))
			.thenReturn(new SearchFacilityResponse()
				.withSearchFacilityResult(new ArrayOfSearchFacilityResultSvcDto()
					.withSearchFacilityResultSvcDto(new SearchFacilityResultSvcDto()
						.withFacilityName("someFacilityName")
						.withFacilityCollectionName("someFacilityName")
						.withFacilityId("someFacilityId"))));

		final var dto = getEnvironmentalCaseDTO(facility, Map.of());

		service.updateRiskClass(dto, "someCaseId");

		verify(minutMiljoClient, times(1)).searchFacility(any());
		verify(minutMiljoClient, times(1)).addFacilityToCase(any());
		verify(minutMiljoClient, times(1)).updateRiskClass(any());
		verifyNoMoreInteractions(minutMiljoClient);
	}

}
