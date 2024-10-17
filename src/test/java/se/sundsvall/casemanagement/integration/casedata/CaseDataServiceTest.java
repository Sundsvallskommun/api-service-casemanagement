package se.sundsvall.casemanagement.integration.casedata;

import generated.client.casedata.Errand;
import generated.client.casedata.Errand.ChannelEnum;
import generated.client.casedata.PatchErrand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casemanagement.TestUtil;
import se.sundsvall.casemanagement.api.model.CaseDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.api.model.enums.*;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.service.CaseMappingService;
import se.sundsvall.casemanagement.util.Constants;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.*;
import static se.sundsvall.casemanagement.integration.casedata.CaseDataMapper.toAttachment;
import static se.sundsvall.casemanagement.util.Constants.SERVICE_NAME;

@ExtendWith(MockitoExtension.class)
class CaseDataServiceTest {

	private static final String MUNICIPALITY_ID = "2281";

	@InjectMocks
	private CaseDataService caseDataService;

	@Mock
	private CaseMappingService caseMappingServiceMock;

	@Mock
	private CaseDataClient caseDataClientMock;

	@Captor
	private ArgumentCaptor<PatchErrand> patchErrandArgumentCaptor;

	@Captor
	private ArgumentCaptor<List<generated.client.casedata.Status>> statusListArgumentCaptor;

	@Captor
	private ArgumentCaptor<List<generated.client.casedata.Stakeholder>> stakeholderListArgumentCaptor;

	@Captor
	private ArgumentCaptor<generated.client.casedata.Attachment> attachmentArgumentCaptor;

	@ParameterizedTest
	@EnumSource(value = CaseType.class, names = { PARKING_PERMIT, LOST_PARKING_PERMIT, PARKING_PERMIT_RENEWAL })
	void testPostCases(final CaseType caseType) throws URISyntaxException {
		// Arrange
		final var errandId = new Random().nextLong();
		final var uri = new URI("https://sundsvall-test.se/errands/" + errandId);
		final var getErrand = new Errand();
		getErrand.setErrandNumber("Inskickat");
		final var inputCase = createCase(caseType);
		final var municipalityId = "2281";
		final var namspace = "PRH";

		// Mock
		when(caseDataClientMock.postErrands(eq(MUNICIPALITY_ID), eq(namspace), any())).thenReturn(ResponseEntity.created(uri).build());
		when(caseDataClientMock.getErrand(MUNICIPALITY_ID, namspace, errandId)).thenReturn(getErrand);

		// Act
		final var response = caseDataService.postErrand(inputCase, municipalityId);

		// Assert
		assertThat(response).isEqualTo("Inskickat");

		final var errandArgumentCaptor = ArgumentCaptor.forClass(Errand.class);
		verify(caseDataClientMock).postErrands(eq(MUNICIPALITY_ID), eq(namspace), errandArgumentCaptor.capture());
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
		assertThat(errand.getStatuses().getFirst().getStatusType()).isEqualTo("Ärende inkommit");
		assertThat(errand.getStatuses().getFirst().getDateTime()).isNotNull();

		attachmentArgumentCaptor = ArgumentCaptor.forClass(generated.client.casedata.Attachment.class);
		verify(caseDataClientMock, times(3)).postAttachment(eq(MUNICIPALITY_ID), eq(namspace), eq(errandId), attachmentArgumentCaptor.capture());
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
		final var errandId = new Random().nextLong();
		final var inputCase = createCase(CaseType.PARKING_PERMIT);
		final var namspace = "PRH";
		// Mock
		when(caseDataClientMock.patchErrand(eq(MUNICIPALITY_ID), eq(namspace), any(), any())).thenReturn(null);
		when(caseDataClientMock.putStatusOnErrand(eq(MUNICIPALITY_ID), eq(namspace), any(), any())).thenReturn(null);
		when(caseDataClientMock.putStakeholdersOnErrand(eq(MUNICIPALITY_ID), eq(namspace), any(), any())).thenReturn(null);
		when(caseDataClientMock.postAttachment(eq(MUNICIPALITY_ID), eq(namspace), eq(errandId), any())).thenReturn(null);

		// Act
		caseDataService.putErrand(errandId, inputCase, MUNICIPALITY_ID);

		// Assert
		verify(caseDataClientMock, times(1)).patchErrand(eq(MUNICIPALITY_ID), eq(namspace), eq(errandId), patchErrandArgumentCaptor.capture());
		verify(caseDataClientMock, times(1)).putStakeholdersOnErrand(eq(MUNICIPALITY_ID), eq(namspace), eq(errandId), stakeholderListArgumentCaptor.capture());
		verify(caseDataClientMock, times(3)).postAttachment(eq(MUNICIPALITY_ID), eq(namspace), eq(errandId), attachmentArgumentCaptor.capture());
		verify(caseDataClientMock, times(1)).putStatusOnErrand(eq(MUNICIPALITY_ID), eq(namspace), eq(errandId), statusListArgumentCaptor.capture());

		final var patchErrand = patchErrandArgumentCaptor.getValue();
		assertThat(patchErrand.getCaseType()).isEqualTo(PatchErrand.CaseTypeEnum.fromValue(inputCase.getCaseType()));
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

		final var status = statusListArgumentCaptor.getValue();
		assertThat(status).hasSize(1);
		assertThat(status.getFirst().getStatusType()).isEqualTo("Komplettering inkommen");
		assertThat(status.getFirst().getDateTime()).isNotNull();

		assertThat(stakeholderListArgumentCaptor.getValue()).hasSameSizeAs(inputCase.getStakeholders());
		assertThat(attachmentArgumentCaptor.getValue().getCategory()).isEqualTo(AttachmentCategory.ANMALAN_VARMEPUMP.toString());
	}

