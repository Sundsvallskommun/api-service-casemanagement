package se.sundsvall.casemanagement.integration.db.model;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Random;
import javax.sql.rowset.serial.SerialClob;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.sundsvall.casemanagement.api.model.EcosCaseDTO;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class CaseEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDateTime.now().plusDays(new Random().nextInt()), LocalDateTime.class);
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
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
		// Arrange
		final var id = "123";
		final var deliveryStatus = DeliveryStatus.CREATED;
		final var ecosCaseDTO = EcosCaseDTO.builder().build();
		final var dto = new SerialClob(ecosCaseDTO.toString().toCharArray());
		final var municipalityId = "2281";
		final var created = OffsetDateTime.now();
		final var requestId = "requestId";

		// Act
		final var object = CaseEntity.builder()
			.withId(id)
			.withDeliveryStatus(deliveryStatus)
			.withDto(dto)
			.withMunicipalityId(municipalityId)
			.withCreated(created)
			.withRequestId(requestId)
			.build();

		// Assert
		assertThat(object).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(object.getId()).isEqualTo(id);
		assertThat(object.getDeliveryStatus()).isEqualTo(deliveryStatus);
		assertThat(object.getDto()).isEqualTo(dto);
		assertThat(object.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(object.getCreated()).isEqualTo(created);
		assertThat(object.getRequestId()).isEqualTo(requestId);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(CaseEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new CaseEntity()).hasAllNullFieldsOrProperties();
	}

}
