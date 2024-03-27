package se.sundsvall.casemanagement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.Mockito;

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
import se.sundsvall.casemanagement.api.model.CoordinatesDTO;
import se.sundsvall.casemanagement.api.model.EnvironmentalCaseDTO;
import se.sundsvall.casemanagement.api.model.EnvironmentalFacilityDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.PlanningPermissionCaseDTO;
import se.sundsvall.casemanagement.api.model.PlanningPermissionFacilityDTO;
import se.sundsvall.casemanagement.api.model.StakeholderDTO;
import se.sundsvall.casemanagement.api.model.enums.AddressCategory;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.FacilityType;
import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.api.model.enums.StakeholderType;
import se.sundsvall.casemanagement.integration.byggr.ArendeExportClient;
import se.sundsvall.casemanagement.integration.db.model.CaseTypeData;
import se.sundsvall.casemanagement.integration.ecos.MinutMiljoClient;
import se.sundsvall.casemanagement.integration.ecos.MinutMiljoClientV2;
import se.sundsvall.casemanagement.integration.ecos.PartyService;
import se.sundsvall.casemanagement.integration.fb.FbClient;
import se.sundsvall.casemanagement.integration.fb.model.DataItem;
import se.sundsvall.casemanagement.integration.fb.model.FbPropertyInfo;
import se.sundsvall.casemanagement.integration.fb.model.GruppItem;
import se.sundsvall.casemanagement.integration.fb.model.ResponseDto;
import se.sundsvall.casemanagement.service.CitizenService;
import se.sundsvall.casemanagement.service.FbService;
import se.sundsvall.casemanagement.util.Constants;

public final class TestUtil {

	public static final Integer FNR = 22045604;

	public static final Integer ADRESSPLATS_ID = 90022392;

	public static void mockFbPropertyOwners(final FbClient fbClientMock, final List<StakeholderDTO> propertyOwners) {
		final ResponseDto lagfarenAgareResponse = new ResponseDto();
		final DataItem lagfarenAgareDataItem = new DataItem();
		final List<GruppItem> lagfarenAgareGruppItemList = new ArrayList<>();

		final ResponseDto agareInfoResponse = new ResponseDto();
		final List<DataItem> agareInfoDataItemList = new ArrayList<>();

		propertyOwners.forEach(propertyOwner -> {

			final GruppItem gruppItem = new GruppItem();
			final DataItem dataItem = new DataItem();

			if (propertyOwner instanceof final PersonDTO personDTO) {

				gruppItem.setIdentitetsnummer(personDTO.getPersonalNumber());
				gruppItem.setUuid(UUID.randomUUID().toString());

				dataItem.setGallandeFornamn(personDTO.getFirstName());
				dataItem.setGallandeEfternamn(personDTO.getLastName());
				dataItem.setIdentitetsnummer(personDTO.getPersonalNumber());
				dataItem.setJuridiskForm(Constants.FB_JURIDISK_FORM_PRIVATPERSON);

			} else if (propertyOwner instanceof final OrganizationDTO organizationDTO) {

				gruppItem.setIdentitetsnummer(organizationDTO.getOrganizationNumber());
				gruppItem.setUuid(UUID.randomUUID().toString());

				dataItem.setGallandeOrganisationsnamn(organizationDTO.getOrganizationName());
				dataItem.setIdentitetsnummer(organizationDTO.getOrganizationNumber());
				dataItem.setJuridiskForm("16");
			}

			lagfarenAgareGruppItemList.add(gruppItem);
			agareInfoDataItemList.add(dataItem);
		});

		lagfarenAgareDataItem.setGrupp(lagfarenAgareGruppItemList);
		lagfarenAgareResponse.setData(List.of(lagfarenAgareDataItem));
		Mockito.doReturn(lagfarenAgareResponse).when(fbClientMock).getPropertyOwnerByFnr(any(), any(), any(), any());

		agareInfoResponse.setData(agareInfoDataItemList);
		Mockito.doReturn(agareInfoResponse).when(fbClientMock).getPropertyOwnerInfoByUuid(any(), any(), any(), any());
	}

