package se.sundsvall.casemanagement.integration.citizenmapping.model;

import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.allOf;

class CitizenMappingErrorResponseTest {

    @Test
    void testBean() {
        MatcherAssert.assertThat(CitizenMappingErrorResponse.class, allOf(
                hasValidBeanConstructor(),
                hasValidGettersAndSetters(),
                hasValidBeanHashCode(),
                hasValidBeanEquals(),
                hasValidBeanToString()));

        MatcherAssert.assertThat(CitizenMappingErrorResponse.Errors.class, allOf(
                hasValidBeanConstructor(),
                hasValidGettersAndSetters(),
                hasValidBeanHashCode(),
                hasValidBeanEquals(),
                hasValidBeanToString()));
    }

    @Test
    void testFields() {
        CitizenMappingErrorResponse object = new CitizenMappingErrorResponse();
        object.setType("type");
        object.setTitle("title");
        object.setStatus(400);
        object.setTraceId(UUID.randomUUID().toString());
        CitizenMappingErrorResponse.Errors errors = new CitizenMappingErrorResponse.Errors();
        errors.setPersonId(new String[]{UUID.randomUUID().toString()});
        object.setErrors(errors);

        Assertions.assertThat(object).isNotNull().hasNoNullFieldsOrProperties();
    }

}
