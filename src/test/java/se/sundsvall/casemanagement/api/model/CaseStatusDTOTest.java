package se.sundsvall.casemanagement.api.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
import se.sundsvall.casemanagement.TestUtil;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.SystemType;

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

class CaseStatusDTOTest {

    @BeforeAll
    static void setup() {
        registerValueGenerator(() -> LocalDateTime.now().plusDays(new Random().nextInt()), LocalDateTime.class);
    }

    @Test
    void testBean() {
        MatcherAssert.assertThat(CaseStatusDTO.class, allOf(
                hasValidBeanConstructor(),
                hasValidGettersAndSetters(),
                hasValidBeanHashCode(),
                hasValidBeanEquals(),
                hasValidBeanToString()));
    }

    @Test
    void testFields() {
        CaseStatusDTO dto = new CaseStatusDTO();
        dto.setStatus(RandomStringUtils.random(10));
        dto.setCaseId(RandomStringUtils.random(10));
        dto.setExternalCaseId(RandomStringUtils.random(10));
        dto.setServiceName(RandomStringUtils.random(10));
        dto.setCaseType((CaseType) TestUtil.getRandomOfEnum(CaseType.class));
        dto.setSystem((SystemType) TestUtil.getRandomOfEnum(SystemType.class));
        dto.setTimestamp(LocalDateTime.now());

        assertThat(dto).isNotNull().hasNoNullFieldsOrProperties();
    }

}
