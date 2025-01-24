package se.sundsvall.casemanagement.integration.ecos;

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

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import minutmiljo.ArrayOfBoreholeSvcDto;
import minutmiljo.ArrayOfDocumentSvcDto;
import minutmiljo.ArrayOfHeatCollectorTubeSvcDto;
import minutmiljo.ArrayOfPurificationStepSvcDto;
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
import minutmiljo.HeatCollectorTubeSvcDto;
import minutmiljo.InfiltrationPlantSvcDto;
import minutmiljo.LocationSvcDto;
import minutmiljo.MiniSewageSvcDto;
import minutmiljo.PhosphorusTrapSvcDto;
import minutmiljo.PurificationStepSvcDto;
import minutmiljo.SandFilterSvcDto;
import minutmiljo.SepticTankSvcDto;
import minutmiljoV2.RegisterDocument;
import minutmiljoV2.RegisterDocumentCaseResultSvcDto;
import minutmiljoV2.RegisterDocumentCaseSvcDtoV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.api.model.EcosCaseDTO;
import se.sundsvall.casemanagement.api.model.FacilityDTO;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.integration.fb.model.FbPropertyInfo;
import se.sundsvall.casemanagement.util.CaseUtil;
import se.sundsvall.casemanagement.util.Constants;

@Component
public class EcosObjectFactory {

	private static final Logger LOG = LoggerFactory.getLogger(EcosObjectFactory.class);

	private final EcosIntegration ecosIntegration;

	public EcosObjectFactory(EcosIntegration ecosIntegration) {
		this.ecosIntegration = ecosIntegration;
	}

	public RegisterDocumentCaseResultSvcDto createDocument(final EcosCaseDTO eCase) {
		final var registerDocumentCaseSvcDtoV2 = new RegisterDocumentCaseSvcDtoV2()
			.withOccurrenceTypeId(Constants.ECOS_OCCURENCE_TYPE_ID_ANMALAN)
			.withHandlingOfficerGroupId(Constants.ECOS_HANDLING_OFFICER_GROUP_ID_EXPEDITIONEN)
			.withDiaryPlanId(getDiaryPlanId(eCase.getCaseType()))
			.withProcessTypeId(getProcessTypeId(eCase.getCaseType()))
			.withDocuments(createArrayOfDocumentSvcDtoV2(eCase.getAttachments()));

		final var eFacility = eCase.getFacilities().getFirst();

		final var fixedFacilityType = Optional.ofNullable(eCase.getExtraParameters())
			.map(params -> params.get("fixedFacilityType"))
			.orElse("")
			.trim();

		final var propertyDesignation = Optional.ofNullable(eFacility.getAddress())
			.map(AddressDTO::getPropertyDesignation)
			.orElse("")
			.trim()
			.toUpperCase();

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

		final var registerDocument = new RegisterDocument()
			.withRegisterDocumentCaseSvcDto(registerDocumentCaseSvcDtoV2);

		return ecosIntegration.registerDocumentV2(registerDocument);
	}

	public String createFoodFacility(final EcosCaseDTO eCase, final FbPropertyInfo propertyInfo, final RegisterDocumentCaseResultSvcDto registerDocumentResult) {
		final var createFoodFacilitySvcDto = new CreateFoodFacilitySvcDto()
			.withAddress(createAddress(propertyInfo))
			.withCase(registerDocumentResult.getCaseId())
			.withEstateDesignation(new EstateSvcDto().withFnr(propertyInfo.getFnr()))
			.withFacilityCollectionName(eCase.getFacilities().getFirst().getFacilityCollectionName())
			.withNote(eCase.getFacilities().getFirst().getDescription());

		final var createFoodFacility = new CreateFoodFacility()
			.withCreateFoodFacilitySvcDto(createFoodFacilitySvcDto);

		return ecosIntegration.createFoodFacility(createFoodFacility);
	}

