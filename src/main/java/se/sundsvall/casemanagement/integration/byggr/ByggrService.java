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
import se.sundsvall.casemanagement.service.CitizenService;
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

	private final CitizenService citizenService;

	private final CaseMappingService caseMappingService;

	private final ArendeExportClient arendeExportClient;

	private final Map<String, CaseTypeData> caseTypeMap = new HashMap<>();

	private final CaseTypeRepository caseTypeRepository;

	public ByggrService(final FbService fbService, final CitizenService citizenService, final CaseMappingService caseMappingService, final ArendeExportClient arendeExportClient, final CaseTypeRepository caseTypeRepository) {
		this.fbService = fbService;
		this.citizenService = citizenService;
		this.caseMappingService = caseMappingService;
		this.arendeExportClient = arendeExportClient;
		this.caseTypeRepository = caseTypeRepository;
	}

	public SaveNewArendeResponse2 postCase(final PlanningPermissionCaseDTO caseInput) {

		caseTypeRepository.findAll().forEach(caseTypeData -> caseTypeMap.put(caseTypeData.getValue(), caseTypeData));
		// This StringBuilder is used to create a note on the case with information about potential manual actions that is
		// needed.
		final var byggrAdminMessageSb = new StringBuilder();
		final var saveNewArende = new SaveNewArende()
			.withMessage(new SaveNewArendeMessage()
				.withAnkomststamplaHandlingar(true)
				.withArende(getByggrCase(caseInput))
				.withHandlingar(getArrayOfHandling(caseInput.getAttachments()))
				.withHandelse(getByggrHandelse(caseInput))
				.withHandlaggarSign(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN));

		final var response = arendeExportClient.saveNewArende(saveNewArende).getSaveNewArendeResult();

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

	private void writeEventNote(final String note, final StringBuilder byggrAdminMessageSb) {
		if (!byggrAdminMessageSb.toString().contains(note)) {
			byggrAdminMessageSb.append(byggrAdminMessageSb.toString().isEmpty() ? "" : "\n\n").append(note);
		}
	}

	private boolean containsControlOfficial(final List<StakeholderDTO> stakeholderDTOList) {
		for (final StakeholderDTO s : stakeholderDTOList) {

			if (s.getRoles().contains(StakeholderRole.CONTROL_OFFICIAL.toString())) {
				return true;
			}
		}

		return false;
	}

	private boolean containsPersonDuplicates(final List<StakeholderDTO> stakeholderDTOList) {
		final List<String> personIdList = filterPersonId(stakeholderDTOList);

		for (final StakeholderDTO s : stakeholderDTOList) {
			// If the request contains two person with the same personId, it must be handled manually
			if (s instanceof final PersonDTO personDTO && (personIdList.stream().filter(personId -> personId.equals(personDTO.getPersonId())).count() > 1)) {
				return true;
			}
		}
		return false;
	}

	private boolean containsPropertyOwner(final List<ArendeIntressent> stakeholders) {

		for (final ArendeIntressent stakeholder : stakeholders) {
			if (stakeholder.getRollLista().getRoll().contains(StakeholderRole.PROPERTY_OWNER.getText())) {
				return true;
			}
		}

		return false;
	}

	private Handelse getByggrHandelse(final PlanningPermissionCaseDTO dto) {
		final var caseType = caseTypeMap.get(dto.getCaseType());
		final var handelse = new Handelse()
			.withStartDatum(LocalDateTime.now())
			.withRiktning(Constants.BYGGR_HANDELSE_RIKTNING_IN)
			.withRubrik(caseType.getHandelseRubrik())
			.withHandelsetyp(caseType.getHandelseTyp())
			.withHandelseslag(caseType.getHandelseSlag());

		if (dto.getFacilities().getFirst().getFacilityType() != null) {

			if (FIREPLACE.equals(FacilityType.valueOf(dto.getFacilities().getFirst().getFacilityType()))) {
				handelse
					.withRubrik(Constants.BYGGR_HANDELSE_RUBRIK_ELDSTAD)
					.withHandelseslag(Constants.BYGGR_HANDELSESLAG_ELDSTAD);
			} else if (FIREPLACE_SMOKECHANNEL.equals(FacilityType.valueOf(dto.getFacilities().getFirst().getFacilityType()))) {
				handelse
					.withRubrik(Constants.BYGGR_HANDELSE_RUBRIK_ELDSTAD_ROKKANAL)
					.withHandelseslag(Constants.BYGGR_HANDELSESLAG_ELDSTAD_ROKKANAL);
			}
		}

		return handelse;
	}

	private SaveNewHandelse saveNewManuellHanteringHandelse(final String dnr, final String note) {
		final SaveNewHandelse saveNewHandelse = new SaveNewHandelse();
		final SaveNewHandelseMessage saveNewHandelseMessage = new SaveNewHandelseMessage();
		saveNewHandelseMessage.setDnr(dnr);
		saveNewHandelseMessage.setHandlaggarSign(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN);

		final Handelse handelse = new Handelse();
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

	public void saveNewIncomingAttachmentHandelse(final String dnr, final List<AttachmentDTO> attachmentDTOList) {
		final var saveNewHandelseMessage = new SaveNewHandelseMessage()
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

		final var saveNewHandelse = new SaveNewHandelse()
			.withMessage(saveNewHandelseMessage);
		arendeExportClient.saveNewHandelse(saveNewHandelse);
	}

	private ArrayOfHandling getArrayOfHandling(final List<AttachmentDTO> attachmentDTOList) {
		final ArrayOfHandling arrayOfHandling = new ArrayOfHandling();
		arrayOfHandling.getHandling().addAll(getHandelseHandlingList(attachmentDTOList));
		return arrayOfHandling;
	}

	private List<HandelseHandling> getHandelseHandlingList(final List<AttachmentDTO> attachmentDTOList) {
		final List<HandelseHandling> handelseHandlingList = new ArrayList<>();
		for (final AttachmentDTO file : attachmentDTOList) {
			final HandelseHandling handling = new HandelseHandling();
			// The administrators in ByggR wants the name as a note to enable a quick overview of all documents.
			handling.setAnteckning(file.getName());

			final Dokument doc = new Dokument();
			final DokumentFil docFile = new DokumentFil();
			docFile.setFilBuffer(Base64.getDecoder().decode(file.getFile().getBytes()));
			docFile.setFilAndelse(file.getExtension().toLowerCase());
			doc.setFil(docFile);
			doc.setNamn(file.getName());
			doc.setBeskrivning(file.getNote());

			handling.setDokument(doc);

			handling.setStatus(Constants.BYGGR_HANDLING_STATUS_INKOMMEN);
			handling.setTyp(file.getCategory());

			handelseHandlingList.add(handling);
		}

		return handelseHandlingList;
	}

	private Arende2 getByggrCase(final PlanningPermissionCaseDTO pCase) {

		final var caseType = caseTypeMap.get(pCase.getCaseType());
		final var arende = new Arende2();

		if ((pCase.getFacilities() == null) || (pCase.getFacilities().getFirst() == null)) {
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

	private String parsePropertyDesignation(final List<PlanningPermissionFacilityDTO> facilities) {
		var propertyDesignation = getPropertyDesignation(facilities);
		if ((propertyDesignation != null) && propertyDesignation.startsWith("SUNDSVALL ")) {
			propertyDesignation = propertyDesignation.substring(propertyDesignation.indexOf(" ") + 1);
		}
		return propertyDesignation;
	}

	private String getInvoiceMarking(final PlanningPermissionCaseDTO pCase) {
		String invoiceMarking = null;

		for (final StakeholderDTO stakeholderDTO : pCase.getStakeholders()) {
			if (stakeholderDTO.getAddresses() != null) {
				for (final AddressDTO addressDTO : stakeholderDTO.getAddresses()) {
					if (addressDTO.getAddressCategories().contains(AddressCategory.INVOICE_ADDRESS)
						&& (addressDTO.getInvoiceMarking() != null) && !addressDTO.getInvoiceMarking().isBlank()) {
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
	private String getArendeBeskrivning(final PlanningPermissionCaseDTO pCase, final String arendeMening) {

		if ((arendeMening == null) || pCase.getFacilities().isEmpty()) {
			return null;
		}

		final var arendeMeningBuilder = new StringBuilder(arendeMening);

		final var facilityList = pCase.getFacilities().stream()
			.filter(facility -> facility.getFacilityType() != null)
			.sorted(Comparator.comparing(PlanningPermissionFacilityDTO::isMainFacility, Comparator.reverseOrder()))
			.toList();

		IntStream.range(0, facilityList.size())
			.forEach(i -> {
				if (facilityList.size() > 1) {
					if (i == (facilityList.size() - 1)) {
						arendeMeningBuilder.append(" &");
					} else if (i != 0) {
						arendeMeningBuilder.append(",");
					}
				}
				arendeMeningBuilder
					.append(" ")
					.append(
						FacilityType.valueOf(facilityList.get(i).getFacilityType())
							.getDescription()
							.trim()
							.toLowerCase());
			});


		if ((pCase.getCaseTitleAddition() != null) && !pCase.getCaseTitleAddition().isBlank()) {
			arendeMeningBuilder.append(" samt ").append(pCase.getCaseTitleAddition().trim().toLowerCase());
		}

		return arendeMeningBuilder.toString();
	}

	private String getPropertyDesignation(final List<PlanningPermissionFacilityDTO> facilityList) {
		final PlanningPermissionFacilityDTO facility = getMainOrTheOnlyFacility(facilityList);
		return facility != null ? facility.getAddress().getPropertyDesignation().trim().toUpperCase() : null;
	}

	private Boolean getInomPlan(final List<PlanningPermissionFacilityDTO> facilityList) {
		return facilityList.stream()
			.findFirst()
			.map(PlanningPermissionFacilityDTO::getAddress)
			.map(AddressDTO::getIsZoningPlanArea)
			.orElse(null);
	}

	private String getArendeKlass(final List<PlanningPermissionFacilityDTO> facilityList) {
		return facilityList.stream()
			.findFirst()
			.map(PlanningPermissionFacilityDTO::getFacilityType)
			.map((String t) -> FacilityType.valueOf(t).getValue())
			.orElse(FacilityType.OTHER.getValue());
	}

	private String getMainOrOnlyArendeslag(final List<PlanningPermissionFacilityDTO> facilityList) {
		return FacilityType.valueOf(facilityList.stream()
				.filter(facility -> FacilityType.USAGE_CHANGE.equals(FacilityType.valueOf(facility.getFacilityType())))
				.findFirst()
				.orElse(facilityList.getFirst())
				.getFacilityType())
			.getValue();
	}

	private PlanningPermissionFacilityDTO getMainOrTheOnlyFacility(final List<PlanningPermissionFacilityDTO> facilityList) {
		if (facilityList.size() == 1) {
			// The list only contains one facility, return it.
			return facilityList.getFirst();
		}

		// If the list contains more than one facility and mainFacility exists, return it.
		// If the list doesn't contain a mainFacility, return null.
		return facilityList.stream().anyMatch(PlanningPermissionFacilityDTO::isMainFacility) ? facilityList.stream().filter(PlanningPermissionFacilityDTO::isMainFacility).toList().getFirst() : null;
	}

	private ArrayOfAbstractArendeObjekt2 getByggrArendeObjektLista(final PlanningPermissionCaseDTO pCase) {

		final List<String> usedPropertyDesignations = new ArrayList<>();
		final ArrayOfAbstractArendeObjekt2 arendeObjektLista = new ArrayOfAbstractArendeObjekt2();

		pCase.getFacilities().forEach(f -> {
			if (usedPropertyDesignations.contains(f.getAddress().getPropertyDesignation())) {
				// If we already have created a "arendeFastighet" with the same propertyDesignation,
				// we should not create a duplicate. Skip this iteration.
				return;
			}

			final ArendeFastighet arendeFastighet = new ArendeFastighet();

			arendeFastighet.setArHuvudObjekt(f.isMainFacility());

			final Fastighet fastighet = new Fastighet();
			fastighet.setFnr(fbService.getPropertyInfoByPropertyDesignation(f.getAddress().getPropertyDesignation()).getFnr());

			arendeFastighet.setFastighet(fastighet);

			arendeObjektLista.getAbstractArendeObjekt().add(arendeFastighet);
			usedPropertyDesignations.add(f.getAddress().getPropertyDesignation());
		});

		return arendeObjektLista;
	}

	private ArrayOfArendeIntressent2 getByggrIntressenter(final PlanningPermissionCaseDTO pCase) {

		// Add all stakeholders from case to the list
		final List<StakeholderDTO> stakeholderDTOList = new ArrayList<>(pCase.getStakeholders());
		populateStakeholderListWithPropertyOwners(pCase, stakeholderDTOList);

		final ArrayOfArendeIntressent2 intressenter = new ArrayOfArendeIntressent2();

		final List<String> personIdList = filterPersonId(stakeholderDTOList);

		// We don't create stakeholders with the role "Kontrollansvarig", this must be handled manually.
		stakeholderDTOList.stream().filter(s -> !s.getRoles().contains(StakeholderRole.CONTROL_OFFICIAL.toString())).forEach(s -> {
			final ArendeIntressent intressent = new ArendeIntressent();
			switch (s) {
				case PersonDTO personDTO -> {
					// If the request contains two person with the same personId, it must be handled manually
					if (personIdList.stream().filter(personId -> personId.equals(personDTO.getPersonId())).count() > 1) {
						return;
					}
					setPersonFields(intressent, personDTO);
				}
				case final OrganizationDTO organizationDTO ->
					setOrganizationFields(intressent, organizationDTO);
				default -> throw Problem.valueOf(Status.BAD_REQUEST, "Invalid stakeholder type");
			}
			if (s.getAddresses() != null) {

				for (final AddressDTO addressDTO : s.getAddresses()) {
					for (final AddressCategory addressCategory : addressDTO.getAddressCategories()) {
						if (AddressCategory.POSTAL_ADDRESS.equals(addressCategory)) {

							setPostalAddressFields(intressent, addressDTO);

							if (s instanceof OrganizationDTO) {
								final IntressentAttention intressentAttention = new IntressentAttention();
								intressentAttention.setAttention(addressDTO.getAttention());
								intressent.setAttention(intressentAttention);
							}

						}
						if (AddressCategory.INVOICE_ADDRESS.equals(addressCategory)) {
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
		});

		return intressenter;
	}

	private void populateStakeholderListWithPropertyOwners(final PlanningPermissionCaseDTO pCase, final List<StakeholderDTO> stakeholderDTOList) {
		// Filter all persons
		final List<PersonDTO> personDTOStakeholders = stakeholderDTOList.stream()
			.filter(PersonDTO.class::isInstance)
			.map(PersonDTO.class::cast)
			.toList();

		// Filter all organizations
		final List<OrganizationDTO> organizationDTOStakeholders = stakeholderDTOList.stream()
			.filter(OrganizationDTO.class::isInstance)
			.map(OrganizationDTO.class::cast)
			.toList();

		// Populate personalNumber for every person
		for (final PersonDTO personDTOStakeholder : personDTOStakeholders) {
			String pnr = citizenService.getPersonalNumber(personDTOStakeholder.getPersonId());
			if ((pnr != null) && (pnr.length() == 12)) {
				pnr = pnr.substring(0, 8) + "-" + pnr.substring(8);
			}
			personDTOStakeholder.setPersonalNumber(pnr);
		}

		// Change the organization number for each organization so that it follows the "Sokigo-format"
		organizationDTOStakeholders.forEach(organization -> organization.setOrganizationNumber(CaseUtil.getSokigoFormattedOrganizationNumber(organization.getOrganizationNumber())));

		// Loop through each facility and get the property owners for each one
		pCase.getFacilities().forEach(facility -> {
			final List<StakeholderDTO> propertyOwnerList = fbService.getPropertyOwnerByPropertyDesignation(facility.getAddress().getPropertyDesignation());

			populateStakeholderListWithPropertyOwnerPersons(personDTOStakeholders, stakeholderDTOList, propertyOwnerList);
			populateStakeholderListWithPropertyOwnerOrganizations(organizationDTOStakeholders, stakeholderDTOList, propertyOwnerList);
		});
	}

	private void populateStakeholderListWithPropertyOwnerPersons(final List<PersonDTO> personDTOStakeholderList, final List<StakeholderDTO> stakeholderDTOList, final List<StakeholderDTO> propertyOwnerList) {
		final List<PersonDTO> personDTOPropertyOwnerList = propertyOwnerList.stream()
			.filter(PersonDTO.class::isInstance)
			.map(PersonDTO.class::cast).toList();

		// All incoming personStakeholders that is also propertyOwners
		final List<PersonDTO> personDTOStakeholderPropertyOwnerList = personDTOStakeholderList.stream()
			.filter(personStakeholder -> personDTOPropertyOwnerList.stream()
				.map(PersonDTO::getPersonalNumber).toList()
				.contains(personStakeholder.getPersonalNumber()))
			.toList();

		log.debug("All incoming personStakeholders that is also propertyOwners: {}", personDTOStakeholderPropertyOwnerList);

		personDTOStakeholderPropertyOwnerList.forEach(person -> person.setRoles(Stream.of(person.getRoles(), List.of(StakeholderRole.PROPERTY_OWNER.toString()))
			.flatMap(Collection::stream)
			.toList()));

		// All personPropertyOwners that does not exist in the incoming request
		final List<PersonDTO> notExistingPersonPropertyOwnerListDTO = personDTOPropertyOwnerList.stream()
			.filter(not(personPropertyOwner -> personDTOStakeholderList.stream()
				.map(PersonDTO::getPersonalNumber).toList()
				.contains(personPropertyOwner.getPersonalNumber())))
			.toList();

		log.debug("All personPropertyOwners that does not exist in the incoming request: {}", notExistingPersonPropertyOwnerListDTO);

		stakeholderDTOList.addAll(notExistingPersonPropertyOwnerListDTO);
	}

	private void populateStakeholderListWithPropertyOwnerOrganizations(final List<OrganizationDTO> organizationDTOStakeholders, final List<StakeholderDTO> stakeholderDTOList, final List<StakeholderDTO> propertyOwnerList) {
		final List<OrganizationDTO> organizationDTOPropertyOwnerList = propertyOwnerList.stream()
			.filter(OrganizationDTO.class::isInstance)
			.map(OrganizationDTO.class::cast)
			.toList();

		// All incoming organizationStakeholders that is also propertyOwners
		final List<OrganizationDTO> organizationDTOStakeholderPropertyOwnerList = organizationDTOStakeholders.stream()
			.filter(organizationStakeholder -> organizationDTOPropertyOwnerList.stream()
				.map(OrganizationDTO::getOrganizationNumber)
				.toList()
				.contains(organizationStakeholder.getOrganizationNumber()))
			.toList();

		log.debug("All incoming organizationStakeholders that is also propertyOwners: {}", organizationDTOStakeholderPropertyOwnerList);

		organizationDTOStakeholderPropertyOwnerList.forEach(orgStakeholder -> orgStakeholder.setRoles(Stream.of(orgStakeholder.getRoles(), List.of(StakeholderRole.PROPERTY_OWNER.toString()))
			.flatMap(Collection::stream)
			.toList()));

		// All organizationPropertyOwners that does not exist in the incoming request
		final List<OrganizationDTO> notExistingOrgPropertyOwnerList = organizationDTOPropertyOwnerList.stream()
			.filter(not(organizationPropertyOwner -> organizationDTOStakeholders.stream()
				.map(OrganizationDTO::getOrganizationNumber)
				.toList()
				.contains(organizationPropertyOwner.getOrganizationNumber())))
			.toList();

		log.debug("All organizationPropertyOwners that does not exist in the incoming request: {}", notExistingOrgPropertyOwnerList);

		stakeholderDTOList.addAll(notExistingOrgPropertyOwnerList);
	}

	private List<String> filterPersonId(final List<StakeholderDTO> stakeholderDTOList) {
		return stakeholderDTOList.stream()
			.filter(PersonDTO.class::isInstance)
			.map(PersonDTO.class::cast)
			.map(PersonDTO::getPersonId)
			.filter(Objects::nonNull)
			.toList();
	}

	void setPostalAddressFields(final ArendeIntressent intressent, final AddressDTO addressDTO) {
		intressent.setAdress(addressDTO.getHouseNumber() != null
			? addressDTO.getStreet() + " " + addressDTO.getHouseNumber()
			: addressDTO.getStreet());
		intressent.setPostNr(addressDTO.getPostalCode());
		intressent.setOrt(addressDTO.getCity());
		intressent.setLand(addressDTO.getCountry());

		intressent.setCoAdress(addressDTO.getCareOf());
	}

	void setOrganizationFields(final ArendeIntressent intressent, final OrganizationDTO organizationDTO) {
		intressent.setArForetag(true);
		intressent.setNamn(organizationDTO.getOrganizationName());
		intressent.setPersOrgNr(organizationDTO.getOrganizationNumber());
	}

	void setPersonFields(final ArendeIntressent intressent, final PersonDTO personDTO) {
		intressent.setArForetag(false);
		intressent.setFornamn(personDTO.getFirstName());
		intressent.setEfternamn(personDTO.getLastName());
		intressent.setPersOrgNr(personDTO.getPersonalNumber());

	}

	ArrayOfString2 getByggrRoles(final StakeholderDTO s) {
		final ArrayOfString2 roles = new ArrayOfString2();
		s.getRoles().stream()
			.distinct()
			.forEach(r -> roles.getRoll().add(StakeholderRole.valueOf(r).getText()));
		return roles;
	}

	Fakturaadress getByggrFakturaadress(final AddressDTO addressDTO) {
		final Fakturaadress fakturaAdress = new Fakturaadress();
		fakturaAdress.setAdress(addressDTO.getHouseNumber() != null
			? addressDTO.getStreet() + " " + addressDTO.getHouseNumber()
			: addressDTO.getStreet());
		fakturaAdress.setAttention(addressDTO.getAttention());
		fakturaAdress.setLand(addressDTO.getCountry());
		fakturaAdress.setOrt(addressDTO.getCity());
		fakturaAdress.setPostNr(addressDTO.getPostalCode());
		return fakturaAdress;
	}

	ArrayOfIntressentKommunikation getByggrContactInfo(final StakeholderDTO s, final IntressentAttention intressentAttention) {
		final ArrayOfIntressentKommunikation arrayOfIntressentKommunikation = new ArrayOfIntressentKommunikation();
		if (notNullOrBlank(s.getCellphoneNumber())) {
			final IntressentKommunikation intressentKommunikation = new IntressentKommunikation();
			intressentKommunikation.setArAktiv(true);
			intressentKommunikation.setBeskrivning(s.getCellphoneNumber());
			intressentKommunikation.setKomtyp(Constants.BYGGR_KOMTYP_MOBIL);
			intressentKommunikation.setAttention(intressentAttention);
			arrayOfIntressentKommunikation.getIntressentKommunikation().add(intressentKommunikation);
		}
		if (notNullOrBlank(s.getPhoneNumber())) {
			final IntressentKommunikation intressentKommunikation = new IntressentKommunikation();
			intressentKommunikation.setArAktiv(true);
			intressentKommunikation.setBeskrivning(s.getPhoneNumber());
			intressentKommunikation.setKomtyp(Constants.BYGGR_KOMTYP_HEMTELEFON);
			intressentKommunikation.setAttention(intressentAttention);
			arrayOfIntressentKommunikation.getIntressentKommunikation().add(intressentKommunikation);
		}
		if (notNullOrBlank(s.getEmailAddress())) {
			final IntressentKommunikation intressentKommunikation = new IntressentKommunikation();
			intressentKommunikation.setArAktiv(true);
			intressentKommunikation.setBeskrivning(s.getEmailAddress());
			intressentKommunikation.setKomtyp(Constants.BYGGR_KOMTYP_EPOST);
			intressentKommunikation.setAttention(intressentAttention);
			arrayOfIntressentKommunikation.getIntressentKommunikation().add(intressentKommunikation);
		}
		return arrayOfIntressentKommunikation;
	}

	private boolean notNullOrBlank(final String string) {
		return (string != null) && !string.isBlank();
	}

	public CaseStatusDTO getByggrStatus(final String caseId, final String externalCaseId) {
		return getByggrStatus(getArende(caseId), externalCaseId);
	}

	/**
	 * @return CaseStatus from ByggR.
	 */
	private CaseStatusDTO getByggrStatus(final Arende arende, final String externalCaseId) {
		final CaseStatusDTO caseStatusDTO = new CaseStatusDTO();
		caseStatusDTO.setSystem(SystemType.BYGGR);
		caseStatusDTO.setExternalCaseId(externalCaseId);
		caseStatusDTO.setCaseId(arende.getDnr());
		final List<CaseMapping> caseMappingList = caseMappingService.getCaseMapping(externalCaseId, arende.getDnr());
		caseStatusDTO.setCaseType(caseMappingList.isEmpty() ? null : caseMappingList.getFirst().getCaseType());
		caseStatusDTO.setServiceName(caseMappingList.isEmpty() ? null : caseMappingList.getFirst().getServiceName());

		// OEP-status = Ärendet arkiveras
		if ((arende.getStatus() != null) && Constants.BYGGR_STATUS_AVSLUTAT.equals(arende.getStatus())) {
			// If the case is closed, we don't need to check for any more occurrence
			caseStatusDTO.setStatus(arende.getStatus());
			return caseStatusDTO;
		}

		if ((arende.getHandelseLista() != null)
			&& (arende.getHandelseLista().getHandelse() != null)) {
			final List<Handelse> handelseLista = arende.getHandelseLista().getHandelse();

			handelseLista.sort(Comparator.comparing(Handelse::getStartDatum).reversed());

			for (final Handelse h : handelseLista) {

				caseStatusDTO.setStatus(getHandelseStatus(h.getHandelsetyp(), h.getHandelseslag(), h.getHandelseutfall()));

				if (caseStatusDTO.getStatus() != null) {
					caseStatusDTO.setTimestamp(h.getStartDatum());
					return caseStatusDTO;
				}
			}
		}
		throw Problem.valueOf(Status.NOT_FOUND, Constants.ERR_MSG_STATUS_NOT_FOUND);
	}

	private Arende getArende(final String caseId) {
		final GetArende getArende = new GetArende();
		getArende.setDnr(caseId);

		return arendeExportClient.getArende(getArende).getGetArendeResult();
	}

	private String getHandelseStatus(final String handelsetyp, final String handelseslag, final String handelseutfall) {

		// OEP-status = Inskickat
		// UNDER
		// OEP-status = Väntar på komplettering
		// KOMP, KOMP1
		if (HANDELSETYP_ANMALAN.equals(handelsetyp)
			|| HANDELSETYP_ANSOKAN.equals(handelsetyp)
			|| Constants.BYGGR_HANDELSETYP_UNDERRATTELSE.equals(handelsetyp) && (Constants.BYGGR_HANDELSESLAG_MED_KRAV_PA_SVAR.equals(handelseslag)
			|| Constants.BYGGR_HANDELSESLAG_UTAN_KRAV_PA_SVAR.equals(handelseslag))
			|| Constants.BYGGR_HANDELSETYP_KOMPLETTERINGSFORELAGGANDE.equals(handelsetyp)
			|| Constants.BYGGR_HANDELSETYP_KOMPLETTERINGSFORELAGGANDE_PAMINNELSE.equals(handelsetyp)) {
			// ANM, ANSÖKAN
			return handelsetyp;
		}
		// OEP-status = Under behandling
		if (Constants.BYGGR_HANDELSETYP_BESLUT.equals(handelsetyp) && (Constants.BYGGR_HANDELSESLAG_SLUTBESKED.equals(handelseslag)
			|| Constants.BYGGR_HANDELSESLAG_AVSKRIVNING.equals(handelseslag))
			|| ((Constants.BYGGR_HANDELSETYP_HANDLING.equals(handelsetyp) && Constants.BYGGR_HANDELSESLAG_KOMPLETTERANDE_HANDLINGAR.equals(handelseslag))
			|| Constants.BYGGR_HANDELSESLAG_KOMPLETTERANDE_BYGGLOVHANDLINGAR.equals(handelseslag)
			|| Constants.BYGGR_HANDELSESLAG_KOMPLETTERANDE_TEKNISKA_HANDLINGAR.equals(handelseslag)
			|| Constants.BYGGR_HANDELSESLAG_REVIDERADE_HANDLINGAR.equals(handelseslag))) {
			// SLU, UAB
			return handelseslag;
		} else if (Constants.BYGGR_HANDELSETYP_REMISS.equals(handelsetyp)
			&& Constants.BYGGR_HANDELSESLAG_UTSKICK_AV_REMISS.equals(handelseslag)) {
			// UTSKICK
			return handelseslag;
		}
		// OEP-status = Kompletterad
		else if (Constants.BYGGR_HANDELSETYP_ATOMHANDELSE.equals(handelsetyp)
			&& Constants.BYGGR_HANDELSESLAG_ATOM_KVITTENS.equals(handelseslag)
			&& Constants.BYGGR_HANDELSEUTFALL_ATOM_KVITTENS_HL_BYTE.equals(handelseutfall)) {
			// Kv2
			return handelseutfall;
		}

		return null;
	}

	public List<CaseStatusDTO> getByggrStatusByOrgNr(final String organizationNumber) {
		final List<CaseStatusDTO> caseStatusDTOList = new ArrayList<>();
		final ArrayOfString arendeIntressentRoller = new ArrayOfString();
		arendeIntressentRoller.getString().add(StakeholderRole.APPLICANT.getText());
		final ArrayOfString handelseIntressentRoller = new ArrayOfString();
		handelseIntressentRoller.getString().add(StakeholderRole.APPLICANT.getText());

		final GetRelateradeArendenByPersOrgNrAndRole getRelateradeArendenByPersOrgNrAndRoleInput = new GetRelateradeArendenByPersOrgNrAndRole();
		getRelateradeArendenByPersOrgNrAndRoleInput.setPersOrgNr(organizationNumber);
		getRelateradeArendenByPersOrgNrAndRoleInput.setArendeIntressentRoller(arendeIntressentRoller);
		getRelateradeArendenByPersOrgNrAndRoleInput.setHandelseIntressentRoller(handelseIntressentRoller);
		ArrayOfArende1 arrayOfByggrArende = arendeExportClient.getRelateradeArendenByPersOrgNrAndRole(getRelateradeArendenByPersOrgNrAndRoleInput).getGetRelateradeArendenByPersOrgNrAndRoleResult();

		if (arrayOfByggrArende != null) {
			if (arrayOfByggrArende.getArende().isEmpty()) {
				final String modifiedOrgNr = CaseUtil.getSokigoFormattedOrganizationNumber(organizationNumber);
				getRelateradeArendenByPersOrgNrAndRoleInput.setPersOrgNr(modifiedOrgNr);
				arrayOfByggrArende = arendeExportClient.getRelateradeArendenByPersOrgNrAndRole(getRelateradeArendenByPersOrgNrAndRoleInput).getGetRelateradeArendenByPersOrgNrAndRoleResult();
			}

			arrayOfByggrArende.getArende().forEach(byggrArende -> {
				final List<CaseMapping> caseMappingList = caseMappingService.getCaseMapping(null, byggrArende.getDnr());
				final CaseStatusDTO status = getByggrStatus(byggrArende, caseMappingList.isEmpty() ? null : caseMappingList.getFirst().getExternalCaseId());
				caseStatusDTOList.add(status);
			});
		}

		return caseStatusDTOList;
	}

}
