package se.sundsvall.casemanagement.ecos;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;

import se.sundsvall.casemanagement.Application;
import se.sundsvall.casemanagement.api.model.CaseResourceResponseDTO;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.CaseMappingRepository;
import se.sundsvall.casemanagement.integration.db.CaseRepository;
import se.sundsvall.casemanagement.testutils.CustomAbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@Testcontainers
@WireMockAppTestSuite(files = "classpath:/EcosCreateCaseIT/", classes = Application.class)
public class EcosCreateCaseIT extends CustomAbstractAppTest {

	public static final String ECOS_CASE_ID = "e19981ad-34b2-4e14-88f5-133f61ca85aa";


	@Autowired
	private CaseMappingRepository caseMappingRepository;

	@Autowired
	private CaseRepository caseRepository;


	@Test
	void test2_AnmalanVarmepump() throws JsonProcessingException, ClassNotFoundException {


		final var result = setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath("/cases")
			.withRequest("request.json")
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse("expected-response.json")
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		//  Make sure that there doesn't exist a case entity
		assertThat(caseRepository.findById("874407364").isPresent()).isFalse();
		// Make sure that there exists a case mapping
		assertThat(caseMappingRepository.findAllByExternalCaseId("874407364"))
			.isNotNull()
			.hasSize(1)
			.allSatisfy(caseMapping -> {
				assertThat(caseMapping.getExternalCaseId()).isEqualTo("874407364");
				assertThat(caseMapping.getCaseType()).isEqualTo(CaseType.ANMALAN_INSTALLATION_VARMEPUMP);
				assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
				assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
			})
		;

		caseMappingRepository.delete(caseMappingRepository.findAllByExternalCaseId("874407364").get(0));
	}


	@Test
	void test3_TillstandVarmepump() throws JsonProcessingException, ClassNotFoundException {
		final var EXTERNAL_CASE_ID = "414646967";

		final var result = setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath("/cases")
			.withRequest("request.json")
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse("expected-response.json")
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		// Make sure that there doesn't exist a case entity
		assertThat(caseRepository.findById(EXTERNAL_CASE_ID).isPresent()).isFalse();
		// Make sure that there exists a case mapping
		assertThat(caseMappingRepository.findAllByExternalCaseId(EXTERNAL_CASE_ID))
			.isNotNull()
			.hasSize(1)
			.allSatisfy(caseMapping -> {
				assertThat(caseMapping.getExternalCaseId()).isEqualTo(EXTERNAL_CASE_ID);
				assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
				assertThat(caseMapping.getCaseType()).isEqualTo(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP);
				assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
			});

		caseMappingRepository.delete(caseMappingRepository.findAllByExternalCaseId(EXTERNAL_CASE_ID).get(0));
	}


	@Test
	void test4_AnsokanAvlopp() throws JsonProcessingException, ClassNotFoundException {
		final var EXTERNAL_CASE_ID = "869540711";

		final var result = setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath("/cases")
			.withRequest("request.json")
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse("expected-response.json")
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");


		// Make sure that there doesn't exist a case entity
		assertThat(caseRepository.findById(EXTERNAL_CASE_ID).isPresent()).isFalse();
		// Make sure that there exists a case mapping
		assertThat(caseMappingRepository.findAllByExternalCaseId(EXTERNAL_CASE_ID))
			.isNotNull()
			.hasSize(1)
			.allSatisfy(caseMapping -> {
				assertThat(caseMapping.getExternalCaseId()).isEqualTo(EXTERNAL_CASE_ID);
				assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
				assertThat(caseMapping.getCaseType()).isEqualTo(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP);
				assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
			});

		caseMappingRepository.delete(caseMappingRepository.findAllByExternalCaseId(EXTERNAL_CASE_ID).get(0));

	}


	@Test
	void test5_AnmalanAvlopp() throws JsonProcessingException, ClassNotFoundException {
		final var EXTERNAL_CASE_ID = "1569613013";

		final var result = setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath("/cases")
			.withRequest("request.json")
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse("expected-response.json")
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		// Make sure that there doesn't exist a case entity
		assertThat(caseRepository.findById(EXTERNAL_CASE_ID).isPresent()).isFalse();
		// Make sure that there exists a case mapping
		assertThat(caseMappingRepository.findAllByExternalCaseId(EXTERNAL_CASE_ID))
			.isNotNull()
			.hasSize(1)
			.allSatisfy(caseMapping -> {
				assertThat(caseMapping.getExternalCaseId()).isEqualTo(EXTERNAL_CASE_ID);
				assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
				assertThat(caseMapping.getCaseType()).isEqualTo(CaseType.ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC);
				assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
			});

		caseMappingRepository.delete(caseMappingRepository.findAllByExternalCaseId(EXTERNAL_CASE_ID).get(0));

	}


