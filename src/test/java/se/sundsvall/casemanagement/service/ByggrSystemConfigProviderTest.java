package se.sundsvall.casemanagement.service;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casemanagement.integration.db.ByggrStatusMappingRuleRepository;
import se.sundsvall.casemanagement.integration.db.model.ByggrStatusMappingRuleEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ByggrSystemConfigProviderTest {

	@Mock
	private ByggrStatusMappingRuleRepository statusMappingRuleRepository;

	@Test
	void resolveHandelseStatus_matchesTypOnly() {
		when(statusMappingRuleRepository.findAll()).thenReturn(List.of(
			ByggrStatusMappingRuleEntity.builder().withHandelseTyp("ANM").withReturnField("TYP").build()));

		final var provider = new ByggrSystemConfigProvider(statusMappingRuleRepository);

		assertThat(provider.resolveHandelseStatus("ANM", "anything", null)).isEqualTo("ANM");
	}

	@Test
	void resolveHandelseStatus_matchesTypAndSlag() {
		when(statusMappingRuleRepository.findAll()).thenReturn(List.of(
			ByggrStatusMappingRuleEntity.builder().withHandelseTyp("BESLUT").withHandelseSlag("SLU").withReturnField("SLAG").build()));

		final var provider = new ByggrSystemConfigProvider(statusMappingRuleRepository);

		assertThat(provider.resolveHandelseStatus("BESLUT", "SLU", null)).isEqualTo("SLU");
	}

	@Test
	void resolveHandelseStatus_matchesAllThree() {
		when(statusMappingRuleRepository.findAll()).thenReturn(List.of(
			ByggrStatusMappingRuleEntity.builder().withHandelseTyp("Atom").withHandelseSlag("Kv").withHandelseUtfall("Kv2").withReturnField("UTFALL").build()));

		final var provider = new ByggrSystemConfigProvider(statusMappingRuleRepository);

		assertThat(provider.resolveHandelseStatus("Atom", "Kv", "Kv2")).isEqualTo("Kv2");
	}

	@Test
	void resolveHandelseStatus_nullTypMatchesAny() {
		when(statusMappingRuleRepository.findAll()).thenReturn(List.of(
			ByggrStatusMappingRuleEntity.builder().withHandelseSlag("KOMPBYGG").withReturnField("SLAG").build()));

		final var provider = new ByggrSystemConfigProvider(statusMappingRuleRepository);

		assertThat(provider.resolveHandelseStatus("ANY_TYP", "KOMPBYGG", null)).isEqualTo("KOMPBYGG");
	}

	@Test
	void resolveHandelseStatus_noMatch_returnsNull() {
		when(statusMappingRuleRepository.findAll()).thenReturn(List.of(
			ByggrStatusMappingRuleEntity.builder().withHandelseTyp("ANM").withReturnField("TYP").build()));

		final var provider = new ByggrSystemConfigProvider(statusMappingRuleRepository);

		assertThat(provider.resolveHandelseStatus("UNKNOWN", "UNKNOWN", "UNKNOWN")).isNull();
	}

	@Test
	void resolveHandelseStatus_unknownReturnField_returnsNull() {
		when(statusMappingRuleRepository.findAll()).thenReturn(List.of(
			ByggrStatusMappingRuleEntity.builder().withHandelseTyp("ANM").withReturnField("INVALID").build()));

		final var provider = new ByggrSystemConfigProvider(statusMappingRuleRepository);

		assertThat(provider.resolveHandelseStatus("ANM", null, null)).isNull();
	}

}