	public static EnvironmentalCaseDTO createEnvironmentalCase(final CaseType caseType, final AttachmentCategory attachmentCategory) {
		final EnvironmentalCaseDTO eCase = new EnvironmentalCaseDTO();
		final List<AttachmentDTO> aList = new ArrayList<>();
		aList.add(createAttachmentDTO(attachmentCategory));
		eCase.setAttachments(aList);
		eCase.setStartDate(LocalDate.now());

		final List<StakeholderDTO> sList = new ArrayList<>();
		sList.add(createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT.toString(), StakeholderRole.OPERATOR.toString())));
		sList.add(createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.CONTACT_PERSON.toString())));
		sList.add(createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.CONTACT_PERSON.toString())));

		eCase.setStakeholders(sList);

		eCase.setFacilities(List.of(createEnvironmentalFacilityDTO(caseType)));

		eCase.setCaseType(caseType.toString());
		eCase.setCaseTitleAddition("Some case title addition");
		eCase.setDescription(RandomStringUtils.random(10, true, false));
		eCase.setStartDate(LocalDate.now().plusDays(10));
		eCase.setEndDate(LocalDate.now().plusDays(365));
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

	public static EnvironmentalFacilityDTO createEnvironmentalFacilityDTO(final CaseType caseType) {
		final EnvironmentalFacilityDTO facility = new EnvironmentalFacilityDTO();
		facility.setFacilityCollectionName(RandomStringUtils.random(10, true, false));
		final AddressDTO addressDTO = new AddressDTO();
		addressDTO.setAddressCategories(List.of(AddressCategory.VISITING_ADDRESS));
		addressDTO.setPropertyDesignation("SUNDSVALL BALDER 2");
		facility.setAddress(addressDTO);
		facility.setDescription(RandomStringUtils.random(10, true, false));
		facility.setExtraParameters(switch (caseType) {
			case ANSOKAN_TILLSTAND_VARMEPUMP, ANMALAN_INSTALLATION_VARMEPUMP -> getHeatPumpExtraParams();
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

		final List<StakeholderDTO> sList = new ArrayList<>();
		sList.add(createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT.toString(), StakeholderRole.PAYMENT_PERSON.toString())));
		sList.add(createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.CONTACT_PERSON.toString())));
		sList.add(createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.CONTACT_PERSON.toString())));

		pCase.setStakeholders(sList);

		pCase.setFacilities(List.of(createPlanningPermissionFacilityDTO(false)));

		pCase.setCaseType(caseType.toString());
		pCase.setCaseTitleAddition(RandomStringUtils.random(10, true, false));
		pCase.setDescription(RandomStringUtils.random(10, true, false));
		pCase.setExternalCaseId(String.valueOf(new Random().nextLong()));
		pCase.setDiaryNumber(RandomStringUtils.random(5));
		pCase.setExtraParameters(createExtraParameters());

		return pCase;
	}

	public static PlanningPermissionFacilityDTO createPlanningPermissionFacilityDTO(final boolean mainFacility) {
		final PlanningPermissionFacilityDTO facility = new PlanningPermissionFacilityDTO();
		final AddressDTO addressDTO = new AddressDTO();
		addressDTO.setAddressCategories(List.of(AddressCategory.VISITING_ADDRESS));
		addressDTO.setPropertyDesignation("SUNDSVALL BALDER 2");
		facility.setAddress(addressDTO);
		facility.setFacilityType(FacilityType.ONE_FAMILY_HOUSE.toString());
		facility.setDescription(RandomStringUtils.random(10, true, false));
		facility.setExtraParameters(createExtraParameters());
		facility.setMainFacility(mainFacility);
		return facility;
	}

	public static StakeholderDTO createStakeholder(final StakeholderType stakeholderType, final List<String> stakeholderRoles) {
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
		final CoordinatesDTO coordinatesDTO = new CoordinatesDTO();
		coordinatesDTO.setLatitude(new Random().nextDouble());
		coordinatesDTO.setLongitude(new Random().nextDouble());
		addressDTO.setLocation(coordinatesDTO);
		addressDTO.setExtraParameters(createExtraParameters());

		return addressDTO;
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
			.findAndAddPartyToCase(any(EnvironmentalCaseDTO.class), any(String.class));

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
		final AttachmentDTO attachmentDTO = new AttachmentDTO();
		attachmentDTO.setCategory(attachmentCategory.toString());
		attachmentDTO.setName("Some attachment name.pdf");
		attachmentDTO.setNote("Some attachment note");
		attachmentDTO.setExtension(".pdf");
		attachmentDTO.setMimeType("application/pdf");
		attachmentDTO.setFile("dGVzdA==");
		attachmentDTO.setExtraParameters(createExtraParameters());

		return attachmentDTO;
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
