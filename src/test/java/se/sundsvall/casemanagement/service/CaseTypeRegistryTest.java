package se.sundsvall.casemanagement.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casemanagement.integration.casedata.configuration.CaseDataProperties;
import se.sundsvall.casemanagement.integration.db.CaseTypeRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseTypeEntity;
import se.sundsvall.dept44.exception.ServerProblem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static se.sundsvall.casemanagement.api.model.enums.SystemType.BYGGR;
import static se.sundsvall.casemanagement.api.model.enums.SystemType.CASE_DATA;
import static se.sundsvall.casemanagement.api.model.enums.SystemType.ECOS;
import static se.sundsvall.casemanagement.api.model.enums.SystemType.EDPFUTURE;

@ExtendWith(MockitoExtension.class)
class CaseTypeRegistryTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String NAMESPACE = "SBK_PARKING";
	private static final String DYNAMIC_CASE_TYPE = "PARKING_PERMIT";
	private static final ServerProblem CASE_DATA_UNAVAILABLE = new ServerProblem(BAD_GATEWAY, "case-data error: {status=500 Internal Server Error}");

	@Mock
	private CaseTypeRepository caseTypeRepository;

	@Mock
	private CaseDataCaseTypeProvider caseDataCaseTypeProvider;

	@Mock
	private CaseDataProperties caseDataProperties;

	private CaseTypeRegistry caseTypeRegistry;

	@BeforeEach
	void setUp() {
		lenient().when(caseTypeRepository.findById("NYBYGGNAD_ANSOKAN_OM_BYGGLOV"))
			.thenReturn(Optional.of(CaseTypeEntity.builder().withName("NYBYGGNAD_ANSOKAN_OM_BYGGLOV").withSystemType(BYGGR).build()));
		lenient().when(caseTypeRepository.findById("REGISTRERING_AV_LIVSMEDEL"))
			.thenReturn(Optional.of(CaseTypeEntity.builder().withName("REGISTRERING_AV_LIVSMEDEL").withSystemType(ECOS).build()));
		lenient().when(caseTypeRepository.findById("EXTRA_SACK"))
			.thenReturn(Optional.of(CaseTypeEntity.builder().withName("EXTRA_SACK").withSystemType(EDPFUTURE).build()));

		caseTypeRegistry = new CaseTypeRegistry(caseTypeRepository, caseDataCaseTypeProvider, caseDataProperties);
	}

	@Test
	void resolveSystem_withByggRType_returnsByggr() {
		final var result = caseTypeRegistry.resolveSystem("NYBYGGNAD_ANSOKAN_OM_BYGGLOV");

		assertThat(result).hasValue(BYGGR);
		verifyNoInteractions(caseDataCaseTypeProvider);
	}

	@Test
	void resolveSystem_withEcosType_returnsEcos() {
		final var result = caseTypeRegistry.resolveSystem("REGISTRERING_AV_LIVSMEDEL");

		assertThat(result).hasValue(ECOS);
		verifyNoInteractions(caseDataCaseTypeProvider);
	}

	@Test
	void resolveSystem_withEdpFutureType_returnsEdpFuture() {
		final var result = caseTypeRegistry.resolveSystem("EXTRA_SACK");

		assertThat(result).hasValue(EDPFUTURE);
		verifyNoInteractions(caseDataCaseTypeProvider);
	}

	@Test
	void resolveSystem_withNonStaticType_returnsCaseData() {
		final var result = caseTypeRegistry.resolveSystem(DYNAMIC_CASE_TYPE);

		assertThat(result).hasValue(CASE_DATA);
		verifyNoInteractions(caseDataCaseTypeProvider);
	}

	@Test
	void resolveSystem_withNull_returnsEmpty() {
		final var result = caseTypeRegistry.resolveSystem(null);

		assertThat(result).isEmpty();
		verifyNoInteractions(caseDataCaseTypeProvider);
	}

	@Test
	void isCaseDataType_withMatchingType_returnsTrue() {
		when(caseDataProperties.namespaces()).thenReturn(Map.of(MUNICIPALITY_ID, List.of(NAMESPACE)));
		when(caseDataCaseTypeProvider.getCaseDataTypesByNamespace(MUNICIPALITY_ID, NAMESPACE))
			.thenReturn(Map.of(DYNAMIC_CASE_TYPE, "Parking Permit"));

		final var result = caseTypeRegistry.isCaseDataType(DYNAMIC_CASE_TYPE, MUNICIPALITY_ID);

		assertThat(result).isTrue();
		verify(caseDataCaseTypeProvider).getCaseDataTypesByNamespace(MUNICIPALITY_ID, NAMESPACE);
	}

	@Test
	void isCaseDataType_withNoMatch_returnsFalse() {
		when(caseDataProperties.namespaces()).thenReturn(Map.of(MUNICIPALITY_ID, List.of(NAMESPACE)));
		when(caseDataCaseTypeProvider.getCaseDataTypesByNamespace(MUNICIPALITY_ID, NAMESPACE))
			.thenReturn(Map.of());

		final var result = caseTypeRegistry.isCaseDataType("NONEXISTENT_TYPE", MUNICIPALITY_ID);

		assertThat(result).isFalse();
	}

	@Test
	void isCaseDataType_withNoNamespacesConfigured_returnsFalse() {
		when(caseDataProperties.namespaces()).thenReturn(null);

		final var result = caseTypeRegistry.isCaseDataType(DYNAMIC_CASE_TYPE, MUNICIPALITY_ID);

		assertThat(result).isFalse();
		verifyNoInteractions(caseDataCaseTypeProvider);
	}

	@Test
	void resolveNamespace_withMatchingNamespace_returnsNamespace() {
		final var otherNamespace = "SBK_MEX";
		when(caseDataProperties.namespaces()).thenReturn(Map.of(MUNICIPALITY_ID, List.of(otherNamespace, NAMESPACE)));
		when(caseDataCaseTypeProvider.getCaseDataTypesByNamespace(MUNICIPALITY_ID, otherNamespace))
			.thenReturn(Map.of());
		when(caseDataCaseTypeProvider.getCaseDataTypesByNamespace(MUNICIPALITY_ID, NAMESPACE))
			.thenReturn(Map.of(DYNAMIC_CASE_TYPE, "Parking Permit"));

		final var result = caseTypeRegistry.resolveNamespace(DYNAMIC_CASE_TYPE, MUNICIPALITY_ID);

		assertThat(result).isEqualTo(NAMESPACE);
	}

	@Test
	void isCaseDataType_whenLookupFailsForOneNamespace_andTypeFoundInAnother_returnsTrue() {
		final var otherNamespace = "SBK_MEX";
		when(caseDataProperties.namespaces()).thenReturn(Map.of(MUNICIPALITY_ID, List.of(otherNamespace, NAMESPACE)));
		when(caseDataCaseTypeProvider.getCaseDataTypesByNamespace(MUNICIPALITY_ID, otherNamespace)).thenThrow(CASE_DATA_UNAVAILABLE);
		when(caseDataCaseTypeProvider.getCaseDataTypesByNamespace(MUNICIPALITY_ID, NAMESPACE))
			.thenReturn(Map.of(DYNAMIC_CASE_TYPE, "Parking Permit"));

		final var result = caseTypeRegistry.isCaseDataType(DYNAMIC_CASE_TYPE, MUNICIPALITY_ID);

		assertThat(result).isTrue();
	}

	@Test
	void isCaseDataType_whenLookupFailsForOneNamespace_andTypeNotFoundInAnother_rethrows() {
		final var otherNamespace = "SBK_MEX";
		when(caseDataProperties.namespaces()).thenReturn(Map.of(MUNICIPALITY_ID, List.of(otherNamespace, NAMESPACE)));
		when(caseDataCaseTypeProvider.getCaseDataTypesByNamespace(MUNICIPALITY_ID, otherNamespace)).thenThrow(CASE_DATA_UNAVAILABLE);
		when(caseDataCaseTypeProvider.getCaseDataTypesByNamespace(MUNICIPALITY_ID, NAMESPACE)).thenReturn(Map.of());

		assertThatThrownBy(() -> caseTypeRegistry.isCaseDataType(DYNAMIC_CASE_TYPE, MUNICIPALITY_ID))
			.isSameAs(CASE_DATA_UNAVAILABLE);
	}

	@Test
	void resolveNamespace_whenLookupFails_rethrowsWithoutQueryingLaterNamespaces() {
		final var otherNamespace = "SBK_MEX";
		when(caseDataProperties.namespaces()).thenReturn(Map.of(MUNICIPALITY_ID, List.of(otherNamespace, NAMESPACE)));
		when(caseDataCaseTypeProvider.getCaseDataTypesByNamespace(MUNICIPALITY_ID, otherNamespace)).thenThrow(CASE_DATA_UNAVAILABLE);

		assertThatThrownBy(() -> caseTypeRegistry.resolveNamespace(DYNAMIC_CASE_TYPE, MUNICIPALITY_ID))
			.isSameAs(CASE_DATA_UNAVAILABLE);
		verify(caseDataCaseTypeProvider, never()).getCaseDataTypesByNamespace(MUNICIPALITY_ID, NAMESPACE);
	}

	@Test
	void resolveNamespace_withNoMatch_returnsOther() {
		when(caseDataProperties.namespaces()).thenReturn(Map.of(MUNICIPALITY_ID, List.of(NAMESPACE)));
		when(caseDataCaseTypeProvider.getCaseDataTypesByNamespace(MUNICIPALITY_ID, NAMESPACE))
			.thenReturn(Map.of());

		final var result = caseTypeRegistry.resolveNamespace("NONEXISTENT_TYPE", MUNICIPALITY_ID);

		assertThat(result).isEqualTo("OTHER");
	}

	@Test
	void resolveNamespace_withNullCaseType_returnsOther() {
		final var result = caseTypeRegistry.resolveNamespace(null, MUNICIPALITY_ID);

		assertThat(result).isEqualTo("OTHER");
		verifyNoInteractions(caseDataCaseTypeProvider);
	}

	@Test
	void resolveNamespace_withNullMunicipalityId_returnsOther() {
		final var result = caseTypeRegistry.resolveNamespace(DYNAMIC_CASE_TYPE, null);

		assertThat(result).isEqualTo("OTHER");
		verifyNoInteractions(caseDataCaseTypeProvider);
	}

	@Test
	void resolveNamespace_withNoNamespacesConfigured_returnsOther() {
		when(caseDataProperties.namespaces()).thenReturn(null);

		final var result = caseTypeRegistry.resolveNamespace(DYNAMIC_CASE_TYPE, MUNICIPALITY_ID);

		assertThat(result).isEqualTo("OTHER");
		verifyNoInteractions(caseDataCaseTypeProvider);
	}

	@Test
	void resolveNamespace_withMunicipalityNotInConfig_returnsOther() {
		when(caseDataProperties.namespaces()).thenReturn(Map.of("9999", List.of(NAMESPACE)));

		final var result = caseTypeRegistry.resolveNamespace(DYNAMIC_CASE_TYPE, MUNICIPALITY_ID);

		assertThat(result).isEqualTo("OTHER");
		verifyNoInteractions(caseDataCaseTypeProvider);
	}

}
