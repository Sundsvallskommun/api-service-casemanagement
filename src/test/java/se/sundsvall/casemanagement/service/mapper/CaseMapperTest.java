package se.sundsvall.casemanagement.service.mapper;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static se.sundsvall.casemanagement.api.model.enums.AttachmentCategory.UNDERLAG_RISKKLASSNING;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.casemanagement.TestUtil;
import se.sundsvall.casemanagement.api.model.CaseDTO;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.integration.db.model.DeliveryStatus;

@ExtendWith(MockitoExtension.class)
class CaseMapperTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

	private static Stream<Arguments> toCaseEntityArguments() {
		return Stream.of(
			Arguments.of(TestUtil.createEcosCaseDTO(CaseType.REGISTRERING_AV_LIVSMEDEL, UNDERLAG_RISKKLASSNING), "2281"),
			Arguments.of(TestUtil.createByggRCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.BUILDING_PERMIT_APPLICATION), "2281"),
			Arguments.of(TestUtil.createOtherCaseDTO(), "2281"));
	}

	@ParameterizedTest
	@MethodSource("toCaseEntityArguments")
	void toCaseEntity(final CaseDTO dto, final String municipalityId) throws Exception {
		final var entity = CaseMapper.toCaseEntity(dto, municipalityId);

		assertThat(entity.getId()).isEqualTo(dto.getExternalCaseId());
		assertThat(entity.getDeliveryStatus()).isEqualTo(DeliveryStatus.PENDING);
		assertThat(entity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(toString(entity.getDto().getCharacterStream())).isEqualTo(OBJECT_MAPPER.writeValueAsString(dto));
	}

	@Test
	void toCaseEntityWhenToClobThrowsError() {
		final var externalCaseId = "externalCaseId";
		final var municipalityId = "2281";
		final var dto = Mockito.mock(CaseDTO.class);

		when(dto.getExternalCaseId()).thenReturn(externalCaseId);

		final var entity = CaseMapper.toCaseEntity(dto, municipalityId);
		assertThat(entity.getId()).isEqualTo(externalCaseId);
		assertThat(entity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(entity.getDeliveryStatus()).isEqualTo(DeliveryStatus.PENDING);
		assertThat(entity.getDto()).isNull();
	}

	private String toString(final Reader reader) throws Exception {
		final var bf = new BufferedReader(reader);
		String line = null;
		final var builder = new StringBuilder();
		while (nonNull(line = bf.readLine())) {
			builder.append(line);
		}
		return builder.toString();
	}

}