	public String createHeatPumpFacility(final Map<String, String> facilityExtraParameters, final FbPropertyInfo propertyInfo, final RegisterDocumentCaseResultSvcDto registerDocumentResult) {
		if ((facilityExtraParameters == null) || facilityExtraParameters.isEmpty()) {
			LOG.info("facilityExtraParameters was null or empty, do not create facility. Return null.");
			return null;
		}

		final CreateHeatPumpFacilitySvcDto createHeatPumpFacilitySvcDto;

		if (facilityExtraParameters.keySet().stream().anyMatch(s -> s.startsWith(CREATE_AIR_HEATING_FACILITY_SVC_DTO_PREFIX))) {
			createHeatPumpFacilitySvcDto = createAirHeatingFacility(facilityExtraParameters, propertyInfo, registerDocumentResult);
		} else if (facilityExtraParameters.keySet().stream().anyMatch(s -> s.startsWith(CREATE_GEOTHERMAL_HEATING_FACILITY_SVC_DTO_PREFIX))) {
			createHeatPumpFacilitySvcDto = createGeoThermalHeatingFacility(facilityExtraParameters, propertyInfo, registerDocumentResult);
		} else if (facilityExtraParameters.keySet().stream().anyMatch(s -> s.startsWith(CREATE_SOIL_HEATING_FACILITY_SVC_DTO_PREFIX))) {
			createHeatPumpFacilitySvcDto = createSoilHeatingFacility(facilityExtraParameters, propertyInfo, registerDocumentResult);
		} else if (facilityExtraParameters.keySet().stream().anyMatch(s -> s.startsWith(CREATE_MARINE_HEATING_FACILITY_SVC_DTO_PREFIX))) {
			createHeatPumpFacilitySvcDto = createMarineHeatingFacility(facilityExtraParameters, propertyInfo, registerDocumentResult);
		} else {
			throw Problem.valueOf(Status.BAD_REQUEST, MessageFormat.format("The request does not contain any extraParameters on the facility-object with prefix: \"{0}\", \"{1}\"\", \"{2}\"\" or \"{3}\"",
				CREATE_AIR_HEATING_FACILITY_SVC_DTO_PREFIX, CREATE_GEOTHERMAL_HEATING_FACILITY_SVC_DTO_PREFIX, CREATE_SOIL_HEATING_FACILITY_SVC_DTO_PREFIX, CREATE_MARINE_HEATING_FACILITY_SVC_DTO_PREFIX));
		}

		final var createHeatPumpFacility = new CreateHeatPumpFacility()
			.withCreateIndividualSewageSvcDto(createHeatPumpFacilitySvcDto);

		return ecosIntegration.createHeatPumpFacility(createHeatPumpFacility);
	}

	public String createIndividualSewage(final FacilityDTO eFacility, final FbPropertyInfo propertyInfo, final RegisterDocumentCaseResultSvcDto registerDocumentResult) {
		final var createIndividualSewageFacilitySvcDto = new CreateIndividualSewageFacilitySvcDto()
			.withAddress(createAddress(propertyInfo))
			.withFacilityStatusId(Constants.ECOS_FACILITY_STATUS_ID_ANMALD_ANSOKT)
			.withCreatedFromCaseId(registerDocumentResult.getCaseId())
			.withEstate(new EstateSvcDto().withFnr(propertyInfo.getFnr()))
			.withNote(eFacility.getDescription())
			.withOnGrantLand(CaseUtil.parseBoolean(eFacility.getExtraParameters().get("OnGrantLand")))
			.withProtectionLevelApprovedEnvironmentId(eFacility.getExtraParameters().get("ProtectionLevelApprovedEnvironmentId"))
			.withProtectionLevelApprovedHealthId(eFacility.getExtraParameters().get("ProtectionLevelApprovedHealthId"))
			.withWastewaterApprovedForId(eFacility.getExtraParameters().get("WastewaterApprovedForId"))
			.withWasteWaterInboundId(eFacility.getExtraParameters().get("WasteWaterInboundId"))
			.withAccommodationTypeId(eFacility.getExtraParameters().get("AccommodationTypeId"))
			.withPurificationSteps(createPurificationSteps(eFacility.getExtraParameters()));
		final var createIndividualSewageFacility = new CreateIndividualSewageFacility()
			.withCreateIndividualSewageSvcDto(createIndividualSewageFacilitySvcDto);

		return ecosIntegration.createIndividualSewageFacility(createIndividualSewageFacility);
	}

