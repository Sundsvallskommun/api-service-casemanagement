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

import arendeexport.AbstractArendeObjekt;
import arendeexport.Arende;
import arendeexport.ArendeFastighet;
import arendeexport.ArendeIntressent;
import arendeexport.ArrayOfAbstractArendeObjekt2;
import arendeexport.ArrayOfArendeIntressent2;
import arendeexport.ArrayOfHandelse;
import arendeexport.ArrayOfHandelseHandling;
import arendeexport.ArrayOfHandelseIntressent2;
import arendeexport.ArrayOfHandling;
import arendeexport.ArrayOfIntressentKommunikation;
import arendeexport.Fastighet;
import arendeexport.Handelse;
import arendeexport.HandelseHandling;
import arendeexport.HandelseIntressent;
import arendeexport.HandlaggareBas;
import arendeexport.IntressentAttention;
import arendeexport.IntressentKommunikation;
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

public final class TestUtil {

	public static final Integer FNR = 22045604;

	public static final Integer ADRESSPLATS_ID = 90022392;

	public static EcosCaseDTO createEcosCaseDTO(final CaseType caseType, final AttachmentCategory attachmentCategory) {
		final List<StakeholderDTO> stakeholderDTOs = new ArrayList<>();
		stakeholderDTOs.add(createStakeholderDTO(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT.toString(), StakeholderRole.OPERATOR.toString())));
		stakeholderDTOs.add(createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.CONTACT_PERSON.toString())));
		stakeholderDTOs.add(createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.CONTACT_PERSON.toString())));

		return EcosCaseDTO.builder()
			.withFacilities(List.of(createFacilityDTO(caseType)))
			.withAttachments(List.of(createAttachmentDTO(attachmentCategory)))
			.withStakeholders(stakeholderDTOs)
			.withCaseType(caseType.toString())
			.withStartDate(LocalDate.now().plusDays(10))
			.withEndDate(LocalDate.now().plusDays(365))
			.withCaseTitleAddition(RandomStringUtils.random(10, true, false))
			.withDescription(RandomStringUtils.random(10, true, false))
			.withExternalCaseId(String.valueOf(new Random().nextInt()))
			.withExtraParameters(createExtraParameters())
			.build();

	}

	public static AttachmentDTO createAttachmentDTO(final AttachmentCategory attachmentCategory) {
		return AttachmentDTO.builder()
			.withCategory(attachmentCategory.toString())
			.withExtension(".pdf")
			.withMimeType("application/pdf")
			.withName(RandomStringUtils.random(10, true, false))
			.withNote(RandomStringUtils.random(10, true, false))
			.withFile("dGVzdA==")
			.build();
	}

	public static FacilityDTO createFacilityDTO(final CaseType caseType) {
		final var facility = FacilityDTO.builder()
			.withFacilityType(FacilityType.ONE_FAMILY_HOUSE.toString())
			.withDescription(RandomStringUtils.random(10, true, false))
			.withFacilityCollectionName(RandomStringUtils.random(10, true, false))
			.withMainFacility(true)
			.withAddress(createAddressDTO(List.of(AddressCategory.VISITING_ADDRESS)));

		final var extraParameters = switch (caseType) {
			case ANSOKAN_TILLSTAND_VARMEPUMP, ANMALAN_INSTALLATION_VARMEPUMP ->
				getHeatPumpExtraParams();
			default -> createExtraParameters();
		};

		return facility.withExtraParameters(extraParameters).build();
	}

	public static ByggRCaseDTO createByggRCaseDTO(final CaseType caseType, final AttachmentCategory attachmentCategory) {
		final List<StakeholderDTO> stakeholderDTOs = new ArrayList<>();
		stakeholderDTOs.add(createStakeholderDTO(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT.toString(), StakeholderRole.PAYMENT_PERSON.toString())));
		stakeholderDTOs.add(createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.CONTACT_PERSON.toString())));
		stakeholderDTOs.add(createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.CONTACT_PERSON.toString())));

		return ByggRCaseDTO.builder()
			.withDiaryNumber(RandomStringUtils.random(5))
			.withFacilities(List.of(createFacilityDTO(true)))
			.withStakeholders(stakeholderDTOs)
			.withCaseType(caseType.toString())
			.withCaseTitleAddition(RandomStringUtils.random(10, true, false))
			.withDescription(RandomStringUtils.random(10, true, false))
			.withExternalCaseId(String.valueOf(new Random().nextLong()))
			.withExtraParameters(createExtraParameters())
			.withAttachments(List.of(createAttachmentDTO(attachmentCategory)))
			.build();
	}

	public static FacilityDTO createFacilityDTO(final boolean mainFacility) {
		return FacilityDTO.builder()
			.withAddress(createAddressDTO(List.of(AddressCategory.VISITING_ADDRESS)))
			.withFacilityType(FacilityType.ONE_FAMILY_HOUSE.toString())
			.withDescription(RandomStringUtils.random(10, true, false))
			.withExtraParameters(createExtraParameters())
			.withMainFacility(mainFacility)
			.build();
	}


	public static StakeholderDTO createStakeholderDTO(final StakeholderType stakeholderType, final List<String> stakeholderRoles) {
		if (stakeholderType.equals(StakeholderType.PERSON)) {
			return PersonDTO.builder()
				.withType(StakeholderType.PERSON)
				.withPersonId(UUID.randomUUID().toString())
				.withPersonalNumber(generateRandomPersonalNumber())
				.withFirstName(RandomStringUtils.random(10, true, false))
				.withLastName(RandomStringUtils.random(10, true, false))
				.withRoles(stakeholderRoles)
				.withEmailAddress(MessageFormat.format("{0}@{1}.com", RandomStringUtils.random(10, true, false), RandomStringUtils.random(5, true, false)))
				.withCellphoneNumber(RandomStringUtils.random(10, true, false))
				.withPhoneNumber(RandomStringUtils.random(10, true, false))
				.withAddresses(List.of(createAddressDTO(List.of(AddressCategory.VISITING_ADDRESS, AddressCategory.POSTAL_ADDRESS))))
				.withExtraParameters(createExtraParameters())
				.build();
		} else {
			return OrganizationDTO.builder()
				.withType(StakeholderType.ORGANIZATION)
				.withOrganizationNumber(generateRandomOrganizationNumber())
				.withOrganizationName(RandomStringUtils.random(10, true, false))
				.withRoles(stakeholderRoles)
				.withEmailAddress(MessageFormat.format("{0}@{1}.com", RandomStringUtils.random(10, true, false), RandomStringUtils.random(5, true, false)))
				.withCellphoneNumber(RandomStringUtils.random(10, true, false))
				.withPhoneNumber(RandomStringUtils.random(10, true, false))
				.withAddresses(List.of(createAddressDTO(List.of(AddressCategory.VISITING_ADDRESS, AddressCategory.INVOICE_ADDRESS, AddressCategory.POSTAL_ADDRESS))))
				.withAuthorizedSignatory(RandomStringUtils.random(10, true, false))
				.withExtraParameters(createExtraParameters())
				.build();
		}
	}

	public static OtherCaseDTO createOtherCaseDTO() {
		return OtherCaseDTO.builder()
			.withCaseType(CaseType.PARKING_PERMIT.toString())
			.withCaseTitleAddition("caseTitleAddition")
			.withDescription("description")
			.withExternalCaseId("externalCaseId")
			.withExtraParameters(createExtraParameters())
			.withFacilities(List.of(createFacilityDTO(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP)))
			.withAttachments(List.of(createAttachmentDTO(AttachmentCategory.ANMALAN_VARMEPUMP)))
			.withStakeholders(List.of(createStakeholderDTO(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT.toString()))))
			.build();
	}

	public static AddressDTO createAddressDTO(final List<AddressCategory> addressCategories) {
		return AddressDTO.builder()
			.withAddressCategories(addressCategories)
			.withCity(RandomStringUtils.random(10, true, false))
			.withCountry(Constants.SWEDEN)
			.withPropertyDesignation("SUNDSVALL FILLA 8:185")
			.withStreet(RandomStringUtils.random(10, true, false))
			.withHouseNumber(RandomStringUtils.random(10, true, true))
			.withCareOf(RandomStringUtils.random(10, true, false))
			.withPostalCode(RandomStringUtils.random(10, false, true))
			.withAppartmentNumber(RandomStringUtils.random(10, true, true))
			.withAttention(RandomStringUtils.random(10, true, false))
			.withInvoiceMarking(RandomStringUtils.random(10, true, true))
			.withIsZoningPlanArea(false)
			.withLocation(createCoordinatesDTO())
			.withExtraParameters(createExtraParameters())
			.build();
	}

	public static CoordinatesDTO createCoordinatesDTO() {
		return CoordinatesDTO.builder()
			.withLatitude(new Random().nextDouble(100.0))
			.withLongitude(new Random().nextDouble(100.0))
			.build();
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

	public static Arende createArende() {
		var arende = new Arende();
		arende.setArendeId(229982);
		arende.setDnr("BYGG-DNR-0000");
		arende.withDiarieprefix("BYGG");
		arende.setKommun("Sundsvall");
		arende.setEnhet("SBK");
		arende.setArendegrupp("LOV");
		arende.setArendetyp("BL");
		arende.setArendeslag("P");
		arende.setArendeklass("FRI");
		arende.setNamndkod("SBN");
		arende.setKalla("3");
		arende.setStatus("Pågående");
		arende.setBeskrivning("Bygglov för tillbyggnad av fritidshus");
		arende.setAnkomstDatum(LocalDate.now());
		arende.setUppdateradDatum(LocalDate.now());
		arende.setRegistreradDatum(LocalDate.now());
		arende.setHandlaggare(new HandlaggareBas().withEfternamn("Testersson").withFornamn("Test"));
		arende.setIntressentLista(createArrayOfArendeIntressent2());
		arende.setHandelseLista(createArrayOfHandelse());
		arende.setArInomplan(false);
		arende.setProjektnr("123456");
		arende.setObjektLista(createArrayOfAbstractArendeObjekt2());

		return arende;
	}

	public static ArrayOfAbstractArendeObjekt2 createArrayOfAbstractArendeObjekt2() {
		var arrayOfAbstractArendeObjekt2 = new ArrayOfAbstractArendeObjekt2();
		arrayOfAbstractArendeObjekt2.getAbstractArendeObjekt().add(createArendeFastighet());
		return arrayOfAbstractArendeObjekt2;
	}

	public static AbstractArendeObjekt createArendeFastighet() {
		var arendeFastighet = new ArendeFastighet();
		arendeFastighet.setFastighet(createFastighet());
		return arendeFastighet;
	}

	public static Fastighet createFastighet() {
		var fastighet = new Fastighet();
		fastighet.setFnr(22045604);
		fastighet.setTrakt("Sundsvall");
		fastighet.setFbetNr("Sundsvall 1:1");
		return fastighet;
	}

	public static ArrayOfHandelse createArrayOfHandelse() {
		var arrayOfHandelse = new ArrayOfHandelse();
		arrayOfHandelse.getHandelse().add(createHandelse());
		return arrayOfHandelse;
	}

	public static Handelse createHandelse() {
		var handelse = new Handelse();
		handelse.setRiktning("IN");
		handelse.setRubrik("Bygglov");
		handelse.setHandelseId(123456);
		handelse.setStartDatum(LocalDateTime.now());
		handelse.setHandelseslag("GRAUTS");
		handelse.setHandelsetyp("GRANHO");
		handelse.setSekretess(false);
		handelse.setMakulerad(false);
		handelse.setIntressentLista(createArrayOfHandelseIntressent2());
		handelse.setHandlingLista(createArrayOfHandelseHandling());
		return handelse;
	}

	public static ArrayOfHandelseIntressent2 createArrayOfHandelseIntressent2() {
		var arrayOfHandelseIntressent2 = new ArrayOfHandelseIntressent2();
		arrayOfHandelseIntressent2.getIntressent().add(createHandelseIntressent());
		arrayOfHandelseIntressent2.getIntressent().add(createHandelseIntressent());
		return arrayOfHandelseIntressent2;
	}

	public static ArrayOfHandelseHandling createArrayOfHandelseHandling() {
		var arrayOfHandelseHandling = new ArrayOfHandelseHandling();
		arrayOfHandelseHandling.getHandling().add(createHandling());
		arrayOfHandelseHandling.getHandling().add(createHandling());
		return arrayOfHandelseHandling;
	}

	public static ArrayOfHandling createArrayOfHandling() {
		var arrayOfHandling = new ArrayOfHandling();
		arrayOfHandling.getHandling().add(createHandling());
		arrayOfHandling.getHandling().add(createHandling());
		return arrayOfHandling;
	}

	public static HandelseHandling createHandling() {
		var handelseHandling = new HandelseHandling();
		handelseHandling.setHandlingId(123456);
		handelseHandling.setTyp("ANS");
		handelseHandling.setStatus("Inkommen");
		handelseHandling.setHandlingDatum(LocalDate.now());
		handelseHandling.setAnteckning("Inkommen handling");
		return handelseHandling;
	}

	public static ArrayOfArendeIntressent2 createArrayOfArendeIntressent2() {
		var arrayOfArendeIntressent2 = new ArrayOfArendeIntressent2();
		arrayOfArendeIntressent2.getIntressent().add(createArrendeIntressent());
		arrayOfArendeIntressent2.getIntressent().add(createArrendeIntressent());
		return arrayOfArendeIntressent2;
	}

	public static ArendeIntressent createArrendeIntressent() {
		var arendeIntressent = new ArendeIntressent();
		arendeIntressent.setNamn("Test Testsson");
		arendeIntressent.setAdress("Testgatan 1");
		arendeIntressent.setPostNr("12345");
		arendeIntressent.setOrt("Sundsvall");
		arendeIntressent.setFornamn("Test");
		arendeIntressent.setEfternamn("Testsson");
		arendeIntressent.setPersOrgNr("20000101-1234");
		return arendeIntressent;
	}

	public static HandelseIntressent createHandelseIntressent() {
		var handelseIntressent = new HandelseIntressent();
		handelseIntressent.setAdress("Testgatan 1");
		handelseIntressent.setIntressentId(123456);
		handelseIntressent.setFornamn("Test");
		handelseIntressent.setEfternamn("Testsson");
		handelseIntressent.setNamn("Test Testsson");
		handelseIntressent.setPersOrgNr("20000101-1234");
		handelseIntressent.setIntressentKommunikationLista(new ArrayOfIntressentKommunikation()
			.withIntressentKommunikation(new IntressentKommunikation()
				.withBeskrivning("Testkommunikation")
				.withAttention(new IntressentAttention()
					.withAttention("attention")
					.withAttentionId(12345))
				.withKomtyp("Epost")));
		return handelseIntressent;
	}
}
