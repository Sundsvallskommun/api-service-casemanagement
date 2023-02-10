package se.sundsvall.casemanagement.service.ecos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import se.sundsvall.casemanagement.api.model.EnvironmentalCaseDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.integration.soap.minutmiljo.MinutMiljoClient;

import minutmiljo.AddFacilityToCase;
import minutmiljo.ArrayOfFacilityFilterSvcDto;
import minutmiljo.ArrayOfSaveRiskClass2024ActivityDto;
import minutmiljo.ArrayOfSaveRiskClass2024CertificationDto;
import minutmiljo.ArrayOfSaveRiskClass2024ProductGroupDto;
import minutmiljo.CalculateRiskClass2024;
import minutmiljo.CalculateRiskClass2024SvcDto;
import minutmiljo.FacilityPartyOrganizationNumberFilterSvcDto;
import minutmiljo.GetRiskClass2024BaseData;
import minutmiljo.GetRiskClass2024BaseDataResponse;
import minutmiljo.SaveFoodFacility2024RiskClassData;
import minutmiljo.SaveRiskClass2024ActivityDto;
import minutmiljo.SaveRiskClass2024CertificationDto;
import minutmiljo.SaveRiskClass2024DataDto;
import minutmiljo.SaveRiskClass2024ProductGroupDto;
import minutmiljo.SearchFacility;
import minutmiljo.SearchFacilitySvcDto;

@Service
public class RiskClassService {
    
    private final MinutMiljoClient minutMiljoClient;
    
    private final static String MAIN_ORIENTATION_ID="MainOrientationId";
    private final static String PROD_SIZE_ID="ProductionSizeId";
    private final static String IS_SEASONAL="IsSeasonal";
    private final static String SEASONAL_NOTE="seasonalNote";
    private final static String ACTIVITIES="activities";
    private final static String PRODUCT_GROUPS="productGroups";
    private final static String THIRD_PARTY_CERTS="thirdPartyCertifications";
    
    public RiskClassService(MinutMiljoClient minutMiljoClient) {
        this.minutMiljoClient = minutMiljoClient;
    }
    
    
    public String updateRiskClass(EnvironmentalCaseDTO caseInput, String caseId) {
        var facilityId = searchFacility(extractOrgNr(caseInput));
        addFacilityToCase(facilityId, caseId);
        var data = createSaveRiskClassObject(facilityId, caseId, caseInput);
        
        minutMiljoClient.updateRiskClass(data);
        return null;
        
    }
    
    public GetRiskClass2024BaseDataResponse getBaseRiskData() {
       return minutMiljoClient.getRiskklasses(new GetRiskClass2024BaseData());
    }
    
    private String extractOrgNr(EnvironmentalCaseDTO eCase) {
        
        return eCase.getStakeholders().stream()
            .map(stakeholderDTO -> {
                if (stakeholderDTO instanceof OrganizationDTO orgDTO) {
                    return orgDTO.getOrganizationNumber();
                } else return "";
            })
            .findFirst()
            .orElse("");
    }
    
    private String searchFacility(String orgNr) {
        return minutMiljoClient.searchFacility(new SearchFacility()
            .withSearchFacilitySvcDto(new SearchFacilitySvcDto()
                .withFacilityFilters(new ArrayOfFacilityFilterSvcDto()
                    .withFacilityFilterSvcDto(new FacilityPartyOrganizationNumberFilterSvcDto()
                        .withOrganizationNumber(orgNr)
                    ))))
            .getSearchFacilityResult()
            .getSearchFacilityResultSvcDto()
            .get(0)
            .getFacilityId();
    }
    
    private void addFacilityToCase(String facilityId, String caseId) {
        minutMiljoClient.addFacilityToCase(new AddFacilityToCase()
            .withFacilityId(facilityId)
            .withCaseId(caseId));
        
    }
    
    private SaveFoodFacility2024RiskClassData createSaveRiskClassObject(String facilityId,
        String caseId, EnvironmentalCaseDTO dto) {
        
        return new SaveFoodFacility2024RiskClassData()
            .withModel(new SaveRiskClass2024DataDto()
                
                .withCaseId(caseId)
                .withFacilityId(facilityId)
                .withMainOrientationSlvCode(dto.getExtraParameters().get(MAIN_ORIENTATION_ID))
                .withProductionSizeSlvCode(dto.getExtraParameters().get(PROD_SIZE_ID))
                .withIsSeasonal(Optional.ofNullable(dto.getExtraParameters().get(IS_SEASONAL)).orElse("").equalsIgnoreCase("true"))
                .withSeasonalNote(dto.getExtraParameters().get(SEASONAL_NOTE))
                .withActivities(mapActivities(List.of(Optional.ofNullable(dto.getExtraParameters().get(ACTIVITIES)).orElse("").split(","))))
                .withProductGroups(mapProductGroups(List.of(Optional.ofNullable(dto.getExtraParameters().get(PRODUCT_GROUPS)).orElse("").split(","))))
                .withThirdPartyCertifications(mapThirdPartyCertifications(List.of(Optional.ofNullable(dto.getExtraParameters().get(THIRD_PARTY_CERTS)).orElse("").split(",")))));
    }
    
    
    //TODO WIP
    private ArrayOfSaveRiskClass2024ActivityDto mapActivities(List<String> activities) {
        return new ArrayOfSaveRiskClass2024ActivityDto()
            .withSaveRiskClass2024ActivityDto(activities.stream()
                .map(activityDto -> new SaveRiskClass2024ActivityDto()
                    .withSlvCode(activityDto)
                ).toList());
    }
    //TODO WIP
    private ArrayOfSaveRiskClass2024ProductGroupDto mapProductGroups(List<String> productGroupIds) {
        return new ArrayOfSaveRiskClass2024ProductGroupDto()
            .withSaveRiskClass2024ProductGroupDto(productGroupIds.stream()
                .map(productGroupId -> new SaveRiskClass2024ProductGroupDto()
                    .withSlvCode(productGroupId))
                .toList());
    }
    //TODO WIP
    private ArrayOfSaveRiskClass2024CertificationDto mapThirdPartyCertifications(List<String> dtos) {
        
        return new ArrayOfSaveRiskClass2024CertificationDto()
            .withSaveRiskClass2024CertificationDto(
                dtos.stream()
                    .map(dto -> new SaveRiskClass2024CertificationDto()
                        .withThirdPartyCertificationId(dto)
                        .withThirdPartyCertificationText(dto))
                    .toList());
    }
    
    
}
