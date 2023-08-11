package se.sundsvall.casemanagement.integration.ecos;

import static java.util.Objects.isNull;
import static se.sundsvall.casemanagement.util.Constants.SERVICE_NAME;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import minutmiljo.AddDocumentsToCase;
import minutmiljo.AddDocumentsToCaseSvcDto;
import minutmiljo.AddPartyToCase;
import minutmiljo.AddPartyToCaseSvcDto;
import minutmiljo.AddPartyToFacility;
import minutmiljo.AddPartyToFacilitySvcDto;
import minutmiljo.AddressTypeSvcDto;
import minutmiljo.ArrayOfAddressTypeSvcDto;
import minutmiljo.ArrayOfBoreholeSvcDto;
import minutmiljo.ArrayOfContactInfoItemSvcDto;
import minutmiljo.ArrayOfContactInfoSvcDto;
import minutmiljo.ArrayOfDocumentSvcDto;
import minutmiljo.ArrayOfFilterSvcDto;
import minutmiljo.ArrayOfHeatCollectorTubeSvcDto;
import minutmiljo.ArrayOfPartyAddressSvcDto;
import minutmiljo.ArrayOfPartySvcDto;
import minutmiljo.ArrayOfPurificationStepSvcDto;
import minutmiljo.ArrayOfSearchCaseResultSvcDto;
import minutmiljo.ArrayOfguid;
import minutmiljo.BiologicalStepSvcDto;
import minutmiljo.BoreholeSvcDto;
import minutmiljo.CaseSvcDto;
import minutmiljo.ChemicalPretreatmentSvcDto;
import minutmiljo.ClosedTankSvcDto;
import minutmiljo.ContactInfoItemSvcDto;
import minutmiljo.ContactInfoSvcDto;
import minutmiljo.CreateAirHeatingFacilitySvcDto;
import minutmiljo.CreateFoodFacility;
import minutmiljo.CreateFoodFacilitySvcDto;
import minutmiljo.CreateGeothermalHeatingFacilitySvcDto;
import minutmiljo.CreateHealthProtectionFacility;
import minutmiljo.CreateHealthProtectionFacilitySvcDto;
import minutmiljo.CreateHeatPumpFacility;
import minutmiljo.CreateHeatPumpFacilitySvcDto;
import minutmiljo.CreateHeatPumpFacilityWithHeatTransferFluidSvcDto;
import minutmiljo.CreateIndividualSewageFacility;
import minutmiljo.CreateIndividualSewageFacilitySvcDto;
import minutmiljo.CreateMarineHeatingFacilitySvcDto;
import minutmiljo.CreateOccurrenceOnCase;
import minutmiljo.CreateOccurrenceOnCaseSvcDto;
import minutmiljo.CreateOrganizationParty;
import minutmiljo.CreatePersonParty;
import minutmiljo.CreateSoilHeatingFacilitySvcDto;
import minutmiljo.DocumentSvcDto;
import minutmiljo.DrySolutionSvcDto;
import minutmiljo.EstateSvcDto;
import minutmiljo.FacilityAddressSvcDto;
import minutmiljo.FilterBedSvcDto;
import minutmiljo.GetCase;
import minutmiljo.HeatCollectorTubeSvcDto;
import minutmiljo.InfiltrationPlantSvcDto;
import minutmiljo.LocationSvcDto;
import minutmiljo.MiniSewageSvcDto;
import minutmiljo.OccurrenceListItemSvcDto;
import minutmiljo.OrganizationSvcDto;
import minutmiljo.PartyAddressSvcDto;
import minutmiljo.PartySvcDto;
import minutmiljo.PersonSvcDto;
import minutmiljo.PhosphorusTrapSvcDto;
import minutmiljo.PurificationStepSvcDto;
import minutmiljo.SandFilterSvcDto;
import minutmiljo.SearchCase;
import minutmiljo.SearchCaseSvcDto;
import minutmiljo.SearchParty;
import minutmiljo.SearchPartySvcDto;
import minutmiljo.SepticTankSvcDto;
import minutmiljo.SinglePartyRoleFilterSvcDto;
import minutmiljoV2.RegisterDocument;
import minutmiljoV2.RegisterDocumentCaseResultSvcDto;
import minutmiljoV2.RegisterDocumentCaseSvcDtoV2;
import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.api.model.CaseStatusDTO;
import se.sundsvall.casemanagement.api.model.EnvironmentalCaseDTO;
import se.sundsvall.casemanagement.api.model.EnvironmentalFacilityDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.StakeholderDTO;
import se.sundsvall.casemanagement.api.model.enums.AddressCategory;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.integration.fb.model.FbPropertyInfo;
import se.sundsvall.casemanagement.service.CaseMappingService;
import se.sundsvall.casemanagement.service.CitizenMappingService;
import se.sundsvall.casemanagement.service.FbService;
import se.sundsvall.casemanagement.util.CaseUtil;
import se.sundsvall.casemanagement.util.Constants;

@Service
public class EcosService {

	private static final Logger log = LoggerFactory.getLogger(EcosService.class);
	private final CitizenMappingService citizenMappingService;
	private final CaseMappingService caseMappingService;
	private final MinutMiljoClient minutMiljoClient;
	private final MinutMiljoClientV2 minutMiljoClientV2;
	private final FbService fbService;
	private final RiskClassService riskClassService;

	public EcosService(CitizenMappingService citizenMappingService, CaseMappingService caseMappingService, MinutMiljoClient minutMiljoClient, MinutMiljoClientV2 minutMiljoClientV2, FbService fbService, RiskClassService riskClassService) {
		this.citizenMappingService = citizenMappingService;
		this.caseMappingService = caseMappingService;
		this.minutMiljoClient = minutMiljoClient;
		this.minutMiljoClientV2 = minutMiljoClientV2;
		this.fbService = fbService;
		this.riskClassService = riskClassService;
	}

	@NotNull
	private static String getFilename(AttachmentDTO attachmentDTO) {
		// Filename must end with extension for the preview in Ecos to work
		String filename = attachmentDTO.getName().toLowerCase();
		final String extension = attachmentDTO.getExtension().toLowerCase();
		if (!filename.endsWith(extension)) {
			filename = attachmentDTO.getName() + extension;
		}
		return filename;
	}

	public RegisterDocumentCaseResultSvcDto postCase(EnvironmentalCaseDTO caseInput) {

		final var eFacility = caseInput.getFacilities().get(0);

		FbPropertyInfo propertyInfo = null;
		if ((eFacility.getAddress() != null) && (eFacility.getAddress().getPropertyDesignation() != null)) {
			// Collects this early to avoid creating something before we discover potential errors
			propertyInfo = fbService.getPropertyInfoByPropertyDesignation(eFacility.getAddress().getPropertyDesignation());
		}

		// Do requests to SearchParty for every stakeholder and collect these stakeholders to be able to add them
		// to the facility later.
		final var partyList = new ArrayList<PartySvcDto>();

		// The stakeholder is stored with associated roles so that we can set roles later.
		final var partyRoles = new HashMap<String, ArrayOfguid>();

		// If the stakeholder is missing in Ecos, we keep it in this list and create them later (CreateParty)
		final var missingStakeholderDTOS = new ArrayList<StakeholderDTO>();

		// -----> SearchParty
		populatePartyList(caseInput, partyRoles, partyList, missingStakeholderDTOS);

		// -----> CreateParty
		createParty(partyRoles, partyList, missingStakeholderDTOS);

		// -----> RegisterDocument
		final var registerDocumentResult = registerDocument(caseInput);

		// -----> AddPartyToCase
		addPartyToCase(partyRoles, partyList, registerDocumentResult.getCaseId());

		if (propertyInfo != null) {
			final String facilityGuid = switch (caseInput.getCaseType()) {
				case REGISTRERING_AV_LIVSMEDEL -> createFoodFacility(caseInput, propertyInfo, registerDocumentResult);
				case ANMALAN_INSTALLATION_VARMEPUMP, ANSOKAN_TILLSTAND_VARMEPUMP -> createHeatPumpFacility(eFacility.getExtraParameters(), propertyInfo, registerDocumentResult);
				case ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC, ANMALAN_ANDRING_AVLOPPSANLAGGNING, ANMALAN_ANDRING_AVLOPPSANORDNING -> createIndividualSewage(eFacility, propertyInfo, registerDocumentResult);
				case ANMALAN_HALSOSKYDDSVERKSAMHET -> createHealthProtectionFacility(eFacility, propertyInfo, registerDocumentResult);
				case ANMALAN_KOMPOSTERING, ANMALAN_AVHJALPANDEATGARD_FORORENING -> "";
				default -> throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "CaseType: " + caseInput.getCaseType() + " is not valid. There is a problem in the API validation.");
			};

			// -----> AddPartyToFacility
			if ((facilityGuid != null) && !CaseType.WITH_NULLABLE_FACILITY_TYPE.contains(caseInput.getCaseType())) {
				addPartyToFacility(partyRoles, partyList, facilityGuid);
			}

		} else {
			if (CaseType.UPPDATERING_RISKKLASSNING.equals(caseInput.getCaseType())) {
				try {
					riskClassService.updateRiskClass(caseInput, registerDocumentResult.getCaseId());
				} catch (final Exception e) {
					log.warn("Error when updating risk class for case with OpenE-ID: {}", caseInput.getExternalCaseId(), e);
				}
			} else {
				createOccurrenceOnCase(registerDocumentResult.getCaseId());
			}
		}

