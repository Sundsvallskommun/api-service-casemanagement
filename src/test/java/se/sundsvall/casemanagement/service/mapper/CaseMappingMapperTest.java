package se.sundsvall.casemanagement.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casemanagement.api.model.enums.AttachmentCategory.UNDERLAG_RISKKLASSNING;
import static se.sundsvall.casemanagement.util.Constants.SERVICE_NAME;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import se.sundsvall.casemanagement.TestUtil;
import se.sundsvall.casemanagement.api.model.CaseDTO;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.SystemType;

class CaseMappingMapperTest {

	@ParameterizedTest
	@MethodSource("toCaseMappingArguments")
	void toCaseMapping(CaseDTO caseInput, String caseId, SystemType systemType, String serviceName) {
		Optional.ofNullable(serviceName).ifPresent(name -> caseInput.getExtraParameters().put(SERVICE_NAME, name));

		final var bean = CaseMappingMapper.toCaseMapping(caseInput, caseId, systemType);

		assertThat(bean.getExternalCaseId()).isEqualTo(caseInput.getExternalCaseId());
		assertThat(bean.getCaseId()).isEqualTo(caseId);
		assertThat(bean.getSystem()).isEqualTo(systemType);
		assertThat(bean.getCaseType()).isEqualTo(caseInput.getCaseType());
		assertThat(bean.getServiceName()).isEqualTo(serviceName);
	}

	private static Stream<Arguments> toCaseMappingArguments() {
		return Stream.of(
			Arguments.of(TestUtil.createEnvironmentalCase(CaseType.REGISTRERING_AV_LIVSMEDEL, UNDERLAG_RISKKLASSNING), UUID.randomUUID().toString(), SystemType.ECOS, "SomeSystem"),
			Arguments.of(TestUtil.createPlanningPermissionCase(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.BUILDING_PERMIT_APPLICATION), UUID.randomUUID().toString(), SystemType.BYGGR, null),
			Arguments.of(TestUtil.createOtherCase(CaseType.PARKING_PERMIT), UUID.randomUUID().toString(), SystemType.CASE_DATA, "SomeOtherSystem"));
	}
}
