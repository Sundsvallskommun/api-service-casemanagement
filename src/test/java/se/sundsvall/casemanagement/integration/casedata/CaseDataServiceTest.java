package se.sundsvall.casemanagement.integration.casedata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.LOST_PARKING_PERMIT;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.PARKING_PERMIT;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.PARKING_PERMIT_RENEWAL;
import static se.sundsvall.casemanagement.integration.casedata.CaseDataMapper.toAttachment;
import static se.sundsvall.casemanagement.util.Constants.SERVICE_NAME;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
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
import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.api.model.enums.StakeholderType;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.service.CaseMappingService;
import se.sundsvall.casemanagement.util.Constants;

import generated.client.casedata.ErrandDTO;
import generated.client.casedata.ErrandDTO.ChannelEnum;
import generated.client.casedata.PatchErrandDTO;
import generated.client.casedata.StatusDTO;

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
	private ArgumentCaptor<PatchErrandDTO> patchErrandDTOArgumentCaptor;

	@Captor
	private ArgumentCaptor<List<StatusDTO>> statusDTOListArgumentCaptor;

	@Captor
	private ArgumentCaptor<List<generated.client.casedata.StakeholderDTO>> stakeholderDTOListArgumentCaptor;

	@Captor
	private ArgumentCaptor<generated.client.casedata.AttachmentDTO> attachmentDTOArgumentCaptor;

	@ParameterizedTest
	@EnumSource(value = CaseType.class, names = {PARKING_PERMIT, LOST_PARKING_PERMIT, PARKING_PERMIT_RENEWAL})
	void testPostCases(final CaseType caseType) throws URISyntaxException {
		// Arrange
		final var errandId = new Random().nextLong();
		final var uri = new URI("https://sundsvall-test.se/errands/" + errandId);
		final var getErrandDTO = new ErrandDTO();
		getErrandDTO.setErrandNumber("Inskickat");
		final var inputCase = createCase(caseType);
		final var municipalityId = "2281";

		// Mock
		when(caseDataClientMock.postErrands(eq(MUNICIPALITY_ID), any())).thenReturn(ResponseEntity.created(uri).build());
		when(caseDataClientMock.getErrand(MUNICIPALITY_ID, errandId)).thenReturn(getErrandDTO);

		// Act
		final var response = caseDataService.postErrand(inputCase, municipalityId);

		// Assert
		assertThat(response).isEqualTo("Inskickat");

		final var errandDTOArgumentCaptor = ArgumentCaptor.forClass(ErrandDTO.class);
		verify(caseDataClientMock).postErrands(eq(MUNICIPALITY_ID), errandDTOArgumentCaptor.capture());
		final var errandDTO = errandDTOArgumentCaptor.getValue();

		assertThat(errandDTO.getCaseTitleAddition()).isEqualTo(inputCase.getCaseTitleAddition());
		assertThat(errandDTO.getCaseType()).isEqualTo(inputCase.getCaseType());
		assertThat(errandDTO.getChannel()).isEqualTo(ChannelEnum.ESERVICE);
		assertThat(errandDTO.getDescription()).isEqualTo(inputCase.getDescription());
		assertThat(errandDTO.getExtraParameters()).isEqualTo(inputCase.getExtraParameters());
		assertThat(errandDTO.getExternalCaseId()).isEqualTo(inputCase.getExternalCaseId());
		assertThat(errandDTO.getPhase()).isEqualTo("Aktualisering");
		assertThat(errandDTO.getPriority()).isEqualTo(ErrandDTO.PriorityEnum.HIGH);
		assertThat(errandDTO.getStakeholders()).hasSameSizeAs(inputCase.getStakeholders());
		assertThat(errandDTO.getStatuses().getFirst().getStatusType()).isEqualTo("Ärende inkommit");
		assertThat(errandDTO.getStatuses().getFirst().getDateTime()).isNotNull();

		attachmentDTOArgumentCaptor = ArgumentCaptor.forClass(generated.client.casedata.AttachmentDTO.class);
		verify(caseDataClientMock, times(3)).postAttachment(eq(MUNICIPALITY_ID), attachmentDTOArgumentCaptor.capture());
		final var attachmentDTO = attachmentDTOArgumentCaptor.getValue();
		assertThat(attachmentDTO).isNotNull();
		assertThat(attachmentDTO.getCategory()).isEqualTo(AttachmentCategory.ANMALAN_VARMEPUMP.toString());

		final var caseDTOArgumentCaptor = ArgumentCaptor.forClass(CaseDTO.class);
		verify(caseMappingServiceMock).postCaseMapping(caseDTOArgumentCaptor.capture(), any(String.class), any(SystemType.class), eq(MUNICIPALITY_ID));
		final var caseMapping = caseDTOArgumentCaptor.getValue();
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

		// Mock
		when(caseDataClientMock.patchErrand(eq(MUNICIPALITY_ID), any(), any())).thenReturn(null);
		when(caseDataClientMock.putStatusOnErrand(eq(MUNICIPALITY_ID), any(), any())).thenReturn(null);
		when(caseDataClientMock.putStakeholdersOnErrand(eq(MUNICIPALITY_ID), any(), any())).thenReturn(null);
		when(caseDataClientMock.postAttachment(eq(MUNICIPALITY_ID), any())).thenReturn(null);

		// Act
		caseDataService.putErrand(errandId, inputCase, MUNICIPALITY_ID);

		// Assert
		verify(caseDataClientMock, times(1)).patchErrand(eq(MUNICIPALITY_ID), eq(errandId), patchErrandDTOArgumentCaptor.capture());
		verify(caseDataClientMock, times(1)).putStakeholdersOnErrand(eq(MUNICIPALITY_ID), eq(errandId), stakeholderDTOListArgumentCaptor.capture());
		verify(caseDataClientMock, times(3)).postAttachment(eq(MUNICIPALITY_ID), attachmentDTOArgumentCaptor.capture());
		verify(caseDataClientMock, times(1)).putStatusOnErrand(eq(MUNICIPALITY_ID), eq(errandId), statusDTOListArgumentCaptor.capture());

		final var patchErrandDTO = patchErrandDTOArgumentCaptor.getValue();
		assertThat(patchErrandDTO.getCaseType()).isEqualTo(PatchErrandDTO.CaseTypeEnum.fromValue(inputCase.getCaseType()));
		assertThat(patchErrandDTO.getExternalCaseId()).isEqualTo(inputCase.getExternalCaseId());
		assertThat(patchErrandDTO.getDescription()).isEqualTo(inputCase.getDescription());
		assertThat(patchErrandDTO.getCaseTitleAddition()).isEqualTo(inputCase.getCaseTitleAddition());
		assertThat(patchErrandDTO.getExtraParameters()).isEqualTo(inputCase.getExtraParameters());
		assertThat(patchErrandDTO.getPriority()).isEqualTo(PatchErrandDTO.PriorityEnum.HIGH);
		assertThat(patchErrandDTO.getPhase()).isNull();
		assertThat(patchErrandDTO.getDiaryNumber()).isNull();
		assertThat(patchErrandDTO.getStartDate()).isNull();
		assertThat(patchErrandDTO.getEndDate()).isNull();
		assertThat(patchErrandDTO.getApplicationReceived()).isNull();

		final var statusDTOs = statusDTOListArgumentCaptor.getValue();
		assertThat(statusDTOs).hasSize(1);
		assertThat(statusDTOs.getFirst().getStatusType()).isEqualTo("Komplettering inkommen");
		assertThat(statusDTOs.getFirst().getDateTime()).isNotNull();

		assertThat(stakeholderDTOListArgumentCaptor.getValue()).hasSameSizeAs(inputCase.getStakeholders());
		assertThat(attachmentDTOArgumentCaptor.getValue().getCategory()).isEqualTo(AttachmentCategory.ANMALAN_VARMEPUMP.toString());
	}

	@Test
	void testGetStatus() {
		// Arrange
		final var caseId = new Random().nextLong();
		final var errandDTOMock = new ErrandDTO();
		errandDTOMock.setId(caseId);
		final var statusDTOMock1 = new StatusDTO()
			.statusType(RandomStringUtils.random(10, true, false))
			.dateTime(OffsetDateTime.now().minusDays(10))
			.description(RandomStringUtils.random(10, true, false));
		final var statusDTOMock2 = new StatusDTO()
			.statusType(RandomStringUtils.random(10, true, false))
			.dateTime(OffsetDateTime.now().minusDays(5))
			.description(RandomStringUtils.random(10, true, false));
		final var statusDTOMock3 = new StatusDTO()
			.statusType(RandomStringUtils.random(10, true, false))
			.dateTime(OffsetDateTime.now().minusDays(20))
			.description(RandomStringUtils.random(10, true, false));
		errandDTOMock.setStatuses(List.of(statusDTOMock1, statusDTOMock2, statusDTOMock3));

		final var caseMapping = CaseMapping.builder()
			.withCaseId(String.valueOf(caseId))
			.withExternalCaseId(UUID.randomUUID().toString())
			.withSystem(SystemType.CASE_DATA)
			.withCaseType(CaseType.PARKING_PERMIT.toString())
			.withServiceName(RandomStringUtils.random(10, true, false))
			.build();
		// Mock
		when(caseDataClientMock.getErrand(MUNICIPALITY_ID, caseId)).thenReturn(errandDTOMock);

		// Act
		final var result = caseDataService.getStatus(caseMapping, MUNICIPALITY_ID);

		// Assert
		assertThat(result.getCaseId()).isEqualTo(caseMapping.getCaseId());
		assertThat(result.getExternalCaseId()).isEqualTo(caseMapping.getExternalCaseId());
		assertThat(result.getCaseType()).isEqualTo(caseMapping.getCaseType());
		assertThat(result.getSystem()).isEqualTo(caseMapping.getSystem());
		assertThat(result.getServiceName()).isEqualTo(caseMapping.getServiceName());
		assertThat(result.getStatus()).isEqualTo(statusDTOMock2.getStatusType());
		assertThat(result.getTimestamp()).isEqualTo(statusDTOMock2.getDateTime().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime());
	}

	@Test
	void testGetStatusErrandNotFound() {
		// Arrange
		final var caseMapping = CaseMapping.builder().withCaseId("1").build();
		// Mock
		when(caseDataClientMock.getErrand(eq(MUNICIPALITY_ID), any())).thenThrow(Problem.valueOf(Status.NOT_FOUND));
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
		final var attachmentDTO = new se.sundsvall.casemanagement.api.model.AttachmentDTO();
		final var attachments = List.of(attachmentDTO);
		when(caseDataClientMock.postAttachment(MUNICIPALITY_ID, toAttachment(attachmentDTO, errandNumber))).thenThrow(Problem.valueOf(Status.NOT_FOUND));

		assertThatThrownBy(() -> caseDataService.patchErrandWithAttachment(errandNumber, attachments, MUNICIPALITY_ID))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "No case was found in CaseData with caseId: " + errandNumber);
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
