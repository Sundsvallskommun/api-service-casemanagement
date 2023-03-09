package se.sundsvall.casemanagement.service.ecos;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.casemanagement.api.model.EnvironmentalCaseDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.integration.soap.minutmiljo.MinutMiljoClient;
import se.sundsvall.casemanagement.service.util.CaseUtil;

import minutmiljo.AddFacilityToCase;
import minutmiljo.ArrayOfFacilityFilterSvcDto;
import minutmiljo.ArrayOfSaveRiskClass2024ActivityDto;
import minutmiljo.ArrayOfSaveRiskClass2024CertificationDto;
import minutmiljo.ArrayOfSaveRiskClass2024ProductGroupDto;
import minutmiljo.ArrayOfguid;
import minutmiljo.FacilityFacilityStatusIdsFilterSvcDto;
import minutmiljo.FacilityFacilityTypeIdsFilterSvcDto;
import minutmiljo.FacilityNotFilterSvcDto;
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
    
    private final static String MAIN_ORIENTATION_ID = "MainOrientationId";
    private final static String PROD_SIZE_ID = "ProductionSizeId";
    private final static String IS_SEASONAL = "IsSeasonal";
    private final static String SEASONAL_NOTE = "seasonalNote";
    //Since there is too many activities for one extra parameter (on the openE side)
    // we need to split it up indifferent extra parameters.
    private final static List<String> ACTIVITIES = List.of("activities", "activities2", "activities3", "activities4");
    private final static String PRODUCT_GROUPS = "productGroups";
    private final static String THIRD_PARTY_CERTS = "thirdPartyCertifications";
    private final MinutMiljoClient minutMiljoClient;
    
    public RiskClassService(MinutMiljoClient minutMiljoClient) {
        this.minutMiljoClient = minutMiljoClient;
    }
    
    public void updateRiskClass(EnvironmentalCaseDTO caseInput, String caseId) {
        var facilityId = searchFacility(extractOrgNr(caseInput));
        addFacilityToCase(facilityId, caseId);
        var data = createSaveRiskClassObject(facilityId, caseId, caseInput);
        minutMiljoClient.updateRiskClass(data);
    }
    
    public GetRiskClass2024BaseDataResponse getBaseRiskData() {
        return minutMiljoClient.getRiskklasses(new GetRiskClass2024BaseData());
    }
    
    private String extractOrgNr(EnvironmentalCaseDTO eCase) {
        return CaseUtil.getSokigoFormattedOrganizationNumber(eCase.getStakeholders().stream()
            .map(stakeholderDTO -> {
                if (stakeholderDTO instanceof OrganizationDTO orgDTO) {
                    return orgDTO.getOrganizationNumber();
                } else return "";
            })
            .findFirst()
            .orElse(""));
    }
    
    private String searchFacility(String orgNr) {
        
        
        var facilityTypeFilter = new FacilityFacilityTypeIdsFilterSvcDto()
            .withFacilityTypeIds("4958BC00-76E8-4D5B-A862-AAF8E815202A");
        
        var facilityStatusFilter =
            new FacilityFacilityStatusIdsFilterSvcDto()
                .withFacilityStatusIds(new ArrayOfguid().withGuid(
                    "88E11CAA-DF35-4C5E-94A8-3C7B0369D8F2", //Anmäld/Ansökt
                    "64B2DB7A-9A11-4F20-A57C-8122B1A469E6", // Inaktiv
                    "D203BB33-EB9A-4679-8E1C-BBD8AF86E554", //Aktiv
                    "C5A98B2B-C2B8-428E-B597-A3F97A77B818" ///Beviljad
                ));
        
        var notFacilityStatusFilters = new FacilityNotFilterSvcDto()
            .withFilter(new FacilityFacilityStatusIdsFilterSvcDto()
                .withFacilityStatusIds(new ArrayOfguid()
                    .withGuid(List.of("9A748E4E-BD7E-481A-B449-73CBD0992213", //Upphörd/Skrotad
                        "80FFA45C-B3DF-4A10-8DB3-A042F36C64B7")))); // Makulerad
        
        var orgFilter = new FacilityPartyOrganizationNumberFilterSvcDto()
            .withOrganizationNumber(orgNr);
        
        var result = Optional.ofNullable(minutMiljoClient
                .searchFacility(new SearchFacility().withSearchFacilitySvcDto(new SearchFacilitySvcDto()
                    .withFacilityFilters(new ArrayOfFacilityFilterSvcDto()
                        .withFacilityFilterSvcDto(facilityStatusFilter, facilityTypeFilter, notFacilityStatusFilters, orgFilter))))
                .getSearchFacilityResult()
                .getSearchFacilityResultSvcDto())
            .orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, "Could not find facility "));
        
        return result.get(0).getFacilityId();
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
                .withActivities(mapActivities(dto))
                .withProductGroups(mapProductGroups(dto.getExtraParameters().get(PRODUCT_GROUPS)))
                .withThirdPartyCertifications(mapThirdPartyCertifications(dto.getExtraParameters().get(THIRD_PARTY_CERTS))));
    }
    
    private ArrayOfSaveRiskClass2024ActivityDto mapActivities(EnvironmentalCaseDTO dto) {
        StringBuilder activityString = new StringBuilder();
        
        for (var activityParam : ACTIVITIES) {
            
            activityString.append(Optional.ofNullable(dto.getExtraParameters().get(activityParam)).orElse(" ")).append(
                ",");
        }
        
        if (activityString.length() == 0) {
            return null;
        }
        var activities = splitString(activityString.toString());
        
        
        return new ArrayOfSaveRiskClass2024ActivityDto()
            .withSaveRiskClass2024ActivityDto(activities.stream()
                .map(ActivitySlv -> new SaveRiskClass2024ActivityDto()
                    .withSlvCode(ActivitySlv))
                .filter(activityDto -> !activityDto.getSlvCode().isEmpty())
                .toList());
    }
    
    private ArrayOfSaveRiskClass2024ProductGroupDto mapProductGroups(String productGroupIdString) {
        if (productGroupIdString == null) {
            return null;
        }
        
        var productGroupIds = splitString(productGroupIdString);
        
        if (productGroupIds.get(0).isEmpty()) {
            return null;
        }
        return new ArrayOfSaveRiskClass2024ProductGroupDto()
            .withSaveRiskClass2024ProductGroupDto(productGroupIds.stream()
                .map(productGroupId -> new SaveRiskClass2024ProductGroupDto()
                    .withSlvCode(productGroupId))
                .toList());
    }
    
    private ArrayOfSaveRiskClass2024CertificationDto mapThirdPartyCertifications(String thirdPartyCertString) {
        if (thirdPartyCertString == null) {
            return null;
        }
        var dtos = splitString(thirdPartyCertString);
        
        if (dtos.get(0).isEmpty()) {
            return null;
        }
        return new ArrayOfSaveRiskClass2024CertificationDto()
            .withSaveRiskClass2024CertificationDto(
                dtos.stream()
                    .map(dto -> new SaveRiskClass2024CertificationDto()
                        .withThirdPartyCertificationText(dto))
                    .toList());
    }
    
    private List<String> splitString(String string) {
        return Arrays.stream(string.split(" "))
            .map(s -> s.split(","))
            .flatMap(Arrays::stream)
            .toList();
    }
    
    
}
