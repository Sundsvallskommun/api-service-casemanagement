package se.sundsvall.casemanagement.integration.byggr;

import static java.util.function.Predicate.not;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.WITH_NULLABLE_FACILITY_TYPE;
import static se.sundsvall.casemanagement.api.model.enums.FacilityType.FIREPLACE;
import static se.sundsvall.casemanagement.api.model.enums.FacilityType.FIREPLACE_SMOKECHANNEL;
import static se.sundsvall.casemanagement.util.Constants.HANDELSETYP_ANMALAN;
import static se.sundsvall.casemanagement.util.Constants.HANDELSETYP_ANSOKAN;
import static se.sundsvall.casemanagement.util.Constants.SERVICE_NAME;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.api.model.CaseStatusDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.PlanningPermissionCaseDTO;
import se.sundsvall.casemanagement.api.model.PlanningPermissionFacilityDTO;
import se.sundsvall.casemanagement.api.model.StakeholderDTO;
import se.sundsvall.casemanagement.api.model.enums.AddressCategory;
import se.sundsvall.casemanagement.api.model.enums.FacilityType;
import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.CaseTypeRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.integration.db.model.CaseTypeData;
import se.sundsvall.casemanagement.service.CaseMappingService;
import se.sundsvall.casemanagement.service.CitizenMappingService;
import se.sundsvall.casemanagement.service.FbService;
import se.sundsvall.casemanagement.util.CaseUtil;
import se.sundsvall.casemanagement.util.Constants;

import arendeexport.Arende;
import arendeexport.Arende2;
import arendeexport.ArendeFastighet;
import arendeexport.ArendeIntressent;
import arendeexport.ArrayOfAbstractArendeObjekt2;
import arendeexport.ArrayOfArende1;
import arendeexport.ArrayOfArendeIntressent2;
import arendeexport.ArrayOfHandling;
import arendeexport.ArrayOfIntressentKommunikation;
import arendeexport.ArrayOfString;
import arendeexport.ArrayOfString2;
import arendeexport.Dokument;
import arendeexport.DokumentFil;
import arendeexport.Fakturaadress;
import arendeexport.Fastighet;
import arendeexport.GetArende;
import arendeexport.GetRelateradeArendenByPersOrgNrAndRole;
import arendeexport.Handelse;
import arendeexport.HandelseHandling;
import arendeexport.HandlaggareBas;
import arendeexport.IntressentAttention;
import arendeexport.IntressentKommunikation;
import arendeexport.SaveNewArende;
import arendeexport.SaveNewArendeMessage;
import arendeexport.SaveNewArendeResponse2;
import arendeexport.SaveNewHandelse;
import arendeexport.SaveNewHandelseMessage;

@Service
public class ByggrService {

    private static final Logger log = LoggerFactory.getLogger(ByggrService.class);
    private final FbService fbService;
    private final CitizenMappingService citizenMappingService;
    private final CaseMappingService caseMappingService;
    private final ArendeExportClient arendeExportClient;
    private final Map<String, CaseTypeData> caseTypeMap = new HashMap<>();
    private final CaseTypeRepository caseTypeRepository;

    public ByggrService(FbService fbService, CitizenMappingService citizenMappingService, CaseMappingService caseMappingService, ArendeExportClient arendeExportClient, CaseTypeRepository caseTypeRepository) {
        this.fbService = fbService;
        this.citizenMappingService = citizenMappingService;
        this.caseMappingService = caseMappingService;
        this.arendeExportClient = arendeExportClient;
        this.caseTypeRepository = caseTypeRepository;
    }

    public SaveNewArendeResponse2 postCase(PlanningPermissionCaseDTO caseInput) {

        caseTypeRepository.findAll().forEach(caseTypeData -> caseTypeMap.put(caseTypeData.getValue(), caseTypeData));

        // This StringBuilder is used to create a note on the case with information about potential manual actions that is needed.
        var byggrAdminMessageSb = new StringBuilder();

        var saveNewArende = new SaveNewArende()
            .withMessage(new SaveNewArendeMessage()
                .withAnkomststamplaHandlingar(true)
                .withArende(getByggrCase(caseInput))
                .withHandlingar(getArrayOfHandling(caseInput.getAttachments()))
                .withHandelse(getByggrHandelse(caseInput))
                .withHandlaggarSign(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN));

        var response = arendeExportClient.saveNewArende(saveNewArende).getSaveNewArendeResult();

        // If it's something that we should inform the administrator about, we create a new occurrence in the case.
        if (containsControlOfficial(caseInput.getStakeholders())) {
            writeEventNote(Constants.BYGGR_HANDELSE_ANTECKNING_KONTROLLANSVARIG, byggrAdminMessageSb);
        }
        if (containsPersonDuplicates(caseInput.getStakeholders())) {
            writeEventNote(Constants.BYGGR_HANDELSE_ANTECKNING_INTRESSENT_KUNDE_INTE_REGISTRERAS, byggrAdminMessageSb);
        }
        if (!containsPropertyOwner(saveNewArende.getMessage().getArende().getIntressentLista().getIntressent())) {
            writeEventNote(Constants.BYGGR_HANDELSE_ANTECKNING_FASTIGHETSAGARE, byggrAdminMessageSb);
        }
        if (!byggrAdminMessageSb.toString().isEmpty()) {
            writeEventNote(Constants.BYGGR_HANDELSE_ANTECKNING_DU_MASTE_REGISTRERA_DETTA_MANUELLT, byggrAdminMessageSb);
            arendeExportClient.saveNewHandelse(saveNewManuellHanteringHandelse(response.getDnr(), byggrAdminMessageSb.toString()));
        }
        caseMappingService.postCaseMapping(CaseMapping.builder()
            .withExternalCaseId(caseInput.getExternalCaseId())
            .withCaseId(response.getDnr())
            .withSystem(SystemType.BYGGR)
            .withCaseType(caseInput.getCaseType())
            .withServiceName(Optional.ofNullable(caseInput.getExtraParameters())
                .orElse(Map.of())
                .get(SERVICE_NAME))
            .build());
        return response;
    }

