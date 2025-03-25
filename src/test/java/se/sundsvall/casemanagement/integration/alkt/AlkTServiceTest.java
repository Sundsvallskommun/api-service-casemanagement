package se.sundsvall.casemanagement.integration.alkt;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casemanagement.api.model.enums.SystemType.ALKT;
import static se.sundsvall.casemanagement.integration.alkt.AlkTService.CASE_TYPE;
import static se.sundsvall.casemanagement.integration.alkt.AlkTService.FINISHED;
import static se.sundsvall.casemanagement.integration.alkt.AlkTService.ONGOING;

import generated.client.alkt.Establishment;
import generated.client.alkt.ModelCase;
import generated.client.alkt.Owner;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlkTServiceTest {

	private static final String PARTY_ID = "4af37fbe-0225-4abc-a2dc-ffdab407bc50";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String DESCRIPTION = "description";
	private static final String REGISTRATION_NUMBER = "123";
	private static final Integer ID = 123456;

	@Mock
	private AlkTClient alkTClientMock;

	@InjectMocks
	private AlkTService service;

	@Test
	void getStatusesByPartyId() {
		final var owners = List.of(createOwner(), createOwner());
		when(alkTClientMock.getOwners(MUNICIPALITY_ID, PARTY_ID)).thenReturn(owners);

		final var result = service.getStatusesByPartyId(PARTY_ID, MUNICIPALITY_ID);

		// Each owner have 2 establishments, each establishment have 2 cases, total 8 cases
		assertThat(result).hasSize(8)
			.allSatisfy(caseStatus -> {
				assertThat(caseStatus.getSystem()).isEqualTo(ALKT);
				assertThat(caseStatus.getCaseType()).isEqualTo(CASE_TYPE);
				assertThat(caseStatus.getCaseId()).isEqualTo(String.valueOf(ID));
				assertThat(caseStatus.getErrandNumber()).isEqualTo(REGISTRATION_NUMBER);
				assertThat(caseStatus.getStatus()).isIn(ONGOING, FINISHED);
				assertThat(caseStatus.getServiceName()).isEqualTo(DESCRIPTION);
				assertThat(caseStatus.getTimestamp()).isCloseTo(LocalDateTime.now(), within(3, SECONDS));
			});

		verify(alkTClientMock).getOwners(MUNICIPALITY_ID, PARTY_ID);
		verifyNoMoreInteractions(alkTClientMock);
	}

	@Test
	void getOwnersByPartyId() {
		final var owners = List.of(createOwner(), createOwner());
		when(alkTClientMock.getOwners(MUNICIPALITY_ID, PARTY_ID)).thenReturn(owners);

		final var result = service.getOwnersByPartyId(PARTY_ID, MUNICIPALITY_ID);

		assertThat(result).hasSize(2).isEqualTo(owners);

		verify(alkTClientMock).getOwners(MUNICIPALITY_ID, PARTY_ID);
		verifyNoMoreInteractions(alkTClientMock);
	}

	@Test
	void mapToCaseStatuses() {
		final var modelCases = List.of(createModelCase(), createModelCase(), createModelCase());

		final var caseStatuses = service.mapToCaseStatuses(modelCases);

		assertThat(caseStatuses).hasSize(3)
			.allSatisfy(caseStatus -> {
				assertThat(caseStatus.getSystem()).isEqualTo(ALKT);
				assertThat(caseStatus.getCaseType()).isEqualTo(CASE_TYPE);
				assertThat(caseStatus.getCaseId()).isEqualTo(String.valueOf(ID));
				assertThat(caseStatus.getErrandNumber()).isEqualTo(REGISTRATION_NUMBER);
				assertThat(caseStatus.getStatus()).isEqualTo(ONGOING);
				assertThat(caseStatus.getServiceName()).isEqualTo(DESCRIPTION);
				assertThat(caseStatus.getTimestamp()).isCloseTo(LocalDateTime.now(), within(3, SECONDS));
			});
	}

	@ParameterizedTest
	@ValueSource(booleans = {
		true, false
	})
	void mapToCaseStatus(final boolean open) {
		final var modelCase = createModelCase(modelCase1 -> modelCase1.setOpen(open));

		final var caseStatus = service.mapToCaseStatus(modelCase);

		assertThat(caseStatus.getSystem()).isEqualTo(ALKT);
		assertThat(caseStatus.getCaseType()).isEqualTo(CASE_TYPE);
		assertThat(caseStatus.getCaseId()).isEqualTo(String.valueOf(modelCase.getId()));
		assertThat(caseStatus.getErrandNumber()).isEqualTo(REGISTRATION_NUMBER);
		assertThat(caseStatus.getStatus()).isEqualTo(open ? ONGOING : FINISHED);
		assertThat(caseStatus.getServiceName()).isEqualTo(modelCase.getDescription());
		assertThat(caseStatus.getTimestamp()).isEqualTo(modelCase.getChanged().toLocalDateTime());
	}

	private Owner createOwner() {
		return new Owner()
			.establishments(List.of(createEstablishment(), createEstablishment()));
	}

	private Establishment createEstablishment() {
		return new Establishment()
			.cases(List.of(createModelCase(modelCase -> modelCase.setOpen(false)), createModelCase(modelCase -> modelCase.setOpen(true))));
	}

	private ModelCase createModelCase() {
		return createModelCase(null);
	}

	private ModelCase createModelCase(final Consumer<ModelCase> modifier) {
		final var modelCase = new ModelCase()
			.id(ID)
			.registrationNumber(REGISTRATION_NUMBER)
			.open(true)
			.description(DESCRIPTION)
			.changed(OffsetDateTime.now());

		if (modifier != null) {
			modifier.accept(modelCase);
		}
		return modelCase;
	}

}