	public String createHealthProtectionFacility(final FacilityDTO eFacility, final FbPropertyInfo propertyInfo, final RegisterDocumentCaseResultSvcDto registerDocumentResult) {
		final var createHealthProtectionFacilitySvcDto = new CreateHealthProtectionFacilitySvcDto()
			.withAddress(createAddress(propertyInfo))
			.withEstateDesignation(new EstateSvcDto().withFnr(propertyInfo.getFnr()))
			.withCase(registerDocumentResult.getCaseId())
			.withNote(eFacility.getDescription())
			.withFacilityCollectionName(eFacility.getFacilityCollectionName());
		final var createHealthProtectionFacility = new CreateHealthProtectionFacility()
			.withCreateHealthProtectionFacilitySvcDto(createHealthProtectionFacilitySvcDto);

		return ecosIntegration.createHealthProtectionFacility(createHealthProtectionFacility);
	}

	public void createOccurrenceOnCase(final String caseId) {
		final CreateOccurrenceOnCase createOccurrenceOnCase = new CreateOccurrenceOnCase();
		final CreateOccurrenceOnCaseSvcDto createOccurrenceOnCaseSvcDto = new CreateOccurrenceOnCaseSvcDto();
		createOccurrenceOnCaseSvcDto.setCaseId(caseId);
		createOccurrenceOnCaseSvcDto.setOccurrenceDate(LocalDateTime.now());
		createOccurrenceOnCaseSvcDto.setOccurrenceTypeId(Constants.ECOS_OCCURRENCE_TYPE_ID_INFO_FRAN_ETJANST);
		createOccurrenceOnCaseSvcDto.setNote(Constants.ECOS_OCCURENCE_TEXT_MOBIL_ANLAGGNING);
		createOccurrenceOnCase.setCreateOccurrenceOnCaseSvcDto(createOccurrenceOnCaseSvcDto);

		ecosIntegration.createOccurrenceOnCase(createOccurrenceOnCase);
	}

	public ArrayOfDocumentSvcDto createArrayOfDocumentSvcDto(final List<AttachmentDTO> attachments) {
		final ArrayOfDocumentSvcDto arrayOfDocumentSvcDto = new ArrayOfDocumentSvcDto();
		for (final var attachment : attachments) {
			final var documentSvcDto = new DocumentSvcDto()
				.withContentType(attachment.getMimeType() != null ? attachment.getMimeType().toLowerCase() : null)
				.withData(Base64.getDecoder().decode(attachment.getFile().getBytes()))
				.withDocumentTypeId(AttachmentCategory.fromCode(attachment.getCategory()).getDescription())
				.withFilename(getFilename(attachment))
				.withNote(attachment.getNote());
			arrayOfDocumentSvcDto.getDocumentSvcDto().add(documentSvcDto);
		}
		return arrayOfDocumentSvcDto;
	}

	ArrayOfPurificationStepSvcDto createPurificationSteps(final Map<String, String> extraParameters) {
		final var arrayOfPurificationStepSvcDto = new ArrayOfPurificationStepSvcDto();

		extraParameters.keySet().forEach(key -> {
			if (key.startsWith(SEPTIC_TANK_SVC_DTO)) {
				arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(createSepticTankSvcDto(extraParameters));
			} else if (key.startsWith(INFILTRATION_PLANT_SVC_DTO)) {
				arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(createInfiltrationPlantSvcDto(extraParameters));
			} else if (key.startsWith(CLOSED_TANK_SVC_DTO)) {
				arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(createClosedTankSvcDto(extraParameters));
			} else if (key.startsWith(DRY_SOLUTION_SVC_DTO)) {
				arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(createDrySolutionSvcDto(extraParameters));
			} else if (key.startsWith(MINI_SEWAGE_SVC_DTO)) {
				arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(createMiniSewageSvcDto(extraParameters));
			} else if (key.startsWith(FILTER_BED_SVC_DTO)) {
				arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(createFilterBedSvcDto(extraParameters));
			} else if (key.startsWith(SAND_FILTER_SVC_DTO)) {
				arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(createSandFilterSvcDto(extraParameters));
			} else if (key.startsWith(BIOLOGICAL_STEP_SVC_DTO)) {
				arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(createBiologicalStepSvcDto(extraParameters));
			} else if (key.startsWith(PHOSPHORUS_TRAP_SVC_DTO)) {
				arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(createPhosphorusTrapSvcDto(extraParameters));
			} else if (key.startsWith(CHEMICAL_PRETREATMENT_SVC_DTO)) {
				arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(createChemicalPretreatmentSvcDto(extraParameters));
			}
		});

		return arrayOfPurificationStepSvcDto;
	}