    private void writeEventNote(String note, StringBuilder byggrAdminMessageSb) {
        if (!byggrAdminMessageSb.toString().contains(note)) {
            byggrAdminMessageSb.append(byggrAdminMessageSb.toString().isEmpty() ? "" : "\n\n").append(note);
        }
    }

    private boolean containsControlOfficial(List<StakeholderDTO> stakeholderDTOList) {
        for (StakeholderDTO s : stakeholderDTOList) {

            if (s.getRoles().contains(StakeholderRole.CONTROL_OFFICIAL)) {
                return true;
            }
        }

        return false;
    }

    private boolean containsPersonDuplicates(List<StakeholderDTO> stakeholderDTOList) {
        List<String> personIdList = filterPersonId(stakeholderDTOList);

        for (StakeholderDTO s : stakeholderDTOList) {
            // If the request contains two person with the same personId, it must be handled manually
            if (s instanceof PersonDTO personDTO && personIdList.stream().filter(personId -> personId.equals(personDTO.getPersonId())).count() > 1) {
                return true;
            }
        }
        return false;
    }

    private boolean containsPropertyOwner(List<ArendeIntressent> stakeholders) {

        for (ArendeIntressent stakeholder : stakeholders) {
            if (stakeholder.getRollLista().getRoll().contains(StakeholderRole.PROPERTY_OWNER.getText())) {
                return true;
            }
        }

        return false;
    }

    private Handelse getByggrHandelse(PlanningPermissionCaseDTO dto) {
        var caseType = caseTypeMap.get(dto.getCaseType().getValue());

        var handelse = new Handelse()
            .withStartDatum(LocalDateTime.now())
            .withRiktning(Constants.BYGGR_HANDELSE_RIKTNING_IN)
            .withRubrik(caseType.getHandelseRubrik())
            .withHandelsetyp(caseType.getHandelseTyp())
            .withHandelseslag(caseType.getHandelseSlag());

        if (dto.getFacilities().get(0).getFacilityType() != null) {

            if (dto.getFacilities().get(0).getFacilityType().equals(FIREPLACE)) {
                handelse
                    .withRubrik(Constants.BYGGR_HANDELSE_RUBRIK_ELDSTAD)
                    .withHandelseslag(Constants.BYGGR_HANDELSESLAG_ELDSTAD);
            } else if (dto.getFacilities().get(0).getFacilityType().equals(FIREPLACE_SMOKECHANNEL)) {
                handelse
                    .withRubrik(Constants.BYGGR_HANDELSE_RUBRIK_ELDSTAD_ROKKANAL)
                    .withHandelseslag(Constants.BYGGR_HANDELSESLAG_ELDSTAD_ROKKANAL);
            }
        }

        return handelse;
    }

    private SaveNewHandelse saveNewManuellHanteringHandelse(String dnr, String note) {
        SaveNewHandelse saveNewHandelse = new SaveNewHandelse();
        SaveNewHandelseMessage saveNewHandelseMessage = new SaveNewHandelseMessage();
        saveNewHandelseMessage.setDnr(dnr);
        saveNewHandelseMessage.setHandlaggarSign(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN);

        Handelse handelse = new Handelse();
        handelse.setRubrik(Constants.BYGGR_HANDELSE_RUBRIK_MANUELL_HANTERING);
        handelse.setRiktning(Constants.BYGGR_HANDELSE_RIKTNING_IN);
        handelse.setHandelsetyp(Constants.BYGGR_HANDELSETYP_STATUS);
        handelse.setHandelseslag(Constants.BYGGR_HANDELSESLAG_MANUELL_HANTERING_KRAVS);
        handelse.setStartDatum(LocalDateTime.now());
        handelse.setAnteckning(note);

        saveNewHandelseMessage.setHandelse(handelse);

        saveNewHandelse.setMessage(saveNewHandelseMessage);
        return saveNewHandelse;
    }

