package se.sundsvall.casemanagement.api.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.sundsvall.casemanagement.TestUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class PlanningPermissionFacilityDTOTest {

    @BeforeAll
    static void setup() {
        registerValueGenerator(() -> LocalDateTime.now().plusDays(new Random().nextInt()), LocalDateTime.class);
        registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt()), LocalDate.class);
    }

    @Test
    void testBean() {
        MatcherAssert.assertThat(PlanningPermissionFacilityDTO.class, allOf(
                hasValidBeanConstructor(),
                hasValidGettersAndSetters(),
                hasValidBeanHashCode(),
                hasValidBeanEquals(),
                hasValidBeanToString()));
    }

    @Test
    void testFields() {
        PlanningPermissionFacilityDTO dto = TestUtil.createPlanningPermissionFacilityDTO(false);
        assertThat(dto).isNotNull().hasNoNullFieldsOrProperties();
    }

}
