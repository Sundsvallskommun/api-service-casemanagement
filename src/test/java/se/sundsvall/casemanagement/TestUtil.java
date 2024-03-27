package se.sundsvall.casemanagement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.commons.lang3.RandomStringUtils;

import arendeexport.SaveNewArendeResponse;
import arendeexport.SaveNewArendeResponse2;
import minutmiljo.ArrayOfguid;
import minutmiljo.CreateFoodFacilityResponse;
import minutmiljo.CreateHealthProtectionFacilityResponse;
import minutmiljo.CreateHeatPumpFacilityResponse;
import minutmiljo.CreateIndividualSewageFacilityResponse;
import minutmiljo.CreateOrganizationPartyResponse;
import minutmiljo.CreatePersonPartyResponse;
import minutmiljo.SearchPartyResponse;
import minutmiljoV2.RegisterDocumentCaseResultSvcDto;
import minutmiljoV2.RegisterDocumentResponse;
import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;
import se.sundsvall.casemanagement.api.model.CaseStatusDTO;
import se.sundsvall.casemanagement.api.model.CoordinatesDTO;
import se.sundsvall.casemanagement.api.model.EcosCaseDTO;
import se.sundsvall.casemanagement.api.model.FacilityDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.StakeholderDTO;
import se.sundsvall.casemanagement.api.model.enums.AddressCategory;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.FacilityType;
import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.api.model.enums.StakeholderType;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.byggr.ArendeExportClient;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.integration.db.model.CaseTypeData;
import se.sundsvall.casemanagement.integration.ecos.MinutMiljoClient;
import se.sundsvall.casemanagement.integration.ecos.MinutMiljoClientV2;
import se.sundsvall.casemanagement.integration.ecos.PartyService;
import se.sundsvall.casemanagement.integration.fb.model.FbPropertyInfo;
import se.sundsvall.casemanagement.service.CitizenService;
import se.sundsvall.casemanagement.service.FbService;
import se.sundsvall.casemanagement.util.Constants;

public final class TestUtil {

	public static final Integer FNR = 22045604;

	public static final Integer ADRESSPLATS_ID = 90022392;