    public void saveNewIncomingAttachmentHandelse(String dnr, List<AttachmentDTO> attachmentDTOList) {
        var saveNewHandelseMessage = new SaveNewHandelseMessage()
            .withDnr(dnr)
            .withHandlaggarSign(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN)
            .withHandlingar(getArrayOfHandling(attachmentDTOList))
            .withAnkomststamplaHandlingar(true)
            .withHandelse(new Handelse()
                .withRiktning(Constants.BYGGR_HANDELSE_RIKTNING_IN)
                .withRubrik(Constants.BYGGR_HANDELSE_RUBRIK_KOMPLETTERING_TILL_ADMIN)
                .withHandelsetyp(Constants.BYGGR_HANDELSETYP_HANDLING)
                .withHandelseslag(Constants.BYGGR_HANDELSESLAG_KOMPLETTERING_TILL_ADMIN)
                .withStartDatum(LocalDateTime.now(ZoneId.systemDefault()))
                .withAnteckning(Constants.BYGGR_HANDELSE_ANTECKNING));

        var saveNewHandelse = new SaveNewHandelse()
            .withMessage(saveNewHandelseMessage);

        arendeExportClient.saveNewHandelse(saveNewHandelse);
    }


    private ArrayOfHandling getArrayOfHandling(List<AttachmentDTO> attachmentDTOList) {
        ArrayOfHandling arrayOfHandling = new ArrayOfHandling();
        arrayOfHandling.getHandling().addAll(getHandelseHandlingList(attachmentDTOList));
        return arrayOfHandling;
    }

    private List<HandelseHandling> getHandelseHandlingList(List<AttachmentDTO> attachmentDTOList) {
        List<HandelseHandling> handelseHandlingList = new ArrayList<>();
        for (AttachmentDTO file : attachmentDTOList) {
            HandelseHandling handling = new HandelseHandling();
            // The administrators in ByggR wants the name as a note to enable a quick overview of all documents.
            handling.setAnteckning(file.getName());

            Dokument doc = new Dokument();
            DokumentFil docFile = new DokumentFil();
            docFile.setFilBuffer(Base64.getDecoder().decode(file.getFile().getBytes()));
            docFile.setFilAndelse(file.getExtension().toLowerCase());
            doc.setFil(docFile);
            doc.setNamn(file.getName());
            doc.setBeskrivning(file.getNote());

            handling.setDokument(doc);

            handling.setStatus(Constants.BYGGR_HANDLING_STATUS_INKOMMEN);
            handling.setTyp(file.getCategory().name());

            handelseHandlingList.add(handling);
        }

        return handelseHandlingList;
    }

    private Arende2 getByggrCase(PlanningPermissionCaseDTO pCase) {


        var caseType = caseTypeMap.get(pCase.getCaseType().getValue());
        var arende = new Arende2();


        if (pCase.getFacilities() == null || pCase.getFacilities().get(0) == null) {
            arende.withArendeslag(caseType.getArendeSlag());

        } else if (caseType.getArendeSlag() != null) {
            arende.withArendeslag(caseType.getArendeSlag());
            if (!WITH_NULLABLE_FACILITY_TYPE.contains(pCase.getCaseType())) {
                arende.withArendeklass(getArendeKlass(pCase.getFacilities()));
            }
        } else {
            arende.withArendeslag(getMainOrOnlyArendeslag(pCase.getFacilities()));
        }

        return arende
            .withArendegrupp(caseType.getArendeGrupp())
            .withArendetyp(caseType.getArendeTyp())
            .withNamndkod(Constants.BYGGR_NAMNDKOD_STADSBYGGNADSNAMNDEN)
            .withEnhetkod(Constants.BYGGR_ENHETKOD_STADSBYGGNADSKONTORET)
            .withKommun(Constants.BYGGR_KOMMUNKOD_SUNDSVALL_KOMMUN)
            .withHandlaggare(new HandlaggareBas().withSignatur(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN))
            .withArInomplan(getInomPlan(pCase.getFacilities()))
            .withBeskrivning(getArendeBeskrivning(pCase, caseType.getArendeMening()))
            .withIntressentLista(getByggrIntressenter(pCase))
            .withObjektLista(getByggrArendeObjektLista(pCase))
            .withAnkomstDatum(LocalDate.now())
            // Projnr/Faktid in ByggR.
            .withProjektnr(Optional.ofNullable(getInvoiceMarking(pCase))
                .orElse(parsePropertyDesignation(pCase.getFacilities())));
    }


    private String parsePropertyDesignation(List<PlanningPermissionFacilityDTO> facilities) {
        var propertyDesignation = getPropertyDesignation(facilities);
        if (propertyDesignation != null && propertyDesignation.startsWith("SUNDSVALL ")) {
            propertyDesignation = propertyDesignation.substring(propertyDesignation.indexOf(" ") + 1);
        }
        return propertyDesignation;
    }

