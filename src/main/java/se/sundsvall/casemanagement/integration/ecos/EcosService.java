package se.sundsvall.casemanagement.integration.ecos;

import minutmiljo.AddDocumentsToCase;
import minutmiljo.AddDocumentsToCaseSvcDto;
import minutmiljo.AddPartyToFacility;
import minutmiljo.AddPartyToFacilitySvcDto;
import minutmiljo.ArrayOfBoreholeSvcDto;
import minutmiljo.ArrayOfDocumentSvcDto;
import minutmiljo.ArrayOfFilterSvcDto;
import minutmiljo.ArrayOfHeatCollectorTubeSvcDto;
import minutmiljo.ArrayOfPartySvcDto;
import minutmiljo.ArrayOfPurificationStepSvcDto;
import minutmiljo.ArrayOfSearchCaseResultSvcDto;
import minutmiljo.ArrayOfguid;
import minutmiljo.BiologicalStepSvcDto;
import minutmiljo.BoreholeSvcDto;
import minutmiljo.ChemicalPretreatmentSvcDto;
import minutmiljo.ClosedTankSvcDto;
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
import minutmiljo.PhosphorusTrapSvcDto;
import minutmiljo.PurificationStepSvcDto;
import minutmiljo.SandFilterSvcDto;
import minutmiljo.SearchCase;
import minutmiljo.SearchCaseSvcDto;
import minutmiljo.SepticTankSvcDto;
import minutmiljo.SinglePartyRoleFilterSvcDto;
import minutmiljoV2.RegisterDocument;
import minutmiljoV2.RegisterDocumentCaseResultSvcDto;
import minutmiljoV2.RegisterDocumentCaseSvcDtoV2;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.api.model.CaseStatusDTO;
import se.sundsvall.casemanagement.api.model.EcosCaseDTO;
import se.sundsvall.casemanagement.api.model.FacilityDTO;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.integration.fb.model.FbPropertyInfo;
import se.sundsvall.casemanagement.service.CaseMappingService;
import se.sundsvall.casemanagement.service.FbService;
import se.sundsvall.casemanagement.util.CaseUtil;
import se.sundsvall.casemanagement.util.Constants;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_ANDRING_AVLOPPSANLAGGNING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_ANDRING_AVLOPPSANORDNING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_AVHJALPANDEATGARD_FORORENING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_HALSOSKYDDSVERKSAMHET;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_INSTALLATION_VARMEPUMP;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_KOMPOSTERING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANSOKAN_TILLSTAND_VARMEPUMP;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.REGISTRERING_AV_LIVSMEDEL;
import static se.sundsvall.casemanagement.util.Constants.BIOLOGICAL_STEP_SVC_DTO;
import static se.sundsvall.casemanagement.util.Constants.CHEMICAL_PRETREATMENT_SVC_DTO;
import static se.sundsvall.casemanagement.util.Constants.CLOSED_TANK_SVC_DTO;
import static se.sundsvall.casemanagement.util.Constants.CREATE_AIR_HEATING_FACILITY_SVC_DTO_PREFIX;
import static se.sundsvall.casemanagement.util.Constants.CREATE_GEOTHERMAL_HEATING_FACILITY_SVC_DTO_PREFIX;
import static se.sundsvall.casemanagement.util.Constants.CREATE_MARINE_HEATING_FACILITY_SVC_DTO_PREFIX;
import static se.sundsvall.casemanagement.util.Constants.CREATE_SOIL_HEATING_FACILITY_SVC_DTO_PREFIX;
import static se.sundsvall.casemanagement.util.Constants.DRY_SOLUTION_SVC_DTO;
import static se.sundsvall.casemanagement.util.Constants.FILTER_BED_SVC_DTO;
import static se.sundsvall.casemanagement.util.Constants.INFILTRATION_PLANT_SVC_DTO;
import static se.sundsvall.casemanagement.util.Constants.MINI_SEWAGE_SVC_DTO;
import static se.sundsvall.casemanagement.util.Constants.PHOSPHORUS_TRAP_SVC_DTO;
import static se.sundsvall.casemanagement.util.Constants.SAND_FILTER_SVC_DTO;
import static se.sundsvall.casemanagement.util.Constants.SEPTIC_TANK_SVC_DTO;

@Service
public class EcosService {

	private static final String VOLUME = "Volume";

	private static final Logger log = LoggerFactory.getLogger(EcosService.class);

	private final CaseMappingService caseMappingService;

	private final PartyService partyService;

	private final MinutMiljoClient minutMiljoClient;

	private final MinutMiljoClientV2 minutMiljoClientV2;

	private final FbService fbService;

	private final RiskClassService riskClassService;

	public EcosService(final CaseMappingService caseMappingService, final PartyService partyService, final MinutMiljoClient minutMiljoClient, final MinutMiljoClientV2 minutMiljoClientV2, final FbService fbService, final RiskClassService riskClassService) {
		this.caseMappingService = caseMappingService;
		this.partyService = partyService;
		this.minutMiljoClient = minutMiljoClient;
		this.minutMiljoClientV2 = minutMiljoClientV2;
		this.fbService = fbService;
		this.riskClassService = riskClassService;
	}

