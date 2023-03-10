package se.sundsvall.casemanagement.service;

import generated.client.casedata.ErrandDTO;
import generated.client.casedata.PatchErrandDTO;
import generated.client.casedata.StakeholderDTO;
import generated.client.casedata.StatusDTO;
import org.junit.jupiter.api.Assertions;
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
import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.api.model.enums.StakeholderType;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.integration.rest.casedata.CaseDataClient;
import se.sundsvall.casemanagement.service.util.Constants;
import se.sundsvall.casemanagement.testutils.TestConstants;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Constants.LOST_PARKING_PERMIT_VALUE;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Constants.PARKING_PERMIT_RENEWAL_VALUE;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Constants.PARKING_PERMIT_VALUE;

@ExtendWith(MockitoExtension.class)
class CaseDataServiceTest {

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
    private ArgumentCaptor<List<generated.client.casedata.AttachmentDTO>> attachmentDTOListArgumentCaptor;

    @ParameterizedTest
    @EnumSource(value = CaseType.class, mode = EnumSource.Mode.INCLUDE, names = {PARKING_PERMIT_VALUE, LOST_PARKING_PERMIT_VALUE, PARKING_PERMIT_RENEWAL_VALUE})
    void testPostCases(CaseType caseType) throws URISyntaxException {
        long errandId = new Random().nextLong();
        ResponseEntity<Void> mockResponse = ResponseEntity.created(new URI("https://sundsvall-test.se/errands/" + errandId)).build();
        doReturn(mockResponse).when(caseDataClientMock).postErrands(any());

        ErrandDTO getErrandDTO = new ErrandDTO();
        getErrandDTO.setErrandNumber(TestConstants.CASE_DATA_ERRAND_NUMBER);
        doReturn(getErrandDTO).when(caseDataClientMock).getErrand(errandId);

        OtherCaseDTO inputCase = createCase(caseType);
        var response = caseDataService.postErrand(inputCase);
        assertEquals(TestConstants.CASE_DATA_ERRAND_NUMBER, response);

        ArgumentCaptor<ErrandDTO> errandDTOArgumentCaptor = ArgumentCaptor.forClass(ErrandDTO.class);
        verify(caseDataClientMock, times(1)).postErrands(errandDTOArgumentCaptor.capture());
        ErrandDTO errandDTO = errandDTOArgumentCaptor.getValue();
        assertEquals(ErrandDTO.CaseTypeEnum.fromValue(inputCase.getCaseType().toString()), errandDTO.getCaseType());
        assertEquals(inputCase.getExternalCaseId(), errandDTO.getExternalCaseId());
        assertEquals(inputCase.getDescription(), errandDTO.getDescription());
        assertEquals(inputCase.getCaseTitleAddition(), errandDTO.getCaseTitleAddition());
        assertEquals(inputCase.getStakeholders().size(), errandDTO.getStakeholders().size());
        assertEquals(inputCase.getExtraParameters(), errandDTO.getExtraParameters());
        assertEquals("Aktualisering", errandDTO.getPhase());
        assertEquals(ErrandDTO.PriorityEnum.HIGH, errandDTO.getPriority());
        assertEquals("Ärende inkommit", errandDTO.getStatuses().get(0).getStatusType());
        assertNotNull(errandDTO.getStatuses().get(0).getDateTime());

        List<generated.client.casedata.AttachmentDTO> attachmentDTOs = errandDTO.getAttachments();
        assertEquals(3, attachmentDTOs.size());
        for (int i = 0; i < attachmentDTOs.size(); i++) {
            generated.client.casedata.AttachmentDTO attachmentDTO = attachmentDTOs.get(i);
            AttachmentDTO inputAttachment = inputCase.getAttachments().get(i);
            assertEquals(generated.client.casedata.AttachmentDTO.CategoryEnum.fromValue(inputAttachment.getCategory().toString()), attachmentDTO.getCategory());
            assertEquals(inputAttachment.getName(), attachmentDTO.getName());
            assertEquals(inputAttachment.getExtension(), attachmentDTO.getExtension());
            assertEquals(inputAttachment.getMimeType(), attachmentDTO.getMimeType());
            assertEquals(inputAttachment.getFile(), attachmentDTO.getFile());
            assertEquals(inputAttachment.getExtraParameters(), attachmentDTO.getExtraParameters());
        }

        ArgumentCaptor<CaseMapping> caseMappingArgumentCaptor = ArgumentCaptor.forClass(CaseMapping.class);
        verify(caseMappingServiceMock, times(1)).postCaseMapping(caseMappingArgumentCaptor.capture());
        CaseMapping caseMapping = caseMappingArgumentCaptor.getValue();
        assertEquals(inputCase.getExternalCaseId(), caseMapping.getExternalCaseId());
        assertEquals(String.valueOf(errandId), caseMapping.getCaseId());
        assertEquals(SystemType.CASE_DATA, caseMapping.getSystem());
        assertEquals(inputCase.getCaseType(), caseMapping.getCaseType());
        assertNull(caseMapping.getServiceName());
    }

