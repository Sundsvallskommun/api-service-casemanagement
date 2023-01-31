package se.sundsvall.casemanagement.api.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import se.sundsvall.casemanagement.TestUtil;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class AttachmentDTOTest {

    @Test
    void testBean() {
        MatcherAssert.assertThat(AttachmentDTO.class, allOf(
                hasValidBeanConstructor(),
                hasValidGettersAndSetters(),
                hasValidBeanHashCode(),
                hasValidBeanEquals(),
                hasValidBeanToString()));
    }

    @Test
    void testFields() {
        AttachmentDTO dto = TestUtil.createAttachment((AttachmentCategory) TestUtil.getRandomOfEnum(AttachmentCategory.class));

        assertThat(dto).isNotNull().hasNoNullFieldsOrProperties();
    }

}
