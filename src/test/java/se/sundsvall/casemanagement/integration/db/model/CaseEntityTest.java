package se.sundsvall.casemanagement.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.hamcrest.CoreMatchers.allOf;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Random;

import javax.sql.rowset.serial.SerialClob;

import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import se.sundsvall.casemanagement.api.model.EnvironmentalCaseDTO;

class CaseEntityTest {


    @BeforeAll
    static void setup() {
        registerValueGenerator(() -> LocalDateTime.now().plusDays(new Random().nextInt()), LocalDateTime.class);
    }

    @Test
    void testBean() {
        MatcherAssert.assertThat(CaseEntity.class, allOf(
            hasValidBeanConstructor(),
            hasValidGettersAndSetters(),
            hasValidBeanHashCode(),
            hasValidBeanEquals(),
            hasValidBeanToString()));
    }

    @Test
    void testFields() throws SQLException {

        var object = CaseEntity.builder()
            .withId(String.valueOf(new Random().nextInt()))
            .withDeliveryStatus(DeliveryStatus.CREATED)
            .withDto(new SerialClob(new EnvironmentalCaseDTO().toString().toCharArray()));

        Assertions.assertThat(object).isNotNull().hasNoNullFieldsOrProperties();
    }

}
