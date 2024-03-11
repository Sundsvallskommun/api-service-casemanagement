package se.sundsvall.casemanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.casemanagement.TestUtil.ADRESSPLATS_ID;
import static se.sundsvall.casemanagement.TestUtil.FNR;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
import org.zalando.problem.Problem;

import se.sundsvall.casemanagement.TestUtil;
import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.api.model.CaseDTO;
import se.sundsvall.casemanagement.api.model.EnvironmentalCaseDTO;
import se.sundsvall.casemanagement.api.model.EnvironmentalFacilityDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.enums.AddressCategory;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.api.model.enums.StakeholderType;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.integration.ecos.EcosService;
import se.sundsvall.casemanagement.integration.ecos.MinutMiljoClient;
import se.sundsvall.casemanagement.integration.ecos.MinutMiljoClientV2;
import se.sundsvall.casemanagement.integration.ecos.PartyService;
import se.sundsvall.casemanagement.util.CaseUtil;
import se.sundsvall.casemanagement.util.Constants;

import minutmiljo.AddDocumentsToCase;
import minutmiljo.ArrayOfOccurrenceListItemSvcDto;
import minutmiljo.ArrayOfPartySvcDto;
import minutmiljo.ArrayOfSearchCaseResultSvcDto;
import minutmiljo.BiologicalStepSvcDto;
import minutmiljo.CaseSvcDto;
import minutmiljo.ClosedTankSvcDto;
import minutmiljo.CreateFoodFacility;
import minutmiljo.CreateFoodFacilitySvcDto;
import minutmiljo.CreateHealthProtectionFacility;
import minutmiljo.CreateHeatPumpFacility;
import minutmiljo.CreateHeatPumpFacilitySvcDto;
import minutmiljo.CreateHeatPumpFacilityWithHeatTransferFluidSvcDto;
import minutmiljo.CreateIndividualSewageFacility;
import minutmiljo.CreateIndividualSewageFacilitySvcDto;
import minutmiljo.CreateOccurrenceOnCase;
import minutmiljo.CreateSoilHeatingFacilitySvcDto;
import minutmiljo.DrySolutionSvcDto;
import minutmiljo.FilterBedSvcDto;
import minutmiljo.GetCase;
import minutmiljo.GetCaseResponse;
import minutmiljo.InfiltrationPlantSvcDto;
import minutmiljo.MiniSewageSvcDto;
import minutmiljo.OccurrenceListItemSvcDto;
import minutmiljo.PartySvcDto;
import minutmiljo.SandFilterSvcDto;
import minutmiljo.SearchCaseResponse;
import minutmiljo.SearchCaseResultSvcDto;
import minutmiljo.SepticTankSvcDto;
import minutmiljoV2.RegisterDocument;
import minutmiljoV2.RegisterDocumentCaseSvcDtoV2;

@ExtendWith(MockitoExtension.class)
class EcosServiceTest {

	private static final String ECOS_CASE_NUMBER = "Inskickat";

	private static final String CYTONOL = "e19981ad-34b2-4e14-88f5-133f61ca85aa";

	@InjectMocks
	private EcosService ecosService;

	@Mock
	private CitizenService citizenServiceMock;

	@Mock
	private CaseMappingService caseMappingServiceMock;

	@Mock
	private MinutMiljoClient minutMiljoClientMock;

	@Mock
	private MinutMiljoClientV2 minutMiljoClientV2Mock;

	@Mock
	private FbService fbServiceMock;

	@Mock
	private PartyService partyServiceMock;

	private static Map<String, String> getGeoExtraParametersMap() {
		final Map<String, String> extraParameters = new HashMap<>();
		extraParameters.put("CreateGeothermalHeatingFacilitySvcDto_Manufacturer", "Mitsubishi");
		extraParameters.put("CreateGeothermalHeatingFacilitySvcDto_Model", "SuperHeater 2000");
		extraParameters.put("CreateGeothermalHeatingFacilitySvcDto_PowerConsumption", "1.7");
		extraParameters.put("CreateGeothermalHeatingFacilitySvcDto_PowerOutput", "4.8");
		extraParameters.put("CreateGeothermalHeatingFacilitySvcDto_Capacity", "90.8");
		extraParameters.put("CreateGeothermalHeatingFacilitySvcDto_HeatTransferFluidId", CYTONOL);
		return extraParameters;
	}

	private static Map<String, String> getSoilExtraParametersMap() {
		final Map<String, String> extraParameters = new HashMap<>();
		extraParameters.put("CreateSoilHeatingFacilitySvcDto_Manufacturer", "Mitsubishi");
		extraParameters.put("CreateSoilHeatingFacilitySvcDto_Model", "SuperHeater 2000");
		extraParameters.put("CreateSoilHeatingFacilitySvcDto_PowerConsumption", "1.7");
		extraParameters.put("CreateSoilHeatingFacilitySvcDto_PowerOutput", "4.8");
		extraParameters.put("CreateSoilHeatingFacilitySvcDto_Capacity", "90.8");
		extraParameters.put("CreateSoilHeatingFacilitySvcDto_HeatTransferFluidId", CYTONOL);
		return extraParameters;
	}

	private static Map<String, String> getMarineExtraParameters() {
		final Map<String, String> extraParameters = new HashMap<>();
		extraParameters.put("CreateMarineHeatingFacilitySvcDto_Manufacturer", "Mitsubishi");
		extraParameters.put("CreateMarineHeatingFacilitySvcDto_Model", "SuperHeater 2000");
		extraParameters.put("CreateMarineHeatingFacilitySvcDto_PowerConsumption", "1.7");
		extraParameters.put("CreateMarineHeatingFacilitySvcDto_PowerOutput", "4.8");
		extraParameters.put("CreateMarineHeatingFacilitySvcDto_Capacity", "90.8");
		extraParameters.put("CreateMarineHeatingFacilitySvcDto_HeatTransferFluidId", CYTONOL);
		return extraParameters;
	}

	@BeforeEach
	void beforeEach() {
		TestUtil.standardMockFb(fbServiceMock);
		TestUtil.standardMockCitizen(citizenServiceMock);
		TestUtil.standardMockMinutMiljo(minutMiljoClientMock, minutMiljoClientV2Mock);
		TestUtil.standardMockFb(fbServiceMock);
		TestUtil.standardMockCitizen(citizenServiceMock);
		TestUtil.standardMockPartyService(partyServiceMock);
	}

