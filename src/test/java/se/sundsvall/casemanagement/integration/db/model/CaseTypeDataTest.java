package se.sundsvall.casemanagement.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.LocalDateTime;
import java.util.Random;

import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

class CaseTypeDataTest {
    
    
    @BeforeAll
    static void setup() {
        registerValueGenerator(() -> LocalDateTime.now().plusDays(new Random().nextInt()), LocalDateTime.class);
    }
    
    @Test
    void testBean() {
        MatcherAssert.assertThat(CaseTypeData.class, allOf(
            hasValidBeanConstructor(),
            hasValidGettersAndSetters(),
            hasValidBeanHashCode(),
            hasValidBeanEquals(),
            hasValidBeanToString()));
    }
    
    @Test
    void testFields() {
        var object = new CaseTypeData(
            RandomStringUtils.random(5),
            RandomStringUtils.random(5),
            RandomStringUtils.random(5),
            RandomStringUtils.random(5),
            RandomStringUtils.random(5),
            RandomStringUtils.random(5),
            RandomStringUtils.random(5),
            RandomStringUtils.random(5));
        
        Assertions.assertThat(object).isNotNull().hasNoNullFieldsOrProperties();
    }
    
}