    private String getInvoiceMarking(PlanningPermissionCaseDTO pCase) {
        String invoiceMarking = null;

        for (StakeholderDTO stakeholderDTO : pCase.getStakeholders()) {
            if (stakeholderDTO.getAddresses() != null) {
                for (AddressDTO addressDTO : stakeholderDTO.getAddresses()) {
                    if (addressDTO.getAddressCategories().contains(AddressCategory.INVOICE_ADDRESS)
                        && addressDTO.getInvoiceMarking() != null && !addressDTO.getInvoiceMarking().isBlank()) {
                        invoiceMarking = addressDTO.getInvoiceMarking();
                    }
                }
            }
        }
        return invoiceMarking;
    }

    /**
     * "Ärendemening" - Is automatically set in ByggR based on "typ", "slag" and "klass",
     * but when its multiple facilities, it must be set to contain all facilities.
     *
     * @param pCase PlanningPermissionCase
     * @return ärendemening or null
     */
    private String getArendeBeskrivning(PlanningPermissionCaseDTO pCase, String arendeMening) {

        if (arendeMening == null || pCase.getFacilities().isEmpty()) {
            return null;
        }

        var arendeMeningBuilder = new StringBuilder(arendeMening);

        var facilityList = pCase.getFacilities().stream()
            .filter(facility -> facility.getFacilityType() != null)
            .sorted(Comparator.comparing(PlanningPermissionFacilityDTO::isMainFacility, Comparator.reverseOrder()))
            .toList();

        IntStream.range(0, facilityList.size())
            .forEach(i -> {
                if (facilityList.size() > 1) {
                    if (i == facilityList.size() - 1) {
                        arendeMeningBuilder.append(" &");
                    } else if (i != 0) {
                        arendeMeningBuilder.append(",");
                    }
                }
                arendeMeningBuilder
                    .append(" ")
                    .append(facilityList.get(i)
                        .getFacilityType()
                        .getDescription()
                        .trim()
                        .toLowerCase());
            });

        if (pCase.getCaseTitleAddition() != null && !pCase.getCaseTitleAddition().isBlank()) {
            arendeMeningBuilder.append(" samt ").append(pCase.getCaseTitleAddition().trim().toLowerCase());
        }

        return arendeMeningBuilder.toString();

    }

    private String getPropertyDesignation(List<PlanningPermissionFacilityDTO> facilityList) {
        PlanningPermissionFacilityDTO facility = getMainOrTheOnlyFacility(facilityList);
        return facility != null ? facility.getAddress().getPropertyDesignation().trim().toUpperCase() : null;
    }

    private Boolean getInomPlan(List<PlanningPermissionFacilityDTO> facilityList) {
        return facilityList.stream()
            .findFirst()
            .map(PlanningPermissionFacilityDTO::getAddress)
            .map(AddressDTO::getIsZoningPlanArea)
            .orElse(null);
    }


    private String getArendeKlass(List<PlanningPermissionFacilityDTO> facilityList) {
        return facilityList.stream()
            .findFirst()
            .map(PlanningPermissionFacilityDTO::getFacilityType)
            .map(FacilityType::getValue)
            .orElse(FacilityType.OTHER.getValue());
    }

    private String getMainOrOnlyArendeslag(List<PlanningPermissionFacilityDTO> facilityList) {
        return facilityList.stream()
            .filter(facility -> facility.getFacilityType().equals(FacilityType.USAGE_CHANGE))
            .findFirst()
            .orElse(facilityList.get(0))
            .getFacilityType()
            .getValue();
    }

    private PlanningPermissionFacilityDTO getMainOrTheOnlyFacility(List<PlanningPermissionFacilityDTO> facilityList) {
        if (facilityList.size() == 1) {
            // The list only contains one facility, return it.
            return facilityList.get(0);
        }

        // If the list contains more than one facility and mainFacility exists, return it.
        // If the list doesn't contain a mainFacility, return null.
        return facilityList.stream().anyMatch(PlanningPermissionFacilityDTO::isMainFacility) ?
            facilityList.stream().filter(PlanningPermissionFacilityDTO::isMainFacility).toList().get(0) : null;
    }

    private ArrayOfAbstractArendeObjekt2 getByggrArendeObjektLista(PlanningPermissionCaseDTO pCase) {

        List<String> usedPropertyDesignations = new ArrayList<>();
        ArrayOfAbstractArendeObjekt2 arendeObjektLista = new ArrayOfAbstractArendeObjekt2();

        pCase.getFacilities().forEach(f -> {
            if (usedPropertyDesignations.contains(f.getAddress().getPropertyDesignation())) {
                // If we already have created a "arendeFastighet" with the same propertyDesignation,
                // we should not create a duplicate. Skip this iteration.
                return;
            }

            ArendeFastighet arendeFastighet = new ArendeFastighet();

            arendeFastighet.setArHuvudObjekt(f.isMainFacility());

            Fastighet fastighet = new Fastighet();
            fastighet.setFnr(fbService.getPropertyInfoByPropertyDesignation(f.getAddress().getPropertyDesignation()).getFnr());

            arendeFastighet.setFastighet(fastighet);

            arendeObjektLista.getAbstractArendeObjekt().add(arendeFastighet);
            usedPropertyDesignations.add(f.getAddress().getPropertyDesignation());
        });

        return arendeObjektLista;
    }

