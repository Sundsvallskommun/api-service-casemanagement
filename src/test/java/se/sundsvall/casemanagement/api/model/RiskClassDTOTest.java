package se.sundsvall.casemanagement.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.LocalDateTime;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class RiskClassDTOTest {
    
    @Test
    void testBean() {
        MatcherAssert.assertThat(RiskClassDTO.class, allOf(
            hasValidBeanConstructor(),
            hasValidGettersAndSetters(),
            hasValidBeanHashCode(),
            hasValidBeanEquals(),
            hasValidBeanToString()));
        MatcherAssert.assertThat(RiskClassDTO.ActivitesDTO.class, allOf(
            hasValidBeanConstructor(),
            hasValidGettersAndSetters(),
            hasValidBeanHashCode(),
            hasValidBeanEquals(),
            hasValidBeanToString()));
        MatcherAssert.assertThat(RiskClassDTO.ThirdPartyCertifications.class, allOf(
            hasValidBeanConstructor(),
            hasValidGettersAndSetters(),
            hasValidBeanHashCode(),
            hasValidBeanEquals(),
            hasValidBeanToString()));
        MatcherAssert.assertThat(RiskClassDTO.ProductGroups.class, allOf(
            hasValidBeanConstructor(),
            hasValidGettersAndSetters(),
            hasValidBeanHashCode(),
            hasValidBeanEquals(),
            hasValidBeanToString()));
        
    }
    
    @Test
    void testFields() {
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
        
        assertThat(dto).isNotNull().hasNoNullFieldsOrProperties();
        assertThat(dto.getActivities().get(0)).hasNoNullFieldsOrProperties();
        assertThat(dto.getProductGroups().get(0)).hasNoNullFieldsOrProperties();
    assertThat(dto.getThirdPartyCertifications().get(0)).hasNoNullFieldsOrProperties();
    }
    
}