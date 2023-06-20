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
        String extension = attachmentDTO.getExtension().toLowerCase();
        if (!filename.endsWith(extension)) {
            filename = attachmentDTO.getName() + extension;
        }
        return filename;
    }

    public RegisterDocumentCaseResultSvcDto postCase(EnvironmentalCaseDTO caseInput) {

        var eFacility = caseInput.getFacilities().get(0);

        FbPropertyInfo propertyInfo = null;
        if (eFacility.getAddress() != null && eFacility.getAddress().getPropertyDesignation() != null) {
            // Collects this early to avoid creating something before we discover potential errors
            propertyInfo = fbService.getPropertyInfoByPropertyDesignation(eFacility.getAddress().getPropertyDesignation());
        }

        // Do requests to SearchParty for every stakeholder and collect these stakeholders to be able to add them
        // to the facility later.
        var partyList = new ArrayList<PartySvcDto>();

        // The stakeholder is stored with associated roles so that we can set roles later.
        var partyRoles = new HashMap<String, ArrayOfguid>();

        // If the stakeholder is missing in Ecos, we keep it in this list and create them later (CreateParty)
        var missingStakeholderDTOS = new ArrayList<StakeholderDTO>();

        // -----> SearchParty
        populatePartyList(caseInput, partyRoles, partyList, missingStakeholderDTOS);

        // -----> CreateParty
        createParty(partyRoles, partyList, missingStakeholderDTOS);

        // -----> RegisterDocument
        var registerDocumentResult = registerDocument(caseInput);

        // -----> AddPartyToCase
        addPartyToCase(partyRoles, partyList, registerDocumentResult.getCaseId());


        if (propertyInfo != null) {
            String facilityGuid = switch (caseInput.getCaseType()) {
                case REGISTRERING_AV_LIVSMEDEL ->
                    createFoodFacility(caseInput, propertyInfo, registerDocumentResult);
                case ANMALAN_INSTALLATION_VARMEPUMP, ANSOKAN_TILLSTAND_VARMEPUMP ->
                    createHeatPumpFacility(eFacility.getExtraParameters(), propertyInfo, registerDocumentResult);
                case ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC, ANMALAN_ANDRING_AVLOPPSANLAGGNING, ANMALAN_ANDRING_AVLOPPSANORDNING ->
                    createIndividualSewage(eFacility, propertyInfo, registerDocumentResult);
                case ANMALAN_HALSOSKYDDSVERKSAMHET ->
                    createHealthProtectionFacility(eFacility, propertyInfo, registerDocumentResult);
                default ->
                    throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "CaseType: " + caseInput.getCaseType() + " is not valid. There is a problem in the API validation.");
            };

            // -----> AddPartyToFacility
            if (facilityGuid != null) {
                addPartyToFacility(partyRoles, partyList, facilityGuid);
            }

        } else {
            try {
                if (caseInput.getCaseType().equals(CaseType.UPPDATERING_RISKKLASSNING)) {

                    riskClassService.updateRiskClass(caseInput, registerDocumentResult.getCaseId());
                }
            } catch (Exception e) {
                log.warn("Error when updating risk class for case with OpenE-ID: "+caseInput.getExternalCaseId(), e);
            }
            // -----> CreateOccurrenceOnCase
            createOccurrenceOnCase(registerDocumentResult.getCaseId());
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
        for (PartySvcDto p : partyList) {
            AddPartyToCase addPartyToCase = new AddPartyToCase();
            AddPartyToCaseSvcDto addPartyToCaseSvcDto = new AddPartyToCaseSvcDto();
            addPartyToCaseSvcDto.setCaseId(caseId);
            addPartyToCaseSvcDto.setPartyId(p.getId());
            addPartyToCaseSvcDto.setRoles(partyRoles.get(p.getId()));
            addPartyToCase.setModel(addPartyToCaseSvcDto);

            minutMiljoClient.addPartyToCase(addPartyToCase);
        }
    }

    private void addPartyToFacility(Map<String, ArrayOfguid> partyRoles, List<PartySvcDto> partyList, String foodFacilityGuid) {
        for (PartySvcDto p : partyList) {
            AddPartyToFacility addPartyToFacility = new AddPartyToFacility();
            AddPartyToFacilitySvcDto addPartyToFacilitySvcDto = new AddPartyToFacilitySvcDto();
            addPartyToFacilitySvcDto.setFacilityId(foodFacilityGuid);
            addPartyToFacilitySvcDto.setPartyId(p.getId());
            addPartyToFacilitySvcDto.setRoles(partyRoles.get(p.getId()));
            addPartyToFacility.setModel(addPartyToFacilitySvcDto);

            minutMiljoClient.addPartyToFacility(addPartyToFacility);
        }
    }

    private String createFoodFacility(EnvironmentalCaseDTO eCase, FbPropertyInfo propertyInfo, RegisterDocumentCaseResultSvcDto registerDocumentResult) {

        CreateFoodFacility createFoodFacility = new CreateFoodFacility();
        CreateFoodFacilitySvcDto createFoodFacilitySvcDto = new CreateFoodFacilitySvcDto();

        createFoodFacilitySvcDto.setAddress(getAddress(propertyInfo));
        createFoodFacilitySvcDto.setCase(registerDocumentResult.getCaseId());

        createFoodFacilitySvcDto.setEstateDesignation(getEstateSvcDto(propertyInfo));
        createFoodFacilitySvcDto.setFacilityCollectionName(eCase.getFacilities().get(0).getFacilityCollectionName() + " " + LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        createFoodFacilitySvcDto.setNote(eCase.getFacilities().get(0).getDescription());

        createFoodFacility.setCreateFoodFacilitySvcDto(createFoodFacilitySvcDto);

        String foodFacilityGuid = minutMiljoClient.createFoodFacility(createFoodFacility).getCreateFoodFacilityResult();

        if (foodFacilityGuid != null) {
            log.debug("FoodFacility created: {}", foodFacilityGuid);
            return foodFacilityGuid;
        } else {
            throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "FoodFacility could not be created.");
        }
    }

    private String createHeatPumpFacility(Map<String, String> facilityExtraParameters, FbPropertyInfo propertyInfo, RegisterDocumentCaseResultSvcDto registerDocumentResult) {

        CreateHeatPumpFacility createHeatPumpFacility = new CreateHeatPumpFacility();
        CreateHeatPumpFacilitySvcDto createHeatPumpFacilitySvcDto;

        String airPrefix = "CreateAirHeatingFacilitySvcDto_";
        String geoThermalPrefix = "CreateGeothermalHeatingFacilitySvcDto_";
        String soilPrefix = "CreateSoilHeatingFacilitySvcDto_";
        String marinePrefix = "CreateMarineHeatingFacilitySvcDto_";

        if (facilityExtraParameters == null || facilityExtraParameters.isEmpty()) {
            log.info("facilityExtraParameters was null or empty, do not create facility. Return null.");
            return null;
        } else if (facilityExtraParameters.keySet().stream().anyMatch(s -> s.startsWith(airPrefix))) {
            createHeatPumpFacilitySvcDto = getAirHeatingFacility(facilityExtraParameters, propertyInfo, registerDocumentResult, airPrefix);
        } else if (facilityExtraParameters.keySet().stream().anyMatch(s -> s.startsWith(geoThermalPrefix))) {
            createHeatPumpFacilitySvcDto = getGeoThermalHeatingFacility(facilityExtraParameters, propertyInfo, registerDocumentResult, geoThermalPrefix);
        } else if (facilityExtraParameters.keySet().stream().anyMatch(s -> s.startsWith(soilPrefix))) {
            createHeatPumpFacilitySvcDto = getSoilHeatingFacility(facilityExtraParameters, propertyInfo, registerDocumentResult, soilPrefix);
        } else if (facilityExtraParameters.keySet().stream().anyMatch(s -> s.startsWith(marinePrefix))) {
            createHeatPumpFacilitySvcDto = getMarineHeatingFacility(facilityExtraParameters, propertyInfo, registerDocumentResult, marinePrefix);
        } else {
            throw Problem.valueOf(Status.BAD_REQUEST, MessageFormat.format("The request does not contain any extraParameters on the facility-object with prefix: \"{0}\", \"{1}\"\", \"{2}\"\" or \"{3}\"", airPrefix, geoThermalPrefix, soilPrefix, marinePrefix));
        }

        createHeatPumpFacility.setCreateIndividualSewageSvcDto(createHeatPumpFacilitySvcDto);
        String facilityGuid = minutMiljoClient.createHeatPumpFacility(createHeatPumpFacility).getCreateHeatPumpFacilityResult();

        if (facilityGuid != null) {
            log.debug("HeatPumpFacility created: {}", facilityGuid);
            return facilityGuid;
        } else {
            throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "HeatPumpFacility could not be created");
        }
    }

    private CreateHeatPumpFacilityWithHeatTransferFluidSvcDto getGeoThermalHeatingFacility(Map<String, String> extraParameters, FbPropertyInfo propertyInfo, RegisterDocumentCaseResultSvcDto registerDocumentResult, String prefix) {
        CreateGeothermalHeatingFacilitySvcDto createHeatPumpFacilityWithHeatTransferFluidSvcDto = new CreateGeothermalHeatingFacilitySvcDto();

        setHeatPumpStandardParams(extraParameters, propertyInfo, registerDocumentResult, createHeatPumpFacilityWithHeatTransferFluidSvcDto, prefix);
        setHeatPumpFluidStandardParams(extraParameters, prefix, createHeatPumpFacilityWithHeatTransferFluidSvcDto);

        createHeatPumpFacilityWithHeatTransferFluidSvcDto.setBoreholes(getBoreHoles(extraParameters));

        return createHeatPumpFacilityWithHeatTransferFluidSvcDto;
    }

    private ArrayOfBoreholeSvcDto getBoreHoles(Map<String, String> extraParameters) {
        ArrayOfBoreholeSvcDto arrayOfBoreholeSvcDto = new ArrayOfBoreholeSvcDto();
        final String PREFIX = "BoreholeSvcDto_";
        int number = 1;

        while (extraParameters.containsKey(PREFIX + number++)) {
            String currentPrefix = PREFIX + number + "_";

            BoreholeSvcDto boreholeSvcDto = new BoreholeSvcDto();
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
        String locationPrefix = "LocationSvcDto_";
        LocationSvcDto locationSvcDto = new LocationSvcDto();
        locationSvcDto.setE(CaseUtil.parseDouble(extraParameters.get(prefix + locationPrefix + "E")));
        locationSvcDto.setN(CaseUtil.parseDouble(extraParameters.get(prefix + locationPrefix + "N")));
        locationSvcDto.setMeasured(CaseUtil.parseBoolean(extraParameters.get(prefix + locationPrefix + "Measured")));

        return locationSvcDto;
    }

    private CreateSoilHeatingFacilitySvcDto getSoilHeatingFacility(Map<String, String> extraParameters, FbPropertyInfo propertyInfo, RegisterDocumentCaseResultSvcDto registerDocumentResult, String prefix) {
        CreateSoilHeatingFacilitySvcDto createSoilHeatingFacilitySvcDto = new CreateSoilHeatingFacilitySvcDto();

        setHeatPumpStandardParams(extraParameters, propertyInfo, registerDocumentResult, createSoilHeatingFacilitySvcDto, prefix);
        setHeatPumpFluidStandardParams(extraParameters, prefix, createSoilHeatingFacilitySvcDto);

        createSoilHeatingFacilitySvcDto.setHeatCollectorTubes(getHeatCollectorTubes(extraParameters));

        return createSoilHeatingFacilitySvcDto;
    }

    private ArrayOfHeatCollectorTubeSvcDto getHeatCollectorTubes(Map<String, String> extraParameters) {
        ArrayOfHeatCollectorTubeSvcDto arrayOfHeatCollectorTubeSvcDto = new ArrayOfHeatCollectorTubeSvcDto();
        final String PREFIX = "HeatCollectorTubeSvcDto_";
        int number = 1;

        while (extraParameters.containsKey(PREFIX + number++)) {
            String currentPrefix = PREFIX + number + "_";

            HeatCollectorTubeSvcDto heatCollectorTubeSvcDto = new HeatCollectorTubeSvcDto();
            heatCollectorTubeSvcDto.setFacilityStatusId(CaseUtil.parseString(extraParameters.get(currentPrefix + "FacilityStatusId")));
            heatCollectorTubeSvcDto.setLength(CaseUtil.parseInteger(extraParameters.get(currentPrefix + "Length")));
            heatCollectorTubeSvcDto.setName(CaseUtil.parseString(extraParameters.get(currentPrefix + "Name")));
            heatCollectorTubeSvcDto.setLocation(getLocation(extraParameters, currentPrefix));

            arrayOfHeatCollectorTubeSvcDto.getHeatCollectorTubeSvcDto().add(heatCollectorTubeSvcDto);
        }

        return arrayOfHeatCollectorTubeSvcDto;
    }

    private CreateMarineHeatingFacilitySvcDto getMarineHeatingFacility(Map<String, String> extraParameters, FbPropertyInfo propertyInfo, RegisterDocumentCaseResultSvcDto registerDocumentResult, String prefix) {
        CreateMarineHeatingFacilitySvcDto createMarineHeatingFacilitySvcDto = new CreateMarineHeatingFacilitySvcDto();

        setHeatPumpStandardParams(extraParameters, propertyInfo, registerDocumentResult, createMarineHeatingFacilitySvcDto, prefix);
        setHeatPumpFluidStandardParams(extraParameters, prefix, createMarineHeatingFacilitySvcDto);
        createMarineHeatingFacilitySvcDto.setHeatCollectorTubes(getHeatCollectorTubes(extraParameters));

        return createMarineHeatingFacilitySvcDto;
    }

    private CreateAirHeatingFacilitySvcDto getAirHeatingFacility(Map<String, String> extraParameters, FbPropertyInfo propertyInfo, RegisterDocumentCaseResultSvcDto registerDocumentResult, String prefix) {
        CreateAirHeatingFacilitySvcDto createAirHeatingFacilitySvcDto = new CreateAirHeatingFacilitySvcDto();

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

        CreateIndividualSewageFacility createIndividualSewageFacility = new CreateIndividualSewageFacility();
        CreateIndividualSewageFacilitySvcDto createIndividualSewageFacilitySvcDto = new CreateIndividualSewageFacilitySvcDto();

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

        String facilityGuid = minutMiljoClient.createIndividualSewageFacility(createIndividualSewageFacility).getCreateIndividualSewageFacilityResult();

        if (facilityGuid != null) {
            log.debug("Individual Sewage created: {}", facilityGuid);
            return facilityGuid;
        } else {
            throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "Individual Sewage could not be created");
        }
    }

    private FacilityAddressSvcDto getAddress(FbPropertyInfo propertyInfo) {
        if (propertyInfo.getAdressplatsId() != null) {
            FacilityAddressSvcDto facilityAddressSvcDto = new FacilityAddressSvcDto();
            facilityAddressSvcDto.setAdressPlatsId(propertyInfo.getAdressplatsId());
            return facilityAddressSvcDto;
        }
        return null;
    }

    private ArrayOfPurificationStepSvcDto getPurificationSteps(Map<String, String> extraParameters) {

        ArrayOfPurificationStepSvcDto arrayOfPurificationStepSvcDto = new ArrayOfPurificationStepSvcDto();

        String septicTankPrefix = "SepticTankSvcDto_";
        if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(septicTankPrefix))) {
            arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getSepticTankSvcDto(extraParameters, septicTankPrefix));
        }
        String infiltrationPrefix = "InfiltrationPlantSvcDto_";
        if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(infiltrationPrefix))) {
            arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getInfiltrationPlantSvcDto(extraParameters, infiltrationPrefix));
        }
        String closedTankPrefix = "ClosedTankSvcDto_";
        if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(closedTankPrefix))) {
            arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getClosedTankSvcDto(extraParameters, closedTankPrefix));
        }
        String drySolutionPrefix = "DrySolutionSvcDto_";
        if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(drySolutionPrefix))) {
            arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getDrySolutionSvcDto(extraParameters, drySolutionPrefix));
        }
        String miniSewagePrefix = "MiniSewageSvcDto_";
        if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(miniSewagePrefix))) {
            arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getMiniSewageSvcDto(extraParameters, miniSewagePrefix));
        }
        String filterBedPrefix = "FilterBedSvcDto_";
        if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(filterBedPrefix))) {
            arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getFilterBedSvcDto(extraParameters, filterBedPrefix));
        }
        String sandFilterPrefix = "SandFilterSvcDto_";
        if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(sandFilterPrefix))) {
            arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getSandFilterSvcDto(extraParameters, sandFilterPrefix));
        }
        String biologicalStepPrefix = "BiologicalStepSvcDto_";
        if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(biologicalStepPrefix))) {
            arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getBiologicalStepSvcDto(extraParameters, biologicalStepPrefix));
        }
        String phosphorusTrapPrefix = "PhosphorusTrapSvcDto_";
        if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(phosphorusTrapPrefix))) {
            arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getPhosphorusTrapSvcDto(extraParameters, phosphorusTrapPrefix));
        }
        String chemicalPretreatmentPrefix = "ChemicalPretreatmentSvcDto_";
        if (extraParameters.keySet().stream().anyMatch(s -> s.startsWith(chemicalPretreatmentPrefix))) {
            arrayOfPurificationStepSvcDto.getPurificationStepSvcDto().add(getChemicalPretreatmentSvcDto(extraParameters, chemicalPretreatmentPrefix));
        }

        return arrayOfPurificationStepSvcDto;
    }

    private SepticTankSvcDto getSepticTankSvcDto(Map<String, String> extraParameters, String prefix) {
        SepticTankSvcDto svcDto = new SepticTankSvcDto();

        setStandardParams(extraParameters, prefix, svcDto);

        svcDto.setEmptyingInterval(CaseUtil.parseInteger(extraParameters.get(prefix + "EmptyingInterval")));
        svcDto.setHasCeMarking(CaseUtil.parseBoolean(extraParameters.get(prefix + "HasCeMarking")));
        svcDto.setHasTPipe(CaseUtil.parseBoolean(extraParameters.get(prefix + "HasTPipe")));
        svcDto.setVolume(CaseUtil.parseDouble(extraParameters.get(prefix + "Volume")));

        return svcDto;
    }

    private ClosedTankSvcDto getClosedTankSvcDto(Map<String, String> extraParameters, String prefix) {
        ClosedTankSvcDto svcDto = new ClosedTankSvcDto();

        setStandardParams(extraParameters, prefix, svcDto);

        svcDto.setEmptyingInterval(CaseUtil.parseInteger(extraParameters.get(prefix + "EmptyingInterval")));
        svcDto.setHasCeMarking(CaseUtil.parseBoolean(extraParameters.get(prefix + "HasCeMarking")));
        svcDto.setVolume(CaseUtil.parseDouble(extraParameters.get(prefix + "Volume")));

        return svcDto;
    }

    private MiniSewageSvcDto getMiniSewageSvcDto(Map<String, String> extraParameters, String prefix) {
        MiniSewageSvcDto svcDto = new MiniSewageSvcDto();

        setStandardParams(extraParameters, prefix, svcDto);

        svcDto.setCeMarking(CaseUtil.parseBoolean(extraParameters.get(prefix + "CeMarking")));
        svcDto.setManufacturer(CaseUtil.parseString(extraParameters.get(prefix + "Manufacturer")));
        svcDto.setModel(CaseUtil.parseString(extraParameters.get(prefix + "Model")));

        return svcDto;
    }

    private FilterBedSvcDto getFilterBedSvcDto(Map<String, String> extraParameters, String prefix) {
        FilterBedSvcDto svcDto = new FilterBedSvcDto();

        setStandardParams(extraParameters, prefix, svcDto);
        svcDto.setVolume(CaseUtil.parseDouble(extraParameters.get(prefix + "Volume")));

        return svcDto;
    }

    private SandFilterSvcDto getSandFilterSvcDto(Map<String, String> extraParameters, String prefix) {
        SandFilterSvcDto svcDto = new SandFilterSvcDto();

        setStandardParams(extraParameters, prefix, svcDto);

        svcDto.setArea(CaseUtil.parseInteger(extraParameters.get(prefix + "Area")));
        svcDto.setElevated(CaseUtil.parseBoolean(extraParameters.get(prefix + "Elevated")));
        svcDto.setWaterTight(CaseUtil.parseBoolean(extraParameters.get(prefix + "WaterTight")));

        return svcDto;
    }

    private BiologicalStepSvcDto getBiologicalStepSvcDto(Map<String, String> extraParameters, String prefix) {
        BiologicalStepSvcDto svcDto = new BiologicalStepSvcDto();

        setStandardParams(extraParameters, prefix, svcDto);

        svcDto.setArea(CaseUtil.parseInteger(extraParameters.get(prefix + "Area")));
        svcDto.setBiologicalStepTypeId(CaseUtil.parseString(extraParameters.get(prefix + "BiologicalStepTypeId")));

        return svcDto;
    }

    private DrySolutionSvcDto getDrySolutionSvcDto(Map<String, String> extraParameters, String prefix) {
        DrySolutionSvcDto svcDto = new DrySolutionSvcDto();

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
        InfiltrationPlantSvcDto svcDto = new InfiltrationPlantSvcDto();

        setStandardParams(extraParameters, prefix, svcDto);

        svcDto.setElevated(CaseUtil.parseBoolean(extraParameters.get(prefix + "Elevated")));
        svcDto.setReinforced(CaseUtil.parseBoolean(extraParameters.get(prefix + "Reinforced")));
        svcDto.setIsModuleSystem(CaseUtil.parseBoolean(extraParameters.get(prefix + "IsModuleSystem")));
        svcDto.setSpreadLinesCount(CaseUtil.parseInteger(extraParameters.get(prefix + "SpreadLinesCount")));
        svcDto.setSpreadLinesLength(CaseUtil.parseInteger(extraParameters.get(prefix + "SpreadLinesLength")));

        return svcDto;
    }

    private PhosphorusTrapSvcDto getPhosphorusTrapSvcDto(Map<String, String> extraParameters, String prefix) {
        PhosphorusTrapSvcDto svcDto = new PhosphorusTrapSvcDto();
        setStandardParams(extraParameters, prefix, svcDto);
        return svcDto;
    }

    private ChemicalPretreatmentSvcDto getChemicalPretreatmentSvcDto(Map<String, String> extraParameters, String prefix) {
        ChemicalPretreatmentSvcDto svcDto = new ChemicalPretreatmentSvcDto();
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
        var registerDocumentCaseSvcDtoV2 = new RegisterDocumentCaseSvcDtoV2();
        var registerDocument = new RegisterDocument();
        registerDocumentCaseSvcDtoV2.setOccurrenceTypeId(Constants.ECOS_OCCURENCE_TYPE_ID_ANMALAN);
        registerDocumentCaseSvcDtoV2.setHandlingOfficerGroupId(Constants.ECOS_HANDLING_OFFICER_GROUP_ID_EXPEDITIONEN);
        registerDocumentCaseSvcDtoV2.setDiaryPlanId(getDiaryPlanId(eCase.getCaseType()));
        registerDocumentCaseSvcDtoV2.setProcessTypeId(getProcessTypeId(eCase.getCaseType()));
        registerDocumentCaseSvcDtoV2.setDocuments(getArrayOfDocumentSvcDtoV2(eCase.getAttachments()));

        var eFacility = eCase.getFacilities().get(0);

        var fixedFacilityType = Optional.ofNullable(Optional.ofNullable(eCase.getExtraParameters()).orElse(Map.of()).get("fixedFacilityType")).orElse("").trim();

        var propertyDesignation = Optional.ofNullable(Optional.ofNullable(eFacility.getAddress()).orElse(new AddressDTO()).getPropertyDesignation()).orElse("").trim().toUpperCase();

        if (!fixedFacilityType.isEmpty()) {
            registerDocumentCaseSvcDtoV2.setCaseSubtitle(fixedFacilityType);
            registerDocumentCaseSvcDtoV2.setCaseSubtitleFree(propertyDesignation);
        } else {
            var freeSubtitle = eFacility.getFacilityCollectionName().trim();
            if (!propertyDesignation.isEmpty()) {
                freeSubtitle = String.join(", ", freeSubtitle, propertyDesignation);
            }
            registerDocumentCaseSvcDtoV2.setCaseSubtitleFree(freeSubtitle);
        }
        registerDocument.setRegisterDocumentCaseSvcDto(registerDocumentCaseSvcDtoV2);
        var registerDocumentResult = minutMiljoClientV2.registerDocumentV2(registerDocument).getRegisterDocumentResult();

        if (registerDocumentResult == null) {
            throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "Case could not be created.");
        }
        log.debug("Case created with Byggr case number: {}", registerDocumentResult.getCaseNumber());
        return registerDocumentResult;
    }

    private String getDiaryPlanId(CaseType caseType) {
        return switch (caseType) {
            case REGISTRERING_AV_LIVSMEDEL -> Constants.ECOS_DIARY_PLAN_LIVSMEDEL;
            case ANMALAN_ANDRING_AVLOPPSANLAGGNING, ANMALAN_ANDRING_AVLOPPSANORDNING, ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC, ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, ANMALAN_INSTALLATION_VARMEPUMP, ANSOKAN_TILLSTAND_VARMEPUMP ->
                Constants.ECOS_DIARY_PLAN_AVLOPP;
            case ANMALAN_HALSOSKYDDSVERKSAMHET -> Constants.ECOS_DIARY_PLAN_HALSOSKYDD;
            default -> null;
        };
    }

    private String getProcessTypeId(CaseType caseType) {
        return switch (caseType) {
            case REGISTRERING_AV_LIVSMEDEL ->
                Constants.ECOS_PROCESS_TYPE_ID_REGISTRERING_AV_LIVSMEDEL;
            case ANMALAN_INSTALLATION_VARMEPUMP ->
                Constants.ECOS_PROCESS_TYPE_ID_ANMALAN_INSTALLATION_VARMEPUMP;
            case ANSOKAN_TILLSTAND_VARMEPUMP ->
                Constants.ECOS_PROCESS_TYPE_ID_ANSOKAN_TILLSTAND_VARMEPUMP;
            case ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP ->
                Constants.ECOS_PROCESS_TYPE_ID_ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP;
            case ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC ->
                Constants.ECOS_PROCESS_TYPE_ID_ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC;
            case ANMALAN_ANDRING_AVLOPPSANLAGGNING ->
                Constants.ECOS_PROCESS_TYPE_ID_ANMALAN_ANDRING_AVLOPPSANLAGGNING;
            case ANMALAN_ANDRING_AVLOPPSANORDNING ->
                Constants.ECOS_PROCESS_TYPE_ID_ANMALAN_ANDRING_AVLOPPSANORDNING;
            case ANMALAN_HALSOSKYDDSVERKSAMHET ->
                Constants.ECOS_PROCESS_TYPE_ID_ANMALAN_HALSOSKYDDSVERKSAMHET;
            case UPPDATERING_RISKKLASSNING -> Constants.ECOS_PROCESS_TYPE_ID_UPPDATERING_RISKKLASS;
            case ANMALAN_KOMPOSTERING -> Constants.ECOS_PROCESS_TYPE_ID_ANMALAN_KOMPOSTERING;
            default ->
                throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "CaseType: " + caseType + " is not valid...");
        };
    }

    private ArrayOfDocumentSvcDto getArrayOfDocumentSvcDto(List<AttachmentDTO> attachmentDTOList) {
        ArrayOfDocumentSvcDto arrayOfDocumentSvcDto = new ArrayOfDocumentSvcDto();
        for (AttachmentDTO a : attachmentDTOList) {
            DocumentSvcDto documentSvcDto = new DocumentSvcDto();
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
        minutmiljoV2.ArrayOfDocumentSvcDto arrayOfDocumentSvcDto = new minutmiljoV2.ArrayOfDocumentSvcDto();
        for (AttachmentDTO a : attachmentDTOList) {
            minutmiljoV2.DocumentSvcDto documentSvcDto = new minutmiljoV2.DocumentSvcDto();
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

        for (StakeholderDTO s : missingStakeholderDTOS) {
            String guidResult = null;

            if (s instanceof OrganizationDTO organizationDTO) {

                CreateOrganizationParty createOrganizationParty = new CreateOrganizationParty();
                createOrganizationParty.setOrganizationParty(getOrganizationSvcDto(s, organizationDTO));
                guidResult = minutMiljoClient.createOrganizationParty(createOrganizationParty).getCreateOrganizationPartyResult();

            } else if (s instanceof PersonDTO personDTO) {
                CreatePersonParty createPersonParty = new CreatePersonParty();
                createPersonParty.setPersonParty(getPersonSvcDto(s, personDTO));
                guidResult = minutMiljoClient.createPersonParty(createPersonParty).getCreatePersonPartyResult();
            }

            if (guidResult != null) {
                PartySvcDto party = new PartySvcDto();
                party.setId(guidResult);

                partyList.add(party);

                // Adds party in a hashmap with the role so that we can connect stakeholder to case with the
                // role later
                partyRoles.put(party.getId(), getEcosFacilityRoles(s));

            }
        }
    }

    PersonSvcDto getPersonSvcDto(StakeholderDTO s, PersonDTO personDTO) {
        PersonSvcDto personSvcDto = new PersonSvcDto();
        personSvcDto.setFirstName(personDTO.getFirstName());
        personSvcDto.setLastName(personDTO.getLastName());

        if (personDTO.getPersonId() != null && !personDTO.getPersonId().isBlank()) {
            personSvcDto.setNationalIdentificationNumber(CaseUtil.getSokigoFormattedPersonalNumber(citizenMappingService.getPersonalNumber(personDTO.getPersonId())));
        }

        personSvcDto.setAddresses(getEcosAddresses(s.getAddresses()));
        personSvcDto.setContactInfo(getEcosContactInfo(s).getContactInfoSvcDto().get(0));
        return personSvcDto;
    }

    OrganizationSvcDto getOrganizationSvcDto(StakeholderDTO s, OrganizationDTO organizationDTO) {
        OrganizationSvcDto osd = new OrganizationSvcDto();
        osd.setNationalIdentificationNumber(CaseUtil.getSokigoFormattedOrganizationNumber(organizationDTO.getOrganizationNumber()));
        osd.setOrganizationName(organizationDTO.getOrganizationName());

        osd.setAddresses(getEcosAddresses(s.getAddresses()));
        osd.setContactInfo(getEcosContactInfo(s));
        return osd;
    }


    private void populatePartyList(EnvironmentalCaseDTO eCase, Map<String, ArrayOfguid> partyRoles, List<PartySvcDto> partyList, List<StakeholderDTO> missingStakeholderDTOS) {

        for (StakeholderDTO s : eCase.getStakeholders()) {
            ArrayOfPartySvcDto searchPartyResult = null;

            if (s instanceof PersonDTO personDTO) {
                if (personDTO.getPersonId() != null && !personDTO.getPersonId().isBlank()) {
                    searchPartyResult = searchPartyByPersonId(personDTO.getPersonId());
                }
            } else if (s instanceof OrganizationDTO organizationDTO) {
                searchPartyResult = searchPartyByOrganizationNumber(organizationDTO.getOrganizationNumber());
            }

            // If we get a result we put it in partyList, else we put it in missingStakeholders
            if (searchPartyResult == null || searchPartyResult.getPartySvcDto().isEmpty()) {
                // These, we are going to create later
                missingStakeholderDTOS.add(s);
            } else if (!searchPartyResult.getPartySvcDto().isEmpty()) {

                // Sometimes we get multiple search results, but we should only use one in the case.
                PartySvcDto party = searchPartyResult.getPartySvcDto().get(0);
                partyList.add(party);

                // Adds stakeholder to a hashmap with the role so that we can connect the stakeholder to the case later
                partyRoles.put(party.getId(), getEcosFacilityRoles(s));
            }
        }
    }


    ArrayOfguid getEcosFacilityRoles(StakeholderDTO s) {

        ArrayOfguid roles = new ArrayOfguid();

        for (StakeholderRole role : s.getRoles()) {
            String roleId = switch (role) {
                case INVOICE_RECIPENT -> Constants.ECOS_ROLE_ID_FAKTURAMOTTAGARE;
                case OPERATOR -> Constants.ECOS_ROLE_ID_VERKSAMHETSUTOVARE;
                case CONTACT_PERSON -> Constants.ECOS_ROLE_ID_KONTAKTPERSON;
                case APPLICANT -> Constants.ECOS_ROLE_ID_SOKANDE;
                case INSTALLER -> Constants.ECOS_ROLE_ID_INSTALLATOR;
                default ->
                    throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "The request contained a stakeholder role that was not expected. This should be discovered in the validation of the input. Something in the validation is wrong.");
            };

            roles.getGuid().add(roleId);
        }

        return roles;
    }

    ArrayOfContactInfoSvcDto getEcosContactInfo(StakeholderDTO s) {
        ArrayOfContactInfoSvcDto arrayOfContactInfoSvcDto = new ArrayOfContactInfoSvcDto();
        ContactInfoSvcDto contactInfoSvcDto = new ContactInfoSvcDto();
        ArrayOfContactInfoItemSvcDto arrayOfContactInfoItemSvcDto = new ArrayOfContactInfoItemSvcDto();

        if (s.getEmailAddress() != null) {
            ContactInfoItemSvcDto item = new ContactInfoItemSvcDto();

            item.setContactDetailTypeId(Constants.ECOS_CONTACT_DETAIL_TYPE_ID_OVRIGT);
            item.setContactPathId(Constants.ECOS_CONTACT_DETAIL_TYPE_ID_EPOST);
            item.setValue(s.getEmailAddress());

            arrayOfContactInfoItemSvcDto.getContactInfoItemSvcDto().add(item);
        }
        if (s.getCellphoneNumber() != null) {
            ContactInfoItemSvcDto item = new ContactInfoItemSvcDto();

            item.setContactDetailTypeId(Constants.ECOS_CONTACT_DETAIL_TYPE_ID_MOBIL);
            item.setContactPathId(Constants.ECOS_CONTACT_DETAIL_TYPE_ID_TELEFON);
            item.setValue(s.getCellphoneNumber());

            arrayOfContactInfoItemSvcDto.getContactInfoItemSvcDto().add(item);
        }

        if (s.getPhoneNumber() != null) {
            ContactInfoItemSvcDto item = new ContactInfoItemSvcDto();

            item.setContactDetailTypeId(Constants.ECOS_CONTACT_DETAIL_TYPE_ID_HUVUDNUMMER);
            item.setContactPathId(Constants.ECOS_CONTACT_DETAIL_TYPE_ID_TELEFON);
            item.setValue(s.getPhoneNumber());

            arrayOfContactInfoItemSvcDto.getContactInfoItemSvcDto().add(item);
        }

        contactInfoSvcDto.setContactDetails(arrayOfContactInfoItemSvcDto);

        if (s instanceof PersonDTO personDTO) {
            contactInfoSvcDto.setTitle(personDTO.getFirstName() + " " + personDTO.getLastName());
        } else if (s instanceof OrganizationDTO organizationDTO) {
            contactInfoSvcDto.setTitle(organizationDTO.getAuthorizedSignatory());
        }

        arrayOfContactInfoSvcDto.getContactInfoSvcDto().add(contactInfoSvcDto);
        return arrayOfContactInfoSvcDto;
    }

    ArrayOfPartyAddressSvcDto getEcosAddresses(List<AddressDTO> addressDTOS) {
        if (addressDTOS == null) {
            return null;
        }

        ArrayOfPartyAddressSvcDto partyAddresses = new ArrayOfPartyAddressSvcDto();

        for (AddressDTO addressDTO : addressDTOS) {
            PartyAddressSvcDto partyAddress = new PartyAddressSvcDto();

            ArrayOfAddressTypeSvcDto arrayOfAddressType = new ArrayOfAddressTypeSvcDto();

            for (AddressCategory at : addressDTO.getAddressCategories()) {
                AddressTypeSvcDto addressType = new AddressTypeSvcDto();

                switch (at) {
                    case INVOICE_ADDRESS ->
                        addressType.setId(Constants.ECOS_ADDRESS_TYPE_ID_FAKTURAADRESS);
                    case POSTAL_ADDRESS ->
                        addressType.setId(Constants.ECOS_ADDRESS_TYPE_ID_POSTADRESS);
                    case VISITING_ADDRESS ->
                        addressType.setId(Constants.ECOS_ADDRESS_TYPE_ID_BESOKSADRESS);
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

        CreateOccurrenceOnCase createOccurrenceOnCase = new CreateOccurrenceOnCase();
        CreateOccurrenceOnCaseSvcDto createOccurrenceOnCaseSvcDto = new CreateOccurrenceOnCaseSvcDto();
        createOccurrenceOnCaseSvcDto.setCaseId(caseId);
        createOccurrenceOnCaseSvcDto.setOccurrenceDate(LocalDateTime.now());
        createOccurrenceOnCaseSvcDto.setOccurrenceTypeId(Constants.ECOS_OCCURRENCE_TYPE_ID_INFO_FRAN_ETJANST);
        createOccurrenceOnCaseSvcDto.setNote(Constants.ECOS_OCCURENCE_TEXT_MOBIL_ANLAGGNING);
        createOccurrenceOnCase.setCreateOccurrenceOnCaseSvcDto(createOccurrenceOnCaseSvcDto);

        minutMiljoClient.createOccurrenceOnCase(createOccurrenceOnCase);
    }

    /**
     * @return CaseStatus from Ecos.
     * @throws ThrowableProblem NOT_FOUND if no status was found.
     */
    public CaseStatusDTO getStatus(String caseId, String externalCaseId) {

        var getCase = new GetCase().withCaseId(caseId);

        CaseSvcDto ecosCase = minutMiljoClient.getCase(getCase).getGetCaseResult();


        if (Optional.ofNullable(ecosCase)
            .flatMap(caseObj -> Optional.ofNullable(caseObj.getOccurrences()))
            .flatMap(occurrences -> Optional.ofNullable(occurrences.getOccurrenceListItemSvcDto()))
            .filter(list -> !list.isEmpty())
            .isPresent()) {

            var caseMapping = Optional.ofNullable(caseMappingService.getCaseMapping(externalCaseId, caseId).get(0)).orElse(new CaseMapping());

            var latestOccurrence = ecosCase.getOccurrences()
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
        List<CaseStatusDTO> caseStatusDTOList = new ArrayList<>();

        // Find party both with and without prefix "16"
        ArrayOfPartySvcDto allParties = searchPartyByOrganizationNumber(organizationNumber);

        // Search Ecos Case
        if (allParties.getPartySvcDto() != null && !allParties.getPartySvcDto().isEmpty()) {

            ArrayOfSearchCaseResultSvcDto caseResult = new ArrayOfSearchCaseResultSvcDto();

            allParties.getPartySvcDto().forEach(party -> caseResult.getSearchCaseResultSvcDto().addAll(searchCase(party.getId()).getSearchCaseResultSvcDto()));

            // Remove eventual duplicates
            var caseResultWithoutDuplicates = caseResult.getSearchCaseResultSvcDto().stream().distinct().toList();

            caseResultWithoutDuplicates.forEach(ecosCase -> {
                List<CaseMapping> caseMappingList = caseMappingService.getCaseMapping(null, ecosCase.getCaseId());
                String externalCaseId = caseMappingList.isEmpty() ? null : caseMappingList.get(0).getExternalCaseId();
                CaseStatusDTO caseStatusDTO = getStatus(ecosCase.getCaseId(), externalCaseId);

                if (caseStatusDTO != null) {
                    caseStatusDTOList.add(caseStatusDTO);
                }
            });
        }

        return caseStatusDTOList;
    }

    public void addDocumentsToCase(String caseId, List<AttachmentDTO> attachmentDTOList) {
        AddDocumentsToCase addDocumentsToCase = new AddDocumentsToCase();
        AddDocumentsToCaseSvcDto message = new AddDocumentsToCaseSvcDto();
        message.setCaseId(caseId);
        message.setDocuments(getArrayOfDocumentSvcDto(attachmentDTOList));
        message.setOccurrenceTypeId(Constants.ECOS_OCCURRENCE_TYPE_ID_KOMPLETTERING);
        message.setDocumentStatusId(Constants.ECOS_DOCUMENT_STATUS_INKOMMEN);
        addDocumentsToCase.setAddDocumentToCaseSvcDto(message);

        minutMiljoClient.addDocumentsToCase(addDocumentsToCase);
    }

    private ArrayOfPartySvcDto searchPartyByOrganizationNumber(String organizationNumber) {

        // Find party both with and without prefix "16"
        ArrayOfPartySvcDto allParties = new ArrayOfPartySvcDto();

        SearchParty searchPartyWithoutPrefix = new SearchParty();
        SearchPartySvcDto searchPartySvcDtoWithoutPrefix = new SearchPartySvcDto();
        searchPartySvcDtoWithoutPrefix.setOrganizationIdentificationNumber(organizationNumber);
        searchPartyWithoutPrefix.setModel(searchPartySvcDtoWithoutPrefix);
        ArrayOfPartySvcDto partiesWithoutPrefix = minutMiljoClient.searchParty(searchPartyWithoutPrefix).getSearchPartyResult();

        SearchParty searchPartyWithPrefix = new SearchParty();
        SearchPartySvcDto searchPartySvcDtoWithPrefix = new SearchPartySvcDto();
        searchPartySvcDtoWithPrefix.setOrganizationIdentificationNumber(CaseUtil.getSokigoFormattedOrganizationNumber(organizationNumber));
        searchPartyWithPrefix.setModel(searchPartySvcDtoWithPrefix);
        ArrayOfPartySvcDto partiesWithPrefix = minutMiljoClient.searchParty(searchPartyWithPrefix).getSearchPartyResult();

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
     * @param personId personId for person to search for
     * @return ArrayOfPartySvcDto
     */
    private ArrayOfPartySvcDto searchPartyByPersonId(String personId) {
        SearchParty searchPartyWithHyphen = new SearchParty();
        SearchPartySvcDto searchPartySvcDtoWithHyphen = new SearchPartySvcDto();
        searchPartySvcDtoWithHyphen.setPersonalIdentificationNumber(CaseUtil.getSokigoFormattedPersonalNumber(citizenMappingService.getPersonalNumber(personId)));
        searchPartyWithHyphen.setModel(searchPartySvcDtoWithHyphen);

        var resultWithHyphen = minutMiljoClient.searchParty(searchPartyWithHyphen).getSearchPartyResult();

        if (resultWithHyphen != null && !resultWithHyphen.getPartySvcDto().isEmpty()) {
            return resultWithHyphen;
        } else {
            SearchParty searchPartyWithoutHyphen = new SearchParty();
            SearchPartySvcDto searchPartySvcDtoWithoutHyphen = new SearchPartySvcDto();
            searchPartySvcDtoWithoutHyphen.setPersonalIdentificationNumber(citizenMappingService.getPersonalNumber(personId));
            searchPartyWithoutHyphen.setModel(searchPartySvcDtoWithoutHyphen);

            return minutMiljoClient.searchParty(searchPartyWithoutHyphen).getSearchPartyResult();
        }
    }

    private ArrayOfSearchCaseResultSvcDto searchCase(String partyId) {
        SearchCase searchCase = new SearchCase();
        SearchCaseSvcDto searchCaseSvcDto = new SearchCaseSvcDto();
        ArrayOfFilterSvcDto arrayOfFilterSvcDto = new ArrayOfFilterSvcDto();
        SinglePartyRoleFilterSvcDto filter = new SinglePartyRoleFilterSvcDto();

        filter.setPartyId(partyId);
        filter.setRoleId(Constants.ECOS_ROLE_ID_VERKSAMHETSUTOVARE);
        arrayOfFilterSvcDto.getFilterSvcDto().add(filter);
        searchCaseSvcDto.setFilters(arrayOfFilterSvcDto);

        searchCase.setModel(searchCaseSvcDto);
        return minutMiljoClient.searchCase(searchCase).getSearchCaseResult();
    }

    private String createHealthProtectionFacility(EnvironmentalFacilityDTO eFacility, FbPropertyInfo propertyInfo, RegisterDocumentCaseResultSvcDto registerDocumentResult) {

        CreateHealthProtectionFacility createHealthProtectionFacility = new CreateHealthProtectionFacility();
        CreateHealthProtectionFacilitySvcDto createHealthProtectionFacilitySvcDto = new CreateHealthProtectionFacilitySvcDto();
        createHealthProtectionFacilitySvcDto.setAddress(getAddress(propertyInfo));
        createHealthProtectionFacilitySvcDto.setEstateDesignation(getEstateSvcDto(propertyInfo));
        createHealthProtectionFacilitySvcDto.setCase(registerDocumentResult.getCaseId());
        createHealthProtectionFacilitySvcDto.setNote(eFacility.getDescription());
        createHealthProtectionFacilitySvcDto.setFacilityCollectionName(eFacility.getFacilityCollectionName());
        createHealthProtectionFacility.setCreateHealthProtectionFacilitySvcDto(createHealthProtectionFacilitySvcDto);

        String facilityGuid = minutMiljoClient.createHealthProtectionFacility(createHealthProtectionFacility).getCreateHealthProtectionFacilityResult();

        if (facilityGuid != null) {
            log.debug("Health Protection Facility created: {}", facilityGuid);
            return facilityGuid;
        } else {
            throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "Health Protection Facility could not be created");
        }
    }

    private EstateSvcDto getEstateSvcDto(FbPropertyInfo propertyInfo) {
        EstateSvcDto estateSvcDto = new EstateSvcDto();
        estateSvcDto.setFnr(propertyInfo.getFnr());
        return estateSvcDto;
    }
}