    private ArrayOfArendeIntressent2 getByggrIntressenter(PlanningPermissionCaseDTO pCase) {

        // Add all stakeholders from case to the list
        List<StakeholderDTO> stakeholderDTOList = new ArrayList<>(pCase.getStakeholders());
        populateStakeholderListWithPropertyOwners(pCase, stakeholderDTOList);

        ArrayOfArendeIntressent2 intressenter = new ArrayOfArendeIntressent2();

        List<String> personIdList = filterPersonId(stakeholderDTOList);

        for (StakeholderDTO s : stakeholderDTOList) {
            // We don't create stakeholders with the role "Kontrollansvarig", this must be handled manually.
            if (s.getRoles().contains(StakeholderRole.CONTROL_OFFICIAL)) {
                continue;
            }

            ArendeIntressent intressent = new ArendeIntressent();

            if (s instanceof PersonDTO personDTO) {

                // If the request contains two person with the same personId, it must be handled manually
                if (personIdList.stream().filter(personId -> personId.equals(personDTO.getPersonId())).count() > 1) {
                    continue;
                } else {
                    setPersonFields(intressent, personDTO);
                }

            } else if (s instanceof OrganizationDTO organizationDTO) {
                setOrganizationFields(intressent, organizationDTO);
            }

            if (s.getAddresses() != null) {
                for (AddressDTO addressDTO : s.getAddresses()) {
                    for (AddressCategory addressCategory : addressDTO.getAddressCategories()) {
                        if (addressCategory.equals(AddressCategory.POSTAL_ADDRESS)) {

                            setPostalAddressFields(intressent, addressDTO);

                            if (s instanceof OrganizationDTO) {
                                IntressentAttention intressentAttention = new IntressentAttention();
                                intressentAttention.setAttention(addressDTO.getAttention());
                                intressent.setAttention(intressentAttention);
                            }

                        }
                        if (addressCategory.equals(AddressCategory.INVOICE_ADDRESS)) {
                            if (s instanceof PersonDTO) {
                                throw Problem.valueOf(Status.BAD_REQUEST, Constants.ERR_MSG_PERSON_INVOICE_ADDRESS);
                            }

                            intressent.setFakturaAdress(getByggrFakturaadress(addressDTO));
                        }
                    }

                }
            }

            intressent.setIntressentKommunikationLista(getByggrContactInfo(s, intressent.getAttention()));
            intressent.setRollLista(getByggrRoles(s));
            intressenter.getIntressent().add(intressent);
        }


        return intressenter;
    }

    private void populateStakeholderListWithPropertyOwners(PlanningPermissionCaseDTO pCase, List<StakeholderDTO> stakeholderDTOList) {
        // Filter all persons
        List<PersonDTO> personDTOStakeholders = stakeholderDTOList.stream()
            .filter(PersonDTO.class::isInstance)
            .map(PersonDTO.class::cast)
            .toList();

        // Filter all organizations
        List<OrganizationDTO> organizationDTOStakeholders = stakeholderDTOList.stream()
            .filter(OrganizationDTO.class::isInstance)
            .map(OrganizationDTO.class::cast)
            .toList();

        // Populate personalNumber for every person
        for (PersonDTO personDTOStakeholder : personDTOStakeholders) {
            String pnr = citizenMappingService.getPersonalNumber(personDTOStakeholder.getPersonId());
            if (pnr != null && pnr.length() == 12) {
                pnr = pnr.substring(0, 8) + "-" + pnr.substring(8);
            }
            personDTOStakeholder.setPersonalNumber(pnr);
        }

        // Change the organization number for each organization so that it follows the "Sokigo-format"
        organizationDTOStakeholders.forEach(organization -> organization.setOrganizationNumber(CaseUtil.getSokigoFormattedOrganizationNumber(organization.getOrganizationNumber())));

        // Loop through each facility and get the property owners for each one
        pCase.getFacilities().forEach(facility -> {
            List<StakeholderDTO> propertyOwnerList = fbService.getPropertyOwnerByPropertyDesignation(facility.getAddress().getPropertyDesignation());

            populateStakeholderListWithPropertyOwnerPersons(personDTOStakeholders, stakeholderDTOList, propertyOwnerList);
            populateStakeholderListWithPropertyOwnerOrganizations(organizationDTOStakeholders, stakeholderDTOList, propertyOwnerList);
        });
    }

