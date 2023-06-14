package se.sundsvall.casemanagement.integration.db.model;

import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
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
import static org.hamcrest.CoreMatchers.allOf;
import static se.sundsvall.casemanagement.TestUtil.getRandomOfEnum;

class CaseMappingTest {

    @BeforeAll
    static void setup() {
        registerValueGenerator(() -> LocalDateTime.now().plusDays(new Random().nextInt()), LocalDateTime.class);
    }

    @Test
    void testBean() {
        MatcherAssert.assertThat(CaseMapping.class, allOf(
                hasValidBeanConstructor(),
                hasValidGettersAndSetters(),
                hasValidBeanHashCode(),
                hasValidBeanEquals(),
                hasValidBeanToString()));
    }

    @Test
    void testFields() {
        CaseMapping object = new CaseMapping(
                RandomStringUtils.random(5),
                RandomStringUtils.random(5),
                (SystemType) getRandomOfEnum(SystemType.class),
                (CaseType) getRandomOfEnum(CaseType.class),
                RandomStringUtils.random(5));

        object.setTimestamp(LocalDateTime.now());

        Assertions.assertThat(object).isNotNull().hasNoNullFieldsOrProperties();
    }

}