	@Test
	void testGetStatus() {
		// Arrange
		final var caseId = new Random().nextLong();
		final var errandMock = new Errand();
		final var namespace = "PRH";
		errandMock.setId(caseId);
		final var statusMock1 = new generated.client.casedata.Status()
			.statusType(RandomStringUtils.random(10, true, false))
			.dateTime(OffsetDateTime.now().minusDays(10))
			.description(RandomStringUtils.random(10, true, false));
		final var statusMock2 = new generated.client.casedata.Status()
			.statusType(RandomStringUtils.random(10, true, false))
			.dateTime(OffsetDateTime.now().minusDays(5))
			.description(RandomStringUtils.random(10, true, false));
		final var statusMock3 = new generated.client.casedata.Status()
			.statusType(RandomStringUtils.random(10, true, false))
			.dateTime(OffsetDateTime.now().minusDays(20))
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
		assertThat(result.getTimestamp()).isEqualTo(statusMock2.getDateTime().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime());
	}

	@Test
	void testGetStatusErrandNotFound() {
		// Arrange
		final var caseMapping = CaseMapping.builder().withCaseId("1").build();
		final var namespace = "OTHER";
		// Mock
		when(caseDataClientMock.getErrand(eq(MUNICIPALITY_ID), eq(namespace), any())).thenThrow(Problem.valueOf(Status.NOT_FOUND));
		// Act
		assertThatThrownBy(() -> caseDataService.getStatus(caseMapping, MUNICIPALITY_ID)
		)
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasMessage("Not Found: No case was found in CaseData with caseId: 1");
	}

	@Test
	void testGetStatusNotFound() {
		// Arrange
		final var caseMapping = CaseMapping.builder().withCaseId("1").build();
		// Act and assert
		assertThatThrownBy(() -> caseDataService.getStatus(caseMapping, MUNICIPALITY_ID))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", Constants.ERR_MSG_STATUS_NOT_FOUND);
	}

	@Test
	void patchErrandWithAttachmentNotFound() {
		final var errandNumber = UUID.randomUUID().toString();
		final var errandId = new Random().nextLong();
		final var attachment = new se.sundsvall.casemanagement.api.model.AttachmentDTO();
		final var attachments = List.of(attachment);
		final var namespace = "OTHER";
		final var caseMapping = CaseMapping.builder().withCaseId(String.valueOf(errandId)).withExternalCaseId(errandNumber).build();

		when(caseDataClientMock.getErrand(MUNICIPALITY_ID, namespace, errandId)).thenReturn(new Errand());
		when(caseDataClientMock.postAttachment(MUNICIPALITY_ID, namespace, errandId, toAttachment(attachment, null))).thenThrow(Problem.valueOf(Status.NOT_FOUND));

		assertThatThrownBy(() -> caseDataService.patchErrandWithAttachment(caseMapping, attachments, MUNICIPALITY_ID))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "No case was found in CaseData with caseId: " + errandId);
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