    private void populateStakeholderListWithPropertyOwnerPersons(List<PersonDTO> personDTOStakeholderList, List<StakeholderDTO> stakeholderDTOList, List<StakeholderDTO> propertyOwnerList) {
        List<PersonDTO> personDTOPropertyOwnerList = propertyOwnerList.stream()
            .filter(PersonDTO.class::isInstance)
            .map(PersonDTO.class::cast).toList();

        // All incoming personStakeholders that is also propertyOwners
        List<PersonDTO> personDTOStakeholderPropertyOwnerList = personDTOStakeholderList.stream()
            .filter(personStakeholder ->
                personDTOPropertyOwnerList.stream()
                    .map(PersonDTO::getPersonalNumber).toList()
                    .contains(personStakeholder.getPersonalNumber()))
            .toList();

        log.debug("All incoming personStakeholders that is also propertyOwners: {}", personDTOStakeholderPropertyOwnerList);

        personDTOStakeholderPropertyOwnerList.forEach(person -> person.setRoles(Stream.of(person.getRoles(), List.of(StakeholderRole.PROPERTY_OWNER))
            .flatMap(Collection::stream)
            .toList()));

        // All personPropertyOwners that does not exist in the incoming request
        List<PersonDTO> notExistingPersonPropertyOwnerListDTO = personDTOPropertyOwnerList.stream()
            .filter(not(personPropertyOwner ->
                personDTOStakeholderList.stream()
                    .map(PersonDTO::getPersonalNumber).toList()
                    .contains(personPropertyOwner.getPersonalNumber())))
            .toList();

        log.debug("All personPropertyOwners that does not exist in the incoming request: {}", notExistingPersonPropertyOwnerListDTO);

        stakeholderDTOList.addAll(notExistingPersonPropertyOwnerListDTO);
    }

    private void populateStakeholderListWithPropertyOwnerOrganizations(List<OrganizationDTO> organizationDTOStakeholders, List<StakeholderDTO> stakeholderDTOList, List<StakeholderDTO> propertyOwnerList) {
        List<OrganizationDTO> organizationDTOPropertyOwnerList = propertyOwnerList.stream()
            .filter(OrganizationDTO.class::isInstance)
            .map(OrganizationDTO.class::cast)
            .toList();

        // All incoming organizationStakeholders that is also propertyOwners
        List<OrganizationDTO> organizationDTOStakeholderPropertyOwnerList = organizationDTOStakeholders.stream()
            .filter(organizationStakeholder ->
                organizationDTOPropertyOwnerList.stream()
                    .map(OrganizationDTO::getOrganizationNumber)
                    .toList()
                    .contains(organizationStakeholder.getOrganizationNumber()))
            .toList();

        log.debug("All incoming organizationStakeholders that is also propertyOwners: {}", organizationDTOStakeholderPropertyOwnerList);

        organizationDTOStakeholderPropertyOwnerList.forEach(orgStakeholder -> orgStakeholder.setRoles(Stream.of(orgStakeholder.getRoles(), List.of(StakeholderRole.PROPERTY_OWNER))
            .flatMap(Collection::stream)
            .toList()));

        // All organizationPropertyOwners that does not exist in the incoming request
        List<OrganizationDTO> notExistingOrgPropertyOwnerList = organizationDTOPropertyOwnerList.stream()
            .filter(not(organizationPropertyOwner ->
                organizationDTOStakeholders.stream()
                    .map(OrganizationDTO::getOrganizationNumber)
                    .toList()
                    .contains(organizationPropertyOwner.getOrganizationNumber())))
            .toList();

        log.debug("All organizationPropertyOwners that does not exist in the incoming request: {}", notExistingOrgPropertyOwnerList);

        stakeholderDTOList.addAll(notExistingOrgPropertyOwnerList);
    }

    private List<String> filterPersonId(List<StakeholderDTO> stakeholderDTOList) {
        return stakeholderDTOList.stream()
            .filter(PersonDTO.class::isInstance)
            .map(PersonDTO.class::cast)
            .map(PersonDTO::getPersonId)
            .filter(Objects::nonNull)
            .toList();
    }

    void setPostalAddressFields(ArendeIntressent intressent, AddressDTO addressDTO) {
        intressent.setAdress(addressDTO.getHouseNumber() != null
            ? addressDTO.getStreet() + " " + addressDTO.getHouseNumber()
            : addressDTO.getStreet());
        intressent.setPostNr(addressDTO.getPostalCode());
        intressent.setOrt(addressDTO.getCity());
        intressent.setLand(addressDTO.getCountry());

        intressent.setCoAdress(addressDTO.getCareOf());
    }

    void setOrganizationFields(ArendeIntressent intressent, OrganizationDTO organizationDTO) {
        intressent.setArForetag(true);
        intressent.setNamn(organizationDTO.getOrganizationName());
        intressent.setPersOrgNr(organizationDTO.getOrganizationNumber());
    }

    void setPersonFields(ArendeIntressent intressent, PersonDTO personDTO) {
        intressent.setArForetag(false);
        intressent.setFornamn(personDTO.getFirstName());
        intressent.setEfternamn(personDTO.getLastName());
        intressent.setPersOrgNr(personDTO.getPersonalNumber());

    }

    ArrayOfString2 getByggrRoles(StakeholderDTO s) {
        ArrayOfString2 roles = new ArrayOfString2();
        s.getRoles().stream().distinct().toList().forEach(r -> roles.getRoll().add(r.getText()));
        return roles;
    }

