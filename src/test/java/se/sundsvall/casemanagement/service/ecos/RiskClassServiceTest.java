package se.sundsvall.casemanagement.service.ecos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.casemanagement.api.model.EnvironmentalCaseDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.api.model.RiskClassDTO;
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
        stakeholder.setOrganizationNumber("someOrgnr");
        dto.setStakeholders(List.of(stakeholder));
        dto.setExtraParameters(Map.of("", ""));
        
        var result = service.updateRiskClass(dto, "someCaseId");
        
        assertThat(result).isNull();
        
        verify(minutMiljoClient, times(1)).searchFacility(any());
        verify(minutMiljoClient, times(1)).addFacilityToCase(any());
        verify(minutMiljoClient, times(1)).updateRiskClass(any());
        verifyNoMoreInteractions(minutMiljoClient);
    }
    
    
    @Test
    void saveRiskClass(){
        
        RiskClassDTO dto = RiskClassDTO.builder()
            .withActivities(List.of(RiskClassDTO.ActivitesDTO.builder().withActivityId(
                "someActivityId").withSlvCode("someSlvCode").withStartDate(LocalDateTime.now().toString()).build()))
            .withThirdPartyCertifications(List.of(RiskClassDTO.ThirdPartyCertifications.builder().withThirdPartyCertificationId("someId").withThirdPartyCertificationText("someText").build()))
            .withFacilityId("someId")
            .withIsSeasonal(true)
            .withCaseId("someCaseId")
            .withIsMobileFacility(true)
            .withMobileFacilityNote("someNote")
            .withProductGroups(List.of(RiskClassDTO.ProductGroups.builder().withProductGroupId(
                "someProductGroupId").withSlvCode("someSlvCode").build()))
            .withMainOrientationId("someOrientation")
            .withProductionSizeSlvCode("someSize")
            .withSeasonalNote("someNote")
            .withProductionSizeId("someProductionSizeId")
            .withMainOrientationSlvCode("someMainOrientationSlvCode")
            .build();
        
         service.saveRiskClass(dto);
    
        verify(minutMiljoClient, times(1)).updateRiskClass(any());
        verifyNoMoreInteractions(minutMiljoClient);
    }
    
}