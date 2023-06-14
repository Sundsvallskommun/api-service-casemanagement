package se.sundsvall.casemanagement;

import static se.sundsvall.casemanagement.TestUtil.OBJECT_MAPPER;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.testutils.CustomAbstractAppTest;
import se.sundsvall.casemanagement.testutils.TestConstants;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/AttachmentResourceIntegrationTest", classes = Application.class)
class AttachmentResourceIntegrationTest extends CustomAbstractAppTest {

    @Autowired
    CaseMappingRepository caseMappingRepository;

    @ParameterizedTest
    @EnumSource(SystemType.class)
    void testHappyCase(SystemType systemType) throws JsonProcessingException {
        AttachmentDTO attachmentDTO = new AttachmentDTO();
        attachmentDTO.setCategory(AttachmentCategory.BIL);
        attachmentDTO.setExtension(TestConstants.PDF_EXTENSION);
        attachmentDTO.setMimeType(TestConstants.MIMETYPE_PDF);
        attachmentDTO.setName("Filnamnet");
        attachmentDTO.setNote("Anteckning");
        attachmentDTO.setFile(TestConstants.BASE64_STRING);

        String externalCaseId = UUID.randomUUID().toString();
        String caseId = String.valueOf(new Random().nextInt());

        caseMappingRepository.save(new CaseMapping(externalCaseId, caseId, systemType, CaseType.REGISTRERING_AV_LIVSMEDEL, null));

        setupCall()
            .withHttpMethod(HttpMethod.POST)
            .withServicePath("/cases/" + externalCaseId + "/attachments")
            .withRequest(OBJECT_MAPPER.writeValueAsString(List.of(attachmentDTO)))
            .withExpectedResponseStatus(HttpStatus.NO_CONTENT)
            .sendRequestAndVerifyResponse();
    }

    @ParameterizedTest
    @EnumSource(value = SystemType.class, mode = EnumSource.Mode.EXCLUDE, names = {"CASE_DATA"})
    void testCaseNotFound(SystemType systemType) throws JsonProcessingException {
        AttachmentDTO attachmentDTO = new AttachmentDTO();
        attachmentDTO.setCategory(AttachmentCategory.BIL);
        attachmentDTO.setExtension(TestConstants.PDF_EXTENSION);
        attachmentDTO.setMimeType(TestConstants.MIMETYPE_PDF);
        // Set name to this so that wiremock picks up the correct mapping
        attachmentDTO.setName("ARENDE_NOT_FOUND");
        attachmentDTO.setNote("Anteckning");
        attachmentDTO.setFile(TestConstants.BASE64_STRING);

        String externalCaseId = String.valueOf(new Random().nextLong());
        String caseId = String.valueOf(new Random().nextLong());

        caseMappingRepository.save(new CaseMapping(externalCaseId, caseId, systemType, CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, null));

        setupCall()
            .withHttpMethod(HttpMethod.POST)
            .withServicePath("/cases/" + externalCaseId + "/attachments")
            .withRequest(OBJECT_MAPPER.writeValueAsString(List.of(attachmentDTO)))
            .withExpectedResponseStatus(HttpStatus.BAD_GATEWAY)
            .sendRequestAndVerifyResponse();
    }

    @ParameterizedTest
    @EnumSource(value = SystemType.class, mode = EnumSource.Mode.EXCLUDE, names = {"ECOS"})
    void testInternalServerError(SystemType systemType) throws JsonProcessingException {
        AttachmentDTO attachmentDTO = new AttachmentDTO();
        attachmentDTO.setCategory(AttachmentCategory.BIL);
        attachmentDTO.setExtension(TestConstants.PDF_EXTENSION);
        attachmentDTO.setMimeType(TestConstants.MIMETYPE_PDF);
        // Set name to this so that wiremock picks up the correct mapping
        attachmentDTO.setName("INTERNAL_SERVER_ERROR");
        attachmentDTO.setNote("Anteckning");
        attachmentDTO.setFile(TestConstants.BASE64_STRING);

        String externalCaseId = String.valueOf(new Random().nextLong());
        String caseId = String.valueOf(new Random().nextLong());

        caseMappingRepository.save(new CaseMapping(externalCaseId, caseId, systemType, CaseType.ANMALAN_HALSOSKYDDSVERKSAMHET, null));

        setupCall()
            .withHttpMethod(HttpMethod.POST)
            .withServicePath("/cases/" + externalCaseId + "/attachments")
            .withRequest(OBJECT_MAPPER.writeValueAsString(List.of(attachmentDTO)))
            .withExpectedResponseStatus(HttpStatus.BAD_GATEWAY)
            .sendRequestAndVerifyResponse();
    }