		// Persist the connection between OeP-case and Ecos-case
		caseMappingService.postCaseMapping(CaseMapping.builder()
			.withExternalCaseId(caseInput.getExternalCaseId())
			.withCaseId(registerDocumentResult.getCaseId())
			.withSystem(SystemType.ECOS)
			.withCaseType(caseInput.getCaseType())
			.withServiceName(isNull(caseInput.getExtraParameters()) ? null : caseInput.getExtraParameters().get(SERVICE_NAME)).build());
		return registerDocumentResult;
	}

	private void addPartyToCase(Map<String, ArrayOfguid> partyRoles, List<PartySvcDto> partyList, String caseId) {
		for (final PartySvcDto p : partyList) {
			final AddPartyToCase addPartyToCase = new AddPartyToCase();
			final AddPartyToCaseSvcDto addPartyToCaseSvcDto = new AddPartyToCaseSvcDto();
			addPartyToCaseSvcDto.setCaseId(caseId);
			addPartyToCaseSvcDto.setPartyId(p.getId());
			addPartyToCaseSvcDto.setRoles(partyRoles.get(p.getId()));
			addPartyToCase.setModel(addPartyToCaseSvcDto);

			minutMiljoClient.addPartyToCase(addPartyToCase);
		}
	}

	private void addPartyToFacility(Map<String, ArrayOfguid> partyRoles, List<PartySvcDto> partyList, String foodFacilityGuid) {
		for (final PartySvcDto p : partyList) {
			final AddPartyToFacility addPartyToFacility = new AddPartyToFacility();
			final AddPartyToFacilitySvcDto addPartyToFacilitySvcDto = new AddPartyToFacilitySvcDto();
			addPartyToFacilitySvcDto.setFacilityId(foodFacilityGuid);
			addPartyToFacilitySvcDto.setPartyId(p.getId());
			addPartyToFacilitySvcDto.setRoles(partyRoles.get(p.getId()));
			addPartyToFacility.setModel(addPartyToFacilitySvcDto);

			minutMiljoClient.addPartyToFacility(addPartyToFacility);
		}
	}

	private String createFoodFacility(EnvironmentalCaseDTO eCase, FbPropertyInfo propertyInfo, RegisterDocumentCaseResultSvcDto registerDocumentResult) {

		final CreateFoodFacility createFoodFacility = new CreateFoodFacility();
		final CreateFoodFacilitySvcDto createFoodFacilitySvcDto = new CreateFoodFacilitySvcDto();

		createFoodFacilitySvcDto.setAddress(getAddress(propertyInfo));
		createFoodFacilitySvcDto.setCase(registerDocumentResult.getCaseId());

		createFoodFacilitySvcDto.setEstateDesignation(getEstateSvcDto(propertyInfo));
		createFoodFacilitySvcDto.setFacilityCollectionName(eCase.getFacilities().get(0).getFacilityCollectionName() + " " + LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
		createFoodFacilitySvcDto.setNote(eCase.getFacilities().get(0).getDescription());

		createFoodFacility.setCreateFoodFacilitySvcDto(createFoodFacilitySvcDto);

		final String foodFacilityGuid = minutMiljoClient.createFoodFacility(createFoodFacility).getCreateFoodFacilityResult();

		if (foodFacilityGuid != null) {
			log.debug("FoodFacility created: {}", foodFacilityGuid);
			return foodFacilityGuid;
		}
		throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "FoodFacility could not be created.");
	}

	private String createHeatPumpFacility(Map<String, String> facilityExtraParameters, FbPropertyInfo propertyInfo, RegisterDocumentCaseResultSvcDto registerDocumentResult) {

		final CreateHeatPumpFacility createHeatPumpFacility = new CreateHeatPumpFacility();
		CreateHeatPumpFacilitySvcDto createHeatPumpFacilitySvcDto;

		final String airPrefix = "CreateAirHeatingFacilitySvcDto_";
		final String geoThermalPrefix = "CreateGeothermalHeatingFacilitySvcDto_";
		final String soilPrefix = "CreateSoilHeatingFacilitySvcDto_";
		final String marinePrefix = "CreateMarineHeatingFacilitySvcDto_";

		if ((facilityExtraParameters == null) || facilityExtraParameters.isEmpty()) {
			log.info("facilityExtraParameters was null or empty, do not create facility. Return null.");
			return null;
		}
		if (facilityExtraParameters.keySet().stream().anyMatch(s -> s.startsWith(airPrefix))) {
			createHeatPumpFacilitySvcDto = getAirHeatingFacility(facilityExtraParameters, propertyInfo, registerDocumentResult, airPrefix);
		} else if (facilityExtraParameters.keySet().stream().anyMatch(s -> s.startsWith(geoThermalPrefix))) {
			createHeatPumpFacilitySvcDto = getGeoThermalHeatingFacility(facilityExtraParameters, propertyInfo, registerDocumentResult, geoThermalPrefix);
		} else if (facilityExtraParameters.keySet().stream().anyMatch(s -> s.startsWith(soilPrefix))) {
			createHeatPumpFacilitySvcDto = getSoilHeatingFacility(facilityExtraParameters, propertyInfo, registerDocumentResult, soilPrefix);
		} else if (facilityExtraParameters.keySet().stream().anyMatch(s -> s.startsWith(marinePrefix))) {
			createHeatPumpFacilitySvcDto = getMarineHeatingFacility(facilityExtraParameters, propertyInfo, registerDocumentResult, marinePrefix);
		} else {
			throw Problem.valueOf(Status.BAD_REQUEST, MessageFormat.format("The request does not contain any extraParameters on the facility-object with prefix: \"{0}\", \"{1}\"\", \"{2}\"\" or \"{3}\"", airPrefix, geoThermalPrefix, soilPrefix,
				marinePrefix));
		}

		createHeatPumpFacility.setCreateIndividualSewageSvcDto(createHeatPumpFacilitySvcDto);
		final String facilityGuid = minutMiljoClient.createHeatPumpFacility(createHeatPumpFacility).getCreateHeatPumpFacilityResult();

		if (facilityGuid != null) {
			log.debug("HeatPumpFacility created: {}", facilityGuid);
			return facilityGuid;
		}
		throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "HeatPumpFacility could not be created");
	}

	private CreateHeatPumpFacilityWithHeatTransferFluidSvcDto getGeoThermalHeatingFacility(Map<String, String> extraParameters, FbPropertyInfo propertyInfo, RegisterDocumentCaseResultSvcDto registerDocumentResult, String prefix) {
		final CreateGeothermalHeatingFacilitySvcDto createHeatPumpFacilityWithHeatTransferFluidSvcDto = new CreateGeothermalHeatingFacilitySvcDto();

		setHeatPumpStandardParams(extraParameters, propertyInfo, registerDocumentResult, createHeatPumpFacilityWithHeatTransferFluidSvcDto, prefix);
		setHeatPumpFluidStandardParams(extraParameters, prefix, createHeatPumpFacilityWithHeatTransferFluidSvcDto);

		createHeatPumpFacilityWithHeatTransferFluidSvcDto.setBoreholes(getBoreHoles(extraParameters));

		return createHeatPumpFacilityWithHeatTransferFluidSvcDto;
	}

	private ArrayOfBoreholeSvcDto getBoreHoles(Map<String, String> extraParameters) {
		final ArrayOfBoreholeSvcDto arrayOfBoreholeSvcDto = new ArrayOfBoreholeSvcDto();
		final String PREFIX = "BoreholeSvcDto_";
		int number = 1;

		while (extraParameters.containsKey(PREFIX + number++)) {
			final String currentPrefix = PREFIX + number + "_";

			final BoreholeSvcDto boreholeSvcDto = new BoreholeSvcDto();
			boreholeSvcDto.setAngleToVertical(CaseUtil.parseInteger(extraParameters.get(currentPrefix + "AngleToVertical")));
			boreholeSvcDto.setCompassDirection(CaseUtil.parseDouble(extraParameters.get(currentPrefix + "CompassDirection")));
			boreholeSvcDto.setFacilityStatusId(CaseUtil.parseString(extraParameters.get(currentPrefix + "FacilityStatusId")));
			boreholeSvcDto.setLength(CaseUtil.parseInteger(extraParameters.get(currentPrefix + "Length")));
			boreholeSvcDto.setName(CaseUtil.parseString(extraParameters.get(currentPrefix + "Name")));
			boreholeSvcDto.setLocation(getLocation(extraParameters, currentPrefix));

			arrayOfBoreholeSvcDto.getBoreholeSvcDto().add(boreholeSvcDto);
		}

		return arrayOfBoreholeSvcDto;
	}

	private LocationSvcDto getLocation(Map<String, String> extraParameters, String prefix) {
		final String locationPrefix = "LocationSvcDto_";
		final LocationSvcDto locationSvcDto = new LocationSvcDto();
		locationSvcDto.setE(CaseUtil.parseDouble(extraParameters.get(prefix + locationPrefix + "E")));
		locationSvcDto.setN(CaseUtil.parseDouble(extraParameters.get(prefix + locationPrefix + "N")));
		locationSvcDto.setMeasured(CaseUtil.parseBoolean(extraParameters.get(prefix + locationPrefix + "Measured")));

		return locationSvcDto;
	}

	private CreateSoilHeatingFacilitySvcDto getSoilHeatingFacility(Map<String, String> extraParameters, FbPropertyInfo propertyInfo, RegisterDocumentCaseResultSvcDto registerDocumentResult, String prefix) {
		final CreateSoilHeatingFacilitySvcDto createSoilHeatingFacilitySvcDto = new CreateSoilHeatingFacilitySvcDto();

		setHeatPumpStandardParams(extraParameters, propertyInfo, registerDocumentResult, createSoilHeatingFacilitySvcDto, prefix);
		setHeatPumpFluidStandardParams(extraParameters, prefix, createSoilHeatingFacilitySvcDto);

		createSoilHeatingFacilitySvcDto.setHeatCollectorTubes(getHeatCollectorTubes(extraParameters));

		return createSoilHeatingFacilitySvcDto;
	}

	private ArrayOfHeatCollectorTubeSvcDto getHeatCollectorTubes(Map<String, String> extraParameters) {
		final ArrayOfHeatCollectorTubeSvcDto arrayOfHeatCollectorTubeSvcDto = new ArrayOfHeatCollectorTubeSvcDto();
		final String PREFIX = "HeatCollectorTubeSvcDto_";
		int number = 1;

		while (extraParameters.containsKey(PREFIX + number++)) {
			final String currentPrefix = PREFIX + number + "_";

			final HeatCollectorTubeSvcDto heatCollectorTubeSvcDto = new HeatCollectorTubeSvcDto();
			heatCollectorTubeSvcDto.setFacilityStatusId(CaseUtil.parseString(extraParameters.get(currentPrefix + "FacilityStatusId")));
			heatCollectorTubeSvcDto.setLength(CaseUtil.parseInteger(extraParameters.get(currentPrefix + "Length")));
			heatCollectorTubeSvcDto.setName(CaseUtil.parseString(extraParameters.get(currentPrefix + "Name")));
			heatCollectorTubeSvcDto.setLocation(getLocation(extraParameters, currentPrefix));

			arrayOfHeatCollectorTubeSvcDto.getHeatCollectorTubeSvcDto().add(heatCollectorTubeSvcDto);
		}

		return arrayOfHeatCollectorTubeSvcDto;
	}

	private CreateMarineHeatingFacilitySvcDto getMarineHeatingFacility(Map<String, String> extraParameters, FbPropertyInfo propertyInfo, RegisterDocumentCaseResultSvcDto registerDocumentResult, String prefix) {
		final CreateMarineHeatingFacilitySvcDto createMarineHeatingFacilitySvcDto = new CreateMarineHeatingFacilitySvcDto();

		setHeatPumpStandardParams(extraParameters, propertyInfo, registerDocumentResult, createMarineHeatingFacilitySvcDto, prefix);
		setHeatPumpFluidStandardParams(extraParameters, prefix, createMarineHeatingFacilitySvcDto);
		createMarineHeatingFacilitySvcDto.setHeatCollectorTubes(getHeatCollectorTubes(extraParameters));

		return createMarineHeatingFacilitySvcDto;
	}

	private CreateAirHeatingFacilitySvcDto getAirHeatingFacility(Map<String, String> extraParameters, FbPropertyInfo propertyInfo, RegisterDocumentCaseResultSvcDto registerDocumentResult, String prefix) {
		final CreateAirHeatingFacilitySvcDto createAirHeatingFacilitySvcDto = new CreateAirHeatingFacilitySvcDto();

		setHeatPumpStandardParams(extraParameters, propertyInfo, registerDocumentResult, createAirHeatingFacilitySvcDto, prefix);

		return createAirHeatingFacilitySvcDto;
	}

	private void setHeatPumpFluidStandardParams(Map<String, String> extraParameters, String prefix, CreateHeatPumpFacilityWithHeatTransferFluidSvcDto svcDto) {
		svcDto.setCapacity(CaseUtil.parseDouble(extraParameters.get(prefix + "Capacity")));
		svcDto.setHeatTransferFluidId(CaseUtil.parseString(extraParameters.get(prefix + "HeatTransferFluidId")));
	}

	private void setHeatPumpStandardParams(Map<String, String> extraParameters, FbPropertyInfo propertyInfo, RegisterDocumentCaseResultSvcDto registerDocumentResult, CreateHeatPumpFacilitySvcDto svcDto, String prefix) {
		svcDto.setAddress(getAddress(propertyInfo));
		svcDto.setFacilityStatusId(Constants.ECOS_FACILITY_STATUS_ID_ANMALD_ANSOKT);
		svcDto.setCreatedFromCaseId(registerDocumentResult.getCaseId());
		svcDto.setEstate(getEstateSvcDto(propertyInfo));

		svcDto.setManufacturer(CaseUtil.parseString(extraParameters.get(prefix + "Manufacturer")));
		svcDto.setModel(CaseUtil.parseString(extraParameters.get(prefix + "Model")));
		svcDto.setPowerConsumption(CaseUtil.parseDouble(extraParameters.get(prefix + "PowerConsumption")));
		svcDto.setPowerOutput(CaseUtil.parseDouble(extraParameters.get(prefix + "PowerOutput")));
	}

	private String createIndividualSewage(EnvironmentalFacilityDTO eFacility, FbPropertyInfo propertyInfo, RegisterDocumentCaseResultSvcDto registerDocumentResult) {

		final CreateIndividualSewageFacility createIndividualSewageFacility = new CreateIndividualSewageFacility();
		final CreateIndividualSewageFacilitySvcDto createIndividualSewageFacilitySvcDto = new CreateIndividualSewageFacilitySvcDto();

		createIndividualSewageFacilitySvcDto.setAddress(getAddress(propertyInfo));

		createIndividualSewageFacilitySvcDto.setFacilityStatusId(Constants.ECOS_FACILITY_STATUS_ID_ANMALD_ANSOKT);
		createIndividualSewageFacilitySvcDto.setCreatedFromCaseId(registerDocumentResult.getCaseId());
		createIndividualSewageFacilitySvcDto.setEstate(getEstateSvcDto(propertyInfo));

		createIndividualSewageFacilitySvcDto.setNote(eFacility.getDescription());
		createIndividualSewageFacilitySvcDto.setOnGrantLand(CaseUtil.parseBoolean(eFacility.getExtraParameters().get("OnGrantLand")));
		createIndividualSewageFacilitySvcDto.setProtectionLevelApprovedEnvironmentId(CaseUtil.parseString(eFacility.getExtraParameters().get("ProtectionLevelApprovedEnvironmentId")));
		createIndividualSewageFacilitySvcDto.setProtectionLevelApprovedHealthId(CaseUtil.parseString(eFacility.getExtraParameters().get("ProtectionLevelApprovedHealthId")));
		createIndividualSewageFacilitySvcDto.setWastewaterApprovedForId(CaseUtil.parseString(eFacility.getExtraParameters().get("WastewaterApprovedForId")));
		createIndividualSewageFacilitySvcDto.setWasteWaterInboundId(CaseUtil.parseString(eFacility.getExtraParameters().get("WasteWaterInboundId")));
		createIndividualSewageFacilitySvcDto.setAccommodationTypeId(CaseUtil.parseString(eFacility.getExtraParameters().get("AccommodationTypeId")));

		createIndividualSewageFacilitySvcDto.setPurificationSteps(getPurificationSteps(eFacility.getExtraParameters()));

		createIndividualSewageFacility.setCreateIndividualSewageSvcDto(createIndividualSewageFacilitySvcDto);

		final String facilityGuid = minutMiljoClient.createIndividualSewageFacility(createIndividualSewageFacility).getCreateIndividualSewageFacilityResult();

		if (facilityGuid != null) {
			log.debug("Individual Sewage created: {}", facilityGuid);
			return facilityGuid;
		}
		throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "Individual Sewage could not be created");
	}

	private FacilityAddressSvcDto getAddress(FbPropertyInfo propertyInfo) {
		if (propertyInfo.getAdressplatsId() != null) {
			final FacilityAddressSvcDto facilityAddressSvcDto = new FacilityAddressSvcDto();
			facilityAddressSvcDto.setAdressPlatsId(propertyInfo.getAdressplatsId());
			return facilityAddressSvcDto;
		}
		return null;
	}

	private ArrayOfPurificationStepSvcDto getPurificationSteps(Map<String, String> extraParameters) {

		final ArrayOfPurificationStepSvcDto arrayOfPurificationStepSvcDto = new ArrayOfPurificationStepSvcDto();

		final String septicTankPrefix = "SepticTankSvcDto_";
		if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(septicTankPrefix))) {
			arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getSepticTankSvcDto(extraParameters, septicTankPrefix));
		}
		final String infiltrationPrefix = "InfiltrationPlantSvcDto_";
		if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(infiltrationPrefix))) {
			arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getInfiltrationPlantSvcDto(extraParameters, infiltrationPrefix));
		}
		final String closedTankPrefix = "ClosedTankSvcDto_";
		if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(closedTankPrefix))) {
			arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getClosedTankSvcDto(extraParameters, closedTankPrefix));
		}
		final String drySolutionPrefix = "DrySolutionSvcDto_";
		if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(drySolutionPrefix))) {
			arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getDrySolutionSvcDto(extraParameters, drySolutionPrefix));
		}
		final String miniSewagePrefix = "MiniSewageSvcDto_";
		if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(miniSewagePrefix))) {
			arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getMiniSewageSvcDto(extraParameters, miniSewagePrefix));
		}
		final String filterBedPrefix = "FilterBedSvcDto_";
		if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(filterBedPrefix))) {
			arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getFilterBedSvcDto(extraParameters, filterBedPrefix));
		}
		final String sandFilterPrefix = "SandFilterSvcDto_";
		if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(sandFilterPrefix))) {
			arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getSandFilterSvcDto(extraParameters, sandFilterPrefix));
		}
		final String biologicalStepPrefix = "BiologicalStepSvcDto_";
		if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(biologicalStepPrefix))) {
			arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getBiologicalStepSvcDto(extraParameters, biologicalStepPrefix));
		}
		final String phosphorusTrapPrefix = "PhosphorusTrapSvcDto_";
		if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(phosphorusTrapPrefix))) {
			arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getPhosphorusTrapSvcDto(extraParameters, phosphorusTrapPrefix));
		}
		final String chemicalPretreatmentPrefix = "ChemicalPretreatmentSvcDto_";
		if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(chemicalPretreatmentPrefix))) {
			arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getChemicalPretreatmentSvcDto(extraParameters, chemicalPretreatmentPrefix));
		}

		return arrayOfPurificationStepSvcDto;
	}

	private SepticTankSvcDto getSepticTankSvcDto(Map<String, String> extraParameters, String prefix) {
		final SepticTankSvcDto svcDto = new SepticTankSvcDto();

		setStandardParams(extraParameters, prefix, svcDto);

		svcDto.setEmptyingInterval(CaseUtil.parseInteger(extraParameters.get(prefix + "EmptyingInterval")));
		svcDto.setHasCeMarking(CaseUtil.parseBoolean(extraParameters.get(prefix + "HasCeMarking")));
		svcDto.setHasTPipe(CaseUtil.parseBoolean(extraParameters.get(prefix + "HasTPipe")));
		svcDto.setVolume(CaseUtil.parseDouble(extraParameters.get(prefix + "Volume")));

		return svcDto;
	}

	private ClosedTankSvcDto getClosedTankSvcDto(Map<String, String> extraParameters, String prefix) {
		final ClosedTankSvcDto svcDto = new ClosedTankSvcDto();

		setStandardParams(extraParameters, prefix, svcDto);

		svcDto.setEmptyingInterval(CaseUtil.parseInteger(extraParameters.get(prefix + "EmptyingInterval")));
		svcDto.setHasCeMarking(CaseUtil.parseBoolean(extraParameters.get(prefix + "HasCeMarking")));
		svcDto.setVolume(CaseUtil.parseDouble(extraParameters.get(prefix + "Volume")));

		return svcDto;
	}

	private MiniSewageSvcDto getMiniSewageSvcDto(Map<String, String> extraParameters, String prefix) {
		final MiniSewageSvcDto svcDto = new MiniSewageSvcDto();

		setStandardParams(extraParameters, prefix, svcDto);

		svcDto.setCeMarking(CaseUtil.parseBoolean(extraParameters.get(prefix + "CeMarking")));
		svcDto.setManufacturer(CaseUtil.parseString(extraParameters.get(prefix + "Manufacturer")));
		svcDto.setModel(CaseUtil.parseString(extraParameters.get(prefix + "Model")));

		return svcDto;
	}

	private FilterBedSvcDto getFilterBedSvcDto(Map<String, String> extraParameters, String prefix) {
		final FilterBedSvcDto svcDto = new FilterBedSvcDto();

		setStandardParams(extraParameters, prefix, svcDto);
		svcDto.setVolume(CaseUtil.parseDouble(extraParameters.get(prefix + "Volume")));

		return svcDto;
	}

	private SandFilterSvcDto getSandFilterSvcDto(Map<String, String> extraParameters, String prefix) {
		final SandFilterSvcDto svcDto = new SandFilterSvcDto();

		setStandardParams(extraParameters, prefix, svcDto);

		svcDto.setArea(CaseUtil.parseInteger(extraParameters.get(prefix + "Area")));
		svcDto.setElevated(CaseUtil.parseBoolean(extraParameters.get(prefix + "Elevated")));
		svcDto.setWaterTight(CaseUtil.parseBoolean(extraParameters.get(prefix + "WaterTight")));

		return svcDto;
	}

	private BiologicalStepSvcDto getBiologicalStepSvcDto(Map<String, String> extraParameters, String prefix) {
		final BiologicalStepSvcDto svcDto = new BiologicalStepSvcDto();

		setStandardParams(extraParameters, prefix, svcDto);

		svcDto.setArea(CaseUtil.parseInteger(extraParameters.get(prefix + "Area")));
		svcDto.setBiologicalStepTypeId(CaseUtil.parseString(extraParameters.get(prefix + "BiologicalStepTypeId")));

		return svcDto;
	}

	private DrySolutionSvcDto getDrySolutionSvcDto(Map<String, String> extraParameters, String prefix) {
		final DrySolutionSvcDto svcDto = new DrySolutionSvcDto();

		setStandardParams(extraParameters, prefix, svcDto);

		svcDto.setCompostProductName(CaseUtil.parseString(extraParameters.get(prefix + "CompostProductName")));
		svcDto.setDrySolutionCompostTypeId(CaseUtil.parseString(extraParameters.get(prefix + "DrySolutionCompostTypeId")));
		svcDto.setDrySolutionTypeId(CaseUtil.parseString(extraParameters.get(prefix + "DrySolutionTypeId")));
		svcDto.setNoContOrCompt(CaseUtil.parseString(extraParameters.get(prefix + "NoContOrCompt")));
		svcDto.setNoLPerContOrCompt(CaseUtil.parseString(extraParameters.get(prefix + "NoLPerContOrCompt")));
		svcDto.setToiletProductName(CaseUtil.parseString(extraParameters.get(prefix + "ToiletProductName")));
		svcDto.setVolume(CaseUtil.parseDouble(extraParameters.get(prefix + "Volume")));

		return svcDto;
	}

	private InfiltrationPlantSvcDto getInfiltrationPlantSvcDto(Map<String, String> extraParameters, String prefix) {
		final InfiltrationPlantSvcDto svcDto = new InfiltrationPlantSvcDto();

		setStandardParams(extraParameters, prefix, svcDto);

		svcDto.setElevated(CaseUtil.parseBoolean(extraParameters.get(prefix + "Elevated")));
		svcDto.setReinforced(CaseUtil.parseBoolean(extraParameters.get(prefix + "Reinforced")));
		svcDto.setIsModuleSystem(CaseUtil.parseBoolean(extraParameters.get(prefix + "IsModuleSystem")));
		svcDto.setSpreadLinesCount(CaseUtil.parseInteger(extraParameters.get(prefix + "SpreadLinesCount")));
		svcDto.setSpreadLinesLength(CaseUtil.parseInteger(extraParameters.get(prefix + "SpreadLinesLength")));

		return svcDto;
	}

	private PhosphorusTrapSvcDto getPhosphorusTrapSvcDto(Map<String, String> extraParameters, String prefix) {
		final PhosphorusTrapSvcDto svcDto = new PhosphorusTrapSvcDto();
		setStandardParams(extraParameters, prefix, svcDto);
		return svcDto;
	}

	private ChemicalPretreatmentSvcDto getChemicalPretreatmentSvcDto(Map<String, String> extraParameters, String prefix) {
		final ChemicalPretreatmentSvcDto svcDto = new ChemicalPretreatmentSvcDto();
		setStandardParams(extraParameters, prefix, svcDto);
		return svcDto;
	}

	private void setStandardParams(Map<String, String> extraParameters, String prefix, PurificationStepSvcDto svcDto) {
		svcDto.setPurificationStepFacilityStatusId(Constants.ECOS_FACILITY_STATUS_ID_ANMALD_ANSOKT);
		svcDto.setInstallationDate(CaseUtil.parseLocalDateTime(extraParameters.get(prefix + "InstallationDate")));
		svcDto.setHasOverflowAlarm(CaseUtil.parseBoolean(extraParameters.get(prefix + "HasOverflowAlarm")));
		svcDto.setLifeTime(CaseUtil.parseInteger(extraParameters.get(prefix + "LifeTime")));
		svcDto.setPersonCapacity(CaseUtil.parseInteger(extraParameters.get(prefix + "PersonCapacity")));
		svcDto.setStepNumber(CaseUtil.parseInteger(extraParameters.get(prefix + "StepNumber")));
		svcDto.setPurificationStepLocation(null);
	}

	public RegisterDocumentCaseResultSvcDto registerDocument(EnvironmentalCaseDTO eCase) {
		final var registerDocumentCaseSvcDtoV2 = new RegisterDocumentCaseSvcDtoV2();
		final var registerDocument = new RegisterDocument();
		registerDocumentCaseSvcDtoV2.setOccurrenceTypeId(Constants.ECOS_OCCURENCE_TYPE_ID_ANMALAN);
		registerDocumentCaseSvcDtoV2.setHandlingOfficerGroupId(Constants.ECOS_HANDLING_OFFICER_GROUP_ID_EXPEDITIONEN);
		registerDocumentCaseSvcDtoV2.setDiaryPlanId(getDiaryPlanId(eCase.getCaseType()));
		registerDocumentCaseSvcDtoV2.setProcessTypeId(getProcessTypeId(eCase.getCaseType()));
		registerDocumentCaseSvcDtoV2.setDocuments(getArrayOfDocumentSvcDtoV2(eCase.getAttachments()));

		final var eFacility = eCase.getFacilities().get(0);

		final var fixedFacilityType = Optional.ofNullable(Optional.ofNullable(eCase.getExtraParameters()).orElse(Map.of()).get("fixedFacilityType")).orElse("").trim();

		final var propertyDesignation = Optional.ofNullable(Optional.ofNullable(eFacility.getAddress()).orElse(new AddressDTO()).getPropertyDesignation()).orElse("").trim().toUpperCase();

		if (!fixedFacilityType.isEmpty()) {
			registerDocumentCaseSvcDtoV2.setCaseSubtitle(fixedFacilityType);
			registerDocumentCaseSvcDtoV2.setCaseSubtitleFree(propertyDesignation);
		} else {
			var freeSubtitle = Optional.ofNullable(eFacility.getFacilityCollectionName()).orElse("").trim();
			if (!propertyDesignation.isEmpty()) {
				freeSubtitle = String.join(", ", freeSubtitle, propertyDesignation);
			}
			registerDocumentCaseSvcDtoV2.setCaseSubtitleFree(freeSubtitle);
		}
		registerDocument.setRegisterDocumentCaseSvcDto(registerDocumentCaseSvcDtoV2);
		final var registerDocumentResult = minutMiljoClientV2.registerDocumentV2(registerDocument).getRegisterDocumentResult();

		if (registerDocumentResult == null) {
			throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "Case could not be created.");
		}
		log.debug("Case created with Byggr case number: {}", registerDocumentResult.getCaseNumber());
		return registerDocumentResult;
	}

	private String getDiaryPlanId(CaseType caseType) {
		return switch (caseType) {
			case REGISTRERING_AV_LIVSMEDEL, UPPDATERING_RISKKLASSNING -> Constants.ECOS_DIARY_PLAN_LIVSMEDEL;
			case ANMALAN_ANDRING_AVLOPPSANLAGGNING, ANMALAN_ANDRING_AVLOPPSANORDNING, ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC,
				ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, ANMALAN_INSTALLATION_VARMEPUMP, ANSOKAN_TILLSTAND_VARMEPUMP,
				ANMALAN_KOMPOSTERING, ANMALAN_AVHJALPANDEATGARD_FORORENING -> Constants.ECOS_DIARY_PLAN_AVLOPP;
			case ANMALAN_HALSOSKYDDSVERKSAMHET -> Constants.ECOS_DIARY_PLAN_HALSOSKYDD;
			default -> null;
		};
	}

	private String getProcessTypeId(CaseType caseType) {
		return switch (caseType) {
			case REGISTRERING_AV_LIVSMEDEL -> Constants.ECOS_PROCESS_TYPE_ID_REGISTRERING_AV_LIVSMEDEL;
			case ANMALAN_INSTALLATION_VARMEPUMP -> Constants.ECOS_PROCESS_TYPE_ID_ANMALAN_INSTALLATION_VARMEPUMP;
			case ANSOKAN_TILLSTAND_VARMEPUMP -> Constants.ECOS_PROCESS_TYPE_ID_ANSOKAN_TILLSTAND_VARMEPUMP;
			case ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP -> Constants.ECOS_PROCESS_TYPE_ID_ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP;
			case ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC -> Constants.ECOS_PROCESS_TYPE_ID_ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC;
			case ANMALAN_ANDRING_AVLOPPSANLAGGNING -> Constants.ECOS_PROCESS_TYPE_ID_ANMALAN_ANDRING_AVLOPPSANLAGGNING;
			case ANMALAN_ANDRING_AVLOPPSANORDNING -> Constants.ECOS_PROCESS_TYPE_ID_ANMALAN_ANDRING_AVLOPPSANORDNING;
			case ANMALAN_HALSOSKYDDSVERKSAMHET -> Constants.ECOS_PROCESS_TYPE_ID_ANMALAN_HALSOSKYDDSVERKSAMHET;
			case UPPDATERING_RISKKLASSNING -> Constants.ECOS_PROCESS_TYPE_ID_UPPDATERING_RISKKLASS;
			case ANMALAN_KOMPOSTERING -> Constants.ECOS_PROCESS_TYPE_ID_ANMALAN_KOMPOSTERING;
			case ANMALAN_AVHJALPANDEATGARD_FORORENING -> Constants.ECOS_PROCESS_TYPE_ID_ANMALAN_AVHJALPANDEATGARD_FORORENING;
			default -> throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "CaseType: " + caseType + " is not valid...");
		};
	}

	private ArrayOfDocumentSvcDto getArrayOfDocumentSvcDto(List<AttachmentDTO> attachmentDTOList) {
		final ArrayOfDocumentSvcDto arrayOfDocumentSvcDto = new ArrayOfDocumentSvcDto();
		for (final AttachmentDTO a : attachmentDTOList) {
			final DocumentSvcDto documentSvcDto = new DocumentSvcDto();
			documentSvcDto.setContentType(a.getMimeType() != null ? a.getMimeType().toLowerCase() : null);
			documentSvcDto.setData(Base64.getDecoder().decode(a.getFile().getBytes()));
			documentSvcDto.setDocumentTypeId(a.getCategory().getDescription());
			documentSvcDto.setFilename(getFilename(a));
			documentSvcDto.setNote(a.getNote());
			arrayOfDocumentSvcDto.getDocumentSvcDto().add(documentSvcDto);
		}
		return arrayOfDocumentSvcDto;
	}

	private minutmiljoV2.ArrayOfDocumentSvcDto getArrayOfDocumentSvcDtoV2(List<AttachmentDTO> attachmentDTOList) {
		final minutmiljoV2.ArrayOfDocumentSvcDto arrayOfDocumentSvcDto = new minutmiljoV2.ArrayOfDocumentSvcDto();
		for (final AttachmentDTO a : attachmentDTOList) {
			final minutmiljoV2.DocumentSvcDto documentSvcDto = new minutmiljoV2.DocumentSvcDto();
			documentSvcDto.setContentType(a.getMimeType() != null ? a.getMimeType().toLowerCase() : null);
			documentSvcDto.setData(Base64.getDecoder().decode(a.getFile().getBytes()));
			documentSvcDto.setDocumentTypeId(a.getCategory().getDescription());
			documentSvcDto.setFilename(getFilename(a));
			documentSvcDto.setNote(a.getNote());
			arrayOfDocumentSvcDto.getDocumentSvcDto().add(documentSvcDto);
		}
		return arrayOfDocumentSvcDto;
	}

	private void createParty(Map<String, ArrayOfguid> partyRoles, List<PartySvcDto> partyList, List<StakeholderDTO> missingStakeholderDTOS) {

		for (final StakeholderDTO s : missingStakeholderDTOS) {
			String guidResult = null;

			if (s instanceof final OrganizationDTO organizationDTO) {

				final CreateOrganizationParty createOrganizationParty = new CreateOrganizationParty();
				createOrganizationParty.setOrganizationParty(getOrganizationSvcDto(s, organizationDTO));
				guidResult = minutMiljoClient.createOrganizationParty(createOrganizationParty).getCreateOrganizationPartyResult();

			} else if (s instanceof final PersonDTO personDTO) {
				final CreatePersonParty createPersonParty = new CreatePersonParty();
				createPersonParty.setPersonParty(getPersonSvcDto(s, personDTO));
				guidResult = minutMiljoClient.createPersonParty(createPersonParty).getCreatePersonPartyResult();
			}

			if (guidResult != null) {
				final PartySvcDto party = new PartySvcDto();
				party.setId(guidResult);

				partyList.add(party);

				// Adds party in a hashmap with the role so that we can connect stakeholder to case with the
				// role later
				partyRoles.put(party.getId(), getEcosFacilityRoles(s));

			}
		}
	}

	PersonSvcDto getPersonSvcDto(StakeholderDTO s, PersonDTO personDTO) {
		final PersonSvcDto personSvcDto = new PersonSvcDto();
		personSvcDto.setFirstName(personDTO.getFirstName());
		personSvcDto.setLastName(personDTO.getLastName());

		if ((personDTO.getPersonId() != null) && !personDTO.getPersonId().isBlank()) {
			personSvcDto.setNationalIdentificationNumber(CaseUtil.getSokigoFormattedPersonalNumber(citizenMappingService.getPersonalNumber(personDTO.getPersonId())));
		}

		personSvcDto.setAddresses(getEcosAddresses(s.getAddresses()));
		personSvcDto.setContactInfo(getEcosContactInfo(s).getContactInfoSvcDto().get(0));
		return personSvcDto;
	}

	OrganizationSvcDto getOrganizationSvcDto(StakeholderDTO s, OrganizationDTO organizationDTO) {
		final OrganizationSvcDto osd = new OrganizationSvcDto();
		osd.setNationalIdentificationNumber(CaseUtil.getSokigoFormattedOrganizationNumber(organizationDTO.getOrganizationNumber()));
		osd.setOrganizationName(organizationDTO.getOrganizationName());

		osd.setAddresses(getEcosAddresses(s.getAddresses()));
		osd.setContactInfo(getEcosContactInfo(s));
		return osd;
	}

	private void populatePartyList(EnvironmentalCaseDTO eCase, Map<String, ArrayOfguid> partyRoles, List<PartySvcDto> partyList, List<StakeholderDTO> missingStakeholderDTOS) {

		for (final StakeholderDTO s : eCase.getStakeholders()) {
			ArrayOfPartySvcDto searchPartyResult = null;

			if (s instanceof final PersonDTO personDTO) {
				if ((personDTO.getPersonId() != null) && !personDTO.getPersonId().isBlank()) {
					searchPartyResult = searchPartyByPersonId(personDTO.getPersonId());
				}
			} else if (s instanceof final OrganizationDTO organizationDTO) {
				searchPartyResult = searchPartyByOrganizationNumber(organizationDTO.getOrganizationNumber());
			}

			// If we get a result we put it in partyList, else we put it in missingStakeholders
			if ((searchPartyResult == null) || searchPartyResult.getPartySvcDto().isEmpty()) {
				// These, we are going to create later
				missingStakeholderDTOS.add(s);
			} else if (!searchPartyResult.getPartySvcDto().isEmpty()) {

				// Sometimes we get multiple search results, but we should only use one in the case.
				final PartySvcDto party = searchPartyResult.getPartySvcDto().get(0);
				partyList.add(party);

				// Adds stakeholder to a hashmap with the role so that we can connect the stakeholder to the case later
				partyRoles.put(party.getId(), getEcosFacilityRoles(s));
			}
		}
	}

	ArrayOfguid getEcosFacilityRoles(StakeholderDTO s) {

		final ArrayOfguid roles = new ArrayOfguid();

		for (final StakeholderRole role : s.getRoles()) {
			final String roleId = switch (role) {
				case INVOICE_RECIPENT -> Constants.ECOS_ROLE_ID_FAKTURAMOTTAGARE;
				case OPERATOR -> Constants.ECOS_ROLE_ID_VERKSAMHETSUTOVARE;
				case CONTACT_PERSON -> Constants.ECOS_ROLE_ID_KONTAKTPERSON;
				case APPLICANT -> Constants.ECOS_ROLE_ID_SOKANDE;
				case INSTALLER -> Constants.ECOS_ROLE_ID_INSTALLATOR;
				default -> throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "The request contained a stakeholder role that was not expected. This should be discovered in the validation of the input. Something in the validation is wrong.");
			};

			roles.getGuid().add(roleId);
		}

		return roles;
	}

	ArrayOfContactInfoSvcDto getEcosContactInfo(StakeholderDTO s) {
		final ArrayOfContactInfoSvcDto arrayOfContactInfoSvcDto = new ArrayOfContactInfoSvcDto();
		final ContactInfoSvcDto contactInfoSvcDto = new ContactInfoSvcDto();
		final ArrayOfContactInfoItemSvcDto arrayOfContactInfoItemSvcDto = new ArrayOfContactInfoItemSvcDto();

		if (s.getEmailAddress() != null) {
			final ContactInfoItemSvcDto item = new ContactInfoItemSvcDto();

			item.setContactDetailTypeId(Constants.ECOS_CONTACT_DETAIL_TYPE_ID_OVRIGT);
			item.setContactPathId(Constants.ECOS_CONTACT_DETAIL_TYPE_ID_EPOST);
			item.setValue(s.getEmailAddress());

			arrayOfContactInfoItemSvcDto.getContactInfoItemSvcDto().add(item);
		}
		if (s.getCellphoneNumber() != null) {
			final ContactInfoItemSvcDto item = new ContactInfoItemSvcDto();

			item.setContactDetailTypeId(Constants.ECOS_CONTACT_DETAIL_TYPE_ID_MOBIL);
			item.setContactPathId(Constants.ECOS_CONTACT_DETAIL_TYPE_ID_TELEFON);
			item.setValue(s.getCellphoneNumber());

			arrayOfContactInfoItemSvcDto.getContactInfoItemSvcDto().add(item);
		}

		if (s.getPhoneNumber() != null) {
			final ContactInfoItemSvcDto item = new ContactInfoItemSvcDto();

			item.setContactDetailTypeId(Constants.ECOS_CONTACT_DETAIL_TYPE_ID_HUVUDNUMMER);
			item.setContactPathId(Constants.ECOS_CONTACT_DETAIL_TYPE_ID_TELEFON);
			item.setValue(s.getPhoneNumber());

			arrayOfContactInfoItemSvcDto.getContactInfoItemSvcDto().add(item);
		}

		contactInfoSvcDto.setContactDetails(arrayOfContactInfoItemSvcDto);

		if (s instanceof final PersonDTO personDTO) {
			contactInfoSvcDto.setTitle(personDTO.getFirstName() + " " + personDTO.getLastName());
		} else if (s instanceof final OrganizationDTO organizationDTO) {
			contactInfoSvcDto.setTitle(organizationDTO.getAuthorizedSignatory());
		}

		arrayOfContactInfoSvcDto.getContactInfoSvcDto().add(contactInfoSvcDto);
		return arrayOfContactInfoSvcDto;
	}

	ArrayOfPartyAddressSvcDto getEcosAddresses(List<AddressDTO> addressDTOS) {
		if (addressDTOS == null) {
			return null;
		}

		final ArrayOfPartyAddressSvcDto partyAddresses = new ArrayOfPartyAddressSvcDto();

		for (final AddressDTO addressDTO : addressDTOS) {
			final PartyAddressSvcDto partyAddress = new PartyAddressSvcDto();

			final ArrayOfAddressTypeSvcDto arrayOfAddressType = new ArrayOfAddressTypeSvcDto();

			for (final AddressCategory at : addressDTO.getAddressCategories()) {
				final AddressTypeSvcDto addressType = new AddressTypeSvcDto();

				switch (at) {
					case INVOICE_ADDRESS -> addressType.setId(Constants.ECOS_ADDRESS_TYPE_ID_FAKTURAADRESS);
					case POSTAL_ADDRESS -> addressType.setId(Constants.ECOS_ADDRESS_TYPE_ID_POSTADRESS);
					case VISITING_ADDRESS -> addressType.setId(Constants.ECOS_ADDRESS_TYPE_ID_BESOKSADRESS);
				}

				arrayOfAddressType.getAddressTypeSvcDto().add(addressType);
			}

			partyAddress.setAddressTypes(arrayOfAddressType);

			partyAddress.setCareOfName(addressDTO.getCareOf());
			partyAddress.setCountry(addressDTO.getCountry());
			partyAddress.setPostalArea(addressDTO.getCity());
			partyAddress.setPostCode(addressDTO.getPostalCode());
			partyAddress.setStreetName(addressDTO.getStreet());
			partyAddress.setStreetNumber(addressDTO.getHouseNumber());
			partyAddresses.getPartyAddressSvcDto().add(partyAddress);
		}

		return partyAddresses;
	}

	private void createOccurrenceOnCase(String caseId) {

		final CreateOccurrenceOnCase createOccurrenceOnCase = new CreateOccurrenceOnCase();
		final CreateOccurrenceOnCaseSvcDto createOccurrenceOnCaseSvcDto = new CreateOccurrenceOnCaseSvcDto();
		createOccurrenceOnCaseSvcDto.setCaseId(caseId);
		createOccurrenceOnCaseSvcDto.setOccurrenceDate(LocalDateTime.now());
		createOccurrenceOnCaseSvcDto.setOccurrenceTypeId(Constants.ECOS_OCCURRENCE_TYPE_ID_INFO_FRAN_ETJANST);
		createOccurrenceOnCaseSvcDto.setNote(Constants.ECOS_OCCURENCE_TEXT_MOBIL_ANLAGGNING);
		createOccurrenceOnCase.setCreateOccurrenceOnCaseSvcDto(createOccurrenceOnCaseSvcDto);

		minutMiljoClient.createOccurrenceOnCase(createOccurrenceOnCase);
	}

	/**
	 * @return                  CaseStatus from Ecos.
	 * @throws ThrowableProblem NOT_FOUND if no status was found.
	 */
	public CaseStatusDTO getStatus(String caseId, String externalCaseId) {

		final var getCase = new GetCase().withCaseId(caseId);

		final CaseSvcDto ecosCase = minutMiljoClient.getCase(getCase).getGetCaseResult();

		if (Optional.ofNullable(ecosCase)
			.flatMap(caseObj -> Optional.ofNullable(caseObj.getOccurrences()))
			.flatMap(occurrences -> Optional.ofNullable(occurrences.getOccurrenceListItemSvcDto()))
			.filter(list -> !list.isEmpty())
			.isPresent()) {

			final var caseMapping = Optional.ofNullable(caseMappingService.getCaseMapping(externalCaseId, caseId).get(0)).orElse(new CaseMapping());

			final var latestOccurrence = ecosCase.getOccurrences()
				.getOccurrenceListItemSvcDto()
				.stream()
				.max(Comparator.comparing(OccurrenceListItemSvcDto::getOccurrenceDate))
				.orElse(new OccurrenceListItemSvcDto());

			if (latestOccurrence.getOccurrenceDescription() == null) {
				return null;
			}

			return CaseStatusDTO.builder()
				.withSystem(SystemType.ECOS)
				.withExternalCaseId(externalCaseId)
				.withCaseId(ecosCase.getCaseNumber())
				.withCaseType(caseMapping.getCaseType())
				.withServiceName(caseMapping.getServiceName())
				.withStatus(latestOccurrence.getOccurrenceDescription())
				.withTimestamp(latestOccurrence.getOccurrenceDate()).build();
		}
		throw Problem.valueOf(Status.NOT_FOUND, Constants.ERR_MSG_STATUS_NOT_FOUND);
	}

	public List<CaseStatusDTO> getEcosStatusByOrgNr(String organizationNumber) {
		final List<CaseStatusDTO> caseStatusDTOList = new ArrayList<>();

		// Find party both with and without prefix "16"
		final ArrayOfPartySvcDto allParties = searchPartyByOrganizationNumber(organizationNumber);

		// Search Ecos Case
		if ((allParties.getPartySvcDto() != null) && !allParties.getPartySvcDto().isEmpty()) {

			final ArrayOfSearchCaseResultSvcDto caseResult = new ArrayOfSearchCaseResultSvcDto();

			allParties.getPartySvcDto().forEach(party -> caseResult.getSearchCaseResultSvcDto().addAll(searchCase(party.getId()).getSearchCaseResultSvcDto()));

			// Remove eventual duplicates
			final var caseResultWithoutDuplicates = caseResult.getSearchCaseResultSvcDto().stream().distinct().toList();

			caseResultWithoutDuplicates.forEach(ecosCase -> {
				final List<CaseMapping> caseMappingList = caseMappingService.getCaseMapping(null, ecosCase.getCaseId());
				final String externalCaseId = caseMappingList.isEmpty() ? null : caseMappingList.get(0).getExternalCaseId();
				final CaseStatusDTO caseStatusDTO = getStatus(ecosCase.getCaseId(), externalCaseId);

				if (caseStatusDTO != null) {
					caseStatusDTOList.add(caseStatusDTO);
				}
			});
		}

		return caseStatusDTOList;
	}

	public void addDocumentsToCase(String caseId, List<AttachmentDTO> attachmentDTOList) {
		final AddDocumentsToCase addDocumentsToCase = new AddDocumentsToCase();
		final AddDocumentsToCaseSvcDto message = new AddDocumentsToCaseSvcDto();
		message.setCaseId(caseId);
		message.setDocuments(getArrayOfDocumentSvcDto(attachmentDTOList));
		message.setOccurrenceTypeId(Constants.ECOS_OCCURRENCE_TYPE_ID_KOMPLETTERING);
		message.setDocumentStatusId(Constants.ECOS_DOCUMENT_STATUS_INKOMMEN);
		addDocumentsToCase.setAddDocumentToCaseSvcDto(message);

		minutMiljoClient.addDocumentsToCase(addDocumentsToCase);
	}

	private ArrayOfPartySvcDto searchPartyByOrganizationNumber(String organizationNumber) {

		// Find party both with and without prefix "16"
		final ArrayOfPartySvcDto allParties = new ArrayOfPartySvcDto();

		final SearchParty searchPartyWithoutPrefix = new SearchParty();
		final SearchPartySvcDto searchPartySvcDtoWithoutPrefix = new SearchPartySvcDto();
		searchPartySvcDtoWithoutPrefix.setOrganizationIdentificationNumber(organizationNumber);
		searchPartyWithoutPrefix.setModel(searchPartySvcDtoWithoutPrefix);
		final ArrayOfPartySvcDto partiesWithoutPrefix = minutMiljoClient.searchParty(searchPartyWithoutPrefix).getSearchPartyResult();

		final SearchParty searchPartyWithPrefix = new SearchParty();
		final SearchPartySvcDto searchPartySvcDtoWithPrefix = new SearchPartySvcDto();
		searchPartySvcDtoWithPrefix.setOrganizationIdentificationNumber(CaseUtil.getSokigoFormattedOrganizationNumber(organizationNumber));
		searchPartyWithPrefix.setModel(searchPartySvcDtoWithPrefix);
		final ArrayOfPartySvcDto partiesWithPrefix = minutMiljoClient.searchParty(searchPartyWithPrefix).getSearchPartyResult();

		if (partiesWithoutPrefix != null) {
			allParties.getPartySvcDto().addAll(partiesWithoutPrefix.getPartySvcDto());
		}
		if (partiesWithPrefix != null) {
			allParties.getPartySvcDto().addAll(partiesWithPrefix.getPartySvcDto());
		}

		return allParties;
	}

	/**
	 * Search for party with and without hyphen in personal number
	 *
	 * @param  personId personId for person to search for
	 * @return          ArrayOfPartySvcDto
	 */
	private ArrayOfPartySvcDto searchPartyByPersonId(String personId) {
		final SearchParty searchPartyWithHyphen = new SearchParty();
		final SearchPartySvcDto searchPartySvcDtoWithHyphen = new SearchPartySvcDto();
		searchPartySvcDtoWithHyphen.setPersonalIdentificationNumber(CaseUtil.getSokigoFormattedPersonalNumber(citizenMappingService.getPersonalNumber(personId)));
		searchPartyWithHyphen.setModel(searchPartySvcDtoWithHyphen);

		final var resultWithHyphen = minutMiljoClient.searchParty(searchPartyWithHyphen).getSearchPartyResult();

		if ((resultWithHyphen != null) && !resultWithHyphen.getPartySvcDto().isEmpty()) {
			return resultWithHyphen;
		}
		final SearchParty searchPartyWithoutHyphen = new SearchParty();
		final SearchPartySvcDto searchPartySvcDtoWithoutHyphen = new SearchPartySvcDto();
		searchPartySvcDtoWithoutHyphen.setPersonalIdentificationNumber(citizenMappingService.getPersonalNumber(personId));
		searchPartyWithoutHyphen.setModel(searchPartySvcDtoWithoutHyphen);

		return minutMiljoClient.searchParty(searchPartyWithoutHyphen).getSearchPartyResult();
	}

	private ArrayOfSearchCaseResultSvcDto searchCase(String partyId) {
		final SearchCase searchCase = new SearchCase();
		final SearchCaseSvcDto searchCaseSvcDto = new SearchCaseSvcDto();
		final ArrayOfFilterSvcDto arrayOfFilterSvcDto = new ArrayOfFilterSvcDto();
		final SinglePartyRoleFilterSvcDto filter = new SinglePartyRoleFilterSvcDto();

		filter.setPartyId(partyId);
		filter.setRoleId(Constants.ECOS_ROLE_ID_VERKSAMHETSUTOVARE);
		arrayOfFilterSvcDto.getFilterSvcDto().add(filter);
		searchCaseSvcDto.setFilters(arrayOfFilterSvcDto);

		searchCase.setModel(searchCaseSvcDto);
		return minutMiljoClient.searchCase(searchCase).getSearchCaseResult();
	}

	private String createHealthProtectionFacility(EnvironmentalFacilityDTO eFacility, FbPropertyInfo propertyInfo, RegisterDocumentCaseResultSvcDto registerDocumentResult) {

		final CreateHealthProtectionFacility createHealthProtectionFacility = new CreateHealthProtectionFacility();
		final CreateHealthProtectionFacilitySvcDto createHealthProtectionFacilitySvcDto = new CreateHealthProtectionFacilitySvcDto();
		createHealthProtectionFacilitySvcDto.setAddress(getAddress(propertyInfo));
		createHealthProtectionFacilitySvcDto.setEstateDesignation(getEstateSvcDto(propertyInfo));
		createHealthProtectionFacilitySvcDto.setCase(registerDocumentResult.getCaseId());
		createHealthProtectionFacilitySvcDto.setNote(eFacility.getDescription());
		createHealthProtectionFacilitySvcDto.setFacilityCollectionName(eFacility.getFacilityCollectionName());
		createHealthProtectionFacility.setCreateHealthProtectionFacilitySvcDto(createHealthProtectionFacilitySvcDto);

		final String facilityGuid = minutMiljoClient.createHealthProtectionFacility(createHealthProtectionFacility).getCreateHealthProtectionFacilityResult();

		if (facilityGuid != null) {
			log.debug("Health Protection Facility created: {}", facilityGuid);
			return facilityGuid;
		}
		throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "Health Protection Facility could not be created");
	}

	private EstateSvcDto getEstateSvcDto(FbPropertyInfo propertyInfo) {
		final EstateSvcDto estateSvcDto = new EstateSvcDto();
		estateSvcDto.setFnr(propertyInfo.getFnr());
		return estateSvcDto;
	}
}