	public static EcosCaseDTO createEnvironmentalCase(final CaseType caseType, final AttachmentCategory attachmentCategory) {
		final EcosCaseDTO eCase = new EcosCaseDTO();

		final List<StakeholderDTO> stakeholderDTOs = new ArrayList<>();
		stakeholderDTOs.add(createStakeholderDTO(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT.toString(), StakeholderRole.OPERATOR.toString())));
		stakeholderDTOs.add(createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.CONTACT_PERSON.toString())));
		stakeholderDTOs.add(createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.CONTACT_PERSON.toString())));

		eCase.setAttachments(List.of(createAttachmentDTO(attachmentCategory)));
		eCase.setStartDate(LocalDate.now());
		eCase.setStakeholders(stakeholderDTOs);
		eCase.setFacilities(List.of(createFacilityDTO(caseType)));
		eCase.setCaseType(caseType.toString());
		eCase.setStartDate(LocalDate.now().plusDays(10));
		eCase.setEndDate(LocalDate.now().plusDays(365));
		eCase.setCaseTitleAddition(RandomStringUtils.random(10, true, false));
		eCase.setDescription(RandomStringUtils.random(10, true, false));
		eCase.setExternalCaseId(String.valueOf(new Random().nextInt()));
		eCase.setExtraParameters(createExtraParameters());

		return eCase;
	}

	public static AttachmentDTO createAttachmentDTO(final AttachmentCategory attachmentCategory) {
		final AttachmentDTO attachmentDTO = new AttachmentDTO();
		attachmentDTO.setCategory(attachmentCategory.toString());
		attachmentDTO.setExtension(".pdf");
		attachmentDTO.setMimeType("application/pdf");
		attachmentDTO.setName(RandomStringUtils.random(10, true, false));
		attachmentDTO.setNote(RandomStringUtils.random(10, true, false));
		attachmentDTO.setFile("dGVzdA==");

		return attachmentDTO;
	}

	public static FacilityDTO createFacilityDTO(final CaseType caseType) {
		final var facility = new FacilityDTO();
		facility.setFacilityCollectionName(RandomStringUtils.random(10, true, false));
		facility.setDescription(RandomStringUtils.random(10, true, false));
		facility.setMainFacility(true);
		facility.setAddress(createAddressDTO(List.of(AddressCategory.VISITING_ADDRESS)));
		facility.setExtraParameters(
			switch (caseType) {
				case ANSOKAN_TILLSTAND_VARMEPUMP, ANMALAN_INSTALLATION_VARMEPUMP ->
					getHeatPumpExtraParams();
				default -> createExtraParameters();
			});
		return facility;
	}

	public static OtherCaseDTO createOtherCase(final CaseType caseType) {
		final var otherCase = new OtherCaseDTO();
		otherCase.setCaseType(caseType.toString());
		otherCase.setExternalCaseId(UUID.randomUUID().toString());
		otherCase.setCaseTitleAddition("Some case title addition");
		otherCase.setDescription("Some random description");

		otherCase.setStakeholders(List.of(
			TestUtil.createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT.toString(), StakeholderRole.CONTACT_PERSON.toString())),
			TestUtil.createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.PAYMENT_PERSON.toString(), StakeholderRole.INVOICE_RECIPIENT.toString()))));

		otherCase.setAttachments(List.of(
			TestUtil.createAttachment(AttachmentCategory.BUILDING_PERMIT_APPLICATION),
			TestUtil.createAttachment(AttachmentCategory.ANMALAN_VARMEPUMP),
			TestUtil.createAttachment(AttachmentCategory.ANMALAN_VARMEPUMP)));

		otherCase.setExtraParameters(TestUtil.createExtraParameters());
		otherCase.getExtraParameters().put("application.priority", "HIGH");
		return otherCase;
	}

	public static PlanningPermissionCaseDTO createPlanningPermissionCase(final CaseType caseType, final AttachmentCategory attachmentCategory) {
		final PlanningPermissionCaseDTO pCase = new PlanningPermissionCaseDTO();
		final List<AttachmentDTO> aList = new ArrayList<>();
		aList.add(createAttachmentDTO(attachmentCategory));
		pCase.setAttachments(aList);

		final List<StakeholderDTO> stakeholderDTOs = new ArrayList<>();
		stakeholderDTOs.add(createStakeholderDTO(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT.toString(), StakeholderRole.PAYMENT_PERSON.toString())));
		stakeholderDTOs.add(createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.CONTACT_PERSON.toString())));
		stakeholderDTOs.add(createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.CONTACT_PERSON.toString())));

		pCase.setFacilities(List.of(createFacilityDTO(false)));
		pCase.setStakeholders(stakeholderDTOs);

		pCase.setCaseType(caseType.toString());
		pCase.setCaseTitleAddition(RandomStringUtils.random(10, true, false));
		pCase.setDescription(RandomStringUtils.random(10, true, false));
		pCase.setExternalCaseId(String.valueOf(new Random().nextLong()));
		pCase.setDiaryNumber(RandomStringUtils.random(5));
		pCase.setExtraParameters(createExtraParameters());

		return pCase;
	}

	public static FacilityDTO createFacilityDTO(final boolean mainFacility) {
		final var facility = new FacilityDTO();
		facility.setAddress(createAddressDTO(List.of(AddressCategory.VISITING_ADDRESS)));
		facility.setFacilityType(FacilityType.ONE_FAMILY_HOUSE.toString());
		facility.setDescription(RandomStringUtils.random(10, true, false));
		facility.setExtraParameters(createExtraParameters());
		facility.setMainFacility(mainFacility);
		return facility;
	}


	public static StakeholderDTO createStakeholderDTO(final StakeholderType stakeholderType, final List<String> stakeholderRoles) {
		if (stakeholderType.equals(StakeholderType.PERSON)) {
			final PersonDTO personDTO = new PersonDTO();
			personDTO.setType(StakeholderType.PERSON);
			personDTO.setPersonId(UUID.randomUUID().toString());
			personDTO.setPersonalNumber(generateRandomPersonalNumber());
			personDTO.setFirstName(RandomStringUtils.random(10, true, false));
			personDTO.setLastName(RandomStringUtils.random(10, true, false));
			personDTO.setRoles(stakeholderRoles);
			personDTO.setEmailAddress(MessageFormat.format("{0}@{1}.com", RandomStringUtils.random(10, true, false), RandomStringUtils.random(5, true, false)));
			personDTO.setCellphoneNumber(RandomStringUtils.random(10, true, false));
			personDTO.setPhoneNumber(RandomStringUtils.random(10, true, false));
			personDTO.setAddresses(List.of(createAddressDTO(List.of(AddressCategory.VISITING_ADDRESS, AddressCategory.POSTAL_ADDRESS))));
			personDTO.setExtraParameters(createExtraParameters());
			return personDTO;
		} else {
			final OrganizationDTO organizationDTO = new OrganizationDTO();
			organizationDTO.setType(StakeholderType.ORGANIZATION);
			organizationDTO.setOrganizationNumber(generateRandomOrganizationNumber());
			organizationDTO.setOrganizationName(RandomStringUtils.random(10, true, false));
			organizationDTO.setRoles(stakeholderRoles);
			organizationDTO.setEmailAddress(MessageFormat.format("{0}@{1}.com", RandomStringUtils.random(10, true, false), RandomStringUtils.random(5, true, false)));
			organizationDTO.setCellphoneNumber(RandomStringUtils.random(10, true, false));
			organizationDTO.setPhoneNumber(RandomStringUtils.random(10, true, false));
			organizationDTO.setAddresses(List.of(createAddressDTO(List.of(AddressCategory.VISITING_ADDRESS, AddressCategory.INVOICE_ADDRESS, AddressCategory.POSTAL_ADDRESS))));
			organizationDTO.setAuthorizedSignatory(RandomStringUtils.random(10, true, false));
			organizationDTO.setExtraParameters(createExtraParameters());

			return organizationDTO;
		}
	}

	public static OtherCaseDTO createOtherCaseDTO() {
		final var otherCase = new OtherCaseDTO();
		otherCase.setCaseType(CaseType.PARKING_PERMIT.toString());
		otherCase.setCaseTitleAddition("caseTitleAddition");
		otherCase.setDescription("description");
		otherCase.setExternalCaseId("externalCaseId");
		otherCase.setExtraParameters(createExtraParameters());
		otherCase.setFacilities(List.of(createFacilityDTO(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP)));
		otherCase.setAttachments(List.of(createAttachmentDTO(AttachmentCategory.ANMALAN_VARMEPUMP)));
		otherCase.setStakeholders(List.of(createStakeholderDTO(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT.toString()))));

		return otherCase;
	}

	public static AddressDTO createAddressDTO(final List<AddressCategory> addressCategories) {
		final AddressDTO addressDTO = new AddressDTO();
		addressDTO.setAddressCategories(addressCategories);
		addressDTO.setCity(RandomStringUtils.random(10, true, false));
		addressDTO.setCountry(Constants.SWEDEN);
		addressDTO.setPropertyDesignation("SUNDSVALL FILLA 8:185");
		addressDTO.setStreet(RandomStringUtils.random(10, true, false));
		addressDTO.setHouseNumber(RandomStringUtils.random(10, true, true));
		addressDTO.setCareOf(RandomStringUtils.random(10, true, false));
		addressDTO.setPostalCode(RandomStringUtils.random(10, false, true));
		addressDTO.setAppartmentNumber(RandomStringUtils.random(10, true, true));
		addressDTO.setAttention(RandomStringUtils.random(10, true, false));
		addressDTO.setInvoiceMarking(RandomStringUtils.random(10, true, true));
		addressDTO.setIsZoningPlanArea(false);
		addressDTO.setLocation(createCoordinatesDTO());
		addressDTO.setExtraParameters(createExtraParameters());

		return addressDTO;
	}

	public static CoordinatesDTO createCoordinatesDTO() {
		final CoordinatesDTO coordinatesDTO = new CoordinatesDTO();
		coordinatesDTO.setLatitude(new Random().nextDouble(100.0));
		coordinatesDTO.setLongitude(new Random().nextDouble(100.0));
		return coordinatesDTO;
	}

	public static String generateRandomOrganizationNumber() {
		return (new Random().nextInt(999999 - 111111) + 111111) + "-" + (new Random().nextInt(9999 - 1111) + 1111);
	}

	public static String generateRandomPersonalNumber() {
		return "199901" + new Random().nextInt(3) + (new Random().nextInt(9) + 1) + (new Random().nextInt(9999 - 1111) + 1111);
	}

	public static void standardMockPartyService(final PartyService mock) {
		lenient()
			.doReturn(List.of(Map.of(
				"id", new ArrayOfguid().withGuid("123"),
				"id2", new ArrayOfguid().withGuid("123as"),
				"id3", new ArrayOfguid().withGuid("1235"))))
			.when(mock)
			.findAndAddPartyToCase(any(EcosCaseDTO.class), any(String.class));

	}


	public static void standardMockMinutMiljo(final MinutMiljoClient mock, final MinutMiljoClientV2 mockV2) {
		lenient().doReturn(new SearchPartyResponse()).when(mock).searchParty(any());

		final CreatePersonPartyResponse createPersonPartyResponse = new CreatePersonPartyResponse();
		createPersonPartyResponse.setCreatePersonPartyResult(UUID.randomUUID().toString());
		lenient().doReturn(createPersonPartyResponse).when(mock).createPersonParty(any());

		final CreateOrganizationPartyResponse createOrganizationPartyResponse = new CreateOrganizationPartyResponse();
		createOrganizationPartyResponse.setCreateOrganizationPartyResult(UUID.randomUUID().toString());
		lenient().doReturn(createOrganizationPartyResponse).when(mock).createOrganizationParty(any());

		final RegisterDocumentResponse registerDocumentResponse = new RegisterDocumentResponse();
		final RegisterDocumentCaseResultSvcDto registerDocumentCaseResultSvcDto = new RegisterDocumentCaseResultSvcDto();
		registerDocumentCaseResultSvcDto.setCaseId("e19981ad-34b2-4e14-88f5-133f61ca85aa");
		registerDocumentCaseResultSvcDto.setCaseNumber("Inskickat");
		registerDocumentResponse.setRegisterDocumentResult(registerDocumentCaseResultSvcDto);
		lenient().doReturn(registerDocumentResponse).when(mockV2).registerDocumentV2(any());

		final CreateFoodFacilityResponse createFoodFacilityResponse = new CreateFoodFacilityResponse();
		createFoodFacilityResponse.setCreateFoodFacilityResult(UUID.randomUUID().toString());
		lenient().doReturn(createFoodFacilityResponse).when(mock).createFoodFacility(any());

		final CreateHeatPumpFacilityResponse createHeatPumpFacilityResponse = new CreateHeatPumpFacilityResponse();
		createHeatPumpFacilityResponse.setCreateHeatPumpFacilityResult(UUID.randomUUID().toString());
		lenient().doReturn(createHeatPumpFacilityResponse).when(mock).createHeatPumpFacility(any());

		final CreateIndividualSewageFacilityResponse createIndividualSewageFacilityResponse = new CreateIndividualSewageFacilityResponse();
		createIndividualSewageFacilityResponse.setCreateIndividualSewageFacilityResult(UUID.randomUUID().toString());
		lenient().doReturn(createIndividualSewageFacilityResponse).when(mock).createIndividualSewageFacility(any());

		final CreateHealthProtectionFacilityResponse createHealthProtectionFacilityResponse = new CreateHealthProtectionFacilityResponse();
		createHealthProtectionFacilityResponse.setCreateHealthProtectionFacilityResult(UUID.randomUUID().toString());
		lenient().doReturn(createHealthProtectionFacilityResponse).when(mock).createHealthProtectionFacility(any());
	}

	public static void standardMockArendeExport(final ArendeExportClient mock) {
		final SaveNewArendeResponse saveNewArendeResponse = new SaveNewArendeResponse();
		final SaveNewArendeResponse2 saveNewArendeResult = new SaveNewArendeResponse2();
		saveNewArendeResult.setDnr("Inskickat");
		saveNewArendeResponse.setSaveNewArendeResult(saveNewArendeResult);

		lenient().doReturn(saveNewArendeResponse).when(mock).saveNewArende(any());
	}

	public static void standardMockFb(final FbService fbMock) {
		final FbPropertyInfo fbPropertyInfo = new FbPropertyInfo();
		fbPropertyInfo.setFnr(FNR);
		fbPropertyInfo.setAdressplatsId(ADRESSPLATS_ID);

		lenient().doReturn(fbPropertyInfo).when(fbMock).getPropertyInfoByPropertyDesignation(anyString());
	}

	public static void standardMockCitizen(final CitizenService mock) {
		lenient().doReturn(generateRandomPersonalNumber()).when(mock).getPersonalNumber(anyString());
	}

	public static void setSewageStandardExtraParams(final Map<String, String> extraParameters, final String prefix) {
		extraParameters.put("OnGrantLand", String.valueOf(new Random().nextBoolean()));
		extraParameters.put("ProtectionLevelApprovedEnvironmentId", UUID.randomUUID().toString());
		extraParameters.put("ProtectionLevelApprovedHealthId", UUID.randomUUID().toString());
		extraParameters.put("WastewaterApprovedForId", UUID.randomUUID().toString());
		extraParameters.put("WasteWaterInboundId", UUID.randomUUID().toString());
		extraParameters.put(prefix + "StepNumber", String.valueOf(new Random().nextInt()));
		extraParameters.put(prefix + "HasOverflowAlarm", String.valueOf(new Random().nextBoolean()));
		extraParameters.put(prefix + "LifeTime", String.valueOf(new Random().nextInt()));
		extraParameters.put(prefix + "InstallationDate", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
		extraParameters.put(prefix + "PersonCapacity", String.valueOf(new Random().nextInt()));
	}

	public static Map<String, String> getHeatPumpExtraParams() {
		final Map<String, String> extraParameters = new HashMap<>();
		final String prefix = "CreateSoilHeatingFacilitySvcDto_";
		TestUtil.setSewageStandardExtraParams(extraParameters, prefix);

		extraParameters.put(prefix + "Manufacturer", RandomStringUtils.random(10, true, false));
		extraParameters.put(prefix + "Model", RandomStringUtils.random(10, true, false));
		extraParameters.put(prefix + "PowerConsumption", String.valueOf(new Random().nextDouble()));
		extraParameters.put(prefix + "PowerOutput", String.valueOf(new Random().nextDouble()));
		extraParameters.put(prefix + "Capacity", String.valueOf(new Random().nextDouble()));
		extraParameters.put(prefix + "HeatTransferFluidId", UUID.randomUUID().toString());

		return extraParameters;
	}

	public static AttachmentDTO createAttachment(final AttachmentCategory attachmentCategory) {
		return AttachmentDTO.builder()
			.withCategory(attachmentCategory.toString())
			.withExtension(".pdf")
			.withMimeType("application/pdf")
			.withName("Some attachment name")
			.withNote("Some attachment note")
			.withFile("dGVzdA==")
			.withExtraParameters(createExtraParameters())
			.build();
	}

	public static CaseMapping createCaseMapping() {
		return createCaseMapping(null);
	}

	public static CaseMapping createCaseMapping(final Consumer<CaseMapping> modifier) {
		final var caseMapping = CaseMapping.builder()
			.withCaseId("caseId")
			.withExternalCaseId("externalCaseId")
			.withCaseType(CaseType.REGISTRERING_AV_LIVSMEDEL.toString())
			.withServiceName("serviceName")
			.withTimestamp(LocalDate.now().atStartOfDay())
			.withSystem(SystemType.ECOS)
			.build();

		if (modifier != null) {
			modifier.accept(caseMapping);
		}

		return caseMapping;
	}

	public static CaseStatusDTO createCaseStatusDTO() {
		return createCaseStatusDTO(null);
	}

	public static CaseStatusDTO createCaseStatusDTO(final Consumer<CaseStatusDTO> modifier) {
		final var caseStatus = CaseStatusDTO.builder()
			.withCaseId("caseId")
			.withStatus("status")
			.withTimestamp(LocalDateTime.now())
			.withCaseType("caseType")
			.withExternalCaseId("externalCaseID")
			.withSystem(SystemType.ECOS)
			.withServiceName("serviceName")
			.build();

		if (modifier != null) {
			modifier.accept(caseStatus);
		}

		return caseStatus;
	}

	public static Map<String, String> createExtraParameters() {
		final Map<String, String> extraParams = new HashMap<>();
		extraParams.put(RandomStringUtils.random(10, true, false), RandomStringUtils.random(20, true, false));
		extraParams.put(RandomStringUtils.random(10, true, false), RandomStringUtils.random(20, true, false));
		extraParams.put(RandomStringUtils.random(10, true, false), RandomStringUtils.random(20, true, false));

		return extraParams;
	}

	public static <E extends Enum<E>> Enum<?> getRandomOfEnum(final Class<E> enumClass) {
		return Arrays.stream(enumClass.getEnumConstants()).toList().get(new Random().nextInt(enumClass.getEnumConstants().length));
	}


	public static List<CaseTypeData> setUpCaseTypes() {
		final var caseTypeDataList = new ArrayList<CaseTypeData>();
		caseTypeDataList.add(CaseTypeData.builder()
			.withValue("ANDRING_ANSOKAN_OM_BYGGLOV")
			.withArendeTyp("BL")
			.withArendeSlag(null)
			.withHandelseTyp("ANSÖKAN")
			.withArendeMening("Bygglov för")
			.withHandelseRubrik("Bygglov")
			.withHandelseSlag("Bygglov")
			.withArendeGrupp("LOV")
			.build());
		caseTypeDataList.add(CaseTypeData.builder()
			.withValue("ANMALAN_ATTEFALL")
			.withArendeTyp("ATTANM")
			.withArendeSlag(null)
			.withHandelseTyp("ANM")
			.withArendeMening(null)
			.withHandelseRubrik("Anmälan Attefall")
			.withHandelseSlag("ANMATT")
			.withArendeGrupp("LOV")
			.build());
		caseTypeDataList.add(CaseTypeData.builder()
			.withValue("ANMALAN_ELDSTAD")
			.withArendeTyp("ANM")
			.withArendeSlag(null)
			.withHandelseTyp("ANM")
			.withArendeMening(null)
			.withHandelseRubrik(null)
			.withHandelseSlag(null)
			.withArendeGrupp("LOV")
			.build());
		caseTypeDataList.add(CaseTypeData.builder()
			.withValue("NYBYGGNAD_ANSOKAN_OM_BYGGLOV")
			.withArendeTyp("BL")
			.withArendeSlag("A")
			.withHandelseTyp("ANSÖKAN")
			.withArendeMening("Bygglov för nybyggnad av")
			.withHandelseRubrik("Bygglov")
			.withHandelseSlag("Bygglov")
			.withArendeGrupp("LOV")
			.build());
		caseTypeDataList.add(CaseTypeData.builder()
			.withValue("NYBYGGNAD_FORHANDSBESKED")
			.withArendeTyp("FÖRF")
			.withArendeSlag("A")
			.withHandelseTyp("ANSÖKAN")
			.withArendeMening("Förhandsbesked för nybyggnad av")
			.withHandelseRubrik("Förhandsbesked")
			.withHandelseSlag("Förhand")
			.withArendeGrupp("LOV")
			.build());
		caseTypeDataList.add(CaseTypeData.builder()
			.withValue("STRANDSKYDD_ANDRAD_ANVANDNING")
			.withArendeTyp("DI")
			.withArendeSlag("ÄNDR")
			.withHandelseTyp("ANSÖKAN")
			.withArendeMening("Strandskyddsdispens för ändrad användning av")
			.withHandelseRubrik("Strandskyddsdispens")
			.withHandelseSlag("Strand")
			.withArendeGrupp("STRA")
			.build());
		caseTypeDataList.add(CaseTypeData.builder()
			.withValue("STRANDSKYDD_ANLAGGANDE")
			.withArendeTyp("DI")
			.withArendeSlag("A1")
			.withHandelseTyp("ANSÖKAN")
			.withArendeMening("Strandskyddsdispens för anläggande av")
			.withHandelseRubrik("Strandskyddsdispens")
			.withHandelseSlag("Strand")
			.withArendeGrupp("STRA")
			.build());
		caseTypeDataList.add(CaseTypeData.builder()
			.withValue("STRANDSKYDD_ANORDNANDE")
			.withArendeTyp("DI")
			.withArendeSlag("AO")
			.withHandelseTyp("ANSÖKAN")
			.withArendeMening("Strandskyddsdispens för anordnare av")
			.withHandelseRubrik("Strandskyddsdispens")
			.withHandelseSlag("Strand")
			.withArendeGrupp("STRA")
			.build());
		caseTypeDataList.add(CaseTypeData.builder()
			.withValue("STRANDSKYDD_NYBYGGNAD")
			.withArendeTyp("DI")
			.withArendeSlag("NYB")
			.withHandelseTyp("ANSÖKAN")
			.withArendeMening("Strandskyddsdispens för nybyggnad av")
			.withHandelseRubrik("Strandskyddsdispens")
			.withHandelseSlag("Strand")
			.withArendeGrupp("STRA")
			.build());
		caseTypeDataList.add(CaseTypeData.builder()
			.withValue("TILLBYGGNAD_ANSOKAN_OM_BYGGLOV")
			.withArendeTyp("BL")
			.withArendeSlag("B")
			.withHandelseTyp("ANSÖKAN")
			.withArendeMening("Bygglov för tillbyggnad av")
			.withHandelseRubrik("Bygglov")
			.withHandelseSlag("Bygglov")
			.withArendeGrupp("LOV")
			.build());
		caseTypeDataList.add(CaseTypeData.builder()
			.withValue("UPPSATTANDE_SKYLT")
			.withArendeTyp("BL")
			.withArendeSlag("L")
			.withHandelseTyp("ANSÖKAN")
			.withArendeMening("Bygglov för uppsättande av ")
			.withHandelseRubrik("Bygglov")
			.withHandelseSlag("Bygglov")
			.withArendeGrupp("LOV")
			.build());
		return caseTypeDataList;
	}

}
