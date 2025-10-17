package se.sundsvall.casemanagement.integration.casedata;

import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.LOST_PARKING_PERMIT;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.PARKING_PERMIT;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.PARKING_PERMIT_RENEWAL;
import static se.sundsvall.casemanagement.integration.casedata.CaseDataMapper.toAttachment;
import static se.sundsvall.casemanagement.util.Constants.SERVICE_NAME;

import generated.client.casedata.Errand;
import generated.client.casedata.Errand.ChannelEnum;
import generated.client.casedata.PatchErrand;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casemanagement.TestUtil;
import se.sundsvall.casemanagement.api.model.CaseDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.Namespace;
import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.api.model.enums.StakeholderType;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.casedata.configuration.CaseDataProperties;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.service.CaseMappingService;
import se.sundsvall.casemanagement.util.Constants;

@ExtendWith(MockitoExtension.class)
class CaseDataServiceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final Random RANDOM = new Random();

	@InjectMocks
	private CaseDataService caseDataService;

	@Mock
	private CaseMappingService caseMappingServiceMock;

	@Mock
	private CaseDataClient caseDataClientMock;

	@Mock
	private CaseDataProperties caseDataPropertiesMock;

	@Captor
	private ArgumentCaptor<PatchErrand> patchErrandArgumentCaptor;

	@Captor
	private ArgumentCaptor<generated.client.casedata.Status> statusArgumentCaptor;

	@Captor
	private ArgumentCaptor<List<generated.client.casedata.Stakeholder>> stakeholderListArgumentCaptor;

	@Captor
	private ArgumentCaptor<generated.client.casedata.Attachment> attachmentArgumentCaptor;

	@ParameterizedTest
	@EnumSource(value = CaseType.class, names = {
		PARKING_PERMIT, LOST_PARKING_PERMIT, PARKING_PERMIT_RENEWAL
	})
	void testPostCases(final CaseType caseType) throws URISyntaxException {
		// Arrange
		final var errandId = RANDOM.nextLong();
		final var uri = new URI("https://sundsvall-test.se/errands/" + errandId);
		final var getErrand = new Errand();
		getErrand.setErrandNumber("Inskickat");
		final var inputCase = createCase(caseType);
		final var municipalityId = "2281";
		final var namespace = Namespace.SBK_PARKING_PERMIT.name();

		// Mock
		when(caseDataClientMock.postErrands(eq(MUNICIPALITY_ID), eq(namespace), any())).thenReturn(ResponseEntity.created(uri).build());
		when(caseDataClientMock.getErrand(MUNICIPALITY_ID, namespace, errandId)).thenReturn(getErrand);

		// Act
		final var response = caseDataService.postErrand(inputCase, municipalityId);

		// Assert
		assertThat(response).isEqualTo("Inskickat");

		final var errandArgumentCaptor = ArgumentCaptor.forClass(Errand.class);
		verify(caseDataClientMock).postErrands(eq(MUNICIPALITY_ID), eq(namespace), errandArgumentCaptor.capture());
		final var errand = errandArgumentCaptor.getValue();

		assertThat(errand.getCaseTitleAddition()).isEqualTo(inputCase.getCaseTitleAddition());
		assertThat(errand.getCaseType()).isEqualTo(inputCase.getCaseType());
		assertThat(errand.getChannel()).isEqualTo(ChannelEnum.ESERVICE);
		assertThat(errand.getDescription()).isEqualTo(inputCase.getDescription());

		assertThat(errand.getExtraParameters()).hasSize(4);
		assertThat(errand.getExtraParameters().stream().filter(param -> param.getKey().equals("application.priority"))
			.findFirst()
			.orElseThrow()
			.getValues().getFirst()).isEqualTo("HIGH");

		assertThat(errand.getExternalCaseId()).isEqualTo(inputCase.getExternalCaseId());
		assertThat(errand.getPhase()).isEqualTo("Aktualisering");
		assertThat(errand.getPriority()).isEqualTo(Errand.PriorityEnum.HIGH);
		assertThat(errand.getStakeholders()).hasSameSizeAs(inputCase.getStakeholders());
		assertThat(errand.getStatus().getStatusType()).isEqualTo("Ärende inkommit");
		assertThat(errand.getStatus().getCreated()).isNotNull();

		attachmentArgumentCaptor = ArgumentCaptor.forClass(generated.client.casedata.Attachment.class);
		verify(caseDataClientMock, times(3)).postAttachment(eq(MUNICIPALITY_ID), eq(namespace), eq(errandId), attachmentArgumentCaptor.capture());
		final var attachment = attachmentArgumentCaptor.getValue();
		assertThat(attachment).isNotNull();
		assertThat(attachment.getCategory()).isEqualTo(AttachmentCategory.ANMALAN_VARMEPUMP.toString());

		final var caseArgumentCaptor = ArgumentCaptor.forClass(CaseDTO.class);
		verify(caseMappingServiceMock).postCaseMapping(caseArgumentCaptor.capture(), any(String.class), any(SystemType.class), eq(MUNICIPALITY_ID));
		final var caseMapping = caseArgumentCaptor.getValue();
		assertThat(caseMapping.getExternalCaseId()).isEqualTo(inputCase.getExternalCaseId());
		assertThat(caseMapping.getCaseType()).isEqualTo(inputCase.getCaseType());
		assertThat(caseMapping.getExtraParameters().get(SERVICE_NAME)).isNull();
	}

	// Test putErrand
	@Test
	void testPutErrand() {
		// Arrange
		final var errandId = RANDOM.nextLong();
		final var inputCase = createCase(CaseType.PARKING_PERMIT);
		final var namespace = Namespace.SBK_PARKING_PERMIT.name();

		// Mock
		when(caseDataClientMock.patchErrand(eq(MUNICIPALITY_ID), eq(namespace), any(), any())).thenReturn(null);
		when(caseDataClientMock.patchStatusOnErrand(eq(MUNICIPALITY_ID), eq(namespace), any(), any())).thenReturn(null);
		when(caseDataClientMock.putStakeholdersOnErrand(eq(MUNICIPALITY_ID), eq(namespace), any(), any())).thenReturn(null);
		when(caseDataClientMock.postAttachment(eq(MUNICIPALITY_ID), eq(namespace), eq(errandId), any())).thenReturn(null);

		// Act
		caseDataService.putErrand(errandId, inputCase, MUNICIPALITY_ID);

		// Assert
		verify(caseDataClientMock, times(1)).patchErrand(eq(MUNICIPALITY_ID), eq(namespace), eq(errandId), patchErrandArgumentCaptor.capture());
		verify(caseDataClientMock, times(1)).putStakeholdersOnErrand(eq(MUNICIPALITY_ID), eq(namespace), eq(errandId), stakeholderListArgumentCaptor.capture());
		verify(caseDataClientMock, times(3)).postAttachment(eq(MUNICIPALITY_ID), eq(namespace), eq(errandId), attachmentArgumentCaptor.capture());
		verify(caseDataClientMock, times(1)).patchStatusOnErrand(eq(MUNICIPALITY_ID), eq(namespace), eq(errandId), statusArgumentCaptor.capture());

		final var patchErrand = patchErrandArgumentCaptor.getValue();
		assertThat(patchErrand.getCaseType()).isEqualTo(inputCase.getCaseType());
		assertThat(patchErrand.getExternalCaseId()).isEqualTo(inputCase.getExternalCaseId());
		assertThat(patchErrand.getDescription()).isEqualTo(inputCase.getDescription());
		assertThat(patchErrand.getCaseTitleAddition()).isEqualTo(inputCase.getCaseTitleAddition());
		assertThat(patchErrand.getExtraParameters()).hasSize(4);
		assertThat(patchErrand.getExtraParameters().stream().filter(param -> param.getKey().equals("application.priority"))
			.findFirst()
			.orElseThrow()
			.getValues().getFirst()).isEqualTo("HIGH");
		assertThat(patchErrand.getPriority()).isEqualTo(PatchErrand.PriorityEnum.HIGH);
		assertThat(patchErrand.getPhase()).isNull();
		assertThat(patchErrand.getDiaryNumber()).isNull();
		assertThat(patchErrand.getStartDate()).isNull();
		assertThat(patchErrand.getEndDate()).isNull();
		assertThat(patchErrand.getApplicationReceived()).isNull();

		final var status = statusArgumentCaptor.getValue();
		assertThat(status.getStatusType()).isEqualTo("Komplettering inkommen");
		assertThat(status.getCreated()).isNotNull();

		assertThat(stakeholderListArgumentCaptor.getValue()).hasSameSizeAs(inputCase.getStakeholders());
		assertThat(attachmentArgumentCaptor.getValue().getCategory()).isEqualTo(AttachmentCategory.ANMALAN_VARMEPUMP.toString());
	}

	@Test
	void testGetStatus() {
		// Arrange
		final var caseId = RANDOM.nextLong();
		final var errandMock = new Errand();
		final var namespace = Namespace.SBK_PARKING_PERMIT.name();
		errandMock.setId(caseId);
		final var statusMock1 = new generated.client.casedata.Status()
			.statusType(RandomStringUtils.random(10, true, false))
			.created(now().minusDays(10))
			.description(RandomStringUtils.random(10, true, false));
		final var statusMock2 = new generated.client.casedata.Status()
			.statusType(RandomStringUtils.random(10, true, false))
			.created(now().minusDays(5))
			.description(RandomStringUtils.random(10, true, false));
		final var statusMock3 = new generated.client.casedata.Status()
			.statusType(RandomStringUtils.random(10, true, false))
			.created(now().minusDays(20))
			.description(RandomStringUtils.random(10, true, false));
		errandMock.setStatuses(List.of(statusMock1, statusMock2, statusMock3));

		final var caseMapping = CaseMapping.builder()
			.withCaseId(String.valueOf(caseId))
			.withExternalCaseId(UUID.randomUUID().toString())
			.withSystem(SystemType.CASE_DATA)
			.withCaseType(CaseType.PARKING_PERMIT.toString())
			.withServiceName(RandomStringUtils.random(10, true, false))
			.build();
		// Mock
		when(caseDataClientMock.getErrand(MUNICIPALITY_ID, namespace, caseId)).thenReturn(errandMock);

		// Act
		final var result = caseDataService.getStatus(caseMapping, MUNICIPALITY_ID);

		// Assert
		assertThat(result.getCaseId()).isEqualTo(caseMapping.getCaseId());
		assertThat(result.getExternalCaseId()).isEqualTo(caseMapping.getExternalCaseId());
		assertThat(result.getCaseType()).isEqualTo(caseMapping.getCaseType());
		assertThat(result.getSystem()).isEqualTo(caseMapping.getSystem());
		assertThat(result.getServiceName()).isEqualTo(caseMapping.getServiceName());
		assertThat(result.getStatus()).isEqualTo(statusMock2.getStatusType());
		assertThat(result.getTimestamp()).isEqualTo(statusMock2.getCreated().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime());
	}

	@Test
	void testGetStatusErrandNotFound() {
		// Arrange
		final var caseMapping = CaseMapping.builder().withCaseId("1").build();
		final var namespace = "OTHER";
		// Mock
		when(caseDataClientMock.getErrand(eq(MUNICIPALITY_ID), eq(namespace), any())).thenThrow(Problem.valueOf(NOT_FOUND));
		// Act
		assertThatThrownBy(() -> caseDataService.getStatus(caseMapping, MUNICIPALITY_ID))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasMessage("Not Found: No case was found in CaseData with caseId: 1");
	}

	@Test
	void testGetStatusNotFound() {
		// Arrange
		final var caseMapping = CaseMapping.builder().withCaseId("1").build();
		// Act and assert
		assertThatThrownBy(() -> caseDataService.getStatus(caseMapping, MUNICIPALITY_ID))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", Constants.ERR_MSG_STATUS_NOT_FOUND);
	}

	@Test
	void patchErrandWithAttachmentNotFound() {
		final var errandNumber = UUID.randomUUID().toString();
		final var errandId = RANDOM.nextLong();
		final var attachment = new se.sundsvall.casemanagement.api.model.AttachmentDTO();
		final var attachments = List.of(attachment);
		final var namespace = "OTHER";
		final var caseMapping = CaseMapping.builder().withCaseId(String.valueOf(errandId)).withExternalCaseId(errandNumber).build();

		when(caseDataClientMock.postAttachment(MUNICIPALITY_ID, namespace, errandId, toAttachment(attachment, errandId))).thenThrow(Problem.valueOf(Status.NOT_FOUND));

		assertThatThrownBy(() -> caseDataService.patchErrandWithAttachment(caseMapping, attachments, MUNICIPALITY_ID))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "No case was found in CaseData with caseId: " + errandId);
	}

	@Test
	void getErrands() {
		final var partyId = UUID.randomUUID().toString();
		final var filter = "stakeholder.organizationNumber=%s".formatted(partyId);
		final var namespace = "OTHER";
		final Page<Errand> page = new PageImpl<>(List.of(new Errand(), new Errand(), new Errand()));
		when(caseDataClientMock.getErrands(MUNICIPALITY_ID, namespace, filter, "1000")).thenReturn(page);

		final var result = caseDataService.getErrands(MUNICIPALITY_ID, namespace, filter);

		assertThat(result).hasSize(3);

		verify(caseDataClientMock).getErrands(MUNICIPALITY_ID, namespace, filter, "1000");
		verifyNoMoreInteractions(caseDataClientMock);
	}

	@Test
	void getStatusByFilter() {
		final var namespace1 = "namespace1";
		final var namespace2 = "namespace2";
		final var namespaceMap = Map.of(MUNICIPALITY_ID, List.of(namespace1, namespace2));
		final var errand = createErrand();
		final Page<Errand> page = new PageImpl<>(List.of(errand));
		when(caseDataClientMock.getErrands(MUNICIPALITY_ID, namespace1, "filter", "1000")).thenReturn(page);
		when(caseDataClientMock.getErrands(MUNICIPALITY_ID, namespace2, "filter", "1000")).thenReturn(page);
		when(caseDataPropertiesMock.namespaces()).thenReturn(namespaceMap);

		final var result = caseDataService.getStatusesByFilter("filter", MUNICIPALITY_ID);

		assertThat(result).hasSize(2).allSatisfy(caseStatus -> {
			assertThat(caseStatus.getSystem()).isEqualTo(SystemType.CASE_DATA);
			assertThat(caseStatus.getExternalCaseId()).isEqualTo("1234567890");
			assertThat(caseStatus.getCaseId()).isEqualTo("1");
			assertThat(caseStatus.getStatus()).isEqualTo("STATUS_TYPE");
			assertThat(caseStatus.getTimestamp()).isCloseTo(LocalDateTime.now(), within(5, ChronoUnit.SECONDS));
			assertThat(caseStatus.getServiceName()).isEqualTo("VALUE1");
		});
		verify(caseDataPropertiesMock).namespaces();
		verify(caseDataClientMock).getErrands(MUNICIPALITY_ID, namespace1, "filter", "1000");
		verify(caseDataClientMock).getErrands(MUNICIPALITY_ID, namespace2, "filter", "1000");
		verifyNoMoreInteractions(caseDataClientMock, caseDataPropertiesMock);
		verifyNoInteractions(caseMappingServiceMock);
	}

	@Test
	void toCaseStatusDTO() {
		final var errand = createErrand();

		final var result = caseDataService.toCaseStatusDTO(errand);

		assertThat(result.getCaseId()).isEqualTo("1");
		assertThat(result.getExternalCaseId()).isEqualTo("1234567890");
		assertThat(result.getCaseType()).isEqualTo("CASE_TYPE");
		assertThat(result.getSystem()).isEqualTo(SystemType.CASE_DATA);
		assertThat(result.getStatus()).isEqualTo("STATUS_TYPE");
		assertThat(result.getTimestamp()).isCloseTo(LocalDateTime.now(), within(5, ChronoUnit.SECONDS));
		assertThat(result.getServiceName()).isEqualTo("VALUE1");
		assertThat(result.getErrandNumber()).isEqualTo(errand.getErrandNumber());
		assertThat(result.getNamespace()).isEqualTo(errand.getNamespace());
		verifyNoInteractions(caseMappingServiceMock, caseDataPropertiesMock, caseDataClientMock);
	}

	private Errand createErrand() {
		final var errand = new Errand();
		errand.setExternalCaseId("1234567890");
		errand.setId(1L);
		errand.setCaseType("CASE_TYPE");
		errand.setStatuses(List.of(createStatus()));
		errand.setExtraParameters(List.of(createExtraParameter()));
		errand.setErrandNumber("CaseData 2024-0000001");
		errand.setNamespace("namespace1");
		return errand;
	}

	private generated.client.casedata.Status createStatus() {
		final var status = new generated.client.casedata.Status();
		status.setStatusType("STATUS_TYPE");
		status.setCreated(now());
		status.setDescription("DESCRIPTION");
		return status;
	}

	private generated.client.casedata.ExtraParameter createExtraParameter() {
		final var extraParameter = new generated.client.casedata.ExtraParameter();
		extraParameter.setKey("serviceName");
		extraParameter.setValues(List.of("VALUE1", "VALUE2"));
		return extraParameter;
	}

	private OtherCaseDTO createCase(final CaseType caseType) {
		final var otherCase = new OtherCaseDTO();
		otherCase.setCaseType(caseType.toString());
		otherCase.setExternalCaseId(UUID.randomUUID().toString());
		otherCase.setCaseTitleAddition("Some case title addition");
		otherCase.setDescription("Some random description");

		otherCase.setStakeholders(List.of(
			TestUtil.createStakeholderDTO(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT.toString(), StakeholderRole.CONTACT_PERSON.toString())),
			TestUtil.createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.PAYMENT_PERSON.toString(), StakeholderRole.INVOICE_RECIPIENT.toString()))));

		otherCase.setAttachments(List.of(
			TestUtil.createAttachment(AttachmentCategory.BUILDING_PERMIT_APPLICATION),
			TestUtil.createAttachment(AttachmentCategory.ANMALAN_VARMEPUMP),
			TestUtil.createAttachment(AttachmentCategory.ANMALAN_VARMEPUMP)));

		otherCase.setExtraParameters(TestUtil.createExtraParameters());
		otherCase.getExtraParameters().put("application.priority", "HIGH");
		return otherCase;
	}

}