    // Test putErrand
    @Test
    void testPutErrand() throws URISyntaxException {
        long errandId = new Random().nextLong();

        OtherCaseDTO inputCase = createCase(CaseType.PARKING_PERMIT);
        caseDataService.putErrand(errandId, inputCase);

        verify(caseDataClientMock, times(1)).patchErrand(any(), patchErrandDTOArgumentCaptor.capture());
        verify(caseDataClientMock, times(1)).putStatusOnErrand(any(), statusDTOListArgumentCaptor.capture());
        verify(caseDataClientMock, times(1)).putStakeholdersOnErrand(any(), stakeholderDTOListArgumentCaptor.capture());
        verify(caseDataClientMock, times(1)).putAttachmentsOnErrand(any(), attachmentDTOListArgumentCaptor.capture());

        PatchErrandDTO patchErrandDTO = patchErrandDTOArgumentCaptor.getValue();
        assertEquals(PatchErrandDTO.CaseTypeEnum.fromValue(inputCase.getCaseType().toString()), patchErrandDTO.getCaseType());
        assertEquals(inputCase.getExternalCaseId(), patchErrandDTO.getExternalCaseId());
        assertEquals(inputCase.getDescription(), patchErrandDTO.getDescription());
        assertEquals(inputCase.getCaseTitleAddition(), patchErrandDTO.getCaseTitleAddition());
        assertEquals(inputCase.getExtraParameters(), patchErrandDTO.getExtraParameters());
        assertEquals(PatchErrandDTO.PriorityEnum.HIGH, patchErrandDTO.getPriority());
        assertNull(patchErrandDTO.getPhase());
        assertNull(patchErrandDTO.getDiaryNumber());
        assertNull(patchErrandDTO.getMunicipalityId());
        assertNull(patchErrandDTO.getStartDate());
        assertNull(patchErrandDTO.getEndDate());
        assertNull(patchErrandDTO.getApplicationReceived());

        List<StatusDTO> statusDTOs = statusDTOListArgumentCaptor.getValue();
        assertEquals(1, statusDTOs.size());
        assertEquals("Komplettering inkommen", statusDTOs.get(0).getStatusType());
        assertNotNull(statusDTOs.get(0).getDateTime());

        List<StakeholderDTO> stakeholderDTOs = stakeholderDTOListArgumentCaptor.getValue();
        assertEquals(inputCase.getStakeholders().size(), stakeholderDTOs.size());

        List<generated.client.casedata.AttachmentDTO> attachmentDTOs = attachmentDTOListArgumentCaptor.getValue();
        assertEquals(inputCase.getAttachments().size(), attachmentDTOs.size());
    }