	@Test
	void test6_Avloppsanlaggning() throws JsonProcessingException, ClassNotFoundException {
		final var EXTERNAL_CASE_ID = "325594400";

		final var result = setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath("/cases")
			.withRequest("request.json")
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse("expected-response.json")
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		// Make sure that there doesn't exist a case entity
		assertThat(caseRepository.findById(EXTERNAL_CASE_ID).isPresent()).isFalse();
		// Make sure that there exists a case mapping
		assertThat(caseMappingRepository.findAllByExternalCaseId(EXTERNAL_CASE_ID))
			.isNotNull()
			.hasSize(1)
			.allSatisfy(caseMapping -> {
				assertThat(caseMapping.getExternalCaseId()).isEqualTo(EXTERNAL_CASE_ID);
				assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
				assertThat(caseMapping.getCaseType()).isEqualTo(CaseType.ANMALAN_ANDRING_AVLOPPSANLAGGNING);
				assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
			});

		caseMappingRepository.delete(caseMappingRepository.findAllByExternalCaseId(EXTERNAL_CASE_ID).get(0));

	}

	@Test
	void test7_Avloppsanordning() throws JsonProcessingException, ClassNotFoundException {
		final var EXTERNAL_CASE_ID = "516589049";

		final var result = setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath("/cases")
			.withRequest("request.json")
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse("expected-response.json")
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		// Make sure that there doesn't exist a case entity
		assertThat(caseRepository.findById(EXTERNAL_CASE_ID).isPresent()).isFalse();
		// Make sure that there exists a case mapping
		assertThat(caseMappingRepository.findAllByExternalCaseId(EXTERNAL_CASE_ID))
			.isNotNull()
			.hasSize(1)
			.allSatisfy(caseMapping -> {
				assertThat(caseMapping.getExternalCaseId()).isEqualTo(EXTERNAL_CASE_ID);
				assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
				assertThat(caseMapping.getCaseType()).isEqualTo(CaseType.ANMALAN_ANDRING_AVLOPPSANORDNING);
				assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
			});

		caseMappingRepository.delete(caseMappingRepository.findAllByExternalCaseId(EXTERNAL_CASE_ID).get(0));

	}

	@Test
	void test8_Halsoskyddsverksamhet() throws JsonProcessingException, ClassNotFoundException {


		final var EXTERNAL_CASE_ID = "1097173756";

		final var result = setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath("/cases")
			.withRequest("request.json")
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse("expected-response.json")
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		// Make sure that there doesn't exist a case entity
		assertThat(caseRepository.findById(EXTERNAL_CASE_ID).isPresent()).isFalse();
		// Make sure that there exists a case mapping
		assertThat(caseMappingRepository.findAllByExternalCaseId(EXTERNAL_CASE_ID))
			.isNotNull()
			.hasSize(1)
			.allSatisfy(caseMapping -> {
				assertThat(caseMapping.getExternalCaseId()).isEqualTo(EXTERNAL_CASE_ID);
				assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
				assertThat(caseMapping.getCaseType()).isEqualTo(CaseType.ANMALAN_HALSOSKYDDSVERKSAMHET);
				assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
			});

		caseMappingRepository.delete(caseMappingRepository.findAllByExternalCaseId(EXTERNAL_CASE_ID).get(0));

	}

	@Test
	void test9_Livsmedel() throws JsonProcessingException, ClassNotFoundException {
		final var EXTERNAL_CASE_ID = "1195222212";

		final var result = setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath("/cases")
			.withRequest("request.json")
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse("expected-response.json")
			.sendRequestAndVerifyResponse()
			.andReturnBody(CaseResourceResponseDTO.class);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("Inskickat");

		// Make sure that there doesn't exist a case entity
		assertThat(caseRepository.findById(EXTERNAL_CASE_ID).isPresent()).isFalse();
		// Make sure that there exists a case mapping
		assertThat(caseMappingRepository.findAllByExternalCaseId(EXTERNAL_CASE_ID))
			.isNotNull()
			.hasSize(1)
			.allSatisfy(caseMapping -> {
				assertThat(caseMapping.getExternalCaseId()).isEqualTo(EXTERNAL_CASE_ID);
				assertThat(caseMapping.getCaseId()).isEqualTo(ECOS_CASE_ID);
				assertThat(caseMapping.getCaseType()).isEqualTo(CaseType.REGISTRERING_AV_LIVSMEDEL);
				assertThat(caseMapping.getSystem()).isEqualTo(SystemType.ECOS);
			});

		caseMappingRepository.delete(caseMappingRepository.findAllByExternalCaseId(EXTERNAL_CASE_ID).get(0));

	}


}