	@NotNull
	private static String getFilename(final AttachmentDTO attachmentDTO) {
		// Filename must end with extension for the preview in Ecos to work
		String filename = attachmentDTO.getName().toLowerCase();
		final String extension = attachmentDTO.getExtension().toLowerCase();
		if (!filename.endsWith(extension)) {
			filename = attachmentDTO.getName() + extension;
		}
		return filename;
	}

	public RegisterDocumentCaseResultSvcDto postCase(final EcosCaseDTO caseInput, final String municipalityId) {

		final var eFacility = caseInput.getFacilities().getFirst();

		FbPropertyInfo propertyInfo = null;
		if ((eFacility.getAddress() != null) && (eFacility.getAddress().getPropertyDesignation() != null)) {
			// Collects this early to avoid creating something before we discover potential errors
			propertyInfo = fbService.getPropertyInfoByPropertyDesignation(eFacility.getAddress().getPropertyDesignation());
		}

		// -----> RegisterDocument
		final var registerDocumentResult = registerDocument(caseInput);
		// -----> Search party, Create party if not found and add to case
		List<Map<String, ArrayOfguid>> mapped = List.of();
		if (registerDocumentResult.getCaseId() != null) {
			mapped = partyService.findAndAddPartyToCase(caseInput, registerDocumentResult.getCaseId());
		}
		if (propertyInfo != null) {
			final String facilityGuid = switch (caseInput.getCaseType()) {
				case REGISTRERING_AV_LIVSMEDEL -> createFoodFacility(caseInput, propertyInfo, registerDocumentResult);
				case ANMALAN_INSTALLATION_VARMEPUMP, ANSOKAN_TILLSTAND_VARMEPUMP -> createHeatPumpFacility(eFacility.getExtraParameters(), propertyInfo, registerDocumentResult);
				case ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP,
					ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC,
					ANMALAN_ANDRING_AVLOPPSANLAGGNING, ANMALAN_ANDRING_AVLOPPSANORDNING -> createIndividualSewage(eFacility, propertyInfo, registerDocumentResult);
				case ANMALAN_HALSOSKYDDSVERKSAMHET -> createHealthProtectionFacility(eFacility, propertyInfo, registerDocumentResult);
				case ANMALAN_KOMPOSTERING, ANMALAN_AVHJALPANDEATGARD_FORORENING -> "";
				default -> throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "CaseType: " + caseInput.getCaseType() + " is not valid. There is a problem in the API validation.");
			};

			// -----> AddPartyToFacility
			if ((facilityGuid != null) && !CaseType.WITH_NULLABLE_FACILITY_TYPE.contains(caseInput.getCaseType())) {
				mapped.forEach(o -> addPartyToFacility(facilityGuid, o));
			}

		} else {
			if (CaseType.UPPDATERING_RISKKLASSNING.toString().equals(caseInput.getCaseType())) {
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
		caseMappingService.postCaseMapping(caseInput, registerDocumentResult.getCaseId(), SystemType.ECOS, municipalityId);
		return registerDocumentResult;
	}

	private void addPartyToFacility(final String foodFacilityGuid, final Map<String, ArrayOfguid> partyRoles) {

		partyRoles.forEach((partyId, roles) -> {
			final AddPartyToFacility addPartyToFacility = new AddPartyToFacility()
				.withModel(new AddPartyToFacilitySvcDto()
					.withFacilityId(foodFacilityGuid)
					.withPartyId(partyId)
					.withRoles(roles));
			minutMiljoClient.addPartyToFacility(addPartyToFacility);
		});
	}

	private String createFoodFacility(final EcosCaseDTO eCase, final FbPropertyInfo propertyInfo, final RegisterDocumentCaseResultSvcDto registerDocumentResult) {

		final CreateFoodFacility createFoodFacility = new CreateFoodFacility();
		final CreateFoodFacilitySvcDto createFoodFacilitySvcDto = new CreateFoodFacilitySvcDto();

		createFoodFacilitySvcDto.setAddress(getAddress(propertyInfo));
		createFoodFacilitySvcDto.setCase(registerDocumentResult.getCaseId());

		createFoodFacilitySvcDto.setEstateDesignation(new EstateSvcDto().withFnr(propertyInfo.getFnr()));
		createFoodFacilitySvcDto.setFacilityCollectionName(eCase.getFacilities().getFirst().getFacilityCollectionName());
		createFoodFacilitySvcDto.setNote(eCase.getFacilities().getFirst().getDescription());

		createFoodFacility.setCreateFoodFacilitySvcDto(createFoodFacilitySvcDto);

		final String foodFacilityGuid = minutMiljoClient.createFoodFacility(createFoodFacility).getCreateFoodFacilityResult();

		if (foodFacilityGuid != null) {
			log.debug("FoodFacility created: {}", foodFacilityGuid);
			return foodFacilityGuid;
		}
		throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "FoodFacility could not be created.");
	}

