package se.sundsvall.casemanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static se.sundsvall.casemanagement.TestUtil.ADRESSPLATS_ID;
import static se.sundsvall.casemanagement.TestUtil.FNR;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import minutmiljo.AddDocumentsToCase;
import minutmiljo.AddDocumentsToCaseSvcDto;
import minutmiljo.ArrayOfOccurrenceListItemSvcDto;
import minutmiljo.ArrayOfPartySvcDto;
import minutmiljo.ArrayOfSearchCaseResultSvcDto;
import minutmiljo.BiologicalStepSvcDto;
import minutmiljo.CaseSvcDto;
import minutmiljo.ClosedTankSvcDto;
import minutmiljo.CreateFoodFacility;
import minutmiljo.CreateHealthProtectionFacility;
import minutmiljo.CreateHealthProtectionFacilitySvcDto;
import minutmiljo.CreateHeatPumpFacility;
import minutmiljo.CreateHeatPumpFacilitySvcDto;
import minutmiljo.CreateHeatPumpFacilityWithHeatTransferFluidSvcDto;
import minutmiljo.CreateIndividualSewageFacility;
import minutmiljo.CreateIndividualSewageFacilitySvcDto;
import minutmiljo.CreateOccurrenceOnCase;
import minutmiljo.CreateSoilHeatingFacilitySvcDto;
import minutmiljo.DocumentSvcDto;
import minutmiljo.DrySolutionSvcDto;
import minutmiljo.FilterBedSvcDto;
import minutmiljo.GetCase;
import minutmiljo.GetCaseResponse;
import minutmiljo.InfiltrationPlantSvcDto;
import minutmiljo.MiniSewageSvcDto;
import minutmiljo.OccurrenceListItemSvcDto;
import minutmiljo.PartySvcDto;
import minutmiljo.PurificationStepSvcDto;
import minutmiljo.SandFilterSvcDto;
import minutmiljo.SearchCaseResponse;
import minutmiljo.SearchCaseResultSvcDto;
import minutmiljo.SepticTankSvcDto;
import minutmiljoV2.RegisterDocument;
import minutmiljoV2.RegisterDocumentCaseSvcDtoV2;
import se.sundsvall.casemanagement.TestUtil;
import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.api.model.EnvironmentalCaseDTO;
import se.sundsvall.casemanagement.api.model.EnvironmentalFacilityDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.StakeholderDTO;
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

@ExtendWith(MockitoExtension.class)
class EcosServiceTest {

	private static final String ECOS_CASE_NUMBER = "Inskickat";

	private static final String CYTONOL = "e19981ad-34b2-4e14-88f5-133f61ca85aa";

	@InjectMocks
	private EcosService ecosService;

	@Mock
	private CitizenMappingService citizenMappingServiceMock;

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

	@BeforeEach
	void beforeEach() {
		TestUtil.standardMockFb(fbServiceMock);
		TestUtil.standardMockCitizenMapping(citizenMappingServiceMock);
		TestUtil.standardMockMinutMiljo(minutMiljoClientMock, minutMiljoClientV2Mock);
		TestUtil.standardMockFb(fbServiceMock);
		TestUtil.standardMockCitizenMapping(citizenMappingServiceMock);
		TestUtil.standardMockPartyService(partyServiceMock);
	}