	SepticTankSvcDto createSepticTankSvcDto(final Map<String, String> extraParameters) {
		final var svcDto = new SepticTankSvcDto()
			.withEmptyingInterval(CaseUtil.parseInteger(extraParameters.get(SEPTIC_TANK_SVC_DTO + "EmptyingInterval")))
			.withHasCeMarking(CaseUtil.parseBoolean(extraParameters.get(SEPTIC_TANK_SVC_DTO + "HasCeMarking")))
			.withHasTPipe(CaseUtil.parseBoolean(extraParameters.get(SEPTIC_TANK_SVC_DTO + "HasTPipe")))
			.withVolume(CaseUtil.parseDouble(extraParameters.get(SEPTIC_TANK_SVC_DTO + "VOLUME")));
		setStandardParams(extraParameters, Constants.SEPTIC_TANK_SVC_DTO, svcDto);

		return svcDto;
	}

	ClosedTankSvcDto createClosedTankSvcDto(final Map<String, String> extraParameters) {
		final var svcDto = new ClosedTankSvcDto()
			.withEmptyingInterval(CaseUtil.parseInteger(extraParameters.get(CLOSED_TANK_SVC_DTO + "EmptyingInterval")))
			.withHasCeMarking(CaseUtil.parseBoolean(extraParameters.get(CLOSED_TANK_SVC_DTO + "HasCeMarking")))
			.withVolume(CaseUtil.parseDouble(extraParameters.get(CLOSED_TANK_SVC_DTO + "VOLUME")));
		setStandardParams(extraParameters, CLOSED_TANK_SVC_DTO, svcDto);

		return svcDto;
	}

	MiniSewageSvcDto createMiniSewageSvcDto(final Map<String, String> extraParameters) {
		final var svcDto = new MiniSewageSvcDto()
			.withCeMarking(CaseUtil.parseBoolean(extraParameters.get(MINI_SEWAGE_SVC_DTO + "CeMarking")))
			.withManufacturer(CaseUtil.parseString(extraParameters.get(MINI_SEWAGE_SVC_DTO + "Manufacturer")))
			.withModel(CaseUtil.parseString(extraParameters.get(MINI_SEWAGE_SVC_DTO + "Model")));
		setStandardParams(extraParameters, MINI_SEWAGE_SVC_DTO, svcDto);

		return svcDto;
	}

	FilterBedSvcDto createFilterBedSvcDto(final Map<String, String> extraParameters) {
		final var svcDto = new FilterBedSvcDto()
			.withVolume(CaseUtil.parseDouble(extraParameters.get(FILTER_BED_SVC_DTO + "VOLUME")));
		setStandardParams(extraParameters, FILTER_BED_SVC_DTO, svcDto);

		return svcDto;
	}

	SandFilterSvcDto createSandFilterSvcDto(final Map<String, String> extraParameters) {
		final var svcDto = new SandFilterSvcDto()
			.withArea(CaseUtil.parseInteger(extraParameters.get(SAND_FILTER_SVC_DTO + "Area")))
			.withElevated(CaseUtil.parseBoolean(extraParameters.get(SAND_FILTER_SVC_DTO + "Elevated")))
			.withWaterTight(CaseUtil.parseBoolean(extraParameters.get(SAND_FILTER_SVC_DTO + "WaterTight")));
		setStandardParams(extraParameters, SAND_FILTER_SVC_DTO, svcDto);

		return svcDto;
	}

	BiologicalStepSvcDto createBiologicalStepSvcDto(final Map<String, String> extraParameters) {
		final var svcDto = new BiologicalStepSvcDto()
			.withArea(CaseUtil.parseInteger(extraParameters.get(BIOLOGICAL_STEP_SVC_DTO + "Area")))
			.withBiologicalStepTypeId(CaseUtil.parseString(extraParameters.get(BIOLOGICAL_STEP_SVC_DTO + "BiologicalStepTypeId")));
		setStandardParams(extraParameters, BIOLOGICAL_STEP_SVC_DTO, svcDto);

		return svcDto;
	}

