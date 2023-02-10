package se.sundsvall.casemanagement.api.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class RiskClassDTO {
    
    private String caseId;
    private String facilityId;
    private Boolean isMobileFacility;
    private Boolean isSeasonal;
    private String mainOrientationId;
    private String mainOrientationSlvCode;
    private String mobileFacilityNote;
    private String productionSizeId;
    private String productionSizeSlvCode;
    private String seasonalNote;
    private List<ActivitesDTO> activities;
    private List<ProductGroups> productGroups;
    private List<ThirdPartyCertifications> thirdPartyCertifications;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(setterPrefix = "with")
    public static class ActivitesDTO {
        protected String activityId;
        protected String slvCode;
        protected String startDate;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(setterPrefix = "with")
    public static class ProductGroups {
        private String productGroupId;
        private String slvCode;
        
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(setterPrefix = "with")
    public static class ThirdPartyCertifications {
        private String thirdPartyCertificationId;
        private String thirdPartyCertificationText;
    }
}