	@Test
	void testFoodFacilityCase() {

		final EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.REGISTRERING_AV_LIVSMEDEL, AttachmentCategory.ANMALAN_LIVSMEDELSANLAGGNING);
		final OrganizationDTO organization = (OrganizationDTO) TestUtil.createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT, StakeholderRole.OPERATOR));
		final PersonDTO person = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.CONTACT_PERSON));
		eCase.setStakeholders(List.of(organization, person));

		final var result = ecosService.postCase(eCase);
		assertEquals(ECOS_CASE_NUMBER, result.getCaseNumber());

		final ArgumentCaptor<CreateFoodFacility> createFoodFacilityArgumentCaptor = ArgumentCaptor.forClass(CreateFoodFacility.class);
		verify(minutMiljoClientMock, times(1)).createFoodFacility(createFoodFacilityArgumentCaptor.capture());
		assertEquals(ADRESSPLATS_ID, createFoodFacilityArgumentCaptor.getValue().getCreateFoodFacilitySvcDto().getAddress().getAdressPlatsId());
		assertEquals(FNR, createFoodFacilityArgumentCaptor.getValue().getCreateFoodFacilitySvcDto().getEstateDesignation().getFnr());
		assertEquals(result.getCaseId(), createFoodFacilityArgumentCaptor.getValue().getCreateFoodFacilitySvcDto().getCase());
		assertEquals(eCase.getFacilities().get(0).getDescription(), createFoodFacilityArgumentCaptor.getValue().getCreateFoodFacilitySvcDto().getNote());
		assertTrue(createFoodFacilityArgumentCaptor.getValue().getCreateFoodFacilitySvcDto().getFacilityCollectionName().contains(eCase.getFacilities().get(0).getFacilityCollectionName()));

		final ArgumentCaptor<RegisterDocument> registerDocumentArgumentCaptor = ArgumentCaptor.forClass(RegisterDocument.class);
		verify(minutMiljoClientV2Mock, times(1)).registerDocumentV2(registerDocumentArgumentCaptor.capture());
		final RegisterDocumentCaseSvcDtoV2 registerDocumentCaseSvcDtoV2 = registerDocumentArgumentCaptor.getValue().getRegisterDocumentCaseSvcDto();
		assertEquals(eCase.getFacilities().get(0).getFacilityCollectionName() + ", " + eCase.getFacilities().get(0).getAddress().getPropertyDesignation().toUpperCase(), registerDocumentCaseSvcDtoV2.getCaseSubtitleFree());
		assertEquals(Constants.ECOS_OCCURENCE_TYPE_ID_ANMALAN, registerDocumentCaseSvcDtoV2.getOccurrenceTypeId());
		assertEquals(Constants.ECOS_HANDLING_OFFICER_GROUP_ID_EXPEDITIONEN, registerDocumentCaseSvcDtoV2.getHandlingOfficerGroupId());
		assertEquals(Constants.ECOS_DIARY_PLAN_LIVSMEDEL, registerDocumentCaseSvcDtoV2.getDiaryPlanId());
		assertEquals(Constants.ECOS_PROCESS_TYPE_ID_REGISTRERING_AV_LIVSMEDEL, registerDocumentCaseSvcDtoV2.getProcessTypeId());

		verify(caseMappingServiceMock, times(1)).postCaseMapping(any(CaseMapping.class));
	}

	@Test
	void testMinimalFoodFacilityCase() {

		final EnvironmentalCaseDTO eCase = new EnvironmentalCaseDTO();
		final List<AttachmentDTO> aList = new ArrayList<>();
		final AttachmentDTO attachment = new AttachmentDTO();
		attachment.setCategory(AttachmentCategory.ANMALAN_LIVSMEDELSANLAGGNING);
		attachment.setExtension(".pdf");
		attachment.setName("Document Name");
		attachment.setFile("dGVzdA==");
		aList.add(attachment);
		eCase.setAttachments(aList);

		final List<StakeholderDTO> sList = new ArrayList<>();
		final OrganizationDTO organization = new OrganizationDTO();
		organization.setType(StakeholderType.ORGANIZATION);
		final List<StakeholderRole> srList = new ArrayList<>();
		srList.add(StakeholderRole.OPERATOR);
		organization.setRoles(srList);
		organization.setOrganizationName("organizationName");
		organization.setOrganizationNumber("123456-1234");

		sList.add(organization);

		final PersonDTO person = new PersonDTO();
		person.setType(StakeholderType.PERSON);
		final List<StakeholderRole> srList2 = new ArrayList<>();
		srList2.add(StakeholderRole.INVOICE_RECIPENT);
		person.setRoles(srList2);
		person.setFirstName("Förnamn");
		person.setLastName("Efternamn");

		sList.add(person);

		eCase.setStakeholders(sList);

		final EnvironmentalFacilityDTO facility = new EnvironmentalFacilityDTO();
		facility.setFacilityCollectionName("facilityCollectionName");

		final AddressDTO facilityAddress = new AddressDTO();
		facilityAddress.setAddressCategories(List.of(AddressCategory.VISITING_ADDRESS));
		facilityAddress.setPropertyDesignation("SUNDSVALL BALDER 2");
		facility.setAddress(facilityAddress);
		eCase.setFacilities(List.of(facility));

		eCase.setCaseType(CaseType.REGISTRERING_AV_LIVSMEDEL);
		eCase.setExternalCaseId(String.valueOf(new Random().nextLong()));

		ecosService.postCase(eCase);

		verify(minutMiljoClientMock, times(1)).createFoodFacility(any());
		verify(minutMiljoClientV2Mock, times(1)).registerDocumentV2(any());
		verify(caseMappingServiceMock, times(1)).postCaseMapping(any(CaseMapping.class));
	}

	// createHeatPumpFacility
	@Test
	void testCreateHeatPumpFacilityCase() {
		final EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP, AttachmentCategory.ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW);
		final OrganizationDTO organization = (OrganizationDTO) TestUtil.createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT, StakeholderRole.OPERATOR));
		final PersonDTO person = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.CONTACT_PERSON));
		eCase.setStakeholders(List.of(organization, person));

		final var result = ecosService.postCase(eCase);
		assertEquals("Inskickat", result.getCaseNumber());

		final ArgumentCaptor<CreateHeatPumpFacility> createHeatPumpFacilityArgumentCaptor = ArgumentCaptor.forClass(CreateHeatPumpFacility.class);
		verify(minutMiljoClientMock, times(1)).createHeatPumpFacility(createHeatPumpFacilityArgumentCaptor.capture());
		final CreateSoilHeatingFacilitySvcDto createSoilHeatingFacilitySvcDto = (CreateSoilHeatingFacilitySvcDto) createHeatPumpFacilityArgumentCaptor.getValue().getCreateIndividualSewageSvcDto();

		assertEquals(ADRESSPLATS_ID, createSoilHeatingFacilitySvcDto.getAddress().getAdressPlatsId());
		assertEquals(Constants.ECOS_FACILITY_STATUS_ID_ANMALD_ANSOKT, createSoilHeatingFacilitySvcDto.getFacilityStatusId());
		assertEquals(result.getCaseId(), createSoilHeatingFacilitySvcDto.getCreatedFromCaseId());
		assertEquals(FNR, createSoilHeatingFacilitySvcDto.getEstate().getFnr());
		assertNotNull(createSoilHeatingFacilitySvcDto.getManufacturer());
		assertNotNull(createSoilHeatingFacilitySvcDto.getModel());
		assertNotNull(createSoilHeatingFacilitySvcDto.getPowerConsumption());
		assertNotNull(createSoilHeatingFacilitySvcDto.getPowerOutput());
	}

	@Test
	void testAirHeating() {
		final EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP, AttachmentCategory.ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW);
		final Map<String, String> extraParameters = new HashMap<>();
		extraParameters.put("CreateAirHeatingFacilitySvcDto_Manufacturer", "Mitsubishi");
		extraParameters.put("CreateAirHeatingFacilitySvcDto_Model", "SuperHeater 2000");
		extraParameters.put("CreateAirHeatingFacilitySvcDto_PowerConsumption", "1.7");
		extraParameters.put("CreateAirHeatingFacilitySvcDto_PowerOutput", "4.8");
		eCase.getFacilities().get(0).setExtraParameters(extraParameters);

		ecosService.postCase(eCase);

		final ArgumentCaptor<CreateHeatPumpFacility> createHeatPumpFacilityArgumentCaptor = ArgumentCaptor.forClass(CreateHeatPumpFacility.class);
		verifyMinutMiljoCallsForHeatPumpCase(createHeatPumpFacilityArgumentCaptor);

		verifyStandardParams(extraParameters, createHeatPumpFacilityArgumentCaptor.getValue().getCreateIndividualSewageSvcDto());
	}

	@Test
	void testGeothermalHeating() {
		final EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP, AttachmentCategory.ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW);
		final Map<String, String> extraParameters = new HashMap<>();
		extraParameters.put("CreateGeothermalHeatingFacilitySvcDto_Manufacturer", "Mitsubishi");
		extraParameters.put("CreateGeothermalHeatingFacilitySvcDto_Model", "SuperHeater 2000");
		extraParameters.put("CreateGeothermalHeatingFacilitySvcDto_PowerConsumption", "1.7");
		extraParameters.put("CreateGeothermalHeatingFacilitySvcDto_PowerOutput", "4.8");
		extraParameters.put("CreateGeothermalHeatingFacilitySvcDto_Capacity", "90.8");
		extraParameters.put("CreateGeothermalHeatingFacilitySvcDto_HeatTransferFluidId", CYTONOL);
		eCase.getFacilities().get(0).setExtraParameters(extraParameters);

		ecosService.postCase(eCase);

		final ArgumentCaptor<CreateHeatPumpFacility> createHeatPumpFacilityArgumentCaptor = ArgumentCaptor.forClass(CreateHeatPumpFacility.class);
		verifyMinutMiljoCallsForHeatPumpCase(createHeatPumpFacilityArgumentCaptor);

		verifyStandardParams(extraParameters, createHeatPumpFacilityArgumentCaptor.getValue().getCreateIndividualSewageSvcDto());
		verifyFluidParams(extraParameters, (CreateHeatPumpFacilityWithHeatTransferFluidSvcDto) createHeatPumpFacilityArgumentCaptor.getValue().getCreateIndividualSewageSvcDto());
	}

	@Test
	void testSoilHeating() {
		final EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP, AttachmentCategory.ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW);
		final Map<String, String> extraParameters = new HashMap<>();
		extraParameters.put("CreateSoilHeatingFacilitySvcDto_Manufacturer", "Mitsubishi");
		extraParameters.put("CreateSoilHeatingFacilitySvcDto_Model", "SuperHeater 2000");
		extraParameters.put("CreateSoilHeatingFacilitySvcDto_PowerConsumption", "1.7");
		extraParameters.put("CreateSoilHeatingFacilitySvcDto_PowerOutput", "4.8");
		extraParameters.put("CreateSoilHeatingFacilitySvcDto_Capacity", "90.8");
		extraParameters.put("CreateSoilHeatingFacilitySvcDto_HeatTransferFluidId", CYTONOL);
		eCase.getFacilities().get(0).setExtraParameters(extraParameters);

		ecosService.postCase(eCase);

		final ArgumentCaptor<CreateHeatPumpFacility> createHeatPumpFacilityArgumentCaptor = ArgumentCaptor.forClass(CreateHeatPumpFacility.class);
		verifyMinutMiljoCallsForHeatPumpCase(createHeatPumpFacilityArgumentCaptor);

		verifyStandardParams(extraParameters, createHeatPumpFacilityArgumentCaptor.getValue().getCreateIndividualSewageSvcDto());
		verifyFluidParams(extraParameters, (CreateHeatPumpFacilityWithHeatTransferFluidSvcDto) createHeatPumpFacilityArgumentCaptor.getValue().getCreateIndividualSewageSvcDto());
	}

	@Test
	void testExtraParamsNull() {
		final EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP, AttachmentCategory.ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW);
		eCase.getFacilities().get(0).setExtraParameters(null);

		ecosService.postCase(eCase);

		verify(minutMiljoClientMock, times(0)).createHeatPumpFacility(any());
		verify(minutMiljoClientV2Mock, times(1)).registerDocumentV2(any());
	}

	@Test
	void testExtraParamsEmpty() {
		final EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP, AttachmentCategory.ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW);
		final Map<String, String> extraParameters = new HashMap<>();
		eCase.getFacilities().get(0).setExtraParameters(extraParameters);

		ecosService.postCase(eCase);

		verify(minutMiljoClientMock, times(0)).createHeatPumpFacility(any());
		verify(minutMiljoClientV2Mock, times(1)).registerDocumentV2(any());
	}

	@Test
	void testMarineHeating() {
		final EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP, AttachmentCategory.ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW);
		final Map<String, String> extraParameters = new HashMap<>();
		extraParameters.put("CreateMarineHeatingFacilitySvcDto_Manufacturer", "Mitsubishi");
		extraParameters.put("CreateMarineHeatingFacilitySvcDto_Model", "SuperHeater 2000");
		extraParameters.put("CreateMarineHeatingFacilitySvcDto_PowerConsumption", "1.7");
		extraParameters.put("CreateMarineHeatingFacilitySvcDto_PowerOutput", "4.8");
		extraParameters.put("CreateMarineHeatingFacilitySvcDto_Capacity", "90.8");
		extraParameters.put("CreateMarineHeatingFacilitySvcDto_HeatTransferFluidId", CYTONOL);
		eCase.getFacilities().get(0).setExtraParameters(extraParameters);

		ecosService.postCase(eCase);

		final ArgumentCaptor<CreateHeatPumpFacility> createHeatPumpFacilityArgumentCaptor = ArgumentCaptor.forClass(CreateHeatPumpFacility.class);
		verifyMinutMiljoCallsForHeatPumpCase(createHeatPumpFacilityArgumentCaptor);

		verifyStandardParams(extraParameters, createHeatPumpFacilityArgumentCaptor.getValue().getCreateIndividualSewageSvcDto());
		verifyFluidParams(extraParameters, (CreateHeatPumpFacilityWithHeatTransferFluidSvcDto) createHeatPumpFacilityArgumentCaptor.getValue().getCreateIndividualSewageSvcDto());
	}

	private void verifyStandardParams(final Map<String, String> extraParameters, final CreateHeatPumpFacilitySvcDto svcDto) {
		Assertions.assertEquals(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("Manufacturer")).findFirst().orElseThrow().getValue(), svcDto.getManufacturer());
		Assertions.assertEquals(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("Model")).findFirst().orElseThrow().getValue(), svcDto.getModel());
		Assertions.assertEquals(CaseUtil.parseDouble(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("PowerConsumption")).findFirst().orElseThrow().getValue()), svcDto.getPowerConsumption());
		Assertions.assertEquals(CaseUtil.parseDouble(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("PowerOutput")).findFirst().orElseThrow().getValue()), svcDto.getPowerOutput());
	}

	private void verifyFluidParams(final Map<String, String> extraParameters, final CreateHeatPumpFacilityWithHeatTransferFluidSvcDto svcDto) {
		Assertions.assertEquals(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("HeatTransferFluidId")).findFirst().orElseThrow().getValue(), svcDto.getHeatTransferFluidId());
		Assertions.assertEquals(CaseUtil.parseDouble(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("Capacity")).findFirst().orElseThrow().getValue()), svcDto.getCapacity());
	}

	private void verifyMinutMiljoCallsForHeatPumpCase(final ArgumentCaptor<CreateHeatPumpFacility> createHeatPumpFacilityArgumentCaptor) {
		verify(partyServiceMock, times(1)).findAndAddPartyToCase(any(EnvironmentalCaseDTO.class), any(String.class));
		verify(minutMiljoClientMock, times(1)).createHeatPumpFacility(createHeatPumpFacilityArgumentCaptor.capture());
		verify(minutMiljoClientMock, times(3)).addPartyToFacility(any());
		verify(minutMiljoClientV2Mock, times(1)).registerDocumentV2(any());
	}

	@Test
	void healthProtectionTest() {
		final EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANMALAN_HALSOSKYDDSVERKSAMHET, AttachmentCategory.ANMALAN_HALSOSKYDDSVERKSAMHET);

		ecosService.postCase(eCase);

		final ArgumentCaptor<CreateHealthProtectionFacility> createHealthProtectionFacilityArgumentCaptor = ArgumentCaptor.forClass(CreateHealthProtectionFacility.class);
		verify(partyServiceMock, times(1)).findAndAddPartyToCase(any(EnvironmentalCaseDTO.class), any(String.class));
		verify(partyServiceMock, times(1)).findAndAddPartyToCase(any(EnvironmentalCaseDTO.class), any(String.class));
		verify(minutMiljoClientMock, times(1)).createHealthProtectionFacility(createHealthProtectionFacilityArgumentCaptor.capture());
		verify(minutMiljoClientMock, times(3)).addPartyToFacility(any());
		verify(minutMiljoClientV2Mock, times(1)).registerDocumentV2(any());

		final CreateHealthProtectionFacilitySvcDto svcDto = createHealthProtectionFacilityArgumentCaptor.getValue().getCreateHealthProtectionFacilitySvcDto();
		Assertions.assertEquals(ADRESSPLATS_ID, svcDto.getAddress().getAdressPlatsId());
		Assertions.assertEquals(FNR, svcDto.getEstateDesignation().getFnr());
		Assertions.assertEquals("e19981ad-34b2-4e14-88f5-133f61ca85aa", svcDto.getCase());
		Assertions.assertEquals(eCase.getFacilities().get(0).getDescription(), svcDto.getNote());
		Assertions.assertEquals(eCase.getFacilities().get(0).getFacilityCollectionName(), svcDto.getFacilityCollectionName());
	}

	@Test
	void individualSewageSepticTank() {
		final EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
		final Map<String, String> extraParameters = new HashMap<>();
		final String prefix = "SepticTankSvcDto_";
		TestUtil.setSewageStandardExtraParams(extraParameters, prefix);
		extraParameters.put(prefix + "EmptyingInterval", String.valueOf(new Random().nextInt()));
		extraParameters.put(prefix + "HasCeMarking", String.valueOf(new Random().nextBoolean()));
		extraParameters.put(prefix + "HasTPipe", String.valueOf(new Random().nextBoolean()));
		extraParameters.put(prefix + "Volume", String.valueOf(new Random().nextDouble()));
		eCase.getFacilities().get(0).setExtraParameters(extraParameters);

		ecosService.postCase(eCase);

		final ArgumentCaptor<CreateIndividualSewageFacility> argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
		verifyMinutMiljoCallsForSewageCase(argumentCaptor);
		verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);

		final SepticTankSvcDto septicTankSvcDto = (SepticTankSvcDto) argumentCaptor.getValue().getCreateIndividualSewageSvcDto().getPurificationSteps().getPurificationStepSvcDto().get(0);

		Assertions.assertEquals(CaseUtil.parseInteger(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "EmptyingInterval")).findFirst().orElseThrow().getValue()), septicTankSvcDto.getEmptyingInterval());
		Assertions.assertEquals(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "HasCeMarking")).findFirst().orElseThrow().getValue()), septicTankSvcDto.isHasCeMarking());
		Assertions.assertEquals(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "HasTPipe")).findFirst().orElseThrow().getValue()), septicTankSvcDto.isHasTPipe());
		Assertions.assertEquals(CaseUtil.parseDouble(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Volume")).findFirst().orElseThrow().getValue()), septicTankSvcDto.getVolume());
	}

	@Test
	void individualSewageInfiltration() {
		final EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
		final Map<String, String> extraParameters = new HashMap<>();
		final String prefix = "InfiltrationPlantSvcDto_";
		TestUtil.setSewageStandardExtraParams(extraParameters, prefix);

		extraParameters.put(prefix + "Elevated", String.valueOf(new Random().nextBoolean()));
		extraParameters.put(prefix + "Reinforced", String.valueOf(new Random().nextBoolean()));
		extraParameters.put(prefix + "IsModuleSystem", String.valueOf(new Random().nextBoolean()));
		extraParameters.put(prefix + "SpreadLinesCount", String.valueOf(new Random().nextInt()));
		extraParameters.put(prefix + "SpreadLinesLength", String.valueOf(new Random().nextInt()));
		eCase.getFacilities().get(0).setExtraParameters(extraParameters);

		ecosService.postCase(eCase);

		final ArgumentCaptor<CreateIndividualSewageFacility> argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
		verifyMinutMiljoCallsForSewageCase(argumentCaptor);
		verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);

		final InfiltrationPlantSvcDto infiltrationPlantSvcDto = (InfiltrationPlantSvcDto) argumentCaptor.getValue().getCreateIndividualSewageSvcDto().getPurificationSteps().getPurificationStepSvcDto().get(0);

		Assertions.assertEquals(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Elevated")).findFirst().orElseThrow().getValue()), infiltrationPlantSvcDto.isElevated());
		Assertions.assertEquals(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Reinforced")).findFirst().orElseThrow().getValue()), infiltrationPlantSvcDto.isReinforced());
		Assertions.assertEquals(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "IsModuleSystem")).findFirst().orElseThrow().getValue()), infiltrationPlantSvcDto.isIsModuleSystem());
		Assertions.assertEquals(CaseUtil.parseInteger(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "SpreadLinesCount")).findFirst().orElseThrow().getValue()), infiltrationPlantSvcDto.getSpreadLinesCount());
		Assertions.assertEquals(CaseUtil.parseInteger(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "SpreadLinesLength")).findFirst().orElseThrow().getValue()), infiltrationPlantSvcDto.getSpreadLinesLength());
	}

	@Test
	void individualSewageClosedTank() {
		final EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
		final Map<String, String> extraParameters = new HashMap<>();
		final String prefix = "ClosedTankSvcDto_";
		TestUtil.setSewageStandardExtraParams(extraParameters, prefix);

		extraParameters.put(prefix + "EmptyingInterval", String.valueOf(new Random().nextInt()));
		extraParameters.put(prefix + "HasCeMarking", String.valueOf(new Random().nextBoolean()));
		extraParameters.put(prefix + "Volume", String.valueOf(new Random().nextDouble()));
		eCase.getFacilities().get(0).setExtraParameters(extraParameters);

		ecosService.postCase(eCase);

		final ArgumentCaptor<CreateIndividualSewageFacility> argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
		verifyMinutMiljoCallsForSewageCase(argumentCaptor);
		verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);

		final ClosedTankSvcDto closedTankSvcDto = (ClosedTankSvcDto) argumentCaptor.getValue().getCreateIndividualSewageSvcDto().getPurificationSteps().getPurificationStepSvcDto().get(0);

		Assertions.assertEquals(CaseUtil.parseInteger(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "EmptyingInterval")).findFirst().orElseThrow().getValue()), closedTankSvcDto.getEmptyingInterval());
		Assertions.assertEquals(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "HasCeMarking")).findFirst().orElseThrow().getValue()), closedTankSvcDto.isHasCeMarking());
		Assertions.assertEquals(CaseUtil.parseDouble(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Volume")).findFirst().orElseThrow().getValue()), closedTankSvcDto.getVolume());
	}

	@Test
	void individualSewageDrySolution() {
		final EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
		final Map<String, String> extraParameters = new HashMap<>();
		final String prefix = "DrySolutionSvcDto_";
		TestUtil.setSewageStandardExtraParams(extraParameters, prefix);

		extraParameters.put(prefix + "CompostProductName", UUID.randomUUID().toString());
		extraParameters.put(prefix + "DrySolutionCompostTypeId", UUID.randomUUID().toString());
		extraParameters.put(prefix + "DrySolutionTypeId", UUID.randomUUID().toString());
		extraParameters.put(prefix + "NoContOrCompt", UUID.randomUUID().toString());
		extraParameters.put(prefix + "NoLPerContOrCompt", UUID.randomUUID().toString());
		extraParameters.put(prefix + "ToiletProductName", UUID.randomUUID().toString());
		extraParameters.put(prefix + "Volume", String.valueOf(new Random().nextDouble()));
		eCase.getFacilities().get(0).setExtraParameters(extraParameters);

		ecosService.postCase(eCase);

		final ArgumentCaptor<CreateIndividualSewageFacility> argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
		verifyMinutMiljoCallsForSewageCase(argumentCaptor);
		verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);

		final DrySolutionSvcDto drySolutionSvcDto = (DrySolutionSvcDto) argumentCaptor.getValue().getCreateIndividualSewageSvcDto().getPurificationSteps().getPurificationStepSvcDto().get(0);

		Assertions.assertEquals(CaseUtil.parseString(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "CompostProductName")).findFirst().orElseThrow().getValue()), drySolutionSvcDto.getCompostProductName());
		Assertions.assertEquals(CaseUtil.parseString(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "DrySolutionCompostTypeId")).findFirst().orElseThrow().getValue()), drySolutionSvcDto.getDrySolutionCompostTypeId());
		Assertions.assertEquals(CaseUtil.parseString(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "DrySolutionTypeId")).findFirst().orElseThrow().getValue()), drySolutionSvcDto.getDrySolutionTypeId());
		Assertions.assertEquals(CaseUtil.parseString(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "NoContOrCompt")).findFirst().orElseThrow().getValue()), drySolutionSvcDto.getNoContOrCompt());
		Assertions.assertEquals(CaseUtil.parseString(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "NoLPerContOrCompt")).findFirst().orElseThrow().getValue()), drySolutionSvcDto.getNoLPerContOrCompt());
		Assertions.assertEquals(CaseUtil.parseString(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "ToiletProductName")).findFirst().orElseThrow().getValue()), drySolutionSvcDto.getToiletProductName());
		Assertions.assertEquals(CaseUtil.parseDouble(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Volume")).findFirst().orElseThrow().getValue()), drySolutionSvcDto.getVolume());
	}

	@Test
	void individualSewageMiniSewage() {
		final EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
		final Map<String, String> extraParameters = new HashMap<>();
		final String prefix = "MiniSewageSvcDto_";
		TestUtil.setSewageStandardExtraParams(extraParameters, prefix);

		extraParameters.put(prefix + "CeMarking", String.valueOf(new Random().nextBoolean()));
		extraParameters.put(prefix + "Manufacturer", UUID.randomUUID().toString());
		extraParameters.put(prefix + "Model", UUID.randomUUID().toString());
		eCase.getFacilities().get(0).setExtraParameters(extraParameters);

		ecosService.postCase(eCase);

		final ArgumentCaptor<CreateIndividualSewageFacility> argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
		verifyMinutMiljoCallsForSewageCase(argumentCaptor);
		verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);

		final MiniSewageSvcDto svcDto = (MiniSewageSvcDto) argumentCaptor.getValue().getCreateIndividualSewageSvcDto().getPurificationSteps().getPurificationStepSvcDto().get(0);

		Assertions.assertEquals(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "CeMarking")).findFirst().orElseThrow().getValue()), svcDto.isCeMarking());
		Assertions.assertEquals(CaseUtil.parseString(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Manufacturer")).findFirst().orElseThrow().getValue()), svcDto.getManufacturer());
		Assertions.assertEquals(CaseUtil.parseString(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Model")).findFirst().orElseThrow().getValue()), svcDto.getModel());
	}

	@Test
	void individualSewageFilterBed() {
		final EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
		final Map<String, String> extraParameters = new HashMap<>();
		final String prefix = "FilterBedSvcDto_";
		TestUtil.setSewageStandardExtraParams(extraParameters, prefix);

		extraParameters.put(prefix + "Volume", String.valueOf(new Random().nextDouble()));
		eCase.getFacilities().get(0).setExtraParameters(extraParameters);

		ecosService.postCase(eCase);

		final ArgumentCaptor<CreateIndividualSewageFacility> argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
		verifyMinutMiljoCallsForSewageCase(argumentCaptor);
		verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);

		final FilterBedSvcDto svcDto = (FilterBedSvcDto) argumentCaptor.getValue().getCreateIndividualSewageSvcDto().getPurificationSteps().getPurificationStepSvcDto().get(0);
		Assertions.assertEquals(CaseUtil.parseDouble(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Volume")).findFirst().orElseThrow().getValue()), svcDto.getVolume());
	}

	@Test
	void individualSewageSandFilter() {
		final EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
		final Map<String, String> extraParameters = new HashMap<>();
		final String prefix = "SandFilterSvcDto_";
		TestUtil.setSewageStandardExtraParams(extraParameters, prefix);

		extraParameters.put(prefix + "Area", String.valueOf(new Random().nextInt()));
		extraParameters.put(prefix + "Elevated", String.valueOf(new Random().nextBoolean()));
		extraParameters.put(prefix + "WaterTight", String.valueOf(new Random().nextBoolean()));
		eCase.getFacilities().get(0).setExtraParameters(extraParameters);

		ecosService.postCase(eCase);

		final ArgumentCaptor<CreateIndividualSewageFacility> argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
		verifyMinutMiljoCallsForSewageCase(argumentCaptor);
		verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);

		final SandFilterSvcDto svcDto = (SandFilterSvcDto) argumentCaptor.getValue().getCreateIndividualSewageSvcDto().getPurificationSteps().getPurificationStepSvcDto().get(0);
		Assertions.assertEquals(CaseUtil.parseInteger(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Area")).findFirst().orElseThrow().getValue()), svcDto.getArea());
		Assertions.assertEquals(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Elevated")).findFirst().orElseThrow().getValue()), svcDto.isElevated());
		Assertions.assertEquals(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "WaterTight")).findFirst().orElseThrow().getValue()), svcDto.isWaterTight());
	}

	@Test
	void individualSewageBiologicalStep() {
		final EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
		final Map<String, String> extraParameters = new HashMap<>();
		final String prefix = "BiologicalStepSvcDto_";
		TestUtil.setSewageStandardExtraParams(extraParameters, prefix);

		extraParameters.put(prefix + "Area", String.valueOf(new Random().nextInt()));
		extraParameters.put(prefix + "BiologicalStepTypeId", UUID.randomUUID().toString());
		eCase.getFacilities().get(0).setExtraParameters(extraParameters);

		ecosService.postCase(eCase);

		final ArgumentCaptor<CreateIndividualSewageFacility> argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
		verifyMinutMiljoCallsForSewageCase(argumentCaptor);
		verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);

		final BiologicalStepSvcDto svcDto = (BiologicalStepSvcDto) argumentCaptor.getValue().getCreateIndividualSewageSvcDto().getPurificationSteps().getPurificationStepSvcDto().get(0);
		Assertions.assertEquals(CaseUtil.parseInteger(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "Area")).findFirst().orElseThrow().getValue()), svcDto.getArea());
		Assertions.assertEquals(CaseUtil.parseString(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "BiologicalStepTypeId")).findFirst().orElseThrow().getValue()), svcDto.getBiologicalStepTypeId());
	}

	@Test
	void individualSewagePhosphorusTrap() {
		final EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
		final Map<String, String> extraParameters = new HashMap<>();
		final String prefix = "PhosphorusTrapSvcDto_";
		TestUtil.setSewageStandardExtraParams(extraParameters, prefix);
		eCase.getFacilities().get(0).setExtraParameters(extraParameters);

		ecosService.postCase(eCase);

		final ArgumentCaptor<CreateIndividualSewageFacility> argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
		verifyMinutMiljoCallsForSewageCase(argumentCaptor);
		verifyStandardParams(extraParameters, argumentCaptor.getValue().getCreateIndividualSewageSvcDto(), prefix);
	}

	@Test
	void individualSewageChemicalPretreatment() {
		final EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, AttachmentCategory.ANSOKAN_ENSKILT_AVLOPP);
		final Map<String, String> extraParameters = new HashMap<>();
		final String prefix = "ChemicalPretreatmentSvcDto_";
		TestUtil.setSewageStandardExtraParams(extraParameters, prefix);
		eCase.getFacilities().get(0).setExtraParameters(extraParameters);

		ecosService.postCase(eCase);

		final ArgumentCaptor<CreateIndividualSewageFacility> argumentCaptor = ArgumentCaptor.forClass(CreateIndividualSewageFacility.class);
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
		Assertions.assertEquals(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("OnGrantLand")).findFirst().orElseThrow().getValue()), svcDto.isOnGrantLand());
		Assertions.assertEquals(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("ProtectionLevelApprovedEnvironmentId")).findFirst().orElseThrow().getValue(), svcDto.getProtectionLevelApprovedEnvironmentId());
		Assertions.assertEquals(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("ProtectionLevelApprovedHealthId")).findFirst().orElseThrow().getValue(), svcDto.getProtectionLevelApprovedHealthId());
		Assertions.assertEquals(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("WastewaterApprovedForId")).findFirst().orElseThrow().getValue(), svcDto.getWastewaterApprovedForId());
		Assertions.assertEquals(extraParameters.entrySet().stream().filter(e -> e.getKey().contains("WasteWaterInboundId")).findFirst().orElseThrow().getValue(), svcDto.getWasteWaterInboundId());

		final PurificationStepSvcDto purificationStepSvcDto = svcDto.getPurificationSteps().getPurificationStepSvcDto().get(0);
		Assertions.assertEquals(CaseUtil.parseBoolean(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "HasOverflowAlarm")).findFirst().orElseThrow().getValue()), purificationStepSvcDto.isHasOverflowAlarm());
		Assertions.assertEquals(CaseUtil.parseLocalDateTime(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "InstallationDate")).findFirst().orElseThrow().getValue()), purificationStepSvcDto.getInstallationDate());
		Assertions.assertEquals(CaseUtil.parseInteger(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "LifeTime")).findFirst().orElseThrow().getValue()), purificationStepSvcDto.getLifeTime());
		Assertions.assertEquals(CaseUtil.parseInteger(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "PersonCapacity")).findFirst().orElseThrow().getValue()), purificationStepSvcDto.getPersonCapacity());
		Assertions.assertEquals(Constants.ECOS_FACILITY_STATUS_ID_ANMALD_ANSOKT, purificationStepSvcDto.getPurificationStepFacilityStatusId());
		Assertions.assertEquals(CaseUtil.parseInteger(extraParameters.entrySet().stream().filter(e -> e.getKey().contains(prefix + "StepNumber")).findFirst().orElseThrow().getValue()), purificationStepSvcDto.getStepNumber());
	}

	@Test
	void testMissingFacilityAddress() {

		final EnvironmentalCaseDTO eCase = TestUtil.createEnvironmentalCase(CaseType.REGISTRERING_AV_LIVSMEDEL, AttachmentCategory.ANMALAN_LIVSMEDELSANLAGGNING);
		eCase.getFacilities().get(0).setAddress(null);

		final var result = ecosService.postCase(eCase);

		verify(minutMiljoClientV2Mock, times(1)).registerDocumentV2(any());
		final ArgumentCaptor<CreateOccurrenceOnCase> createOccurrenceOnCaseArgumentCaptor = ArgumentCaptor.forClass(CreateOccurrenceOnCase.class);
		verify(minutMiljoClientMock, times(1)).createOccurrenceOnCase(createOccurrenceOnCaseArgumentCaptor.capture());
		verify(partyServiceMock, times(1)).findAndAddPartyToCase(any(EnvironmentalCaseDTO.class), any(String.class));
		assertEquals(result.getCaseId(), createOccurrenceOnCaseArgumentCaptor.getValue().getCreateOccurrenceOnCaseSvcDto().getCaseId());
		assertNotNull(createOccurrenceOnCaseArgumentCaptor.getValue().getCreateOccurrenceOnCaseSvcDto().getOccurrenceDate());
		assertEquals(Constants.ECOS_OCCURRENCE_TYPE_ID_INFO_FRAN_ETJANST, createOccurrenceOnCaseArgumentCaptor.getValue().getCreateOccurrenceOnCaseSvcDto().getOccurrenceTypeId());
		assertEquals(Constants.ECOS_OCCURENCE_TEXT_MOBIL_ANLAGGNING, createOccurrenceOnCaseArgumentCaptor.getValue().getCreateOccurrenceOnCaseSvcDto().getNote());

		// If facility doesn't have any address, we should not register any facility and therefore not add any stakeholder to
		// facility.
		// This results in manual handling for admin.
		verify(minutMiljoClientMock, times(0)).createFoodFacility(any());
		verify(minutMiljoClientMock, times(0)).addPartyToFacility(any());

		verify(caseMappingServiceMock, times(1)).postCaseMapping(any(CaseMapping.class));
	}

	@Test
	void testGetStatus() {
		final String caseId = MessageFormat.format("MK-2022-{0}", new Random().nextInt(100000));
		final String externalCaseID = UUID.randomUUID().toString();

		final CaseMapping caseMapping = new CaseMapping();
		caseMapping.setExternalCaseId(externalCaseID);
		caseMapping.setCaseId(caseId);
		caseMapping.setCaseType(CaseType.REGISTRERING_AV_LIVSMEDEL);
		caseMapping.setSystem(SystemType.ECOS);
		caseMapping.setServiceName(RandomStringUtils.random(10, true, false));
		caseMapping.setTimestamp(LocalDateTime.now());
		doReturn(List.of(caseMapping)).when(caseMappingServiceMock).getCaseMapping(externalCaseID, caseId);

		final GetCaseResponse getCaseResponse = new GetCaseResponse();
		final CaseSvcDto caseSvcDto = new CaseSvcDto();
		caseSvcDto.setCaseNumber(caseId);
		caseSvcDto.setCaseId(UUID.randomUUID().toString());
		final ArrayOfOccurrenceListItemSvcDto arrayOfOccurrenceListItemSvcDto = new ArrayOfOccurrenceListItemSvcDto();
		final OccurrenceListItemSvcDto occurrenceListItemSvcDto_1 = new OccurrenceListItemSvcDto();
		occurrenceListItemSvcDto_1.setOccurrenceDate(LocalDateTime.now().minusDays(5));
		occurrenceListItemSvcDto_1.setOccurrenceDescription(RandomStringUtils.random(5, true, false));
		final OccurrenceListItemSvcDto occurrenceListItemSvcDto_2 = new OccurrenceListItemSvcDto();
		occurrenceListItemSvcDto_2.setOccurrenceDate(LocalDateTime.now().minusDays(1));
		occurrenceListItemSvcDto_2.setOccurrenceDescription(RandomStringUtils.random(5, true, false));
		final OccurrenceListItemSvcDto occurrenceListItemSvcDto_3 = new OccurrenceListItemSvcDto();
		occurrenceListItemSvcDto_3.setOccurrenceDate(LocalDateTime.now().minusDays(3));
		occurrenceListItemSvcDto_3.setOccurrenceDescription(RandomStringUtils.random(5, true, false));
		arrayOfOccurrenceListItemSvcDto.getOccurrenceListItemSvcDto().addAll(List.of(occurrenceListItemSvcDto_1, occurrenceListItemSvcDto_2, occurrenceListItemSvcDto_3));
		caseSvcDto.setOccurrences(arrayOfOccurrenceListItemSvcDto);
		getCaseResponse.setGetCaseResult(caseSvcDto);
		doReturn(getCaseResponse).when(minutMiljoClientMock).getCase(any());

		final var result = ecosService.getStatus(caseId, externalCaseID);

		assertEquals(caseId, result.getCaseId());
		assertEquals(externalCaseID, result.getExternalCaseId());
		assertEquals(caseMapping.getCaseType(), result.getCaseType());
		assertEquals(caseMapping.getSystem(), result.getSystem());
		assertEquals(caseMapping.getServiceName(), result.getServiceName());
		assertEquals(occurrenceListItemSvcDto_2.getOccurrenceDescription(), result.getStatus());
		assertEquals(occurrenceListItemSvcDto_2.getOccurrenceDate(), result.getTimestamp());

		final ArgumentCaptor<GetCase> getCaseArgumentCaptor = ArgumentCaptor.forClass(GetCase.class);
		verify(minutMiljoClientMock, times(1)).getCase(getCaseArgumentCaptor.capture());
		assertEquals(caseId, getCaseArgumentCaptor.getValue().getCaseId());
	}

	@Test
	void testGetStatusNotFound() {
		final String caseId = MessageFormat.format("MK-2022-{0}", new Random().nextInt(100000));
		final String externalCaseID = UUID.randomUUID().toString();

		final GetCaseResponse getCaseResponse = new GetCaseResponse();
		final CaseSvcDto caseSvcDto = new CaseSvcDto();
		caseSvcDto.setCaseNumber(caseId);
		caseSvcDto.setCaseId(UUID.randomUUID().toString());
		getCaseResponse.setGetCaseResult(caseSvcDto);
		doReturn(getCaseResponse).when(minutMiljoClientMock).getCase(any());

		final var problem = assertThrows(ThrowableProblem.class, () -> ecosService.getStatus(caseId, externalCaseID));
		assertEquals(Status.NOT_FOUND, problem.getStatus());
		assertEquals(Constants.ERR_MSG_STATUS_NOT_FOUND, problem.getDetail());
	}

	@Test
	void testGetStatusByOrgnr() {
		final String caseNumber = MessageFormat.format("MK-2022-{0}", new Random().nextInt(100000));
		final String caseId = UUID.randomUUID().toString();
		final String externalCaseID = UUID.randomUUID().toString();
		final String orgnr = TestUtil.generateRandomOrganizationNumber();

		// Mock partyService.searchParty
		final ArrayOfPartySvcDto arrayOfPartySvcDto = new ArrayOfPartySvcDto();
		final PartySvcDto partySvcDto_1 = new PartySvcDto();
		partySvcDto_1.setId(UUID.randomUUID().toString());
		final PartySvcDto partySvcDto_2 = new PartySvcDto();
		partySvcDto_2.setId(UUID.randomUUID().toString());
		arrayOfPartySvcDto.getPartySvcDto().addAll(List.of(partySvcDto_1, partySvcDto_2));

		doReturn(arrayOfPartySvcDto).when(partyServiceMock).searchPartyByOrganizationNumber(any());

		// Mock minutMiljoClient.searchCase
		final SearchCaseResponse searchCaseResponse = new SearchCaseResponse();
		final ArrayOfSearchCaseResultSvcDto arrayOfSearchCaseResultSvcDto = new ArrayOfSearchCaseResultSvcDto();
		final SearchCaseResultSvcDto searchCaseResultSvcDto_1 = new SearchCaseResultSvcDto();
		searchCaseResultSvcDto_1.setCaseId(caseId);
		searchCaseResultSvcDto_1.setCaseNumber(caseNumber);
		final SearchCaseResultSvcDto searchCaseResultSvcDto_2 = new SearchCaseResultSvcDto();
		searchCaseResultSvcDto_2.setCaseId(caseId);
		searchCaseResultSvcDto_2.setCaseNumber(caseNumber);
		arrayOfSearchCaseResultSvcDto.getSearchCaseResultSvcDto().addAll(List.of(searchCaseResultSvcDto_1, searchCaseResultSvcDto_2));
		searchCaseResponse.setSearchCaseResult(arrayOfSearchCaseResultSvcDto);
		doReturn(searchCaseResponse).when(minutMiljoClientMock).searchCase(any());

		// Mock caseMappingService.getCaseMapping
		final CaseMapping caseMapping = new CaseMapping();
		caseMapping.setExternalCaseId(externalCaseID);
		caseMapping.setCaseId(caseId);
		caseMapping.setCaseType(CaseType.REGISTRERING_AV_LIVSMEDEL);
		caseMapping.setSystem(SystemType.ECOS);
		caseMapping.setServiceName(RandomStringUtils.random(10, true, false));
		caseMapping.setTimestamp(LocalDateTime.now());
		doReturn(List.of(caseMapping)).when(caseMappingServiceMock).getCaseMapping(any(), any());

		// Mock minutMiljoClient.getCase
		final GetCaseResponse getCaseResponse = new GetCaseResponse();
		final CaseSvcDto caseSvcDto = new CaseSvcDto();
		caseSvcDto.setCaseNumber(caseNumber);
		caseSvcDto.setCaseId(caseId);
		final ArrayOfOccurrenceListItemSvcDto arrayOfOccurrenceListItemSvcDto = new ArrayOfOccurrenceListItemSvcDto();
		final OccurrenceListItemSvcDto occurrenceListItemSvcDto_1 = new OccurrenceListItemSvcDto();
		occurrenceListItemSvcDto_1.setOccurrenceDate(LocalDateTime.now().minusDays(5));
		occurrenceListItemSvcDto_1.setOccurrenceDescription(RandomStringUtils.random(5, true, false));
		final OccurrenceListItemSvcDto occurrenceListItemSvcDto_2 = new OccurrenceListItemSvcDto();
		occurrenceListItemSvcDto_2.setOccurrenceDate(LocalDateTime.now().minusDays(1));
		occurrenceListItemSvcDto_2.setOccurrenceDescription(RandomStringUtils.random(5, true, false));
		final OccurrenceListItemSvcDto occurrenceListItemSvcDto_3 = new OccurrenceListItemSvcDto();
		occurrenceListItemSvcDto_3.setOccurrenceDate(LocalDateTime.now().minusDays(3));
		occurrenceListItemSvcDto_3.setOccurrenceDescription(RandomStringUtils.random(5, true, false));
		arrayOfOccurrenceListItemSvcDto.getOccurrenceListItemSvcDto().addAll(List.of(occurrenceListItemSvcDto_1, occurrenceListItemSvcDto_2, occurrenceListItemSvcDto_3));
		caseSvcDto.setOccurrences(arrayOfOccurrenceListItemSvcDto);
		getCaseResponse.setGetCaseResult(caseSvcDto);
		doReturn(getCaseResponse).when(minutMiljoClientMock).getCase(any());

		final var result = ecosService.getEcosStatusByOrgNr(orgnr);

		assertEquals(2, result.size());
		result.forEach(status -> {
			assertEquals(caseNumber, status.getCaseId());
			assertEquals(externalCaseID, status.getExternalCaseId());
			assertEquals(caseMapping.getCaseType(), status.getCaseType());
			assertEquals(caseMapping.getSystem(), status.getSystem());
			assertEquals(caseMapping.getServiceName(), status.getServiceName());
			assertEquals(occurrenceListItemSvcDto_2.getOccurrenceDescription(), status.getStatus());
			assertEquals(occurrenceListItemSvcDto_2.getOccurrenceDate(), status.getTimestamp());
		});

		final ArgumentCaptor<GetCase> getCaseArgumentCaptor = ArgumentCaptor.forClass(GetCase.class);
		verify(minutMiljoClientMock, times(2)).getCase(getCaseArgumentCaptor.capture());
	}

	@Test
	void testAddDocumentsToCase() {
		final String caseId = UUID.randomUUID().toString();
		final AttachmentDTO attachmentDTO = TestUtil.createAttachmentDTO(AttachmentCategory.ANMALAN_LIVSMEDELSANLAGGNING);

		ecosService.addDocumentsToCase(caseId, List.of(attachmentDTO));

		final ArgumentCaptor<AddDocumentsToCase> addDocumentsToCaseArgumentCaptor = ArgumentCaptor.forClass(AddDocumentsToCase.class);
		verify(minutMiljoClientMock, times(1)).addDocumentsToCase(addDocumentsToCaseArgumentCaptor.capture());
		final AddDocumentsToCaseSvcDto addDocumentsToCaseSvcDto = addDocumentsToCaseArgumentCaptor.getValue().getAddDocumentToCaseSvcDto();
		assertEquals(caseId, addDocumentsToCaseSvcDto.getCaseId());
		assertEquals(Constants.ECOS_OCCURRENCE_TYPE_ID_KOMPLETTERING, addDocumentsToCaseSvcDto.getOccurrenceTypeId());
		assertEquals(Constants.ECOS_DOCUMENT_STATUS_INKOMMEN, addDocumentsToCaseSvcDto.getDocumentStatusId());
		final DocumentSvcDto documentSvcDto = addDocumentsToCaseSvcDto.getDocuments().getDocumentSvcDto().get(0);
		assertEquals(attachmentDTO.getName() + attachmentDTO.getExtension().toLowerCase(), documentSvcDto.getFilename());
		assertEquals(attachmentDTO.getMimeType().toLowerCase(), documentSvcDto.getContentType());
		assertNotNull(documentSvcDto.getData());
		assertEquals(attachmentDTO.getCategory().getDescription(), documentSvcDto.getDocumentTypeId());
		assertEquals(attachmentDTO.getNote(), documentSvcDto.getNote());
	}
}