	DrySolutionSvcDto createDrySolutionSvcDto(final Map<String, String> extraParameters) {
		final var svcDto = new DrySolutionSvcDto()
			.withCompostProductName(CaseUtil.parseString(extraParameters.get(DRY_SOLUTION_SVC_DTO + "CompostProductName")))
			.withDrySolutionCompostTypeId(CaseUtil.parseString(extraParameters.get(DRY_SOLUTION_SVC_DTO + "DrySolutionCompostTypeId")))
			.withDrySolutionTypeId(CaseUtil.parseString(extraParameters.get(DRY_SOLUTION_SVC_DTO + "DrySolutionTypeId")))
			.withNoContOrCompt(CaseUtil.parseString(extraParameters.get(DRY_SOLUTION_SVC_DTO + "NoContOrCompt")))
			.withNoLPerContOrCompt(CaseUtil.parseString(extraParameters.get(DRY_SOLUTION_SVC_DTO + "NoLPerContOrCompt")))
			.withToiletProductName(CaseUtil.parseString(extraParameters.get(DRY_SOLUTION_SVC_DTO + "ToiletProductName")))
			.withVolume(CaseUtil.parseDouble(extraParameters.get(DRY_SOLUTION_SVC_DTO + "Volume")));
		setStandardParams(extraParameters, DRY_SOLUTION_SVC_DTO, svcDto);

		return svcDto;
	}

	InfiltrationPlantSvcDto createInfiltrationPlantSvcDto(final Map<String, String> extraParameters) {
		final var svcDto = new InfiltrationPlantSvcDto()
			.withElevated(CaseUtil.parseBoolean(extraParameters.get(INFILTRATION_PLANT_SVC_DTO + "Elevated")))
			.withReinforced(CaseUtil.parseBoolean(extraParameters.get(INFILTRATION_PLANT_SVC_DTO + "Reinforced")))
			.withIsModuleSystem(CaseUtil.parseBoolean(extraParameters.get(INFILTRATION_PLANT_SVC_DTO + "IsModuleSystem")))
			.withSpreadLinesCount(CaseUtil.parseInteger(extraParameters.get(INFILTRATION_PLANT_SVC_DTO + "SpreadLinesCount")))
			.withSpreadLinesLength(CaseUtil.parseInteger(extraParameters.get(INFILTRATION_PLANT_SVC_DTO + "SpreadLinesLength")));
		setStandardParams(extraParameters, INFILTRATION_PLANT_SVC_DTO, svcDto);

		return svcDto;
	}

	PhosphorusTrapSvcDto createPhosphorusTrapSvcDto(final Map<String, String> extraParameters) {
		final var svcDto = new PhosphorusTrapSvcDto();
		setStandardParams(extraParameters, PHOSPHORUS_TRAP_SVC_DTO, svcDto);
		return svcDto;
	}

	ChemicalPretreatmentSvcDto createChemicalPretreatmentSvcDto(final Map<String, String> extraParameters) {
		final var svcDto = new ChemicalPretreatmentSvcDto();
		setStandardParams(extraParameters, CHEMICAL_PRETREATMENT_SVC_DTO, svcDto);
		return svcDto;
	}

	CreateMarineHeatingFacilitySvcDto createMarineHeatingFacility(final Map<String, String> extraParameters, final FbPropertyInfo propertyInfo, final RegisterDocumentCaseResultSvcDto registerDocumentResult) {
		final var createMarineHeatingFacilitySvcDto = new CreateMarineHeatingFacilitySvcDto()
			.withHeatCollectorTubes(createHeatCollectorTubes(extraParameters));

		setHeatPumpStandardParams(extraParameters, propertyInfo, registerDocumentResult, createMarineHeatingFacilitySvcDto, "CreateMarineHeatingFacilitySvcDto_");
		setHeatPumpFluidStandardParams(extraParameters, "CreateMarineHeatingFacilitySvcDto_", createMarineHeatingFacilitySvcDto);

		return createMarineHeatingFacilitySvcDto;
	}

	CreateAirHeatingFacilitySvcDto createAirHeatingFacility(final Map<String, String> extraParameters, final FbPropertyInfo propertyInfo, final RegisterDocumentCaseResultSvcDto registerDocumentResult) {
		final var createAirHeatingFacilitySvcDto = new CreateAirHeatingFacilitySvcDto();

		setHeatPumpStandardParams(extraParameters, propertyInfo, registerDocumentResult, createAirHeatingFacilitySvcDto, "CreateAirHeatingFacilitySvcDto_");

		return createAirHeatingFacilitySvcDto;
	}

