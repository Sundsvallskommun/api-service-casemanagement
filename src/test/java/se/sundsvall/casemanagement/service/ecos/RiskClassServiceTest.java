package se.sundsvall.casemanagement.service.ecos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.casemanagement.api.model.EnvironmentalCaseDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.integration.soap.minutmiljo.MinutMiljoClient;

import minutmiljo.ArrayOfSearchFacilityResultSvcDto;
import minutmiljo.SearchFacilityResponse;
import minutmiljo.SearchFacilityResultSvcDto;

@ExtendWith(MockitoExtension.class)
class RiskClassServiceTest {
    @Mock
    MinutMiljoClient minutMiljoClient;
    
    @InjectMocks
    RiskClassService service;
    
    @Test
    void updateRiskClass() {
        
        when(minutMiljoClient.searchFacility(any()))
            .thenReturn(new SearchFacilityResponse()
                .withSearchFacilityResult(new ArrayOfSearchFacilityResultSvcDto()
                    .withSearchFacilityResultSvcDto(new SearchFacilityResultSvcDto()
                        .withFacilityId("someFacilityId"))));
        
        var dto = new EnvironmentalCaseDTO();
        var stakeholder = new OrganizationDTO();
        stakeholder.setOrganizationNumber("123456-7890");
        dto.setStakeholders(List.of(stakeholder));
        dto.setExtraParameters(Map.of("activities", "1,1, 1","activities2", "2,2, 2",
            "activities3", "3,3, 3",
            "productGroups", "123, 123, 123",
            "thirdPartyCertifications", "12, 123,12"));
        
         service.updateRiskClass(dto, "someCaseId");
        
        
        verify(minutMiljoClient, times(1)).searchFacility(any());
        verify(minutMiljoClient, times(1)).addFacilityToCase(any());
        verify(minutMiljoClient, times(1)).updateRiskClass(any());
        verifyNoMoreInteractions(minutMiljoClient);
    }
    
}