    Fakturaadress getByggrFakturaadress(AddressDTO addressDTO) {
        Fakturaadress fakturaAdress = new Fakturaadress();
        fakturaAdress.setAdress(addressDTO.getHouseNumber() != null
            ? addressDTO.getStreet() + " " + addressDTO.getHouseNumber()
            : addressDTO.getStreet());
        fakturaAdress.setAttention(addressDTO.getAttention());
        fakturaAdress.setLand(addressDTO.getCountry());
        fakturaAdress.setOrt(addressDTO.getCity());
        fakturaAdress.setPostNr(addressDTO.getPostalCode());
        return fakturaAdress;
    }

    ArrayOfIntressentKommunikation getByggrContactInfo(StakeholderDTO s, IntressentAttention intressentAttention) {
        ArrayOfIntressentKommunikation arrayOfIntressentKommunikation = new ArrayOfIntressentKommunikation();
        if (notNullOrBlank(s.getCellphoneNumber())) {
            IntressentKommunikation intressentKommunikation = new IntressentKommunikation();
            intressentKommunikation.setArAktiv(true);
            intressentKommunikation.setBeskrivning(s.getCellphoneNumber());
            intressentKommunikation.setKomtyp(Constants.BYGGR_KOMTYP_MOBIL);
            intressentKommunikation.setAttention(intressentAttention);
            arrayOfIntressentKommunikation.getIntressentKommunikation().add(intressentKommunikation);
        }
        if (notNullOrBlank(s.getPhoneNumber())) {
            IntressentKommunikation intressentKommunikation = new IntressentKommunikation();
            intressentKommunikation.setArAktiv(true);
            intressentKommunikation.setBeskrivning(s.getPhoneNumber());
            intressentKommunikation.setKomtyp(Constants.BYGGR_KOMTYP_HEMTELEFON);
            intressentKommunikation.setAttention(intressentAttention);
            arrayOfIntressentKommunikation.getIntressentKommunikation().add(intressentKommunikation);
        }
        if (notNullOrBlank(s.getEmailAddress())) {
            IntressentKommunikation intressentKommunikation = new IntressentKommunikation();
            intressentKommunikation.setArAktiv(true);
            intressentKommunikation.setBeskrivning(s.getEmailAddress());
            intressentKommunikation.setKomtyp(Constants.BYGGR_KOMTYP_EPOST);
            intressentKommunikation.setAttention(intressentAttention);
            arrayOfIntressentKommunikation.getIntressentKommunikation().add(intressentKommunikation);
        }
        return arrayOfIntressentKommunikation;
    }

    private boolean notNullOrBlank(String string) {
        return string != null && !string.isBlank();
    }

    public CaseStatusDTO getByggrStatus(String caseId, String externalCaseId) {
        return getByggrStatus(getArende(caseId), externalCaseId);
    }

    /**
     * @return CaseStatus from ByggR.
     */
    private CaseStatusDTO getByggrStatus(Arende arende, String externalCaseId) {
        CaseStatusDTO caseStatusDTO = new CaseStatusDTO();
        caseStatusDTO.setSystem(SystemType.BYGGR);
        caseStatusDTO.setExternalCaseId(externalCaseId);
        caseStatusDTO.setCaseId(arende.getDnr());
        List<CaseMapping> caseMappingList = caseMappingService.getCaseMapping(externalCaseId, arende.getDnr());
        caseStatusDTO.setCaseType(caseMappingList.isEmpty() ? null : caseMappingList.get(0).getCaseType());
        caseStatusDTO.setServiceName(caseMappingList.isEmpty() ? null : caseMappingList.get(0).getServiceName());

        // OEP-status = Ärendet arkiveras
        if (arende.getStatus() != null && arende.getStatus().equals(Constants.BYGGR_STATUS_AVSLUTAT)) {
            // If the case is closed, we don't need to check for any more occurrence
            caseStatusDTO.setStatus(arende.getStatus());
            return caseStatusDTO;
        }

        if (arende.getHandelseLista() != null
            && arende.getHandelseLista().getHandelse() != null) {
            List<Handelse> handelseLista = arende.getHandelseLista().getHandelse();

            handelseLista.sort(Comparator.comparing(Handelse::getStartDatum).reversed());

            for (Handelse h : handelseLista) {

                caseStatusDTO.setStatus(getHandelseStatus(h.getHandelsetyp(), h.getHandelseslag(), h.getHandelseutfall()));

                if (caseStatusDTO.getStatus() != null) {
                    caseStatusDTO.setTimestamp(h.getStartDatum());
                    return caseStatusDTO;
                }
            }
        }
        throw Problem.valueOf(Status.NOT_FOUND, Constants.ERR_MSG_STATUS_NOT_FOUND);
    }

    private Arende getArende(String caseId) {
        GetArende getArende = new GetArende();
        getArende.setDnr(caseId);

        return arendeExportClient.getArende(getArende).getGetArendeResult();
    }