	CreateHeatPumpFacilityWithHeatTransferFluidSvcDto createGeoThermalHeatingFacility(final Map<String, String> extraParameters, final FbPropertyInfo propertyInfo, final RegisterDocumentCaseResultSvcDto registerDocumentResult) {
		final var createHeatPumpFacilityWithHeatTransferFluidSvcDto = new CreateGeothermalHeatingFacilitySvcDto()
			.withBoreholes(createBoreHoles(extraParameters));

		setHeatPumpStandardParams(extraParameters, propertyInfo, registerDocumentResult, createHeatPumpFacilityWithHeatTransferFluidSvcDto, "CreateGeothermalHeatingFacilitySvcDto_");
		setHeatPumpFluidStandardParams(extraParameters, "CreateGeothermalHeatingFacilitySvcDto_", createHeatPumpFacilityWithHeatTransferFluidSvcDto);

		return createHeatPumpFacilityWithHeatTransferFluidSvcDto;
	}

	CreateSoilHeatingFacilitySvcDto createSoilHeatingFacility(final Map<String, String> extraParameters, final FbPropertyInfo propertyInfo, final RegisterDocumentCaseResultSvcDto registerDocumentResult) {
		final var createSoilHeatingFacilitySvcDto = new CreateSoilHeatingFacilitySvcDto()
			.withHeatCollectorTubes(createHeatCollectorTubes(extraParameters));

		setHeatPumpStandardParams(extraParameters, propertyInfo, registerDocumentResult, createSoilHeatingFacilitySvcDto, "CreateSoilHeatingFacilitySvcDto_");
		setHeatPumpFluidStandardParams(extraParameters, "CreateSoilHeatingFacilitySvcDto_", createSoilHeatingFacilitySvcDto);

		return createSoilHeatingFacilitySvcDto;
	}

	minutmiljoV2.ArrayOfDocumentSvcDto createArrayOfDocumentSvcDtoV2(final List<AttachmentDTO> attachments) {
		final minutmiljoV2.ArrayOfDocumentSvcDto arrayOfDocumentSvcDto = new minutmiljoV2.ArrayOfDocumentSvcDto();
		for (final var attachment : attachments) {

			final var documentSvcDto = new minutmiljoV2.DocumentSvcDto()
				.withContentType(attachment.getMimeType() != null ? attachment.getMimeType().toLowerCase() : null)
				.withData(Base64.getDecoder().decode(attachment.getFile().getBytes()))
				.withDocumentTypeId(AttachmentCategory.fromCode(attachment.getCategory()).getDescription())
				.withFilename(getFilename(attachment))
				.withNote(attachment.getNote());

			arrayOfDocumentSvcDto.getDocumentSvcDto().add(documentSvcDto);
		}
		return arrayOfDocumentSvcDto;
	}

	ArrayOfHeatCollectorTubeSvcDto createHeatCollectorTubes(final Map<String, String> extraParameters) {
		final ArrayOfHeatCollectorTubeSvcDto arrayOfHeatCollectorTubeSvcDto = new ArrayOfHeatCollectorTubeSvcDto();
		final String PREFIX = "HeatCollectorTubeSvcDto_";
		int number = 1;

		while (extraParameters.containsKey(PREFIX + number++)) {
			final String currentPrefix = PREFIX + number + "_";

			final HeatCollectorTubeSvcDto heatCollectorTubeSvcDto = new HeatCollectorTubeSvcDto();
			heatCollectorTubeSvcDto.setFacilityStatusId(CaseUtil.parseString(extraParameters.get(currentPrefix + "FacilityStatusId")));
			heatCollectorTubeSvcDto.setLength(CaseUtil.parseInteger(extraParameters.get(currentPrefix + "Length")));
			heatCollectorTubeSvcDto.setName(CaseUtil.parseString(extraParameters.get(currentPrefix + "Name")));
			heatCollectorTubeSvcDto.setLocation(createLocation(extraParameters, currentPrefix));

			arrayOfHeatCollectorTubeSvcDto.getHeatCollectorTubeSvcDto().add(heatCollectorTubeSvcDto);
		}

		return arrayOfHeatCollectorTubeSvcDto;
	}