    @ParameterizedTest
    @EnumSource(value = SystemType.class, mode = EnumSource.Mode.INCLUDE, names = {"ECOS"})
    void testInternalServerErrorEcos(SystemType systemType) throws JsonProcessingException {
        AttachmentDTO attachmentDTO = new AttachmentDTO();
        attachmentDTO.setCategory(AttachmentCategory.BIL);
        attachmentDTO.setExtension(TestConstants.PDF_EXTENSION);
        attachmentDTO.setMimeType(TestConstants.MIMETYPE_PDF);
        // Set name to this so that wiremock picks up the correct mapping
        attachmentDTO.setName("INTERNAL_SERVER_ERROR");
        attachmentDTO.setNote("Anteckning");
        attachmentDTO.setFile(TestConstants.BASE64_STRING);

        String externalCaseId = String.valueOf(new Random().nextLong());
        String caseId = String.valueOf(new Random().nextLong());

        caseMappingRepository.save(new CaseMapping(externalCaseId, caseId, systemType, CaseType.ANMALAN_HALSOSKYDDSVERKSAMHET, null));

        setupCall()
            .withHttpMethod(HttpMethod.POST)
            .withServicePath("/cases/" + externalCaseId + "/attachments")
            .withRequest(OBJECT_MAPPER.writeValueAsString(List.of(attachmentDTO)))
            .withExpectedResponseStatus(HttpStatus.BAD_GATEWAY)
            .sendRequestAndVerifyResponse();
    }

    @ParameterizedTest
    @EnumSource(value = SystemType.class, mode = EnumSource.Mode.EXCLUDE, names = {"CASE_DATA"})
    void testInternalServerErrorSOAPObjectReference(SystemType systemType) throws JsonProcessingException {
        AttachmentDTO attachmentDTO = new AttachmentDTO();
        attachmentDTO.setCategory(AttachmentCategory.BIL);
        attachmentDTO.setExtension(TestConstants.PDF_EXTENSION);
        attachmentDTO.setMimeType(TestConstants.MIMETYPE_PDF);
        // Set name to this so that wiremock picks up the correct mapping
        attachmentDTO.setName("INTERNAL_SERVER_ERROR_OBJECT_REFERENCE");
        attachmentDTO.setNote("Anteckning");
        attachmentDTO.setFile(TestConstants.BASE64_STRING);

        String externalCaseId = String.valueOf(new Random().nextLong());
        String caseId = String.valueOf(new Random().nextLong());

        caseMappingRepository.save(new CaseMapping(externalCaseId, caseId, systemType, CaseType.ANMALAN_HALSOSKYDDSVERKSAMHET, null));

        setupCall()
            .withHttpMethod(HttpMethod.POST)
            .withServicePath("/cases/" + externalCaseId + "/attachments")
            .withRequest(OBJECT_MAPPER.writeValueAsString(List.of(attachmentDTO)))
            .withExpectedResponseStatus(HttpStatus.BAD_GATEWAY)
            .sendRequestAndVerifyResponse();
    }

    @ParameterizedTest
    @EnumSource(SystemType.class)
    void testOnlyMandatoryAttachmentFields(SystemType systemType) throws JsonProcessingException {
        AttachmentDTO attachmentDTO = new AttachmentDTO();
        attachmentDTO.setCategory(AttachmentCategory.BIL);
        attachmentDTO.setName("TestName");
        attachmentDTO.setExtension(".PNG");
        attachmentDTO.setFile(TestConstants.BASE64_STRING);

        String externalCaseId = UUID.randomUUID().toString();
        String caseId = String.valueOf(new Random().nextLong());

        caseMappingRepository.save(new CaseMapping(externalCaseId, caseId, systemType, CaseType.ANMALAN_ANDRING_AVLOPPSANLAGGNING, null));

        setupCall()
            .withHttpMethod(HttpMethod.POST)
            .withServicePath("/cases/" + externalCaseId + "/attachments")
            .withRequest(OBJECT_MAPPER.writeValueAsString(List.of(attachmentDTO)))
            .withExpectedResponseStatus(HttpStatus.BAD_REQUEST)
            .sendRequestAndVerifyResponse();
    }

    @Test
    void testCaseInsensitiveAttachmentFields() throws JsonProcessingException {
        AttachmentDTO attachmentDTO = new AttachmentDTO();
        attachmentDTO.setCategory(AttachmentCategory.BIL);
        attachmentDTO.setExtension(".pDF");
        attachmentDTO.setMimeType("APPLICATION/pdF");
        attachmentDTO.setName("TestName");
        attachmentDTO.setNote("Anteckning");
        attachmentDTO.setFile(TestConstants.BASE64_STRING);

        String externalCaseId = UUID.randomUUID().toString();
        String caseId = UUID.randomUUID().toString();

        caseMappingRepository.save(new CaseMapping(externalCaseId, caseId, SystemType.BYGGR, CaseType.ANMALAN_ATTEFALL, null));

        setupCall()
            .withHttpMethod(HttpMethod.POST)
            .withServicePath("/cases/" + externalCaseId + "/attachments")
            .withRequest(OBJECT_MAPPER.writeValueAsString(List.of(attachmentDTO)))
            .withExpectedResponseStatus(HttpStatus.NO_CONTENT)
            .sendRequestAndVerifyResponse();
    }
}
