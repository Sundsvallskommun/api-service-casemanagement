package se.sundsvall.casemanagement.integration.fb.model;

import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.allOf;

class FbPropertyInfoTest {
    @Test
    void testBean() {
        MatcherAssert.assertThat(FbPropertyInfo.class, allOf(
                hasValidBeanConstructor(),
                hasValidGettersAndSetters(),
                hasValidBeanHashCode(),
                hasValidBeanEquals(),
                hasValidBeanToString()));
    }

    @Test
    void testFields() {
        FbPropertyInfo object = new FbPropertyInfo();
        object.setAdressplatsId(new Random().nextInt());
        object.setFnr(new Random().nextInt());

        Assertions.assertThat(object).isNotNull().hasNoNullFieldsOrProperties();
    }
}