    private String getHandelseStatus(String handelsetyp, String handelseslag, String handelseutfall) {

        // OEP-status = Inskickat
        if (HANDELSETYP_ANMALAN.equals(handelsetyp)
            || HANDELSETYP_ANSOKAN.equals(handelsetyp)) {
            // ANM, ANSÖKAN
            return handelsetyp;
        }

        // OEP-status = Klart
        else if (Constants.BYGGR_HANDELSETYP_BESLUT.equals(handelsetyp)
            && (Constants.BYGGR_HANDELSESLAG_SLUTBESKED.equals(handelseslag)
            || Constants.BYGGR_HANDELSESLAG_AVSKRIVNING.equals(handelseslag))) {
            // SLU, UAB
            return handelseslag;
        }

        // OEP-status = Kompletterad
        else if (Constants.BYGGR_HANDELSETYP_HANDLING.equals(handelsetyp)
            && Constants.BYGGR_HANDELSESLAG_KOMPLETTERANDE_HANDLINGAR.equals(handelseslag)
            || Constants.BYGGR_HANDELSESLAG_KOMPLETTERANDE_BYGGLOVHANDLINGAR.equals(handelseslag)
            || Constants.BYGGR_HANDELSESLAG_KOMPLETTERANDE_TEKNISKA_HANDLINGAR.equals(handelseslag)
            || Constants.BYGGR_HANDELSESLAG_REVIDERADE_HANDLINGAR.equals(handelseslag)) {
            // KOMPL, KOMPBYGG, KOMPTEK, KOMPREV
            return handelseslag;
        }

        // OEP-status = Under behandling
        else if (Constants.BYGGR_HANDELSETYP_ATOMHANDELSE.equals(handelsetyp)
            && Constants.BYGGR_HANDELSESLAG_ATOM_KVITTENS.equals(handelseslag)
            && Constants.BYGGR_HANDELSEUTFALL_ATOM_KVITTENS_HL_BYTE.equals(handelseutfall)) {
            // Kv2
            return handelseutfall;
        } else if (Constants.BYGGR_HANDELSETYP_REMISS.equals(handelsetyp)
            && Constants.BYGGR_HANDELSESLAG_UTSKICK_AV_REMISS.equals(handelseslag)) {
            // UTSKICK
            return handelseslag;
        } else if (Constants.BYGGR_HANDELSETYP_UNDERRATTELSE.equals(handelsetyp)
            && (Constants.BYGGR_HANDELSESLAG_MED_KRAV_PA_SVAR.equals(handelseslag) || Constants.BYGGR_HANDELSESLAG_UTAN_KRAV_PA_SVAR.equals(handelseslag))) {
            // UNDER
            return handelsetyp;
        }

        // OEP-status = Väntar på komplettering
        else if (Constants.BYGGR_HANDELSETYP_KOMPLETTERINGSFORELAGGANDE.equals(handelsetyp)
            || Constants.BYGGR_HANDELSETYP_KOMPLETTERINGSFORELAGGANDE_PAMINNELSE.equals(handelsetyp)) {
            // KOMP, KOMP1
            return handelsetyp;
        }

        return null;
    }

    public List<CaseStatusDTO> getByggrStatusByOrgNr(String organizationNumber) {
        List<CaseStatusDTO> caseStatusDTOList = new ArrayList<>();
        ArrayOfString arendeIntressentRoller = new ArrayOfString();
        arendeIntressentRoller.getString().add(StakeholderRole.APPLICANT.getText());
        ArrayOfString handelseIntressentRoller = new ArrayOfString();
        handelseIntressentRoller.getString().add(StakeholderRole.APPLICANT.getText());

        GetRelateradeArendenByPersOrgNrAndRole getRelateradeArendenByPersOrgNrAndRoleInput = new GetRelateradeArendenByPersOrgNrAndRole();
        getRelateradeArendenByPersOrgNrAndRoleInput.setPersOrgNr(organizationNumber);
        getRelateradeArendenByPersOrgNrAndRoleInput.setArendeIntressentRoller(arendeIntressentRoller);
        getRelateradeArendenByPersOrgNrAndRoleInput.setHandelseIntressentRoller(handelseIntressentRoller);
        ArrayOfArende1 arrayOfByggrArende = arendeExportClient.getRelateradeArendenByPersOrgNrAndRole(getRelateradeArendenByPersOrgNrAndRoleInput).getGetRelateradeArendenByPersOrgNrAndRoleResult();

        if (arrayOfByggrArende != null) {
            if (arrayOfByggrArende.getArende().isEmpty()) {
                String modifiedOrgNr = CaseUtil.getSokigoFormattedOrganizationNumber(organizationNumber);
                getRelateradeArendenByPersOrgNrAndRoleInput.setPersOrgNr(modifiedOrgNr);
                arrayOfByggrArende = arendeExportClient.getRelateradeArendenByPersOrgNrAndRole(getRelateradeArendenByPersOrgNrAndRoleInput).getGetRelateradeArendenByPersOrgNrAndRoleResult();
            }

            arrayOfByggrArende.getArende().forEach(byggrArende -> {
                List<CaseMapping> caseMappingList = caseMappingService.getCaseMapping(null, byggrArende.getDnr());
                CaseStatusDTO status = getByggrStatus(byggrArende, caseMappingList.isEmpty() ? null : caseMappingList.get(0).getExternalCaseId());
                caseStatusDTOList.add(status);
            });
        }

        return caseStatusDTOList;
    }
}