	@Test
	void testFoodFacilityCase() {
		// Arrange
		final var eCase = TestUtil.createEnvironmentalCase(CaseType.REGISTRERING_AV_LIVSMEDEL, AttachmentCategory.ANMALAN_LIVSMEDELSANLAGGNING);
		final var organization = (OrganizationDTO) TestUtil.createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT.toString(), StakeholderRole.OPERATOR.toString()));
		final var person = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.CONTACT_PERSON.toString()));
		eCase.setStakeholders(List.of(organization, person));

		final var createFoodFacilityArgumentCaptor = ArgumentCaptor.forClass(CreateFoodFacility.class);
		final var registerDocumentArgumentCaptor = ArgumentCaptor.forClass(RegisterDocument.class);

		// Act
		final var result = ecosService.postCase(eCase);

		// Assert
		assertThat(result.getCaseNumber()).isEqualTo(ECOS_CASE_NUMBER);

		verify(minutMiljoClientMock, times(1)).createFoodFacility(createFoodFacilityArgumentCaptor.capture());
		final CreateFoodFacilitySvcDto createFoodFacilitySvcDto = createFoodFacilityArgumentCaptor.getValue().getCreateFoodFacilitySvcDto();

		assertThat(createFoodFacilitySvcDto.getAddress().getAdressPlatsId()).isEqualTo(ADRESSPLATS_ID);
		assertThat(createFoodFacilitySvcDto.getEstateDesignation().getFnr()).isEqualTo(FNR);
		assertThat(createFoodFacilitySvcDto.getCase()).isEqualTo(result.getCaseId());
		assertThat(createFoodFacilitySvcDto.getNote()).isEqualTo(eCase.getFacilities().getFirst().getDescription());
		assertThat(createFoodFacilitySvcDto.getFacilityCollectionName()).contains(eCase.getFacilities().getFirst().getFacilityCollectionName());

		verify(minutMiljoClientV2Mock, times(1)).registerDocumentV2(registerDocumentArgumentCaptor.capture());
		final RegisterDocumentCaseSvcDtoV2 registerDocumentCaseSvcDtoV2 = registerDocumentArgumentCaptor.getValue().getRegisterDocumentCaseSvcDto();

		assertThat(registerDocumentCaseSvcDtoV2.getCaseSubtitleFree()).isEqualTo(eCase.getFacilities().getFirst().getFacilityCollectionName() + ", " + eCase.getFacilities().getFirst().getAddress().getPropertyDesignation().toUpperCase());
		assertThat(registerDocumentCaseSvcDtoV2.getOccurrenceTypeId()).isEqualTo(Constants.ECOS_OCCURENCE_TYPE_ID_ANMALAN);
		assertThat(registerDocumentCaseSvcDtoV2.getHandlingOfficerGroupId()).isEqualTo(Constants.ECOS_HANDLING_OFFICER_GROUP_ID_EXPEDITIONEN);
		assertThat(registerDocumentCaseSvcDtoV2.getDiaryPlanId()).isEqualTo(Constants.ECOS_DIARY_PLAN_LIVSMEDEL);
		assertThat(registerDocumentCaseSvcDtoV2.getProcessTypeId()).isEqualTo(Constants.ECOS_PROCESS_TYPE_ID_REGISTRERING_AV_LIVSMEDEL);

		verify(caseMappingServiceMock, times(1)).postCaseMapping(any(CaseDTO.class), any(String.class), any(SystemType.class));
	}

	@Test
	void testMinimalFoodFacilityCase() {
		// Arrange
		final var eCase = new EnvironmentalCaseDTO();
		final var attachment = new AttachmentDTO();
		attachment.setCategory(AttachmentCategory.ANMALAN_LIVSMEDELSANLAGGNING.toString());
		attachment.setExtension(".pdf");
		attachment.setName("Document Name");
		attachment.setFile("dGVzdA==");
		eCase.setAttachments(List.of(attachment));

		final var organization = new OrganizationDTO();
		organization.setType(StakeholderType.ORGANIZATION);
		organization.setRoles(List.of(StakeholderRole.OPERATOR.toString()));
		organization.setOrganizationName("organizationName");
		organization.setOrganizationNumber("123456-1234");

		final var person = new PersonDTO();
		person.setType(StakeholderType.PERSON);
		person.setRoles(List.of(StakeholderRole.INVOICE_RECIPENT.toString()));
		person.setFirstName("Förnamn");
		person.setLastName("Efternamn");
		eCase.setStakeholders(List.of(person, organization));

		final var facility = new EnvironmentalFacilityDTO();
		facility.setFacilityCollectionName("facilityCollectionName");
		final var facilityAddress = new AddressDTO();
		facilityAddress.setAddressCategories(List.of(AddressCategory.VISITING_ADDRESS));
		facilityAddress.setPropertyDesignation("SUNDSVALL BALDER 2");
		facility.setAddress(facilityAddress);
		eCase.setFacilities(List.of(facility));

		eCase.setCaseType(CaseType.REGISTRERING_AV_LIVSMEDEL.toString());
		eCase.setExternalCaseId(String.valueOf(new Random().nextLong()));

		// Act
		ecosService.postCase(eCase);

		// Assert
		verify(minutMiljoClientMock, times(1)).createFoodFacility(any());
		verify(minutMiljoClientV2Mock, times(1)).registerDocumentV2(any());
		verify(caseMappingServiceMock, times(1)).postCaseMapping(any(CaseDTO.class), any(String.class), any(SystemType.class));
	}

	@Test
	void testCreateHeatPumpFacilityCase() {
		// Arrange
		final var eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP, AttachmentCategory.ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW);
		final var organization = (OrganizationDTO) TestUtil.createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT.toString(), StakeholderRole.OPERATOR.toString()));
		final var person = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.CONTACT_PERSON.toString()));
		eCase.setStakeholders(List.of(organization, person));

		final var createHeatPumpFacilityArgumentCaptor = ArgumentCaptor.forClass(CreateHeatPumpFacility.class);
		// Act
		final var result = ecosService.postCase(eCase);

		// Assert
		assertThat(result.getCaseNumber()).isEqualTo("Inskickat");

		verify(minutMiljoClientMock, times(1)).createHeatPumpFacility(createHeatPumpFacilityArgumentCaptor.capture());
		final var createSoilHeatingFacilitySvcDto = (CreateSoilHeatingFacilitySvcDto) createHeatPumpFacilityArgumentCaptor.getValue().getCreateIndividualSewageSvcDto();

		assertThat(createSoilHeatingFacilitySvcDto.getAddress().getAdressPlatsId()).isEqualTo(ADRESSPLATS_ID);
		assertThat(createSoilHeatingFacilitySvcDto.getFacilityStatusId()).isEqualTo(Constants.ECOS_FACILITY_STATUS_ID_ANMALD_ANSOKT);
		assertThat(createSoilHeatingFacilitySvcDto.getCreatedFromCaseId()).isEqualTo(result.getCaseId());
		assertThat(createSoilHeatingFacilitySvcDto.getEstate().getFnr()).isEqualTo(FNR);
		assertThat(createSoilHeatingFacilitySvcDto.getManufacturer()).isNotNull();
		assertThat(createSoilHeatingFacilitySvcDto.getModel()).isNotNull();
		assertThat(createSoilHeatingFacilitySvcDto.getPowerConsumption()).isNotNull();
		assertThat(createSoilHeatingFacilitySvcDto.getPowerOutput()).isNotNull();
	}

	@Test
	void testAirHeating() {
		// Arrange
		final EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP, AttachmentCategory.ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW);
		final Map<String, String> extraParameters = new HashMap<>();
		extraParameters.put("CreateAirHeatingFacilitySvcDto_Manufacturer", "Mitsubishi");
		extraParameters.put("CreateAirHeatingFacilitySvcDto_Model", "SuperHeater 2000");
		extraParameters.put("CreateAirHeatingFacilitySvcDto_PowerConsumption", "1.7");
		extraParameters.put("CreateAirHeatingFacilitySvcDto_PowerOutput", "4.8");
		eCase.getFacilities().getFirst().setExtraParameters(extraParameters);

		// Act
		ecosService.postCase(eCase);

		// Assert
		final ArgumentCaptor<CreateHeatPumpFacility> createHeatPumpFacilityArgumentCaptor = ArgumentCaptor.forClass(CreateHeatPumpFacility.class);
		verifyMinutMiljoCallsForHeatPumpCase(createHeatPumpFacilityArgumentCaptor);
		verifyStandardParams(extraParameters, createHeatPumpFacilityArgumentCaptor.getValue().getCreateIndividualSewageSvcDto());
	}

	@Test
	void testGeothermalHeating() {
		final EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP, AttachmentCategory.ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW);
		final var extraParameters = getGeoExtraParametersMap();
		eCase.getFacilities().getFirst().setExtraParameters(extraParameters);

		ecosService.postCase(eCase);

		final ArgumentCaptor<CreateHeatPumpFacility> createHeatPumpFacilityArgumentCaptor = ArgumentCaptor.forClass(CreateHeatPumpFacility.class);
		verifyMinutMiljoCallsForHeatPumpCase(createHeatPumpFacilityArgumentCaptor);

		verifyStandardParams(extraParameters, createHeatPumpFacilityArgumentCaptor.getValue().getCreateIndividualSewageSvcDto());
		verifyFluidParams(extraParameters, (CreateHeatPumpFacilityWithHeatTransferFluidSvcDto) createHeatPumpFacilityArgumentCaptor.getValue().getCreateIndividualSewageSvcDto());
	}

	@Test
	void testSoilHeating() {
		final EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP, AttachmentCategory.ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW);
		final var extraParameters = getSoilExtraParametersMap();
		eCase.getFacilities().getFirst().setExtraParameters(extraParameters);

		ecosService.postCase(eCase);

		final ArgumentCaptor<CreateHeatPumpFacility> createHeatPumpFacilityArgumentCaptor = ArgumentCaptor.forClass(CreateHeatPumpFacility.class);
		verifyMinutMiljoCallsForHeatPumpCase(createHeatPumpFacilityArgumentCaptor);

		verifyStandardParams(extraParameters, createHeatPumpFacilityArgumentCaptor.getValue().getCreateIndividualSewageSvcDto());
		verifyFluidParams(extraParameters, (CreateHeatPumpFacilityWithHeatTransferFluidSvcDto) createHeatPumpFacilityArgumentCaptor.getValue().getCreateIndividualSewageSvcDto());
	}

	@Test
	void testExtraParamsNull() {
		final EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP, AttachmentCategory.ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW);
		eCase.getFacilities().getFirst().setExtraParameters(null);

		ecosService.postCase(eCase);

		verify(minutMiljoClientMock, times(0)).createHeatPumpFacility(any());
		verify(minutMiljoClientV2Mock, times(1)).registerDocumentV2(any());
	}

	@Test
	void testExtraParamsEmpty() {
		final EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP, AttachmentCategory.ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW);
		final Map<String, String> extraParameters = new HashMap<>();
		eCase.getFacilities().getFirst().setExtraParameters(extraParameters);

		ecosService.postCase(eCase);

		verify(minutMiljoClientMock, times(0)).createHeatPumpFacility(any());
		verify(minutMiljoClientV2Mock, times(1)).registerDocumentV2(any());
	}

	@Test
	void testMarineHeating() {
		final EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP, AttachmentCategory.ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW);
		final var extraParameters = getMarineExtraParameters();
		eCase.getFacilities().getFirst().setExtraParameters(extraParameters);

		ecosService.postCase(eCase);

		final ArgumentCaptor<CreateHeatPumpFacility> createHeatPumpFacilityArgumentCaptor = ArgumentCaptor.forClass(CreateHeatPumpFacility.class);
		verifyMinutMiljoCallsForHeatPumpCase(createHeatPumpFacilityArgumentCaptor);

		verifyStandardParams(extraParameters, createHeatPumpFacilityArgumentCaptor.getValue().getCreateIndividualSewageSvcDto());
		verifyFluidParams(extraParameters, (CreateHeatPumpFacilityWithHeatTransferFluidSvcDto) createHeatPumpFacilityArgumentCaptor.getValue().getCreateIndividualSewageSvcDto());
	}

	private void verifyStandardParams(final Map<String, String> extraParameters, final CreateHeatPumpFacilitySvcDto svcDto) {
		assertThat(svcDto.getManufacturer()).isEqualTo(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("Manufacturer")).findFirst().orElseThrow().getValue());
		assertThat(svcDto.getModel()).isEqualTo(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("Model")).findFirst().orElseThrow().getValue());
		assertThat(svcDto.getPowerConsumption()).isEqualTo(CaseUtil.parseDouble(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("PowerConsumption")).findFirst().orElseThrow().getValue()));
		assertThat(svcDto.getPowerOutput()).isEqualTo(CaseUtil.parseDouble(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("PowerOutput")).findFirst().orElseThrow().getValue()));
	}

	private void verifyFluidParams(final Map<String, String> extraParameters, final CreateHeatPumpFacilityWithHeatTransferFluidSvcDto svcDto) {
		assertThat(svcDto.getHeatTransferFluidId()).isEqualTo(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("HeatTransferFluidId")).findFirst().orElseThrow().getValue());
		assertThat(svcDto.getCapacity()).isEqualTo(CaseUtil.parseDouble(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("Capacity")).findFirst().orElseThrow().getValue()));
	}

	private void verifyMinutMiljoCallsForHeatPumpCase(final ArgumentCaptor<CreateHeatPumpFacility> createHeatPumpFacilityArgumentCaptor) {
		verify(partyServiceMock, times(1)).findAndAddPartyToCase(any(EnvironmentalCaseDTO.class), any(String.class));
		verify(minutMiljoClientMock, times(1)).createHeatPumpFacility(createHeatPumpFacilityArgumentCaptor.capture());
		verify(minutMiljoClientMock, times(3)).addPartyToFacility(any());
		verify(minutMiljoClientV2Mock, times(1)).registerDocumentV2(any());
	}

	@Test
	void healthProtectionTest() {
		// Arrange
		final var eCase = TestUtil.createEnvironmentalCase(CaseType.ANMALAN_HALSOSKYDDSVERKSAMHET, AttachmentCategory.ANMALAN_HALSOSKYDDSVERKSAMHET);
		final var createHealthProtectionFacilityArgumentCaptor = ArgumentCaptor.forClass(CreateHealthProtectionFacility.class);

		// Act
		ecosService.postCase(eCase);

		// Assert
		verify(partyServiceMock, times(1)).findAndAddPartyToCase(any(EnvironmentalCaseDTO.class), any(String.class));
		verify(partyServiceMock, times(1)).findAndAddPartyToCase(any(EnvironmentalCaseDTO.class), any(String.class));
		verify(minutMiljoClientMock, times(1)).createHealthProtectionFacility(createHealthProtectionFacilityArgumentCaptor.capture());
		verify(minutMiljoClientMock, times(3)).addPartyToFacility(any());
		verify(minutMiljoClientV2Mock, times(1)).registerDocumentV2(any());

		final var svcDto = createHealthProtectionFacilityArgumentCaptor.getValue().getCreateHealthProtectionFacilitySvcDto();
		assertThat(svcDto.getAddress().getAdressPlatsId()).isEqualTo(ADRESSPLATS_ID);
		assertThat(svcDto.getEstateDesignation().getFnr()).isEqualTo(FNR);
		assertThat(svcDto.getCase()).isEqualTo("e19981ad-34b2-4e14-88f5-133f61ca85aa");
		assertThat(svcDto.getNote()).isEqualTo(eCase.getFacilities().getFirst().getDescription());
		assertThat(svcDto.getFacilityCollectionName()).isEqualTo(eCase.getFacilities().getFirst().getFacilityCollectionName());
	}

	@Test
	void individualSewageSepticTank() {
		// Arrange
		final var eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
		final var extraParameters = new HashMap<String, String>();
		final var prefix = "SepticTankSvcDto_";
		TestUtil.setSewageStandardExtraParams(extraParameters, prefix);
		extraParameters.put(prefix + "EmptyingInterval", String.valueOf(new Random().nextInt()));
		extraParameters.put(prefix + "HasCeMarking", String.valueOf(new Random().nextBoolean()));
		extraParameters.put(prefix + "HasTPipe", String.valueOf(new Random().nextBoolean()));
		extraParameters.put(prefix + "Volume", String.valueOf(new Random().nextDouble()));
		eCase.getFacilities().getFirst().setExtraParameters(extraParameters);

		// Act
		ecosService.postCase(eCase);

		final var argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
		verifyMinutMiljoCallsForSewageCase(argumentCaptor);
		verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);

		final var septicTankSvcDto = (SepticTankSvcDto) argumentCaptor.getValue().getCreateIndividualSewageSvcDto().getPurificationSteps().getPurificationStepSvcDto().getFirst();

		// Assert
		assertThat(CaseUtil.parseInteger(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "EmptyingInterval")).findFirst().orElseThrow().getValue())).isEqualTo(septicTankSvcDto.getEmptyingInterval());
		assertThat(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "HasCeMarking")).findFirst().orElseThrow().getValue())).isEqualTo(septicTankSvcDto.isHasCeMarking());
		assertThat(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "HasTPipe")).findFirst().orElseThrow().getValue())).isEqualTo(septicTankSvcDto.isHasTPipe());
		assertThat(CaseUtil.parseDouble(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Volume")).findFirst().orElseThrow().getValue())).isEqualTo(septicTankSvcDto.getVolume());
	}

	@Test
	void individualSewageInfiltration() {
		// Arrange
		final var eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
		final var extraParameters = new HashMap<String, String>();
		final var prefix = "InfiltrationPlantSvcDto_";
		TestUtil.setSewageStandardExtraParams(extraParameters, prefix);

		extraParameters.put(prefix + "Elevated", String.valueOf(new Random().nextBoolean()));
		extraParameters.put(prefix + "Reinforced", String.valueOf(new Random().nextBoolean()));
		extraParameters.put(prefix + "IsModuleSystem", String.valueOf(new Random().nextBoolean()));
		extraParameters.put(prefix + "SpreadLinesCount", String.valueOf(new Random().nextInt()));
		extraParameters.put(prefix + "SpreadLinesLength", String.valueOf(new Random().nextInt()));
		eCase.getFacilities().getFirst().setExtraParameters(extraParameters);

		// Act
		ecosService.postCase(eCase);

		final var argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
		verifyMinutMiljoCallsForSewageCase(argumentCaptor);
		verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);

		final var infiltrationPlantSvcDto = (InfiltrationPlantSvcDto) argumentCaptor.getValue().getCreateIndividualSewageSvcDto().getPurificationSteps().getPurificationStepSvcDto().getFirst();

		// Assert
		assertThat(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Elevated")).findFirst().orElseThrow().getValue())).isEqualTo(infiltrationPlantSvcDto.isElevated());
		assertThat(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Reinforced")).findFirst().orElseThrow().getValue())).isEqualTo(infiltrationPlantSvcDto.isReinforced());
		assertThat(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "IsModuleSystem")).findFirst().orElseThrow().getValue())).isEqualTo(infiltrationPlantSvcDto.isIsModuleSystem());
		assertThat(CaseUtil.parseInteger(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "SpreadLinesCount")).findFirst().orElseThrow().getValue())).isEqualTo(infiltrationPlantSvcDto.getSpreadLinesCount());
		assertThat(CaseUtil.parseInteger(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "SpreadLinesLength")).findFirst().orElseThrow().getValue())).isEqualTo(infiltrationPlantSvcDto.getSpreadLinesLength());
	}

	@Test
	void individualSewageClosedTank() {
		// Arrange
		final var eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
		final var extraParameters = new HashMap<String, String>();
		final var prefix = "ClosedTankSvcDto_";
		TestUtil.setSewageStandardExtraParams(extraParameters, prefix);

		extraParameters.put(prefix + "EmptyingInterval", String.valueOf(new Random().nextInt()));
		extraParameters.put(prefix + "HasCeMarking", String.valueOf(new Random().nextBoolean()));
		extraParameters.put(prefix + "Volume", String.valueOf(new Random().nextDouble()));
		eCase.getFacilities().getFirst().setExtraParameters(extraParameters);

		// Act
		ecosService.postCase(eCase);

		final var argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
		verifyMinutMiljoCallsForSewageCase(argumentCaptor);
		verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);

		final var closedTankSvcDto = (ClosedTankSvcDto) argumentCaptor.getValue().getCreateIndividualSewageSvcDto().getPurificationSteps().getPurificationStepSvcDto().getFirst();

		// Assert
		assertThat(CaseUtil.parseInteger(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "EmptyingInterval")).findFirst().orElseThrow().getValue())).isEqualTo(closedTankSvcDto.getEmptyingInterval());
		assertThat(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "HasCeMarking")).findFirst().orElseThrow().getValue())).isEqualTo(closedTankSvcDto.isHasCeMarking());
		assertThat(CaseUtil.parseDouble(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Volume")).findFirst().orElseThrow().getValue())).isEqualTo(closedTankSvcDto.getVolume());
	}

	@Test
	void individualSewageDrySolution() {
		// Arrange
		final var eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
		final var extraParameters = new HashMap<String, String>();
		final var prefix = "DrySolutionSvcDto_";
		TestUtil.setSewageStandardExtraParams(extraParameters, prefix);

		extraParameters.put(prefix + "CompostProductName", UUID.randomUUID().toString());
		extraParameters.put(prefix + "DrySolutionCompostTypeId", UUID.randomUUID().toString());
		extraParameters.put(prefix + "DrySolutionTypeId", UUID.randomUUID().toString());
		extraParameters.put(prefix + "NoContOrCompt", UUID.randomUUID().toString());
		extraParameters.put(prefix + "NoLPerContOrCompt", UUID.randomUUID().toString());
		extraParameters.put(prefix + "ToiletProductName", UUID.randomUUID().toString());
		extraParameters.put(prefix + "Volume", String.valueOf(new Random().nextDouble()));
		eCase.getFacilities().getFirst().setExtraParameters(extraParameters);

		// Act
		ecosService.postCase(eCase);

		final var argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);

		// Assert
		verifyMinutMiljoCallsForSewageCase(argumentCaptor);
		verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);

		final var drySolutionSvcDto = (DrySolutionSvcDto) argumentCaptor.getValue().getCreateIndividualSewageSvcDto().getPurificationSteps().getPurificationStepSvcDto().getFirst();

		assertThat(CaseUtil.parseString(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "CompostProductName")).findFirst().orElseThrow().getValue())).isEqualTo(drySolutionSvcDto.getCompostProductName());
		assertThat(CaseUtil.parseString(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "DrySolutionCompostTypeId")).findFirst().orElseThrow().getValue())).isEqualTo(drySolutionSvcDto.getDrySolutionCompostTypeId());
		assertThat(CaseUtil.parseString(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "DrySolutionTypeId")).findFirst().orElseThrow().getValue())).isEqualTo(drySolutionSvcDto.getDrySolutionTypeId());
		assertThat(CaseUtil.parseString(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "NoContOrCompt")).findFirst().orElseThrow().getValue())).isEqualTo(drySolutionSvcDto.getNoContOrCompt());
		assertThat(CaseUtil.parseString(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "NoLPerContOrCompt")).findFirst().orElseThrow().getValue())).isEqualTo(drySolutionSvcDto.getNoLPerContOrCompt());
		assertThat(CaseUtil.parseString(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "ToiletProductName")).findFirst().orElseThrow().getValue())).isEqualTo(drySolutionSvcDto.getToiletProductName());
		assertThat(CaseUtil.parseDouble(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Volume")).findFirst().orElseThrow().getValue())).isEqualTo(drySolutionSvcDto.getVolume());
	}

	@Test
	void individualSewageMiniSewage() {
		// Arrange
		final var eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
		final var extraParameters = new HashMap<String, String>();
		final var prefix = "MiniSewageSvcDto_";
		TestUtil.setSewageStandardExtraParams(extraParameters, prefix);

		extraParameters.put(prefix + "CeMarking", String.valueOf(new Random().nextBoolean()));
		extraParameters.put(prefix + "Manufacturer", UUID.randomUUID().toString());
		extraParameters.put(prefix + "Model", UUID.randomUUID().toString());
		eCase.getFacilities().getFirst().setExtraParameters(extraParameters);

		// Act
		ecosService.postCase(eCase);

		final var argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
		verifyMinutMiljoCallsForSewageCase(argumentCaptor);
		verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);

		final var svcDto = (MiniSewageSvcDto) argumentCaptor.getValue().getCreateIndividualSewageSvcDto().getPurificationSteps().getPurificationStepSvcDto().getFirst();

		// Assert
		assertThat(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "CeMarking")).findFirst().orElseThrow().getValue())).isEqualTo(svcDto.isCeMarking());
		assertThat(CaseUtil.parseString(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Manufacturer")).findFirst().orElseThrow().getValue())).isEqualTo(svcDto.getManufacturer());
		assertThat(CaseUtil.parseString(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Model")).findFirst().orElseThrow().getValue())).isEqualTo(svcDto.getModel());
	}

	@Test
	void individualSewageFilterBed() {
		// Arrange
		final var eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
		final var extraParameters = new HashMap<String, String>();
		final var prefix = "FilterBedSvcDto_";
		TestUtil.setSewageStandardExtraParams(extraParameters, prefix);

		extraParameters.put(prefix + "Volume", String.valueOf(new Random().nextDouble()));
		eCase.getFacilities().getFirst().setExtraParameters(extraParameters);

		// Act
		ecosService.postCase(eCase);

		// Assert
		final var argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
		verifyMinutMiljoCallsForSewageCase(argumentCaptor);
		verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);

		final FilterBedSvcDto svcDto = (FilterBedSvcDto) argumentCaptor.getValue().getCreateIndividualSewageSvcDto().getPurificationSteps().getPurificationStepSvcDto().getFirst();
		assertThat(CaseUtil.parseDouble(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Volume")).findFirst().orElseThrow().getValue())).isEqualTo(svcDto.getVolume());
	}

	@Test
	void individualSewageSandFilter() {
		// Arrange
		final var eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
		final var extraParameters = new HashMap<String, String>();
		final var prefix = "SandFilterSvcDto_";
		TestUtil.setSewageStandardExtraParams(extraParameters, prefix);
		extraParameters.put(prefix + "Area", String.valueOf(new Random().nextInt()));
		extraParameters.put(prefix + "Elevated", String.valueOf(new Random().nextBoolean()));
		extraParameters.put(prefix + "WaterTight", String.valueOf(new Random().nextBoolean()));
		eCase.getFacilities().getFirst().setExtraParameters(extraParameters);

		// Act
		ecosService.postCase(eCase);

		// Assert
		final var argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
		verifyMinutMiljoCallsForSewageCase(argumentCaptor);
		verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);

		final var svcDto = (SandFilterSvcDto) argumentCaptor.getValue().getCreateIndividualSewageSvcDto().getPurificationSteps().getPurificationStepSvcDto().getFirst();
		assertThat(CaseUtil.parseInteger(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Area")).findFirst().orElseThrow().getValue())).isEqualTo(svcDto.getArea());
		assertThat(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Elevated")).findFirst().orElseThrow().getValue())).isEqualTo(svcDto.isElevated());
		assertThat(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "WaterTight")).findFirst().orElseThrow().getValue())).isEqualTo(svcDto.isWaterTight());
	}

	@Test
	void individualSewageBiologicalStep() {
		// Arrange
		final var eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
		final var extraParameters = new HashMap<String, String>();
		final var prefix = "BiologicalStepSvcDto_";
		TestUtil.setSewageStandardExtraParams(extraParameters, prefix);

		extraParameters.put(prefix + "Area", String.valueOf(new Random().nextInt()));
		extraParameters.put(prefix + "BiologicalStepTypeId", UUID.randomUUID().toString());
		eCase.getFacilities().getFirst().setExtraParameters(extraParameters);

		// Act
		ecosService.postCase(eCase);

		// Assert
		final var argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
		verifyMinutMiljoCallsForSewageCase(argumentCaptor);
		verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);

		final var svcDto = (BiologicalStepSvcDto) argumentCaptor.getValue().getCreateIndividualSewageSvcDto().getPurificationSteps().getPurificationStepSvcDto().getFirst();
		assertThat(CaseUtil.parseInteger(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Area")).findFirst().orElseThrow().getValue())).isEqualTo(svcDto.getArea());
		assertThat(CaseUtil.parseString(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "BiologicalStepTypeId")).findFirst().orElseThrow().getValue())).isEqualTo(svcDto.getBiologicalStepTypeId());
	}

	@Test
	void individualSewagePhosphorusTrap() {
		final var eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
		final var extraParameters = new HashMap<String, String>();
		final var prefix = "PhosphorusTrapSvcDto_";
		TestUtil.setSewageStandardExtraParams(extraParameters, prefix);
		eCase.getFacilities().getFirst().setExtraParameters(extraParameters);

		ecosService.postCase(eCase);

		final var argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
		verifyMinutMiljoCallsForSewageCase(argumentCaptor);
		verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);
	}

	@Test
	void individualSewageChemicalPretreatment() {
		final var eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
		final var extraParameters = new HashMap<String, String>();
		final var prefix = "ChemicalPretreatmentSvcDto_";
		TestUtil.setSewageStandardExtraParams(extraParameters, prefix);
		eCase.getFacilities().getFirst().setExtraParameters(extraParameters);

		ecosService.postCase(eCase);

		final var argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
		verifyMinutMiljoCallsForSewageCase(argumentCaptor);
		verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);
	}

	private void verifyMinutMiljoCallsForSewageCase(final ArgumentCaptor<CreateIndividualSewageFacility> argumentCaptor) {
		verify(minutMiljoClientMock, times(1)).createIndividualSewageFacility(argumentCaptor.capture());
		verify(minutMiljoClientMock, times(3)).addPartyToFacility(any());
		verify(partyServiceMock, times(1)).findAndAddPartyToCase(any(EnvironmentalCaseDTO.class), any(String.class));
		verify(minutMiljoClientV2Mock, times(1)).registerDocumentV2(any());
	}

	private void verifyStandardParams(final Map<String, String> extraParameters, final CreateIndividualSewageFacilitySvcDto svcDto, final String prefix) {
		assertThat(svcDto.isOnGrantLand()).isEqualTo(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("OnGrantLand")).findFirst().orElseThrow().getValue()));
		assertThat(svcDto.getProtectionLevelApprovedEnvironmentId()).isEqualTo(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("ProtectionLevelApprovedEnvironmentId")).findFirst().orElseThrow().getValue());
		assertThat(svcDto.getProtectionLevelApprovedHealthId()).isEqualTo(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("ProtectionLevelApprovedHealthId")).findFirst().orElseThrow().getValue());
		assertThat(svcDto.getWastewaterApprovedForId()).isEqualTo(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("WastewaterApprovedForId")).findFirst().orElseThrow().getValue());
		assertThat(svcDto.getWasteWaterInboundId()).isEqualTo(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("WasteWaterInboundId")).findFirst().orElseThrow().getValue());

		final var purificationStepSvcDto = svcDto.getPurificationSteps().getPurificationStepSvcDto().getFirst();
		assertThat(purificationStepSvcDto.isHasOverflowAlarm()).isEqualTo(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "HasOverflowAlarm")).findFirst().orElseThrow().getValue()));
		assertThat(purificationStepSvcDto.getInstallationDate()).isEqualTo(CaseUtil.parseLocalDateTime(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "InstallationDate")).findFirst().orElseThrow().getValue()));
		assertThat(purificationStepSvcDto.getLifeTime()).isEqualTo(CaseUtil.parseInteger(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "LifeTime")).findFirst().orElseThrow().getValue()));
		assertThat(purificationStepSvcDto.getPersonCapacity()).isEqualTo(CaseUtil.parseInteger(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "PersonCapacity")).findFirst().orElseThrow().getValue()));
		assertThat(purificationStepSvcDto.getPurificationStepFacilityStatusId()).isEqualTo(Constants.ECOS_FACILITY_STATUS_ID_ANMALD_ANSOKT);
		assertThat(purificationStepSvcDto.getStepNumber()).isEqualTo(CaseUtil.parseInteger(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "StepNumber")).findFirst().orElseThrow().getValue()));
	}

	@Test
	void testMissingFacilityAddress() {
		// Arrange
		final var eCase = TestUtil.createEnvironmentalCase(CaseType.REGISTRERING_AV_LIVSMEDEL, AttachmentCategory.ANMALAN_LIVSMEDELSANLAGGNING);
		eCase.getFacilities().getFirst().setAddress(null);

		// Act
		final var result = ecosService.postCase(eCase);

		// Assert
		final var createOccurrenceOnCaseArgumentCaptor = ArgumentCaptor.forClass(CreateOccurrenceOnCase.class);
		verify(minutMiljoClientMock, times(1)).createOccurrenceOnCase(createOccurrenceOnCaseArgumentCaptor.capture());
		verify(partyServiceMock, times(1)).findAndAddPartyToCase(any(EnvironmentalCaseDTO.class), any(String.class));
		assertThat(result.getCaseId()).isEqualTo(createOccurrenceOnCaseArgumentCaptor.getValue().getCreateOccurrenceOnCaseSvcDto().getCaseId());
		assertThat(createOccurrenceOnCaseArgumentCaptor.getValue().getCreateOccurrenceOnCaseSvcDto().getOccurrenceDate()).isNotNull();
		assertThat(createOccurrenceOnCaseArgumentCaptor.getValue().getCreateOccurrenceOnCaseSvcDto().getOccurrenceTypeId()).isEqualTo(Constants.ECOS_OCCURRENCE_TYPE_ID_INFO_FRAN_ETJANST);
		assertThat(createOccurrenceOnCaseArgumentCaptor.getValue().getCreateOccurrenceOnCaseSvcDto().getNote()).isEqualTo(Constants.ECOS_OCCURENCE_TEXT_MOBIL_ANLAGGNING);

		// If facility doesn't have any address, we should not register any facility and therefore not add any stakeholder to
		// facility.
		// This results in manual handling for admin.
		verify(minutMiljoClientMock, times(0)).createFoodFacility(any());
		verify(minutMiljoClientMock, times(0)).addPartyToFacility(any());

		verify(caseMappingServiceMock, times(1)).postCaseMapping(any(CaseDTO.class), any(String.class), any(SystemType.class));
	}

	@Test
	void testGetStatus() {
		// Arrange
		final var caseId = MessageFormat.format("MK-2022-{0}", new Random().nextInt(100000));
		final var externalCaseID = UUID.randomUUID().toString();

		final var caseMapping = CaseMapping.builder()
			.withExternalCaseId(externalCaseID)
			.withCaseId(caseId)
			.withCaseType(CaseType.REGISTRERING_AV_LIVSMEDEL.toString())
			.withSystem(SystemType.ECOS)
			.withServiceName(RandomStringUtils.random(10, true, false))
			.withTimestamp(LocalDateTime.now())
			.build();

		final var occurrenceListItemSvcDto_1 = new OccurrenceListItemSvcDto()
			.withOccurrenceDate(LocalDateTime.now().minusDays(5))
			.withOccurrenceDescription(RandomStringUtils.random(5, true, false));

		final var occurrenceListItemSvcDto_2 = new OccurrenceListItemSvcDto()
			.withOccurrenceDate(LocalDateTime.now().minusDays(1))
			.withOccurrenceDescription(RandomStringUtils.random(5, true, false));

		final var occurrenceListItemSvcDto_3 = new OccurrenceListItemSvcDto()
			.withOccurrenceDate(LocalDateTime.now().minusDays(3))
			.withOccurrenceDescription(RandomStringUtils.random(5, true, false));

		final var arrayOfOccurrenceListItemSvcDto = new ArrayOfOccurrenceListItemSvcDto();
		arrayOfOccurrenceListItemSvcDto.getOccurrenceListItemSvcDto().addAll(List.of(occurrenceListItemSvcDto_1, occurrenceListItemSvcDto_2, occurrenceListItemSvcDto_3));

		final var caseSvcDto = new CaseSvcDto()
			.withCaseNumber(caseId)
			.withCaseId(UUID.randomUUID().toString())
			.withOccurrences(arrayOfOccurrenceListItemSvcDto);

		final var getCaseResponse = new GetCaseResponse().withGetCaseResult(caseSvcDto);
		//Mock
		when(caseMappingServiceMock.getCaseMapping(externalCaseID, caseId)).thenReturn(List.of(caseMapping));
		when(minutMiljoClientMock.getCase(any())).thenReturn(getCaseResponse);

		// Act
		final var result = ecosService.getStatus(caseId, externalCaseID);

		// Assert
		assertThat(result.getCaseId()).isEqualTo(caseId);
		assertThat(result.getExternalCaseId()).isEqualTo(externalCaseID);
		assertThat(result.getCaseType()).isEqualTo(caseMapping.getCaseType());
		assertThat(result.getSystem()).isEqualTo(caseMapping.getSystem());
		assertThat(result.getServiceName()).isEqualTo(caseMapping.getServiceName());
		assertThat(result.getStatus()).isEqualTo(occurrenceListItemSvcDto_2.getOccurrenceDescription());
		assertThat(result.getTimestamp()).isEqualTo(occurrenceListItemSvcDto_2.getOccurrenceDate());

		final var getCaseArgumentCaptor = ArgumentCaptor.forClass(GetCase.class);
		verify(minutMiljoClientMock, times(1)).getCase(getCaseArgumentCaptor.capture());
		assertThat(getCaseArgumentCaptor.getValue().getCaseId()).isEqualTo(caseId);
	}

	@Test
	void testGetStatusNotFound() {
		// Act & Assert
		assertThatThrownBy(() -> ecosService.getStatus("someCaseId", "someExternalCaseId"))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Status not found");
	}

	@Test
	void testGetStatusByOrgnr() {
		// Arrange
		final var caseNumber = MessageFormat.format("MK-2022-{0}", new Random().nextInt(100000));
		final var caseId = UUID.randomUUID().toString();
		final var externalCaseID = UUID.randomUUID().toString();
		final var orgnr = TestUtil.generateRandomOrganizationNumber();

		final var arrayOfPartySvcDto = new ArrayOfPartySvcDto()
			.withPartySvcDto(List.of(new PartySvcDto().withId(UUID.randomUUID().toString()), new PartySvcDto().withId(UUID.randomUUID().toString())));

		final var searchCaseResultSvcDto_1 = new SearchCaseResultSvcDto()
			.withCaseId(caseId)
			.withCaseNumber(caseNumber);
		final var searchCaseResultSvcDto_2 = new SearchCaseResultSvcDto()
			.withCaseId(caseId)
			.withCaseNumber(caseNumber);

		final var searchCaseResponse = new SearchCaseResponse().withSearchCaseResult(new ArrayOfSearchCaseResultSvcDto()
			.withSearchCaseResultSvcDto(List.of(searchCaseResultSvcDto_1, searchCaseResultSvcDto_2)));

		final var caseMapping = CaseMapping.builder()
			.withExternalCaseId(externalCaseID)
			.withCaseId(caseId)
			.withCaseType(CaseType.REGISTRERING_AV_LIVSMEDEL.toString())
			.withSystem(SystemType.ECOS)
			.withServiceName(RandomStringUtils.random(10, true, false))
			.withTimestamp(LocalDateTime.now())
			.build();

		final var arrayOfOccurrenceListItemSvcDto = new ArrayOfOccurrenceListItemSvcDto();
		final var occurrenceListItemSvcDto_1 = new OccurrenceListItemSvcDto()
			.withOccurrenceDate(LocalDateTime.now().minusDays(5))
			.withOccurrenceDescription(RandomStringUtils.random(5, true, false));

		final var occurrenceListItemSvcDto_2 = new OccurrenceListItemSvcDto()
			.withOccurrenceDate(LocalDateTime.now().minusDays(1))
			.withOccurrenceDescription(RandomStringUtils.random(5, true, false));

		final var occurrenceListItemSvcDto_3 = new OccurrenceListItemSvcDto()
			.withOccurrenceDate(LocalDateTime.now().minusDays(3))
			.withOccurrenceDescription(RandomStringUtils.random(5, true, false));
		arrayOfOccurrenceListItemSvcDto.getOccurrenceListItemSvcDto().addAll(List.of(occurrenceListItemSvcDto_1, occurrenceListItemSvcDto_2, occurrenceListItemSvcDto_3));

		final var caseSvcDto = new CaseSvcDto()
			.withCaseNumber(caseNumber)
			.withCaseId(caseId)
			.withOccurrences(arrayOfOccurrenceListItemSvcDto);

		when(caseMappingServiceMock.getCaseMapping(any(), any())).thenReturn(List.of(caseMapping));
		when(minutMiljoClientMock.getCase(any())).thenReturn(new GetCaseResponse().withGetCaseResult(caseSvcDto));
		when(minutMiljoClientMock.searchCase(any())).thenReturn(searchCaseResponse);
		when(partyServiceMock.searchPartyByOrganizationNumber(any())).thenReturn(arrayOfPartySvcDto);

		// Act
		final var result = ecosService.getEcosStatusByOrgNr(orgnr);

		// Assert
		assertThat(result).hasSize(2);
		result.forEach(status -> {
			assertThat(status.getCaseId()).isEqualTo(caseNumber);
			assertThat(status.getExternalCaseId()).isEqualTo(externalCaseID);
			assertThat(status.getCaseType()).isEqualTo(caseMapping.getCaseType());
			assertThat(status.getSystem()).isEqualTo(caseMapping.getSystem());
			assertThat(status.getServiceName()).isEqualTo(caseMapping.getServiceName());
			assertThat(status.getStatus()).isEqualTo(occurrenceListItemSvcDto_2.getOccurrenceDescription());
			assertThat(status.getTimestamp()).isEqualTo(occurrenceListItemSvcDto_2.getOccurrenceDate());
		});

		final var getCaseArgumentCaptor = ArgumentCaptor.forClass(GetCase.class);
		verify(minutMiljoClientMock, times(2)).getCase(getCaseArgumentCaptor.capture());
	}

	@Test
	void testAddDocumentsToCase() {
		// Arrange
		final var category = AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP.getCode();
		final var extension = ".pdf";
		final var mimeType = "application/pdf";
		final var fileName = "someFileName";
		final var note = "someNote";
		final var file = "dGVzdA==";
		final var attachmentDTO = new AttachmentDTO();
		attachmentDTO.setCategory(category);
		attachmentDTO.setExtension(extension);
		attachmentDTO.setMimeType(mimeType);
		attachmentDTO.setName(fileName);
		attachmentDTO.setNote(note);
		attachmentDTO.setFile(file);
		final var caseId = UUID.randomUUID().toString();

		// Act
		ecosService.addDocumentsToCase(caseId, List.of(attachmentDTO));
		// Capture result
		final var addDocumentsToCaseArgumentCaptor = ArgumentCaptor.forClass(AddDocumentsToCase.class);
		verify(minutMiljoClientMock, times(1)).addDocumentsToCase(addDocumentsToCaseArgumentCaptor.capture());
		// Assert
		assertThat(addDocumentsToCaseArgumentCaptor.getValue().getAddDocumentToCaseSvcDto())
			.satisfies(addDocumentToCaseSvcDto -> {
					assertThat(addDocumentToCaseSvcDto.getCaseId()).isEqualTo(caseId);
					assertThat(addDocumentToCaseSvcDto.getOccurrenceTypeId()).isEqualTo(Constants.ECOS_OCCURRENCE_TYPE_ID_KOMPLETTERING);
					assertThat(addDocumentToCaseSvcDto.getDocumentStatusId()).isEqualTo(Constants.ECOS_DOCUMENT_STATUS_INKOMMEN);
					assertThat(addDocumentToCaseSvcDto.getDocuments().getDocumentSvcDto().getFirst())
						.satisfies(document -> {
								assertThat(document.getFilename()).isEqualTo(fileName + extension);
								assertThat(document.getContentType()).isEqualTo(mimeType);
								assertThat(document.getData()).isEqualTo(Base64.getDecoder().decode(file.getBytes(StandardCharsets.UTF_8)));
								assertThat(document.getDocumentTypeId()).contains(AttachmentCategory.fromCode(category).getDescription());
								assertThat(document.getNote()).isEqualTo(note);
							}
						);
				}
			);

	}

}
