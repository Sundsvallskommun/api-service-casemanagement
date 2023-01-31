package se.sundsvall.casemanagement.integration.db.model;

import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Random;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.hamcrest.CoreMatchers.allOf;

class CaseMappingIdTest {

    @BeforeAll
    static void setup() {
        registerValueGenerator(() -> LocalDateTime.now().plusDays(new Random().nextInt()), LocalDateTime.class);
    }

    @Test
    void testBean() {
        MatcherAssert.assertThat(CaseMappingId.class, allOf(
                hasValidBeanConstructor(),
                hasValidGettersAndSetters(),
                hasValidBeanHashCode(),
                hasValidBeanEquals(),
                hasValidBeanToString()));
    }

    @Test
    void testFields() {
        CaseMappingId object = new CaseMappingId();
        object.setCaseId(String.valueOf(new Random().nextInt()));
        object.setExternalCaseId(String.valueOf(new Random().nextInt()));

        Assertions.assertThat(object).isNotNull().hasNoNullFieldsOrProperties();
    }

}
