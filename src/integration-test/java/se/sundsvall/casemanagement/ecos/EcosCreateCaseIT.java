package se.sundsvall.casemanagement.ecos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import se.sundsvall.casemanagement.Application;
import se.sundsvall.casemanagement.api.model.CaseResourceResponseDTO;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.CaseRepository;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@Testcontainers
@WireMockAppTestSuite(files = "classpath:/EcosCreateCaseIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class EcosCreateCaseIT extends AbstractAppTest {

	private static final String REQUEST = "request.json";
	private static final String RESPONSE = "response.json";
	private static final String ECOS_CASE_ID = "e19981ad-34b2-4e14-88f5-133f61ca85aa";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String PATH = "/" + MUNICIPALITY_ID + "/cases";

	@Autowired
	private CaseMappingRepository caseMappingRepository;

	@Autowired
	private CaseRepository caseRepository;

	@Test
	void test2_AnmalanVarmepump() throws JsonProcessingException, ClassNotFoundException {

		final var result = setupCall()
			.withHttpMethod(POST)
			.withServicePath(PATH)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		// Make sure that there doesn't exist a case entity
		assertThat(caseRepository.findByIdAndMunicipalityId("874407364", MUNICIPALITY_ID)).isEmpty();
		// Make sure that there exists a case mapping
		assertThat(caseMappingRepository.findAllByExternalCaseIdAndMunicipalityId("874407364", MUNICIPALITY_ID))
			.isNotNull()
			.hasSize(1)
			.allSatisfy(caseMapping -> {
				assertThat(caseMapping.getExternalCaseId()).isEqualTo("874407364");
				assertThat(caseMapping.getCaseType()).isEqualTo(CaseType.ANMALAN_INSTALLATION_VARMEPUMP.toString());
				assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
				assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
			});

		caseMappingRepository.delete(caseMappingRepository.findAllByExternalCaseIdAndMunicipalityId("874407364", MUNICIPALITY_ID).getFirst());
	}

	@Test
	void test3_TillstandVarmepump() throws JsonProcessingException, ClassNotFoundException {
		final var EXTERNAL_CASE_ID = "414646967";

		final var result = setupCall()
			.withHttpMethod(POST)
			.withServicePath(PATH)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		// Make sure that there doesn't exist a case entity
		assertThat(caseRepository.findByIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID)).isEmpty();
		// Make sure that there exists a case mapping
		assertThat(caseMappingRepository.findAllByExternalCaseIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID))
			.isNotNull()
			.hasSize(1)
			.allSatisfy(caseMapping -> {
				assertThat(caseMapping.getExternalCaseId()).isEqualTo(EXTERNAL_CASE_ID);
				assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
				assertThat(caseMapping.getCaseType()).isEqualTo(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP.toString());
				assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
			});

		caseMappingRepository.delete(caseMappingRepository.findAllByExternalCaseIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID).getFirst());
	}

	@Test
	void test4_AnsokanAvlopp() throws JsonProcessingException, ClassNotFoundException {
		final var EXTERNAL_CASE_ID = "869540711";

		final var result = setupCall()
			.withHttpMethod(POST)
			.withServicePath(PATH)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		// Make sure that there doesn't exist a case entity
		assertThat(caseRepository.findByIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID)).isEmpty();
		// Make sure that there exists a case mapping
		assertThat(caseMappingRepository.findAllByExternalCaseIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID))
			.isNotNull()
			.hasSize(1)
			.allSatisfy(caseMapping -> {
				assertThat(caseMapping.getExternalCaseId()).isEqualTo(EXTERNAL_CASE_ID);
				assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
				assertThat(caseMapping.getCaseType()).isEqualTo(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP.toString());
				assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
			});

		caseMappingRepository.delete(caseMappingRepository.findAllByExternalCaseIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID).getFirst());

	}

	@Test
	void test5_AnmalanAvlopp() throws JsonProcessingException, ClassNotFoundException {
		final var EXTERNAL_CASE_ID = "1569613013";

		final var result = setupCall()
			.withHttpMethod(POST)
			.withServicePath(PATH)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		// Make sure that there doesn't exist a case entity
		assertThat(caseRepository.findByIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID)).isEmpty();
		// Make sure that there exists a case mapping
		assertThat(caseMappingRepository.findAllByExternalCaseIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID))
			.isNotNull()
			.hasSize(1)
			.allSatisfy(caseMapping -> {
				assertThat(caseMapping.getExternalCaseId()).isEqualTo(EXTERNAL_CASE_ID);
				assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
				assertThat(caseMapping.getCaseType()).isEqualTo(CaseType.ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC.toString());
				assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
			});

		caseMappingRepository.delete(caseMappingRepository.findAllByExternalCaseIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID).getFirst());

	}

	@Test
	void test6_Avloppsanlaggning() throws JsonProcessingException, ClassNotFoundException {
		final var EXTERNAL_CASE_ID = "325594400";

		final var result = setupCall()
			.withHttpMethod(POST)
			.withServicePath(PATH)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		// Make sure that there doesn't exist a case entity
		assertThat(caseRepository.findByIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID)).isEmpty();
		// Make sure that there exists a case mapping
		assertThat(caseMappingRepository.findAllByExternalCaseIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID))
			.isNotNull()
			.hasSize(1)
			.allSatisfy(caseMapping -> {
				assertThat(caseMapping.getExternalCaseId()).isEqualTo(EXTERNAL_CASE_ID);
				assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
				assertThat(caseMapping.getCaseType()).isEqualTo(CaseType.ANMALAN_ANDRING_AVLOPPSANLAGGNING.toString());
				assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
			});

		caseMappingRepository.delete(caseMappingRepository.findAllByExternalCaseIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID).getFirst());

	}

	@Test
	void test7_Avloppsanordning() throws JsonProcessingException, ClassNotFoundException {
		final var EXTERNAL_CASE_ID = "516589049";

		final var result = setupCall()
			.withHttpMethod(POST)
			.withServicePath(PATH)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		// Make sure that there doesn't exist a case entity
		assertThat(caseRepository.findByIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID)).isEmpty();
		// Make sure that there exists a case mapping
		assertThat(caseMappingRepository.findAllByExternalCaseIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID))
			.isNotNull()
			.hasSize(1)
			.allSatisfy(caseMapping -> {
				assertThat(caseMapping.getExternalCaseId()).isEqualTo(EXTERNAL_CASE_ID);
				assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
				assertThat(caseMapping.getCaseType()).isEqualTo(CaseType.ANMALAN_ANDRING_AVLOPPSANORDNING.toString());
				assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
			});

		caseMappingRepository.delete(caseMappingRepository.findAllByExternalCaseIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID).getFirst());

	}

	@Test
	void test8_Halsoskyddsverksamhet() throws JsonProcessingException, ClassNotFoundException {

		final var EXTERNAL_CASE_ID = "1097173756";

		final var result = setupCall()
			.withHttpMethod(POST)
			.withServicePath(PATH)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		// Make sure that there doesn't exist a case entity
		assertThat(caseRepository.findByIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID)).isEmpty();
		// Make sure that there exists a case mapping
		assertThat(caseMappingRepository.findAllByExternalCaseIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID))
			.isNotNull()
			.hasSize(1)
			.allSatisfy(caseMapping -> {
				assertThat(caseMapping.getExternalCaseId()).isEqualTo(EXTERNAL_CASE_ID);
				assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
				assertThat(caseMapping.getCaseType()).isEqualTo(CaseType.ANMALAN_HALSOSKYDDSVERKSAMHET.toString());
				assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
			});

		caseMappingRepository.delete(caseMappingRepository.findAllByExternalCaseIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID).getFirst());

	}

	@Test
	void test9_Livsmedel() throws JsonProcessingException, ClassNotFoundException {
		final var EXTERNAL_CASE_ID = "1195222212";

		final var result = setupCall()
			.withHttpMethod(POST)
			.withServicePath(PATH)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		// Make sure that there doesn't exist a case entity
		assertThat(caseRepository.findByIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID)).isEmpty();
		// Make sure that there exists a case mapping
		assertThat(caseMappingRepository.findAllByExternalCaseIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID))
			.isNotNull()
			.hasSize(1)
			.allSatisfy(caseMapping -> {
				assertThat(caseMapping.getExternalCaseId()).isEqualTo(EXTERNAL_CASE_ID);
				assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
				assertThat(caseMapping.getCaseType()).isEqualTo(CaseType.REGISTRERING_AV_LIVSMEDEL.toString());
				assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
			});

		caseMappingRepository.delete(caseMappingRepository.findAllByExternalCaseIdAndMunicipalityId(EXTERNAL_CASE_ID, MUNICIPALITY_ID).getFirst());

	}

	/**
	 * Test case for creating an Ecos case with case type: ANDRING_AV_LIVSMEDELSVERKSAMHET
	 */
	@Test
	void test10_createCase_10() throws JsonProcessingException, ClassNotFoundException {
		final var externalCaseId = "1195222321";

		var result = setupCall()
			.withServicePath(PATH)
			.withHttpMethod(POST)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		// Make sure that there doesn't exist a case entity
		assertThat(caseRepository.findByIdAndMunicipalityId(externalCaseId, MUNICIPALITY_ID)).isEmpty();
		// Make sure that there exists a case mapping
		assertThat(caseMappingRepository.findAllByExternalCaseIdAndMunicipalityId(externalCaseId, MUNICIPALITY_ID))
			.isNotNull()
			.hasSize(1)
			.allSatisfy(caseMapping -> {
				assertThat(caseMapping.getExternalCaseId()).isEqualTo(externalCaseId);
				assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
				assertThat(caseMapping.getCaseType()).isEqualTo(CaseType.ANDRING_AV_LIVSMEDELSVERKSAMHET.toString());
				assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
			});

		caseMappingRepository.delete(caseMappingRepository.findAllByExternalCaseIdAndMunicipalityId(externalCaseId, MUNICIPALITY_ID).getFirst());
	}

	/**
	 * Test case for creating an Ecos case with case type: INFORMATION_OM_UPPHORANDE_AV_VERKSAMHET
	 */
	@Test
	void test11_createCase_11() throws JsonProcessingException, ClassNotFoundException {
		final var externalCaseId = "1195222123";

		var result = setupCall()
			.withServicePath(PATH)
			.withHttpMethod(POST)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		// Make sure that there doesn't exist a case entity
		assertThat(caseRepository.findByIdAndMunicipalityId(externalCaseId, MUNICIPALITY_ID)).isEmpty();
		// Make sure that there exists a case mapping
		assertThat(caseMappingRepository.findAllByExternalCaseIdAndMunicipalityId(externalCaseId, MUNICIPALITY_ID))
			.isNotNull()
			.hasSize(1)
			.allSatisfy(caseMapping -> {
				assertThat(caseMapping.getExternalCaseId()).isEqualTo(externalCaseId);
				assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
				assertThat(caseMapping.getCaseType()).isEqualTo(CaseType.INFORMATION_OM_UPPHORANDE_AV_VERKSAMHET.toString());
				assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
			});

		caseMappingRepository.delete(caseMappingRepository.findAllByExternalCaseIdAndMunicipalityId(externalCaseId, MUNICIPALITY_ID).getFirst());
	}

}