	private String createHeatPumpFacility(final Map<String, String> facilityExtraParameters, final FbPropertyInfo propertyInfo, final RegisterDocumentCaseResultSvcDto registerDocumentResult) {

		final CreateHeatPumpFacility createHeatPumpFacility = new CreateHeatPumpFacility();
		final CreateHeatPumpFacilitySvcDto createHeatPumpFacilitySvcDto;

		final String airPrefix = CREATE_AIR_HEATING_FACILITY_SVC_DTO_PREFIX;
		final String geoThermalPrefix = CREATE_GEOTHERMAL_HEATING_FACILITY_SVC_DTO_PREFIX;
		final String soilPrefix = CREATE_SOIL_HEATING_FACILITY_SVC_DTO_PREFIX;
		final String marinePrefix = CREATE_MARINE_HEATING_FACILITY_SVC_DTO_PREFIX;

		if ((facilityExtraParameters == null) || facilityExtraParameters.isEmpty()) {
			log.info("facilityExtraParameters was null or empty, do not create facility. Return null.");
			return null;
		}
		if (facilityExtraParameters.keySet().stream().anyMatch(s -> s.startsWith(airPrefix))) {
			createHeatPumpFacilitySvcDto = getAirHeatingFacility(facilityExtraParameters, propertyInfo, registerDocumentResult);
		} else if (facilityExtraParameters.keySet().stream().anyMatch(s -> s.startsWith(geoThermalPrefix))) {
			createHeatPumpFacilitySvcDto = getGeoThermalHeatingFacility(facilityExtraParameters, propertyInfo, registerDocumentResult);
		} else if (facilityExtraParameters.keySet().stream().anyMatch(s -> s.startsWith(soilPrefix))) {
			createHeatPumpFacilitySvcDto = getSoilHeatingFacility(facilityExtraParameters, propertyInfo, registerDocumentResult);
		} else if (facilityExtraParameters.keySet().stream().anyMatch(s -> s.startsWith(marinePrefix))) {
			createHeatPumpFacilitySvcDto = getMarineHeatingFacility(facilityExtraParameters, propertyInfo, registerDocumentResult);
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

	private CreateHeatPumpFacilityWithHeatTransferFluidSvcDto getGeoThermalHeatingFacility(final Map<String, String> extraParameters, final FbPropertyInfo propertyInfo, final RegisterDocumentCaseResultSvcDto registerDocumentResult) {
		final CreateGeothermalHeatingFacilitySvcDto createHeatPumpFacilityWithHeatTransferFluidSvcDto = new CreateGeothermalHeatingFacilitySvcDto();

		setHeatPumpStandardParams(extraParameters, propertyInfo, registerDocumentResult, createHeatPumpFacilityWithHeatTransferFluidSvcDto, "CreateGeothermalHeatingFacilitySvcDto_");
		setHeatPumpFluidStandardParams(extraParameters, "CreateGeothermalHeatingFacilitySvcDto_", createHeatPumpFacilityWithHeatTransferFluidSvcDto);

		createHeatPumpFacilityWithHeatTransferFluidSvcDto.setBoreholes(getBoreHoles(extraParameters));

		return createHeatPumpFacilityWithHeatTransferFluidSvcDto;
	}

	private ArrayOfBoreholeSvcDto getBoreHoles(final Map<String, String> extraParameters) {
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

	private LocationSvcDto getLocation(final Map<String, String> extraParameters, final String prefix) {
		final String locationPrefix = "LocationSvcDto_";
		final LocationSvcDto locationSvcDto = new LocationSvcDto();
		locationSvcDto.setE(CaseUtil.parseDouble(extraParameters.get(prefix + locationPrefix + "E")));
		locationSvcDto.setN(CaseUtil.parseDouble(extraParameters.get(prefix + locationPrefix + "N")));
		locationSvcDto.setMeasured(CaseUtil.parseBoolean(extraParameters.get(prefix + locationPrefix + "Measured")));

		return locationSvcDto;
	}

	private CreateSoilHeatingFacilitySvcDto getSoilHeatingFacility(final Map<String, String> extraParameters, final FbPropertyInfo propertyInfo, final RegisterDocumentCaseResultSvcDto registerDocumentResult) {
		final CreateSoilHeatingFacilitySvcDto createSoilHeatingFacilitySvcDto = new CreateSoilHeatingFacilitySvcDto();

		setHeatPumpStandardParams(extraParameters, propertyInfo, registerDocumentResult, createSoilHeatingFacilitySvcDto, "CreateSoilHeatingFacilitySvcDto_");
		setHeatPumpFluidStandardParams(extraParameters, "CreateSoilHeatingFacilitySvcDto_", createSoilHeatingFacilitySvcDto);

		createSoilHeatingFacilitySvcDto.setHeatCollectorTubes(getHeatCollectorTubes(extraParameters));

		return createSoilHeatingFacilitySvcDto;
	}

	private ArrayOfHeatCollectorTubeSvcDto getHeatCollectorTubes(final Map<String, String> extraParameters) {
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

	private CreateMarineHeatingFacilitySvcDto getMarineHeatingFacility(final Map<String, String> extraParameters, final FbPropertyInfo propertyInfo, final RegisterDocumentCaseResultSvcDto registerDocumentResult) {
		final CreateMarineHeatingFacilitySvcDto createMarineHeatingFacilitySvcDto = new CreateMarineHeatingFacilitySvcDto();

		setHeatPumpStandardParams(extraParameters, propertyInfo, registerDocumentResult, createMarineHeatingFacilitySvcDto, "CreateMarineHeatingFacilitySvcDto_");
		setHeatPumpFluidStandardParams(extraParameters, "CreateMarineHeatingFacilitySvcDto_", createMarineHeatingFacilitySvcDto);
		createMarineHeatingFacilitySvcDto.setHeatCollectorTubes(getHeatCollectorTubes(extraParameters));

		return createMarineHeatingFacilitySvcDto;
	}

	private CreateAirHeatingFacilitySvcDto getAirHeatingFacility(final Map<String, String> extraParameters, final FbPropertyInfo propertyInfo, final RegisterDocumentCaseResultSvcDto registerDocumentResult) {
		final CreateAirHeatingFacilitySvcDto createAirHeatingFacilitySvcDto = new CreateAirHeatingFacilitySvcDto();

		setHeatPumpStandardParams(extraParameters, propertyInfo, registerDocumentResult, createAirHeatingFacilitySvcDto, "CreateAirHeatingFacilitySvcDto_");

		return createAirHeatingFacilitySvcDto;
	}

	private void setHeatPumpFluidStandardParams(final Map<String, String> extraParameters, final String prefix, final CreateHeatPumpFacilityWithHeatTransferFluidSvcDto svcDto) {
		svcDto.setCapacity(CaseUtil.parseDouble(extraParameters.get(prefix + "Capacity")));
		svcDto.setHeatTransferFluidId(CaseUtil.parseString(extraParameters.get(prefix + "HeatTransferFluidId")));
	}

	private void setHeatPumpStandardParams(final Map<String, String> extraParameters, final FbPropertyInfo propertyInfo, final RegisterDocumentCaseResultSvcDto registerDocumentResult, final CreateHeatPumpFacilitySvcDto svcDto, final String prefix) {
		svcDto.setAddress(getAddress(propertyInfo));
		svcDto.setFacilityStatusId(Constants.ECOS_FACILITY_STATUS_ID_ANMALD_ANSOKT);
		svcDto.setCreatedFromCaseId(registerDocumentResult.getCaseId());
		svcDto.setEstate(new EstateSvcDto().withFnr(propertyInfo.getFnr()));

		svcDto.setManufacturer(CaseUtil.parseString(extraParameters.get(prefix + "Manufacturer")));
		svcDto.setModel(CaseUtil.parseString(extraParameters.get(prefix + "Model")));
		svcDto.setPowerConsumption(CaseUtil.parseDouble(extraParameters.get(prefix + "PowerConsumption")));
		svcDto.setPowerOutput(CaseUtil.parseDouble(extraParameters.get(prefix + "PowerOutput")));
	}

	private String createIndividualSewage(final FacilityDTO eFacility, final FbPropertyInfo propertyInfo, final RegisterDocumentCaseResultSvcDto registerDocumentResult) {

		final CreateIndividualSewageFacility createIndividualSewageFacility = new CreateIndividualSewageFacility();
		final CreateIndividualSewageFacilitySvcDto createIndividualSewageFacilitySvcDto = new CreateIndividualSewageFacilitySvcDto();

		createIndividualSewageFacilitySvcDto.setAddress(getAddress(propertyInfo));

		createIndividualSewageFacilitySvcDto.setFacilityStatusId(Constants.ECOS_FACILITY_STATUS_ID_ANMALD_ANSOKT);
		createIndividualSewageFacilitySvcDto.setCreatedFromCaseId(registerDocumentResult.getCaseId());
		createIndividualSewageFacilitySvcDto.setEstate(new EstateSvcDto().withFnr(propertyInfo.getFnr()));

		createIndividualSewageFacilitySvcDto.setNote(eFacility.getDescription());
		createIndividualSewageFacilitySvcDto.setOnGrantLand(CaseUtil.parseBoolean(eFacility.getExtraParameters().get("OnGrantLand")));
		createIndividualSewageFacilitySvcDto.setProtectionLevelApprovedEnvironmentId(eFacility.getExtraParameters().get("ProtectionLevelApprovedEnvironmentId"));
		createIndividualSewageFacilitySvcDto.setProtectionLevelApprovedHealthId(eFacility.getExtraParameters().get("ProtectionLevelApprovedHealthId"));
		createIndividualSewageFacilitySvcDto.setWastewaterApprovedForId(eFacility.getExtraParameters().get("WastewaterApprovedForId"));
		createIndividualSewageFacilitySvcDto.setWasteWaterInboundId(eFacility.getExtraParameters().get("WasteWaterInboundId"));
		createIndividualSewageFacilitySvcDto.setAccommodationTypeId(eFacility.getExtraParameters().get("AccommodationTypeId"));

		createIndividualSewageFacilitySvcDto.setPurificationSteps(getPurificationSteps(eFacility.getExtraParameters()));

		createIndividualSewageFacility.setCreateIndividualSewageSvcDto(createIndividualSewageFacilitySvcDto);

		final String facilityGuid = minutMiljoClient.createIndividualSewageFacility(createIndividualSewageFacility).getCreateIndividualSewageFacilityResult();

		if (facilityGuid != null) {
			log.debug("Individual Sewage created: {}", facilityGuid);
			return facilityGuid;
		}
		throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "Individual Sewage could not be created");
	}

	private FacilityAddressSvcDto getAddress(final FbPropertyInfo propertyInfo) {
		if (propertyInfo.getAdressplatsId() != null) {
			final FacilityAddressSvcDto facilityAddressSvcDto = new FacilityAddressSvcDto();
			facilityAddressSvcDto.setAdressPlatsId(propertyInfo.getAdressplatsId());
			return facilityAddressSvcDto;
		}
		return null;
	}

	private ArrayOfPurificationStepSvcDto getPurificationSteps(final Map<String, String> extraParameters) {

		final ArrayOfPurificationStepSvcDto arrayOfPurificationStepSvcDto = new ArrayOfPurificationStepSvcDto();

		if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(SEPTIC_TANK_SVC_DTO))) {
			arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getSepticTankSvcDto(extraParameters));
		}
		if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(INFILTRATION_PLANT_SVC_DTO))) {
			arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getInfiltrationPlantSvcDto(extraParameters));
		}
		if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(CLOSED_TANK_SVC_DTO))) {
			arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getClosedTankSvcDto(extraParameters));
		}
		if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(DRY_SOLUTION_SVC_DTO))) {
			arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getDrySolutionSvcDto(extraParameters));
		}
		if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(MINI_SEWAGE_SVC_DTO))) {
			arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getMiniSewageSvcDto(extraParameters));
		}
		if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(FILTER_BED_SVC_DTO))) {
			arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getFilterBedSvcDto(extraParameters));
		}
		if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(SAND_FILTER_SVC_DTO))) {
			arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getSandFilterSvcDto(extraParameters));
		}
		if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(BIOLOGICAL_STEP_SVC_DTO))) {
			arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getBiologicalStepSvcDto(extraParameters));
		}
		if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(PHOSPHORUS_TRAP_SVC_DTO))) {
			arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getPhosphorusTrapSvcDto(extraParameters));
		}
		if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(CHEMICAL_PRETREATMENT_SVC_DTO))) {
			arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getChemicalPretreatmentSvcDto(extraParameters));
		}

		return arrayOfPurificationStepSvcDto;
	}

	private SepticTankSvcDto getSepticTankSvcDto(final Map<String, String> extraParameters) {
		final SepticTankSvcDto svcDto = new SepticTankSvcDto();

		setStandardParams(extraParameters, Constants.SEPTIC_TANK_SVC_DTO, svcDto);

		svcDto.setEmptyingInterval(CaseUtil.parseInteger(extraParameters.get(Constants.SEPTIC_TANK_SVC_DTO + "EmptyingInterval")));
		svcDto.setHasCeMarking(CaseUtil.parseBoolean(extraParameters.get(Constants.SEPTIC_TANK_SVC_DTO + "HasCeMarking")));
		svcDto.setHasTPipe(CaseUtil.parseBoolean(extraParameters.get(Constants.SEPTIC_TANK_SVC_DTO + "HasTPipe")));
		svcDto.setVolume(CaseUtil.parseDouble(extraParameters.get(Constants.SEPTIC_TANK_SVC_DTO + VOLUME)));

		return svcDto;
	}

	private ClosedTankSvcDto getClosedTankSvcDto(final Map<String, String> extraParameters) {
		final ClosedTankSvcDto svcDto = new ClosedTankSvcDto();

		setStandardParams(extraParameters, CLOSED_TANK_SVC_DTO, svcDto);

		svcDto.setEmptyingInterval(CaseUtil.parseInteger(extraParameters.get(CLOSED_TANK_SVC_DTO + "EmptyingInterval")));
		svcDto.setHasCeMarking(CaseUtil.parseBoolean(extraParameters.get(CLOSED_TANK_SVC_DTO + "HasCeMarking")));
		svcDto.setVolume(CaseUtil.parseDouble(extraParameters.get(CLOSED_TANK_SVC_DTO + VOLUME)));

		return svcDto;
	}

	private MiniSewageSvcDto getMiniSewageSvcDto(final Map<String, String> extraParameters) {
		final MiniSewageSvcDto svcDto = new MiniSewageSvcDto();

		setStandardParams(extraParameters, MINI_SEWAGE_SVC_DTO, svcDto);

		svcDto.setCeMarking(CaseUtil.parseBoolean(extraParameters.get(MINI_SEWAGE_SVC_DTO + "CeMarking")));
		svcDto.setManufacturer(CaseUtil.parseString(extraParameters.get(MINI_SEWAGE_SVC_DTO + "Manufacturer")));
		svcDto.setModel(CaseUtil.parseString(extraParameters.get(MINI_SEWAGE_SVC_DTO + "Model")));

		return svcDto;
	}

	private FilterBedSvcDto getFilterBedSvcDto(final Map<String, String> extraParameters) {
		final FilterBedSvcDto svcDto = new FilterBedSvcDto();

		setStandardParams(extraParameters, FILTER_BED_SVC_DTO, svcDto);
		svcDto.setVolume(CaseUtil.parseDouble(extraParameters.get(FILTER_BED_SVC_DTO + VOLUME)));

		return svcDto;
	}

	private SandFilterSvcDto getSandFilterSvcDto(final Map<String, String> extraParameters) {
		final SandFilterSvcDto svcDto = new SandFilterSvcDto();

		setStandardParams(extraParameters, SAND_FILTER_SVC_DTO, svcDto);

		svcDto.setArea(CaseUtil.parseInteger(extraParameters.get(SAND_FILTER_SVC_DTO + "Area")));
		svcDto.setElevated(CaseUtil.parseBoolean(extraParameters.get(SAND_FILTER_SVC_DTO + "Elevated")));
		svcDto.setWaterTight(CaseUtil.parseBoolean(extraParameters.get(SAND_FILTER_SVC_DTO + "WaterTight")));

		return svcDto;
	}

	private BiologicalStepSvcDto getBiologicalStepSvcDto(final Map<String, String> extraParameters) {
		final BiologicalStepSvcDto svcDto = new BiologicalStepSvcDto();

		setStandardParams(extraParameters, BIOLOGICAL_STEP_SVC_DTO, svcDto);

		svcDto.setArea(CaseUtil.parseInteger(extraParameters.get(BIOLOGICAL_STEP_SVC_DTO + "Area")));
		svcDto.setBiologicalStepTypeId(CaseUtil.parseString(extraParameters.get(BIOLOGICAL_STEP_SVC_DTO + "BiologicalStepTypeId")));

		return svcDto;
	}

	private DrySolutionSvcDto getDrySolutionSvcDto(final Map<String, String> extraParameters) {
		final DrySolutionSvcDto svcDto = new DrySolutionSvcDto();

		setStandardParams(extraParameters, DRY_SOLUTION_SVC_DTO, svcDto);

		svcDto.setCompostProductName(CaseUtil.parseString(extraParameters.get(DRY_SOLUTION_SVC_DTO + "CompostProductName")));
		svcDto.setDrySolutionCompostTypeId(CaseUtil.parseString(extraParameters.get(DRY_SOLUTION_SVC_DTO + "DrySolutionCompostTypeId")));
		svcDto.setDrySolutionTypeId(CaseUtil.parseString(extraParameters.get(DRY_SOLUTION_SVC_DTO + "DrySolutionTypeId")));
		svcDto.setNoContOrCompt(CaseUtil.parseString(extraParameters.get(DRY_SOLUTION_SVC_DTO + "NoContOrCompt")));
		svcDto.setNoLPerContOrCompt(CaseUtil.parseString(extraParameters.get(DRY_SOLUTION_SVC_DTO + "NoLPerContOrCompt")));
		svcDto.setToiletProductName(CaseUtil.parseString(extraParameters.get(DRY_SOLUTION_SVC_DTO + "ToiletProductName")));
		svcDto.setVolume(CaseUtil.parseDouble(extraParameters.get(DRY_SOLUTION_SVC_DTO + VOLUME)));

		return svcDto;
	}

	private InfiltrationPlantSvcDto getInfiltrationPlantSvcDto(final Map<String, String> extraParameters) {
		final InfiltrationPlantSvcDto svcDto = new InfiltrationPlantSvcDto();

		setStandardParams(extraParameters, INFILTRATION_PLANT_SVC_DTO, svcDto);

		svcDto.setElevated(CaseUtil.parseBoolean(extraParameters.get(INFILTRATION_PLANT_SVC_DTO + "Elevated")));
		svcDto.setReinforced(CaseUtil.parseBoolean(extraParameters.get(INFILTRATION_PLANT_SVC_DTO + "Reinforced")));
		svcDto.setIsModuleSystem(CaseUtil.parseBoolean(extraParameters.get(INFILTRATION_PLANT_SVC_DTO + "IsModuleSystem")));
		svcDto.setSpreadLinesCount(CaseUtil.parseInteger(extraParameters.get(INFILTRATION_PLANT_SVC_DTO + "SpreadLinesCount")));
		svcDto.setSpreadLinesLength(CaseUtil.parseInteger(extraParameters.get(INFILTRATION_PLANT_SVC_DTO + "SpreadLinesLength")));

		return svcDto;
	}

	private PhosphorusTrapSvcDto getPhosphorusTrapSvcDto(final Map<String, String> extraParameters) {
		final PhosphorusTrapSvcDto svcDto = new PhosphorusTrapSvcDto();
		setStandardParams(extraParameters, PHOSPHORUS_TRAP_SVC_DTO, svcDto);
		return svcDto;
	}

	private ChemicalPretreatmentSvcDto getChemicalPretreatmentSvcDto(final Map<String, String> extraParameters) {
		final ChemicalPretreatmentSvcDto svcDto = new ChemicalPretreatmentSvcDto();
		setStandardParams(extraParameters, CHEMICAL_PRETREATMENT_SVC_DTO, svcDto);
		return svcDto;
	}

	private void setStandardParams(final Map<String, String> extraParameters, final String prefix, final PurificationStepSvcDto svcDto) {
		svcDto.setPurificationStepFacilityStatusId(Constants.ECOS_FACILITY_STATUS_ID_ANMALD_ANSOKT);
		svcDto.setInstallationDate(CaseUtil.parseLocalDateTime(extraParameters.get(prefix + "InstallationDate")));
		svcDto.setHasOverflowAlarm(CaseUtil.parseBoolean(extraParameters.get(prefix + "HasOverflowAlarm")));
		svcDto.setLifeTime(CaseUtil.parseInteger(extraParameters.get(prefix + "LifeTime")));
		svcDto.setPersonCapacity(CaseUtil.parseInteger(extraParameters.get(prefix + "PersonCapacity")));
		svcDto.setStepNumber(CaseUtil.parseInteger(extraParameters.get(prefix + "StepNumber")));
		svcDto.setPurificationStepLocation(null);
	}

	public RegisterDocumentCaseResultSvcDto registerDocument(final EcosCaseDTO eCase) {
		final var registerDocumentCaseSvcDtoV2 = new RegisterDocumentCaseSvcDtoV2();
		final var registerDocument = new RegisterDocument();
		registerDocumentCaseSvcDtoV2.setOccurrenceTypeId(Constants.ECOS_OCCURENCE_TYPE_ID_ANMALAN);
		registerDocumentCaseSvcDtoV2.setHandlingOfficerGroupId(Constants.ECOS_HANDLING_OFFICER_GROUP_ID_EXPEDITIONEN);
		registerDocumentCaseSvcDtoV2.setDiaryPlanId(getDiaryPlanId(eCase.getCaseType()));
		registerDocumentCaseSvcDtoV2.setProcessTypeId(getProcessTypeId(eCase.getCaseType()));
		registerDocumentCaseSvcDtoV2.setDocuments(getArrayOfDocumentSvcDtoV2(eCase.getAttachments()));

		final var eFacility = eCase.getFacilities().getFirst();

		final var fixedFacilityType = Optional.ofNullable(Optional.ofNullable(eCase.getExtraParameters()).orElse(Map.of()).get("fixedFacilityType")).orElse("").trim();

		final var propertyDesignation = Optional.ofNullable(Optional.ofNullable(eFacility.getAddress()).orElse(new AddressDTO()).getPropertyDesignation()).orElse("").trim().toUpperCase();

		if (!fixedFacilityType.isEmpty()) {
			registerDocumentCaseSvcDtoV2.setCaseSubtitle(fixedFacilityType);
			registerDocumentCaseSvcDtoV2.setCaseSubtitleFree(propertyDesignation);
		} else {
			var freeSubtitle = Optional.ofNullable(eFacility.getFacilityCollectionName()).orElse("").trim();
			if (!propertyDesignation.isEmpty()) {
				if (freeSubtitle.isEmpty()) {
					freeSubtitle = propertyDesignation;
				}
				freeSubtitle = String.join(", ", freeSubtitle, propertyDesignation);
			}
			registerDocumentCaseSvcDtoV2.setCaseSubtitleFree(freeSubtitle);
		}
		registerDocument.setRegisterDocumentCaseSvcDto(registerDocumentCaseSvcDtoV2);
		final var registerDocumentResult = minutMiljoClientV2.registerDocumentV2(registerDocument).getRegisterDocumentResult();

		if (registerDocumentResult == null) {
			throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "Case could not be created.");
		}
		log.debug("Case created with ByggR case number: {}", registerDocumentResult.getCaseNumber());
		return registerDocumentResult;
	}

	private String getDiaryPlanId(final String caseType) {
		return switch (CaseType.valueOf(caseType)) {
			case REGISTRERING_AV_LIVSMEDEL, UPPDATERING_RISKKLASSNING -> Constants.ECOS_DIARY_PLAN_LIVSMEDEL;
			case ANMALAN_ANDRING_AVLOPPSANLAGGNING, ANMALAN_ANDRING_AVLOPPSANORDNING,
				ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC,
				ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, ANMALAN_INSTALLATION_VARMEPUMP,
				ANSOKAN_TILLSTAND_VARMEPUMP,
				ANMALAN_KOMPOSTERING, ANMALAN_AVHJALPANDEATGARD_FORORENING -> Constants.ECOS_DIARY_PLAN_AVLOPP;
			case ANMALAN_HALSOSKYDDSVERKSAMHET -> Constants.ECOS_DIARY_PLAN_HALSOSKYDD;
			default -> null;
		};
	}

	private String getProcessTypeId(final String caseType) {

		return switch (CaseType.valueOf(caseType)) {
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

	private ArrayOfDocumentSvcDto getArrayOfDocumentSvcDto(final List<AttachmentDTO> attachmentDTOList) {
		final ArrayOfDocumentSvcDto arrayOfDocumentSvcDto = new ArrayOfDocumentSvcDto();
		for (final AttachmentDTO a : attachmentDTOList) {
			final var documentSvcDto = new DocumentSvcDto()
				.withContentType(a.getMimeType() != null ? a.getMimeType().toLowerCase() : null)
				.withData(Base64.getDecoder().decode(a.getFile().getBytes()))
				.withDocumentTypeId(AttachmentCategory.fromCode(a.getCategory()).getDescription())
				.withFilename(getFilename(a))
				.withNote(a.getNote());
			arrayOfDocumentSvcDto.getDocumentSvcDto().add(documentSvcDto);
		}
		return arrayOfDocumentSvcDto;
	}

	private minutmiljoV2.ArrayOfDocumentSvcDto getArrayOfDocumentSvcDtoV2(final List<AttachmentDTO> attachmentDTOList) {
		final minutmiljoV2.ArrayOfDocumentSvcDto arrayOfDocumentSvcDto = new minutmiljoV2.ArrayOfDocumentSvcDto();
		for (final AttachmentDTO a : attachmentDTOList) {

			final var documentSvcDto = new minutmiljoV2.DocumentSvcDto()
				.withContentType(a.getMimeType() != null ? a.getMimeType().toLowerCase() : null)
				.withData(Base64.getDecoder().decode(a.getFile().getBytes()))
				.withDocumentTypeId(AttachmentCategory.fromCode(a.getCategory()).getDescription())
				.withFilename(getFilename(a))
				.withNote(a.getNote());

			arrayOfDocumentSvcDto.getDocumentSvcDto().add(documentSvcDto);
		}
		return arrayOfDocumentSvcDto;
	}

	private void createOccurrenceOnCase(final String caseId) {

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
	public CaseStatusDTO getStatus(final String caseId, final String externalCaseId, final String municipalityId) {

		final var getCase = new GetCase().withCaseId(caseId);

		final var ecosCaseOptional = Optional.ofNullable(minutMiljoClient.getCase(getCase));

		if (ecosCaseOptional.isPresent()) {
			final var ecosCase = ecosCaseOptional.get().getGetCaseResult();

			if (Optional.ofNullable(ecosCase.getOccurrences())
				.flatMap(occurrences -> Optional.ofNullable(occurrences.getOccurrenceListItemSvcDto()))
				.filter(list -> !list.isEmpty())
				.isPresent()) {

				final var caseMapping = Optional.ofNullable(caseMappingService.getCaseMapping(externalCaseId, caseId, municipalityId).getFirst()).orElse(new CaseMapping());

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
		}
		throw Problem.valueOf(Status.NOT_FOUND, Constants.ERR_MSG_STATUS_NOT_FOUND);
	}

	public List<CaseStatusDTO> getEcosStatusByOrgNr(final String organizationNumber, final String municipalityId) {
		final List<CaseStatusDTO> caseStatusDTOList = new ArrayList<>();

		// Find party both with and without prefix "16"
		final ArrayOfPartySvcDto allParties = partyService.searchPartyByOrganizationNumber(organizationNumber);

		// Search Ecos Case
		if ((allParties.getPartySvcDto() != null) && !allParties.getPartySvcDto().isEmpty()) {

			final ArrayOfSearchCaseResultSvcDto caseResult = new ArrayOfSearchCaseResultSvcDto();

			allParties.getPartySvcDto().forEach(party -> caseResult.getSearchCaseResultSvcDto().addAll(searchCase(party.getId()).getSearchCaseResultSvcDto()));

			// Remove eventual duplicates
			final var caseResultWithoutDuplicates = caseResult.getSearchCaseResultSvcDto().stream().distinct().toList();

			caseResultWithoutDuplicates.forEach(ecosCase -> {
				final List<CaseMapping> caseMappingList = caseMappingService.getCaseMapping(null, ecosCase.getCaseId(), municipalityId);
				final String externalCaseId = caseMappingList.isEmpty() ? null : caseMappingList.getFirst().getExternalCaseId();
				final CaseStatusDTO caseStatusDTO = getStatus(ecosCase.getCaseId(), externalCaseId, municipalityId);

				if (caseStatusDTO != null) {
					caseStatusDTOList.add(caseStatusDTO);
				}
			});
		}

		return caseStatusDTOList;
	}

	public void addDocumentsToCase(final String caseId, final List<AttachmentDTO> attachmentDTOList) {
		final AddDocumentsToCase addDocumentsToCase = new AddDocumentsToCase();
		final AddDocumentsToCaseSvcDto message = new AddDocumentsToCaseSvcDto();
		message.setCaseId(caseId);
		message.setDocuments(getArrayOfDocumentSvcDto(attachmentDTOList));
		message.setOccurrenceTypeId(Constants.ECOS_OCCURRENCE_TYPE_ID_KOMPLETTERING);
		message.setDocumentStatusId(Constants.ECOS_DOCUMENT_STATUS_INKOMMEN);
		addDocumentsToCase.setAddDocumentToCaseSvcDto(message);

		minutMiljoClient.addDocumentsToCase(addDocumentsToCase);
	}

	private ArrayOfSearchCaseResultSvcDto searchCase(final String partyId) {
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

	private String createHealthProtectionFacility(final FacilityDTO eFacility, final FbPropertyInfo propertyInfo, final RegisterDocumentCaseResultSvcDto registerDocumentResult) {

		final CreateHealthProtectionFacility createHealthProtectionFacility = new CreateHealthProtectionFacility();
		final CreateHealthProtectionFacilitySvcDto createHealthProtectionFacilitySvcDto = new CreateHealthProtectionFacilitySvcDto();
		createHealthProtectionFacilitySvcDto.setAddress(getAddress(propertyInfo));
		createHealthProtectionFacilitySvcDto.setEstateDesignation(new EstateSvcDto().withFnr(propertyInfo.getFnr()));
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
}