    @Test
    void testGetStatus() {
        Long caseId = new Random().nextLong();
        ErrandDTO errandDTOMock = new ErrandDTO();
        errandDTOMock.setId(caseId);
        StatusDTO statusDTOMock_1 = new StatusDTO();
        statusDTOMock_1.setStatusType(RandomStringUtils.random(10, true, false));
        statusDTOMock_1.setDateTime(OffsetDateTime.now().minusDays(10));
        statusDTOMock_1.setDescription(RandomStringUtils.random(10, true, false));
        StatusDTO statusDTOMock_2 = new StatusDTO();
        statusDTOMock_2.setStatusType(RandomStringUtils.random(10, true, false));
        statusDTOMock_2.setDateTime(OffsetDateTime.now().minusDays(5));
        statusDTOMock_2.setDescription(RandomStringUtils.random(10, true, false));
        StatusDTO statusDTOMock_3 = new StatusDTO();
        statusDTOMock_3.setStatusType(RandomStringUtils.random(10, true, false));
        statusDTOMock_3.setDateTime(OffsetDateTime.now().minusDays(20));
        statusDTOMock_3.setDescription(RandomStringUtils.random(10, true, false));
        errandDTOMock.setStatuses(List.of(statusDTOMock_1, statusDTOMock_2, statusDTOMock_3));

        doReturn(errandDTOMock).when(caseDataClientMock).getErrand(caseId);

        CaseMapping caseMapping = new CaseMapping();
        caseMapping.setCaseId(String.valueOf(caseId));
        caseMapping.setExternalCaseId(UUID.randomUUID().toString());
        caseMapping.setSystem(SystemType.CASE_DATA);
        caseMapping.setCaseType(CaseType.PARKING_PERMIT);
        caseMapping.setServiceName(RandomStringUtils.random(10, true, false));

        var result = caseDataService.getStatus(caseMapping);
        assertEquals(caseMapping.getCaseId(), result.getCaseId());
        assertEquals(caseMapping.getExternalCaseId(), result.getExternalCaseId());
        assertEquals(caseMapping.getCaseType(), result.getCaseType());
        assertEquals(caseMapping.getSystem(), result.getSystem());
        assertEquals(caseMapping.getServiceName(), result.getServiceName());
        assertEquals(statusDTOMock_2.getStatusType(), result.getStatus());
        assertEquals(statusDTOMock_2.getDateTime().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime(), result.getTimestamp());
    }

    @Test
    void testGetStatusErrandNotFound() {
        Long caseId = new Random().nextLong();
        doThrow(Problem.valueOf(Status.NOT_FOUND)).when(caseDataClientMock).getErrand(caseId);

        CaseMapping caseMapping = new CaseMapping();
        caseMapping.setCaseId(String.valueOf(caseId));
        caseMapping.setExternalCaseId(UUID.randomUUID().toString());
        caseMapping.setSystem(SystemType.CASE_DATA);
        caseMapping.setCaseType(CaseType.PARKING_PERMIT);
        caseMapping.setServiceName(RandomStringUtils.random(10, true, false));

        var problem = Assertions.assertThrows(ThrowableProblem.class, () -> caseDataService.getStatus(caseMapping));
        assertEquals(Status.NOT_FOUND, problem.getStatus());
        assertEquals("No case was found in CaseData with caseId: " + caseId, problem.getDetail());
    }

    @Test
    void testGetStatusNotFound() {
        Long caseId = new Random().nextLong();
        ErrandDTO errandDTOMock = new ErrandDTO();
        errandDTOMock.setId(caseId);
        errandDTOMock.setStatuses(List.of());
        doReturn(errandDTOMock).when(caseDataClientMock).getErrand(caseId);

        CaseMapping caseMapping = new CaseMapping();
        caseMapping.setCaseId(String.valueOf(caseId));
        caseMapping.setExternalCaseId(UUID.randomUUID().toString());
        caseMapping.setSystem(SystemType.CASE_DATA);
        caseMapping.setCaseType(CaseType.PARKING_PERMIT);
        caseMapping.setServiceName(RandomStringUtils.random(10, true, false));

        var problem = Assertions.assertThrows(ThrowableProblem.class, () -> caseDataService.getStatus(caseMapping));
        assertEquals(Status.NOT_FOUND, problem.getStatus());
        assertEquals(Constants.ERR_MSG_STATUS_NOT_FOUND, problem.getDetail());
    }

    private OtherCaseDTO createCase(CaseType caseType) {
        OtherCaseDTO otherCase = new OtherCaseDTO();
        otherCase.setCaseType(caseType);
        otherCase.setExternalCaseId(UUID.randomUUID().toString());
        otherCase.setCaseTitleAddition("Some case title addition");
        otherCase.setDescription("Some random description");

        otherCase.setStakeholders(List.of(
                TestUtil.createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT, StakeholderRole.CONTACT_PERSON)),
                TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.PAYMENT_PERSON, StakeholderRole.INVOICE_RECIPENT))));

        otherCase.setAttachments(List.of(
                TestUtil.createAttachment(AttachmentCategory.ANS),
                TestUtil.createAttachment(AttachmentCategory.ANMALAN_VARMEPUMP),
                TestUtil.createAttachment(AttachmentCategory.ANMALAN_VARMEPUMP)));

        otherCase.setExtraParameters(TestUtil.createExtraParameters());
        otherCase.getExtraParameters().put("application.priority", "HIGH");
        return otherCase;
    }
}
