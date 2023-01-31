package se.sundsvall.casemanagement.api.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.api.model.enums.StakeholderType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static se.sundsvall.casemanagement.TestUtil.createStakeholder;
import static se.sundsvall.casemanagement.TestUtil.getRandomOfEnum;

class PersonDTOTest {

    @BeforeAll
    static void setup() {
        registerValueGenerator(() -> LocalDateTime.now().plusDays(new Random().nextInt()), LocalDateTime.class);
        registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt()), LocalDate.class);
    }

    @Test
    void testBean() {
        MatcherAssert.assertThat(PersonDTO.class, allOf(
                hasValidBeanConstructor(),
                hasValidGettersAndSetters(),
                hasValidBeanHashCode(),
                hasValidBeanEquals(),
                hasValidBeanToString()));
    }

    @Test
    void testFields() {
        PersonDTO dto = (PersonDTO) createStakeholder(StakeholderType.PERSON, List.of((StakeholderRole) getRandomOfEnum(StakeholderRole.class)));
        assertThat(dto).isNotNull().hasNoNullFieldsOrProperties();
    }

}