	ArrayOfBoreholeSvcDto createBoreHoles(final Map<String, String> extraParameters) {
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
			boreholeSvcDto.setLocation(createLocation(extraParameters, currentPrefix));

			arrayOfBoreholeSvcDto.getBoreholeSvcDto().add(boreholeSvcDto);
		}

		return arrayOfBoreholeSvcDto;
	}

	FacilityAddressSvcDto createAddress(final FbPropertyInfo propertyInfo) {
		return Optional.ofNullable(propertyInfo)
			.map(property -> new FacilityAddressSvcDto().withAdressPlatsId(property.getAdressplatsId()))
			.orElse(null);
	}

	LocationSvcDto createLocation(final Map<String, String> extraParameters, final String prefix) {
		final String locationPrefix = "LocationSvcDto_";
		final LocationSvcDto locationSvcDto = new LocationSvcDto();
		locationSvcDto.setE(CaseUtil.parseDouble(extraParameters.get(prefix + locationPrefix + "E")));
		locationSvcDto.setN(CaseUtil.parseDouble(extraParameters.get(prefix + locationPrefix + "N")));
		locationSvcDto.setMeasured(CaseUtil.parseBoolean(extraParameters.get(prefix + locationPrefix + "Measured")));

		return locationSvcDto;
	}

	String getFilename(final AttachmentDTO attachment) {
		// Filename must end with extension for the preview in Ecos to work
		String filename = attachment.getName().toLowerCase();
		final String extension = attachment.getExtension().toLowerCase();
		if (!filename.endsWith(extension)) {
			filename = attachment.getName() + extension;
		}
		return filename;
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

	private void setHeatPumpStandardParams(final Map<String, String> extraParameters, final FbPropertyInfo propertyInfo, final RegisterDocumentCaseResultSvcDto registerDocumentResult, final CreateHeatPumpFacilitySvcDto svcDto, final String prefix) {
		svcDto.setAddress(createAddress(propertyInfo));
		svcDto.setFacilityStatusId(Constants.ECOS_FACILITY_STATUS_ID_ANMALD_ANSOKT);
		svcDto.setCreatedFromCaseId(registerDocumentResult.getCaseId());
		svcDto.setEstate(new EstateSvcDto().withFnr(propertyInfo.getFnr()));

		svcDto.setManufacturer(CaseUtil.parseString(extraParameters.get(prefix + "Manufacturer")));
		svcDto.setModel(CaseUtil.parseString(extraParameters.get(prefix + "Model")));
		svcDto.setPowerConsumption(CaseUtil.parseDouble(extraParameters.get(prefix + "PowerConsumption")));
		svcDto.setPowerOutput(CaseUtil.parseDouble(extraParameters.get(prefix + "PowerOutput")));
	}

	private void setHeatPumpFluidStandardParams(final Map<String, String> extraParameters, final String prefix, final CreateHeatPumpFacilityWithHeatTransferFluidSvcDto svcDto) {
		svcDto.setCapacity(CaseUtil.parseDouble(extraParameters.get(prefix + "Capacity")));
		svcDto.setHeatTransferFluidId(CaseUtil.parseString(extraParameters.get(prefix + "HeatTransferFluidId")));
	}

	private String getDiaryPlanId(final String caseType) {
		return switch (CaseType.valueOf(caseType)) {
			case REGISTRERING_AV_LIVSMEDEL, UPPDATERING_RISKKLASSNING,
			     ANDRING_AV_LIVSMEDELSVERKSAMHET, INFORMATION_OM_UPPHORANDE_AV_VERKSAMHET -> Constants.ECOS_DIARY_PLAN_LIVSMEDEL;
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
			case ANDRING_AV_LIVSMEDELSVERKSAMHET -> Constants.ECOS_PROCESS_TYPE_ID_ANDRING_AV_LIVSMEDELSVERKSAMHET;
			case INFORMATION_OM_UPPHORANDE_AV_VERKSAMHET -> Constants.ECOS_PROCESS_TYPE_ID_INFORMATION_OM_UPPHORANDE_AV_VERKSAMHET;
			default -> throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "CaseType: " + caseType + " is not valid...");
		};
	}

}
